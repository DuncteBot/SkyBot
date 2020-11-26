/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.essentials.eval

import groovy.lang.GroovyShell
import kotlinx.coroutines.*
import me.duncte123.botcommons.StringUtils
import me.duncte123.botcommons.messaging.MessageUtils.*
import me.duncte123.botcommons.text.TextColor
import me.duncte123.botcommons.web.WebParserUtils
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.botcommons.web.requests.FormRequestBody
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Authors
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.CommandUtils.isDev
import ml.duncte123.skybot.utils.JSONMessageErrorsHelper.sendErrorJSON
import net.dv8tion.jda.api.requests.RestAction
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import javax.script.ScriptException
import kotlin.system.measureTimeMillis

@Authors(
    authors = [
        Author(nickname = "Sanduhr32", author = "Maurice R S"),
        Author(nickname = "duncte123", author = "Duncan Sterken"),
        Author(nickname = "ramidzkh", author = "Ramid Khan")
    ]
)
class EvalCommand : Command() {
    private val engine: GroovyShell
    private val importString: String

    init {
        this.category = CommandCategory.UNLISTED
        this.name = "eval"
        this.aliases = arrayOf("eval™", "evaluate", "evan", "eva;")
        this.help = "Evaluate groovy/java code on the bot"
        this.usage = "<java/groovy code>"

        engine = GroovyShell()

        val packageImports = listOf(
            "java.io",
            "java.lang",
            "java.math",
            "java.time",
            "java.util",
            "java.util.concurrent",
            "java.util.stream",
            "net.dv8tion.jda.api",
            "net.dv8tion.jda.api.entities",
            "net.dv8tion.jda.api.entities.impl",
            "net.dv8tion.jda.api.managers",
            "net.dv8tion.jda.api.managers.impl",
            "net.dv8tion.jda.api.utils",
            "ml.duncte123.skybot.utils",
            "ml.duncte123.skybot.entities",
            "ml.duncte123.skybot",
            "ml.duncte123.skybot.objects.command"
        )

        val classImports = listOf(
            "fredboat.audio.player.LavalinkManager"
        )

        val staticImports = listOf(
            "ml.duncte123.skybot.objects.EvalFunctions.*",
            "me.duncte123.botcommons.messaging.MessageUtils.*",
            "me.duncte123.botcommons.messaging.EmbedUtils.*",
            "ml.duncte123.skybot.utils.JSONMessageErrorsHelper.*"
        )

        importString = packageImports.joinToString(separator = ".*\nimport ", prefix = "import ", postfix = ".*\nimport ") +
            classImports.joinToString(separator = "\nimport ", postfix = "\n") +
            staticImports.joinToString(prefix = "import static ", separator = "\nimport static ", postfix = "\n")
    }

    @ExperimentalCoroutinesApi
    override fun execute(ctx: CommandContext) {
        if (!isDev(ctx.author) && ctx.author.idLong != Settings.OWNER_ID) {
            sendError(ctx.message)
            return
        }

        val userInput = ctx.message.contentRaw.split("\\s+".toRegex(), 2)

        if (userInput.size < 2) {
            sendSuccess(ctx.message)
            return
        }

        var userIn = ctx.getArgsRaw(false)

        if (userIn.startsWith("```") && userIn.endsWith("```")) {
            userIn = userIn
                .replace("```(.*)\n".toRegex(), "")
                .replace("\n?```".toRegex(), "")
        }

        val script = importString + userIn

        engine.setVariable("commandManager", ctx.commandManager)
        engine.setVariable("message", ctx.message)
        engine.setVariable("channel", ctx.message.textChannel)
        engine.setVariable("guild", ctx.guild)
        engine.setVariable("member", ctx.member)
        engine.setVariable("author", ctx.author)
        engine.setVariable("jda", ctx.jda)
        engine.setVariable("shardManager", ctx.jda.shardManager)
        engine.setVariable("event", ctx.event)

        engine.setVariable("args", ctx.args)
        engine.setVariable("ctx", ctx)
        engine.setVariable("variables", ctx.variables)

        @SinceSkybot("3.58.0")
        GlobalScope.launch(
            Dispatchers.Default, start = CoroutineStart.ATOMIC,
            block = {
                return@launch eval(ctx, script)
            }
        )
    }

    @SinceSkybot("3.58.0")
    private suspend fun eval(ctx: CommandContext, script: String) {
        val time = measureTimeMillis {
            val out = withTimeoutOrNull(60000L /* = 60 seconds */) {
                try {
                    engine.evaluate(script)
                } catch (ex: Throwable) {
                    ex
                }
            }

            parseEvalResponse(out, ctx)
        }

        LOGGER.info(
            "${TextColor.PURPLE}Took ${time}ms for evaluating last script ${TextColor.ORANGE}(User: ${ctx.author})" +
                "${TextColor.YELLOW}(script: ${makeHastePost(script, "2d", "groovy")})${TextColor.RESET}"
        )
    }

    private fun parseEvalResponse(out: Any?, ctx: CommandContext) {
        when (out) {
            null -> sendSuccess(ctx.message)

            is ArrayIndexOutOfBoundsException -> {
                sendSuccess(ctx.message)
            }

            is ExecutionException, is ScriptException -> {
                out as Exception
                sendErrorWithMessage(ctx.message, "ERROR: " + out.cause.toString())
            }

            is TimeoutException, is InterruptedException, is IllegalStateException -> {
                out as Exception
                sendErrorWithMessage(ctx.message, "ERROR: $out")
            }

            is IllegalArgumentException -> {
                sendErrorWithMessage(ctx.message, "ERROR: $out")
            }

            is Throwable -> {
                if (Settings.USE_JSON) {
                    sendErrorJSON(ctx.message, out, false, ctx.variables.jackson)
                } else {
                    sendMsg(ctx, "ERROR: $out")
                    LOGGER.error("Eval error", out)
                }
            }

            is RestAction<*> -> {
                out.queue()
                sendSuccess(ctx.message)
            }

            else -> {
                val toString = out.toString()

                if (toString.isEmpty() || toString.isBlank()) {
                    sendSuccess(ctx.message)
                    return
                }

                sendMsg(ctx, "```\n${StringUtils.abbreviate(toString, 1900)}```")
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun makeHastePost(text: String, expiration: String = "1h", lang: String = "text"): String {
        if (Settings.IS_LOCAL) {
            return "RUNNING_LOCAL"
        }

        val base = "https://paste.menudocs.org"
        val body = FormRequestBody()

        body.append("text", text)
        body.append("expire", expiration)
        body.append("lang", lang)

        val loc = WebUtils.ins.postRequest("$base/paste/new", body)
            .build(
                {
                    return@build it.request().url().url().path
                },
                WebParserUtils::handleError
            ).execute()

        return base + loc
    }
}

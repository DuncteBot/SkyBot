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

import com.github.natanbc.reliqua.request.PendingRequest
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
import java.io.PrintWriter
import java.io.StringWriter
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import kotlin.system.measureTimeMillis

@Authors(
    authors = [
        Author(nickname = "Sanduhr32", author = "Maurice R S"),
        Author(nickname = "duncte123", author = "Duncan Sterken"),
        Author(nickname = "ramidzkh", author = "Ramid Khan")
    ]
)
class EvalCommand : Command() {
    // a little help from minn :)
    private val engine: ScriptEngine by lazy {
        ScriptEngineManager().getEngineByExtension("kts")!!.apply {
            this.eval(
                """
                import java.io.*
                import java.lang.*
                import java.math.*
                import java.time.*
                import java.util.*
                import java.util.concurrent.*
                import java.util.stream.*
                import net.dv8tion.jda.api.*
                import net.dv8tion.jda.api.entities.*
                import net.dv8tion.jda.api.sharding.*
                import net.dv8tion.jda.internal.entities.*
                import net.dv8tion.jda.api.managers.*
                import net.dv8tion.jda.internal.managers.*
                import net.dv8tion.jda.api.utils.*
                import ml.duncte123.skybot.utils.*
                import ml.duncte123.skybot.entities.*
                import ml.duncte123.skybot.*
                import ml.duncte123.skybot.objects.command.*
                import fredboat.audio.player.LavalinkManager
                import ml.duncte123.skybot.objects.EvalFunctions.*
                import me.duncte123.botcommons.messaging.MessageUtils.*
                import me.duncte123.botcommons.messaging.EmbedUtils.*
                import ml.duncte123.skybot.utils.JSONMessageErrorsHelper.*
                """.trimIndent()
            )
        }
    }

    init {
        this.category = CommandCategory.UNLISTED
        this.name = "eval"
        this.aliases = arrayOf("eval™", "evaluate", "evan", "eva;")
        this.help = "Evaluate groovy/java code on the bot"
        this.usage = "<kotlin code>"
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
                .replace("```(?:kt)?\n".toRegex(), "")
                .replace("\n?```".toRegex(), "")
        }

        engine.put("commandManager", ctx.commandManager)
        engine.put("message", ctx.message)
        engine.put("channel", ctx.message.textChannel)
        engine.put("guild", ctx.guild)
        engine.put("member", ctx.member)
        engine.put("author", ctx.author)
        engine.put("jda", ctx.jda)
        engine.put("shardManager", ctx.jda.shardManager)
        engine.put("event", ctx.event)

        engine.put("args", ctx.args)
        engine.put("ctx", ctx)
        engine.put("variables", ctx.variables)

        @SinceSkybot("3.58.0")
        GlobalScope.launch {
            return@launch eval(ctx, userIn)
        }
    }

    @SinceSkybot("3.58.0")
    private suspend fun eval(ctx: CommandContext, script: String) {
        val time = measureTimeMillis {
            val out = withTimeoutOrNull(60000L /* = 60 seconds */) {
                try {
                    engine.eval(script)
                } catch (ex: Throwable) {
                    ex
                }
            }

            parseEvalResponse(out, ctx)
        }

        LOGGER.info(
            "${TextColor.PURPLE}Took ${time}ms for evaluating last script ${TextColor.ORANGE}(User: ${ctx.author})" +
                "${TextColor.YELLOW}(script: ${makeHastePost(script, "2d", "kotlin").execute()})${TextColor.RESET}"
        )
    }

    private fun parseEvalResponse(out: Any?, ctx: CommandContext) {
        when (out) {
            null -> sendSuccess(ctx.message)

            is Throwable -> {
                if (Settings.USE_JSON) {
                    sendErrorJSON(ctx.message, out, false, ctx.variables.jackson)
                } else {
                    // respond instantly
                    sendMsg(ctx, "ERROR: $out")
                    // send the trace when uploaded
                    makeHastePost(out.getString()).async {
                        sendMsg(ctx, "Stacktrace: <$it>")
                    }
                }
            }

            is RestAction<*> -> {
                out.queue({
                    sendMsg(ctx, "Rest action success: $it")
                }) {
                    sendMsg(ctx, "Rest action error: $it")
                }

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

    private fun Throwable.getString(): String {
        val writer = StringWriter()
        val out = PrintWriter(writer)

        this.printStackTrace(out)

        return writer.toString()
    }

    private fun makeHastePost(text: String, expiration: String = "1h", lang: String = "text"): PendingRequest<String> {
        val base = "https://paste.menudocs.org"
        val body = FormRequestBody()

        body.append("text", text)
        body.append("expire", expiration)
        body.append("lang", lang)

        return WebUtils.ins.postRequest("$base/paste/new", body)
            .build(
                {
                    return@build base + it.request().url().url().path
                },
                WebParserUtils::handleError
            )
    }
}

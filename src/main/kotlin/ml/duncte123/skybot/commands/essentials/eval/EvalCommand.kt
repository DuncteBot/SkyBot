/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import me.duncte123.botcommons.messaging.MessageUtils.*
import me.duncte123.botcommons.text.TextColor
import me.duncte123.botcommons.web.WebParserUtils
import me.duncte123.botcommons.web.WebUtils
import me.duncte123.botcommons.web.requests.FormRequestBody
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Authors
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.exceptions.DoomedException
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.CommandUtils.isDev
import ml.duncte123.skybot.utils.JSONMessageErrorsHelper.sendErrorJSON
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.requests.RestAction
import org.jsoup.Jsoup
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import javax.script.ScriptException
import kotlin.system.measureTimeMillis

@Authors(authors = [
    Author(nickname = "Sanduhr32", author = "Maurice R S"),
    Author(nickname = "duncte123", author = "Duncan Sterken"),
    Author(nickname = "ramidzkh", author = "Ramid Khan")
])
class EvalCommand : Command() {
    private val engine: GroovyShell
    private val importString: String

    init {
        this.category = CommandCategory.UNLISTED
        this.name = "eval"
        this.aliases = arrayOf("eval™", "evaluate", "evan", "eva;")
        this.helpFunction = { _, _ -> "Evaluate groovy/java code on the bot" }
        this.usageInstructions = { prefix, invoke -> "`$prefix$invoke <java/groovy code>`" }

        engine = GroovyShell()

        val packageImports = listOf(
            "java.io",
            "java.lang",
            "java.util",
            "java.util.concurrent",
            "net.dv8tion.jda.api",
            "net.dv8tion.jda.api.entities",
            "net.dv8tion.jda.api.entities.impl",
            "net.dv8tion.jda.api.managers",
            "net.dv8tion.jda.api.managers.impl",
            "net.dv8tion.jda.api.utils",
            "ml.duncte123.skybot.utils",
            "ml.duncte123.skybot.entities",
            "ml.duncte123.skybot.entities.delegate",
            "ml.duncte123.skybot",
            "ml.duncte123.skybot.objects.command"
        )

        val classImports = listOf(
            "ml.duncte123.skybot.exceptions.DoomedException",
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
        val event = ctx.event

        if (!isDev(event.author) || event.author.idLong != Settings.OWNER_ID) {
            return
        }

        val userInput = event.message.contentRaw.split("\\s+".toRegex(), 2)

        if (userInput.size < 2) {
            sendSuccess(event.message)
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
        engine.setVariable("event", event)

        engine.setVariable("skraa", script)
        engine.setVariable("args", ctx.args)
        engine.setVariable("ctx", ctx)
        engine.setVariable("variables", ctx.variables)

        @SinceSkybot("3.58.0")
        GlobalScope.launch(Dispatchers.Default, start = CoroutineStart.ATOMIC, block = {
            return@launch eval(event, script, 60000L, ctx)
        })
    }

    @SinceSkybot("3.58.0")
    private suspend fun eval(event: GuildMessageReceivedEvent, script: String, millis: Long, ctx: CommandContext) {
        val time = measureTimeMillis {
            withTimeoutOrNull(millis) {
                val out = try {
                    engine.setVariable("scope", this)

                    engine.evaluate(script)

                } catch (ex: Throwable) {
                    ex
                }

                parseEvalResponse(out, event, ctx)
            }
        }

        logger.info("${TextColor.PURPLE}Took ${time}ms for evaluating last script ${TextColor.ORANGE}(User: ${event.author})" +
            "${TextColor.YELLOW}(script: ${makeHastePost(script, "2d", "groovy")})${TextColor.RESET}")
    }

    private fun parseEvalResponse(out: Any?, event: GuildMessageReceivedEvent, ctx: CommandContext) {
        when (out) {
            null -> {
                sendSuccess(event.message)
            }

            is ArrayIndexOutOfBoundsException -> {
                sendSuccess(event.message)
            }

            is ExecutionException, is ScriptException -> {
                out as Exception
                sendErrorWithMessage(event.message, "ERROR: " + out.cause.toString())
            }

            is TimeoutException, is InterruptedException, is IllegalStateException -> {
                out as Exception
                sendErrorWithMessage(event.message, "ERROR: $out")
            }

            is IllegalArgumentException, is DoomedException -> {
                out as RuntimeException
                sendErrorWithMessage(event.message, "ERROR: $out")
            }

            is Throwable -> {
                if (Settings.USE_JSON) {
                    sendErrorJSON(event.message, out, false, ctx.variables.jackson)
                } else {
                    sendMsg(event, "ERROR: $out")
//                        out.printStackTrace()
                }
            }

            is RestAction<*> -> {
                out.queue()
                sendSuccess(event.message)
            }

            else -> {
                if (out.toString().isEmpty() || out.toString().isBlank()) {
                    sendSuccess(event.message)
                    return
                }

                MessageBuilder()
                    .appendCodeBlock(out.toString(), "")
                    .buildAll(MessageBuilder.SplitPolicy.ANYWHERE)
                    .forEach { sendMsg(event, it) }

            }
        }
    }

    companion object {
        fun makeHastePost(text: String, expiration: String = "1h", lang: String = "text"): String {
            val base = "https://paste.menudocs.org"
            val body = FormRequestBody()

            body.append("text", text)
            body.append("expire", expiration)
            body.append("lang", lang)

            val loc = WebUtils.ins.postRequest("$base/paste/new", body)
                .build({
                    return@build Jsoup.parse(it.body()!!.string())
                        .select("a[title=\"View Raw\"]")
                        .first().attr("href")
                        .replaceFirst("/raw", "")
                }, WebParserUtils::handleError).execute()

            return base + loc
        }
    }
}

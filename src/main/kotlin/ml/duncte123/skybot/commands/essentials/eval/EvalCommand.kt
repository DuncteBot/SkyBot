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
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import java.io.PrintWriter
import java.io.StringWriter
import javax.script.ScriptEngine
import kotlin.system.measureTimeMillis

@Authors(
    authors = [
        Author(nickname = "Sanduhr32", author = "Maurice R S"),
        Author(nickname = "duncte123", author = "Duncan Sterken"),
        Author(nickname = "ramidzkh", author = "Ramid Khan")
    ]
)
class EvalCommand : Command() {
    private val engine: ScriptEngine
    private val importString: String

    init {
        this.category = CommandCategory.UNLISTED
        this.name = "eval"
        this.aliases = arrayOf("eval™", "evaluate", "evan", "eva;")
        this.help = "Evaluate groovy/java code on the bot"
        this.usage = "<java/groovy code>"

        // TODO: KotlinJsr223JvmDaemonLocalEvalScriptEngineFactory?
        // might be better?
        engine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine

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
            "net.dv8tion.jda.api.sharding",
            "net.dv8tion.jda.internal.entities",
            "net.dv8tion.jda.api.managers",
            "net.dv8tion.jda.internal.managers",
            "net.dv8tion.jda.api.utils",
            "ml.duncte123.skybot.utils",
            "ml.duncte123.skybot.entities",
            "ml.duncte123.skybot",
            "ml.duncte123.skybot.objects.command"
        )

        val otherImports = listOf(
            // Class imports
            "fredboat.audio.player.LavalinkManager",

            // static imports
            "ml.duncte123.skybot.objects.EvalFunctions.*",
            "me.duncte123.botcommons.messaging.MessageUtils.*",
            "me.duncte123.botcommons.messaging.EmbedUtils.*",
            "ml.duncte123.skybot.utils.JSONMessageErrorsHelper.*"
        )

        importString = packageImports.joinToString(separator = ".*\nimport ", prefix = "import ", postfix = ".*\nimport ") +
            otherImports.joinToString(separator = "\nimport ", postfix = "\n")
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

        // we have to manually insert all the bindings to use at runtime
//        val script = "$importString\n$bindingsJoined\n//USER SCRIPT\n$userIn"
        val script = """$importString
            |// Bindings (temp fix)
            |val args = bindings["args"] as java.util.ArrayList<*>
            |val guild = bindings["guild"] as ml.duncte123.skybot.entities.jda.DunctebotGuild
            |val variables = bindings["variables"] as ml.duncte123.skybot.Variables
            |val shardManager = bindings["shardManager"] as net.dv8tion.jda.api.sharding.DefaultShardManager
            |val author = bindings["author"] as net.dv8tion.jda.internal.entities.UserImpl
            |val jda = bindings["jda"] as net.dv8tion.jda.internal.JDAImpl
            |val ctx = bindings["ctx"] as ml.duncte123.skybot.objects.command.CommandContext
            |val channel = bindings["channel"] as net.dv8tion.jda.internal.entities.TextChannelImpl
            |val member = bindings["member"] as net.dv8tion.jda.internal.entities.MemberImpl
            |val commandManager = bindings["commandManager"] as ml.duncte123.skybot.CommandManager
            |val message = bindings["message"] as net.dv8tion.jda.internal.entities.ReceivedMessage
            |val event = bindings["event"] as net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
            |
            |// User input
            |$userIn
        """.trimMargin()

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

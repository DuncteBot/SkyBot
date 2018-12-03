/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Authors
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.commands.essentials.eval.filter.EvalFilter
import ml.duncte123.skybot.entities.delegate.*
import ml.duncte123.skybot.exceptions.DoomedException
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.JSONMessageErrors.sendErrorJSON
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.requests.RestAction
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.kohsuke.groovy.sandbox.SandboxTransformer
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

    private val protectedShell: GroovyShell
    private val engine: GroovyShell
    /*private val packageImports: List<String>
    private val classImports: List<String>
    private val staticImports: List<String>*/
    private val importString: String
    private val filter = EvalFilter()

//    private var runIfNotOwner = true

    /**
     * This initialises the engine
     */
    init {
        this.category = CommandCategory.PATRON
        // The GroovyShell is for the public eval
        protectedShell = object : GroovyShell(
            CompilerConfiguration()
                .addCompilationCustomizers(SandboxTransformer())) {
            @Throws(CompilationFailedException::class)
            override fun evaluate(scriptText: String): Any? {
                if (scriptText.isEmpty())
                    return null
                if (filter.filterArrays(scriptText))
                    throw DoomedException("Arrays are not allowed")
                if (filter.filterLoops(scriptText))
                    throw DoomedException("Loops are not allowed")
                return super.evaluate(scriptText)
            }
        }
//        engine = ScriptEngineManager().getEngineByName("groovy")
        engine = GroovyShell()
        val packageImports = listOf(
            "java.io",
            "java.lang",
            "java.util",
            "java.util.concurrent",
            "net.dv8tion.jda.core",
            "net.dv8tion.jda.core.entities",
            "net.dv8tion.jda.core.entities.impl",
            "net.dv8tion.jda.core.managers",
            "net.dv8tion.jda.core.managers.impl",
            "net.dv8tion.jda.core.utils",
            "ml.duncte123.skybot.utils",
            "ml.duncte123.skybot.entities",
            "ml.duncte123.skybot.entities.delegate",
            "ml.duncte123.skybot",
            "ml.duncte123.skybot.objects.command")

        val classImports = listOf(
            "ml.duncte123.skybot.exceptions.DoomedException",
            "fredboat.audio.player.LavalinkManager"
        )

        val staticImports = listOf(
            "ml.duncte123.skybot.objects.EvalFunctions.*",
            "me.duncte123.botcommons.messaging.MessageUtils.*",
            "ml.duncte123.skybot.utils.JSONMessageErrors.*"
        )

        importString = packageImports.joinToString(separator = ".*\nimport ", prefix = "import ", postfix = ".*\n import ") +
            classImports.joinToString(separator = "\nimport ", postfix = "\n") +
            staticImports.joinToString(prefix = "import static ", separator = "\nimport static ", postfix = "\n")
    }

    @ExperimentalCoroutinesApi
    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        val isRanByBotOwner = isDev(event.author) || event.author.idLong == Settings.OWNER_ID
        /*if (!isRanByBotOwner && !runIfNotOwner)
            return*/

        if (!isRanByBotOwner && !isUserOrGuildPatron(event)) return

        val userInput = event.message.contentRaw.split("\\s+".toRegex(), 2)
        if (userInput.size < 2) {
            sendSuccess(event.message)
            return
        }

        var userIn = ctx.argsRaw

        if (userIn.startsWith("```") && userIn.endsWith("```"))
            userIn = userIn
                .replace("```(.*)\n".toRegex(), "")
                .replace("\n?```".toRegex(), "")

        val script = importString + userIn


        var timeout = 5000L

        if (isRanByBotOwner && ctx.invoke.toLowerCase() != "safeeval") {
            timeout = 60000L

            engine.setVariable("commandManager", ctx.commandManager)

            engine.setVariable("message", ctx.message)
            engine.setVariable("channel", ctx.message.textChannel)
            engine.setVariable("guild", ctx.guild)
            engine.setVariable("member", ctx.member)
            engine.setVariable("author", ctx.author)
            engine.setVariable("jda", ctx.jda)
            engine.setVariable("shardManager", ctx.jda.asBot().shardManager)
            engine.setVariable("event", event)

            engine.setVariable("skraa", script)
            engine.setVariable("args", ctx.args)
            engine.setVariable("ctx", ctx)

            @SinceSkybot("3.58.0")
            GlobalScope.launch(Dispatchers.Default, start = CoroutineStart.ATOMIC, block = {
                return@launch eval(event, isRanByBotOwner, script, timeout)
            })
        } else {
            protectedShell.setVariable("author", UserDelegate(event.author))
            protectedShell.setVariable("guild", GuildDelegate(event.guild))
            protectedShell.setVariable("jda", JDADelegate(event.jda))
            protectedShell.setVariable("member", MemberDelegate(event.member))
            protectedShell.setVariable("channel", TextChannelDelegate(event.channel))
            if (event.channel.parent != null)
                protectedShell.setVariable("category", CategoryDelegate(event.channel.parent!!))

            @SinceSkybot("3.58.0")
            GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
                return@launch eval(event, false, script, timeout)
            }
        }
    }

    override fun help() = """Evaluate java code on the bot
        |Usage: `$PREFIX$name <java/groovy code>`
    """.trimMargin()

    override fun getName() = "eval"

    override fun getAliases(): Array<String> {
        return arrayOf("eval™", "evaluate", "evan", "eva;", "safeeval")
    }

    /*fun toggleFilter(): Boolean {
        val ret = runIfNotOwner
        runIfNotOwner = !runIfNotOwner
        return ret
    }

    fun setFilter(status: Boolean): Boolean {
        val ret = runIfNotOwner
        runIfNotOwner = status
        return ret
    }*/

    @SinceSkybot("3.58.0")
    private suspend fun eval(event: GuildMessageReceivedEvent, isRanByBotOwner: Boolean, script: String, millis: Long) {
        val time = measureTimeMillis {
            val out = withTimeoutOrNull(millis) {
                engine.setVariable("scope", this)
                try {
                    if (isRanByBotOwner) engine.evaluate(script)
                    else {
                        filter.register()
                        protectedShell.evaluate(script)
                    }
                } catch (ex: Throwable) {
                    ex
                } finally {
                    filter.unregister()
                }
            }
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
                    sendErrorWithMessage(event.message, "ERROR: " + out.toString())
                }
                is IllegalArgumentException, is DoomedException -> {
                    out as RuntimeException
                    sendErrorWithMessage(event.message, "ERROR: " + out.toString())
                }
                is Throwable -> {
                    if (Settings.useJSON)
                        sendErrorJSON(event.message, out, true)
                    else {
                        sendMsg(event, "ERROR: " + out.toString())
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
                    if (isRanByBotOwner) {
                        MessageBuilder()
                            .appendCodeBlock(out.toString(), "")
                            .buildAll(MessageBuilder.SplitPolicy.ANYWHERE)
                            .forEach { it -> sendMsg(event, it) }
                    } else {
                        if (filter.containsMentions(out.toString())) {
                            sendErrorWithMessage(event.message, "**ERROR:** Mentioning people!")
                        } else {
                            sendMsg(event, "**" + event.author.name
                                + ":** " + out.toString()
                                .replace("@here".toRegex(), "@h\u0435re")
                                .replace("@everyone".toRegex(), "@\u0435veryone"))
                        }
                    }
                }
            }
        }
        logger.info("${TextColor.PURPLE}Took ${time}ms for evaluating last script${TextColor.RESET}")
    }
}

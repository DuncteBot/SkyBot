/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withTimeoutOrNull
import me.duncte123.botCommons.messaging.MessageUtils.*
import me.duncte123.botCommons.text.TextColor
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.commands.essentials.eval.filter.EvalFilter
import ml.duncte123.skybot.entities.delegate.*
import ml.duncte123.skybot.exceptions.DoomedException
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.MessageUtils.sendErrorJSON
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.requests.RestAction
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.kohsuke.groovy.sandbox.SandboxTransformer
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import kotlin.system.measureTimeMillis

class EvalCommand : Command() {

    private val protectedShell: GroovyShell
    private val engine: ScriptEngine
    private val packageImports: List<String>
    private val classImports: List<String>
    private val staticImports: List<String>
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
                if (filter.filterArrays(scriptText))
                    throw DoomedException("Arrays are not allowed")
                if (filter.filterLoops(scriptText))
                    throw DoomedException("Loops are not allowed")
                if(scriptText.isEmpty())
                    return null
                return super.evaluate(scriptText)
            }
        }
        engine = ScriptEngineManager().getEngineByName("groovy")
        packageImports = listOf(
                "java.io",
                "java.lang",
                "java.math",
                "java.util",
                "net.dv8tion.jda.core",
                "net.dv8tion.jda.core.entities",
                "net.dv8tion.jda.core.entities.impl",
                "net.dv8tion.jda.core.managers",
                "net.dv8tion.jda.core.managers.impl",
                "net.dv8tion.jda.core.utils",
                "ml.duncte123.skybot.utils",
                "ml.duncte123.skybot.entities",
                "ml.duncte123.skybot.entities.delegate",
                "ml.duncte123.skybot")
        classImports = listOf(
                "ml.duncte123.skybot.exceptions.DoomedException"
        )

        staticImports = listOf(
                "ml.duncte123.skybot.objects.EvalFunctions.*",
                "me.duncte123.botCommons.messaging.MessageUtils.*",
                "ml.duncte123.skybot.utils.MessageUtils.*"
        )
    }

    override fun executeCommand(ctx: CommandContext) {

        val event = ctx.event

        val isRanByBotOwner = isDev(event.author) || event.author.idLong == Settings.OWNER_ID
        /*if (!isRanByBotOwner && !runIfNotOwner)
            return*/

        if (!isRanByBotOwner && !isUserOrGuildPatron(event)) return

        val importString = packageImports.joinToString(separator = ".*\nimport ", prefix = "import ", postfix = ".*\n import ") +
                classImports.joinToString(separator = "\nimport ", postfix = "\n") +
                staticImports.joinToString(prefix = "import static ", separator = "\nimport static ", postfix = "\n")

        val userInput = event.message.contentRaw.split("\\s+".toRegex(), 2)
        if (userInput.size < 2) {
            sendSuccess(event.message)
            return
        }

        var userIn = ctx.argsRaw

        if(userIn.startsWith("```") && userIn.endsWith("```"))
            userIn = userIn
                    .replace("```(.*)\n".toRegex(), "")
                    .replace("\n?```".toRegex(), "")

        val script = importString + userIn


        var timeout = 5000L

        if (isRanByBotOwner && ctx.invoke.toLowerCase() != "safeeval") {
            timeout = 60000L

            engine.put("commandManager", ctx.commandManager)

            engine.put("message", ctx.message)
            engine.put("channel", ctx.message.textChannel)
            engine.put("guild", ctx.guild)
            engine.put("member", ctx.member)
            engine.put("author", ctx.author)
            engine.put("jda", ctx.jda)
            engine.put("shardManager", ctx.jda.asBot().shardManager)
            engine.put("event", event)

            engine.put("skraa", script)
            engine.put("args", ctx.args)
            engine.put("ctx", ctx)

            @SinceSkybot("3.58.0")
            launch(start = CoroutineStart.ATOMIC) {
                //            async(start = CoroutineStart.ATOMIC) {
                return@launch eval(event, isRanByBotOwner, script, timeout)
            }
        } else {
            protectedShell.setVariable("author", UserDelegate(event.author))
            protectedShell.setVariable("guild", GuildDelegate(event.guild))
            protectedShell.setVariable("jda", JDADelegate(event.jda))
            protectedShell.setVariable("member", MemberDelegate(event.member))
            protectedShell.setVariable("channel", TextChannelDelegate(event.channel))
            if (event.channel.parent != null)
                protectedShell.setVariable("category", CategoryDelegate(event.channel.parent!!))

            @SinceSkybot("3.58.0")
            launch {
                //            async {
                return@launch eval(event, false, script, timeout)
            }
        }

        // Garbage collect
        System.gc()
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
                engine.put("scope", this)
                try {
                    if (isRanByBotOwner) engine.eval(script)
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
                                .append(out.toString())
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

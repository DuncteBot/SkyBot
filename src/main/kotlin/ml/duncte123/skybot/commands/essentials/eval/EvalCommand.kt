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

import Java.lang.VRCubeException
import groovy.lang.GroovyShell
import kotlinx.coroutines.experimental.*
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.commands.essentials.eval.filter.EvalFilter
import ml.duncte123.skybot.entities.delegate.*
import ml.duncte123.skybot.objects.EvalFunctions
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.unstable.utils.ComparatingUtils
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.MessageUtils
import ml.duncte123.skybot.utils.TextColor
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.kohsuke.groovy.sandbox.SandboxTransformer
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeoutException
import java.util.function.Consumer
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import kotlin.system.measureTimeMillis

class EvalCommand : Command() {

    private val protectedShell: GroovyShell
    private val engine: ScriptEngine
    private val packageImports: List<String>
    private val classImports: List<String>
    private val services = ArrayList<ScheduledExecutorService>()
    private val filter = EvalFilter()

    private var runIfNotOwner = true

    /**
     * This initialises the engine
     */
    init {
        this.category = CommandCategory.UNLISTED
        // The GroovyShell is for the public eval
        protectedShell = object : GroovyShell(
                CompilerConfiguration()
                        .addCompilationCustomizers(SandboxTransformer())) {
            @Throws(CompilationFailedException::class)
            override fun evaluate(scriptText: String): Any {
                if (filter.filterArrays(scriptText))
                    throw VRCubeException("Arrays are not allowed")
                if (filter.filterLoops(scriptText))
                    throw VRCubeException("Loops are not allowed")
                return super.evaluate(scriptText)
            }
        }
        engine = ScriptEngineManager().getEngineByName("groovy")
        packageImports = listOf(
                "java.io",
                "java.lang",
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
                "ml.duncte123.skybot.objects.FakeInterface",
                "Java.lang.VRCubeException"
        )
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        @Suppress("DEPRECATION")
        val isRanByBotOwner = Settings.wbkxwkZPaG4ni5lm8laY.contains(event.author.id) || event.author.id == Settings.ownerId
        if (!isRanByBotOwner && !runIfNotOwner)
            return

        if (!isRanByBotOwner && !isPatron(event.author, event.channel)) return

        val importString = packageImports.joinToString(separator = ".*\nimport ", prefix = "import ", postfix = ".*\n import ") +
                classImports.joinToString(separator = "\n", postfix = "\n")

        val script = try {
            importString + event.message.contentRaw.split("\\s+".toRegex(), 2)[1]
        } catch (ex: ArrayIndexOutOfBoundsException) {
            MessageUtils.sendSuccess(event.message)
            return
        }

        var timeout = 5000L

        if (isRanByBotOwner) {
            timeout = 60000L

            engine.put("commandManager", AirUtils.commandManager)

            engine.put("message", event.message)
            engine.put("channel", event.message.textChannel)
            engine.put("guild", event.guild)
            engine.put("member", event.member)
            engine.put("user", event.author)
            engine.put("jda", event.jda)
            engine.put("shardManager", event.jda.asBot().shardManager)
            engine.put("event", event)

            engine.put("skraa", script)
            engine.put("args", args)

            engine.put("funs", EvalFunctions())

            @SinceSkybot("3.58.0")
            async(start = CoroutineStart.ATOMIC) {
                return@async eval(event, isRanByBotOwner, script, timeout)
            }
        } else {
            protectedShell.setVariable("user", UserDelegate(event.author))
            protectedShell.setVariable("guild", GuildDelegate(event.guild))
            protectedShell.setVariable("jda", JDADelegate(event.jda))
            protectedShell.setVariable("member", MemberDelegate(event.member))
            protectedShell.setVariable("channel", TextChannelDelegate(event.channel))
            if (event.channel.parent != null)
                protectedShell.setVariable("category", CategoryDelegate(event.channel.parent!!))

            @SinceSkybot("3.58.0")
            async {
                return@async eval(event, isRanByBotOwner, script, timeout)
            }
        }

        // Garbage collect
        System.gc()
    }

    fun shutdown() {
        services.forEach(Consumer<ScheduledExecutorService> { it.shutdownNow() })
        services.clear()
    }

    override fun help(): String {
        return "A simple eval command"
    }

    override fun getName(): String {
        return "eval"
    }

    override fun getAliases(): Array<String> {
        return arrayOf("eval™", "evaluate", "evan", "eva;")
    }

    fun toggleFilter(): Boolean {
        val ret = runIfNotOwner
        runIfNotOwner = !runIfNotOwner
        return ret
    }

    fun setFilter(status: Boolean): Boolean {
        val ret = runIfNotOwner
        runIfNotOwner = status
        return ret
    }

    @SinceSkybot("3.58.0")
    suspend fun eval(event: GuildMessageReceivedEvent, isRanByBotOwner: Boolean, script: String, millis: Long) {
        val time = measureTimeMillis {
            lateinit var coroutine: CoroutineScope
            val out = withTimeoutOrNull(millis) {
                engine.put("scope", this)
                coroutine = this
                try {
                    if(isRanByBotOwner) engine.eval(script)
                    else {
                        filter.register()
                        protectedShell.evaluate(script)
                    }
                } catch (ex: Throwable) {

                    ComparatingUtils.checkEx(ex)
                    ex
                }
            }
            when (out) {
                null -> {
                    coroutine.coroutineContext.cancel()
                    MessageUtils.sendSuccess(event.message)
                }
                is ArrayIndexOutOfBoundsException -> {
                    MessageUtils.sendSuccess(event.message)
                }
                is ExecutionException, is ScriptException -> {
                    out as Exception
                    MessageUtils.sendErrorWithMessage(event.message, event, "ERROR: " + out.cause.toString())
                }
                is TimeoutException, is InterruptedException, is IllegalStateException -> {
                    out as Exception
                    coroutine.coroutineContext.cancel()
                    if (coroutine.isActive)
                        coroutine.coroutineContext.cancel()
                    MessageUtils.sendErrorWithMessage(event.message, event, "ERROR: " + out.toString())
                }
                is IllegalArgumentException, is VRCubeException -> {
                    out as RuntimeException
                    MessageUtils.sendErrorWithMessage(event.message, event, "ERROR: " + out.toString())
                }
                is Throwable -> {
                    if (Settings.useJSON)
                        MessageUtils.sendErrorJSON(event.message, out, true)
                    else {
                        MessageUtils.sendMsg(event, "ERROR: " + out.toString())
                        // out.printStackTrace()
                    }
                }
                else -> {
                    if (isRanByBotOwner) {
                        MessageBuilder()
                                .append(out.toString())
                                .buildAll(MessageBuilder.SplitPolicy.ANYWHERE)
                                .forEach { it -> MessageUtils.sendMsg(event, it) }
                    } else {
                        if (filter.containsMentions(out.toString())) {
                            MessageUtils.sendErrorWithMessage(event.message, event, "**ERROR:** Mentioning people!")
                        } else {
                            MessageUtils.sendMsg(event, "**" + event.author.name
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

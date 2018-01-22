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
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withTimeout
import ml.duncte123.skybot.SinceSkybot
import ml.duncte123.skybot.commands.essentials.eval.filter.EvalFilter
import ml.duncte123.skybot.entities.delegate.*
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.Settings
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.kohsuke.groovy.sandbox.SandboxTransformer
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Consumer
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException

class EvalCommand : Command() {

    private val protectedShell: GroovyShell
    private val engine: ScriptEngine
    private val packageImports: List<String>
    private val classImports: List<String>
    private val services = ArrayList<ScheduledExecutorService>()
    private val filter = EvalFilter()

    private var runIfNotOwner = false

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
        engine = ScriptEngineManager(protectedShell.getClassLoader()).getEngineByName("groovy")
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
                "ml.duncte123.skybot.utils")
        classImports = listOf(
                "ml.duncte123.skybot.objects.FakeInterface",
                "Java.lang.VRCubeException"
        )

        //Add functions to the owner eval
        //This is because I want to use those methods in the eval
        try {
            engine.eval("def isEven(int number) {\n" +
                    "return number % 2 == 0\n" +
                    "}\n")
            engine.eval("def quick_mafs(int x) {\n" +
                    "def the_thing = x + 2 -1 \n " +
                    "return the_thing \n" +
                    "}")
        } catch (e: ScriptException) {
            e.printStackTrace()
        }

    }

    override fun executeCommand(invoke: String, args: Array<String>, event: GuildMessageReceivedEvent) {
        val isRanByBotOwner = Settings.wbkxwkZPaG4ni5lm8laY.contains(event.author.id) || event.author.id == Settings.ownerId

        if (!isRanByBotOwner && !runIfNotOwner)
            return

        if (!isRanByBotOwner && !hasUpvoted(event.author)) {
            sendError(event.message)
            sendEmbed(event,
                    EmbedUtils.embedMessage("This command is a hidden command, hidden commands are not available to users that have not upvoted the bot, " +
                            "Please consider to give this bot an upvote over at " +
                            "[https://discordbots.org/bot/210363111729790977](https://discordbots.org/bot/210363111729790977)\n" +
                            "\uD83D\uDDD2: The check might be limited and would have a minimum cooldown of 20 seconds!"))
            return
        }

        @SinceSkybot("3.58.0")
        val future: Deferred<Any?>?

        val importString = packageImports.joinToString(separator = ".*\nimport ", prefix = "import ", postfix = ".*\n import ") +
                classImports.joinToString(separator = "\n", postfix = "\n")

        val script = importString + event.message.contentRaw.split("\\s+".toRegex(), 2)[1]

        var timeout = 5

        if (isRanByBotOwner) {
            timeout = 60

            engine.put("commandManager", AirUtils.commandManager)

            engine.put("message", event.message)
            engine.put("channel", event.message.textChannel)
            engine.put("guild", event.guild)
            engine.put("member", event.member)
            engine.put("user", event.author)
            engine.put("jda", event.jda)
            engine.put("shardManager", event.jda.asBot().shardManager)
            engine.put("event", event)

            engine.put("args", args)

            @SinceSkybot("3.58.0")
            future = async {
                withTimeout(timeout.toLong(), TimeUnit.SECONDS) {
                    try {
                        engine.eval(script)
                    } catch (ex: Throwable) {
                        ex
                    }
                }
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
            future = async {
                withTimeout(timeout.toLong(), TimeUnit.SECONDS) {
                    try {
                        engine.eval(script)
                    } catch (ex: Throwable) {
                        ex
                    }
                }
            }
        }


        @SinceSkybot("3.58.0")
        async {
            val out = future.await()
            try {
                when {
                    out == null && out.toString().isEmpty() -> sendSuccess(event.message)
                    else -> {
                        when (out) {
                            is ArrayIndexOutOfBoundsException -> {
                                sendSuccess(event.message)
                            }
                            is ExecutionException, is ScriptException -> {
                                out as Exception
                                event.channel.sendMessage("ERROR: " + out.cause.toString()).queue()
                                //e.printStackTrace();
                                sendError(event.message)
                            }
                            is TimeoutException, is InterruptedException, is IllegalStateException -> {
                                future.cancel()
                                event.channel.sendMessage("ERROR: " + out.toString()).queue()
                                if (!future.isCancelled) future.cancel()
                                sendError(event.message)
                            }
                            is IllegalArgumentException, is VRCubeException -> {
                                out as RuntimeException
                                sendMsg(event, "ERROR: " + out::class.java.name + ": " + out.localizedMessage)
                                sendError(event.message)
                            }
                            is Throwable -> {
                                if (Settings.useJSON)
                                    sendErrorJSON(event.message, out, true)
                                else {
                                    sendMsg(event, "ERROR: " + out.toString())
                                    out.printStackTrace()
                                }
                            }
                            else -> {
                                if (isRanByBotOwner) {
                                    MessageBuilder()
                                            .append(out.toString())
                                            .buildAll(MessageBuilder.SplitPolicy.ANYWHERE)
                                            .forEach { it -> sendMsg(event, it) }
                                }
                                else {
                                    if (filter.containsMentions(out.toString())) {
                                        sendMsg(event, "**ERROR:** Mentioning people!")
                                        sendError(event.message)
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

                }
            } catch (ex: Throwable) {

            } finally {
                filter.unregister()
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
}

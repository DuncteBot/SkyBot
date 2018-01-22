///*
// * Skybot, a multipurpose discord bot
// *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License as published
// * by the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//package ml.duncte123.skybot.commands.essentials.eval;
//
//import Java.lang.VRCubeException;
//import groovy.lang.GroovyShell;
//import ml.duncte123.skybot.commands.essentials.eval.filter.EvalFilter;
//import ml.duncte123.skybot.entities.delegate.*;
//import ml.duncte123.skybot.objects.command.Command;
//import ml.duncte123.skybot.objects.command.CommandCategory;
//import ml.duncte123.skybot.utils.AirUtils;
//import ml.duncte123.skybot.utils.EmbedUtils;
//import ml.duncte123.skybot.utils.Settings;
//import net.dv8tion.jda.core.MessageBuilder;
//import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
//import org.codehaus.groovy.control.CompilationFailedException;
//import org.codehaus.groovy.control.CompilerConfiguration;
//import org.kohsuke.groovy.sandbox.SandboxTransformer;
//
//import javax.script.ScriptEngine;
//import javax.script.ScriptEngineManager;
//import javax.script.ScriptException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.*;
//import java.util.function.Supplier;
//import java.util.stream.Collectors;
//
//public class EvalCommand extends Command {
//
//    private GroovyShell protected_;
//    private ScriptEngine engine;
//    private List<String> packageImports;
//    private List<String> classImports;
//    private List<ScheduledExecutorService> services = new ArrayList<>();
//    private int evalCounter = 0;
//    private Supplier<ScheduledExecutorService> service =
//            () -> {
//                ScheduledExecutorService service
//                        = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Eval-Thread-" + evalCounter++));
//                services.add(service);
//                return service;
//            };
//    private EvalFilter filter = new EvalFilter();
//
//    private boolean runIfNotOwner = false;
//
//    /**
//     * This initialises the engine
//     */
//    public EvalCommand() {
//        this.category = CommandCategory.UNLISTED;
//        // The GroovyShell is for the public eval
//        protected_ = new GroovyShell(
//                            new CompilerConfiguration()
//                                    .addCompilationCustomizers(new SandboxTransformer())) {
//            @Override
//            public Object evaluate(String scriptText) throws CompilationFailedException {
//                if (filter.filterArrays(scriptText))
//                    throw new VRCubeException("Arrays are not allowed");
//                if (filter.filterLoops(scriptText))
//                    throw new VRCubeException("Loops are not allowed");
//                return super.evaluate(scriptText);
//            }
//        };
//        engine = new ScriptEngineManager(protected_.getClassLoader()).getEngineByName("groovy");
//        packageImports = Arrays.asList(
//                "java.io",
//                "java.lang",
//                "java.util",
//                "net.dv8tion.jda.core",
//                "net.dv8tion.jda.core.entities",
//                "net.dv8tion.jda.core.entities.impl",
//                "net.dv8tion.jda.core.managers",
//                "net.dv8tion.jda.core.managers.impl",
//                "net.dv8tion.jda.core.utils",
//                "ml.duncte123.skybot.utils",
//                "ml.duncte123.skybot.entities",
//                "ml.duncte123.skybot.entities.delegate",
//                "ml.duncte123.skybot.utils");
//        classImports = Arrays.asList(
//                "ml.duncte123.skybot.objects.FakeInterface",
//                "Java.lang.VRCubeException"
//        );
//
//        //Add functions to the owner eval
//        //This is because I want to use those methods in the eval
//        try {
//            engine.eval("def isEven(int number) {\n" +
//                        "return number % 2 == 0\n" +
//                    "}\n");
//            engine.eval( "def quick_mafs(int x) {\n" +
//                        "def the_thing = x + 2 -1 \n " +
//                        "return the_thing \n" +
//                    "}");
//        }
//        catch (ScriptException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
//        boolean isRanByBotOwner = Arrays.asList(Settings.wbkxwkZPaG4ni5lm8laY).contains(
//                event.getAuthor().getId()) ||
//                                          event.getAuthor().getId().equals(Settings.ownerId);
//
//        if (!isRanByBotOwner && !runIfNotOwner)
//            return;
//
//        if (!isRanByBotOwner && !hasUpvoted(event.getAuthor())) {
//            sendError(event.getMessage());
//            sendEmbed(event,
//                    EmbedUtils.embedMessage("This command is a hidden command, hidden commands are not available to users that have not upvoted the bot, " +
//                            "Please consider to give this bot an upvote over at " +
//                            "[https://discordbots.org/bot/210363111729790977](https://discordbots.org/bot/210363111729790977)\n" +
//                            "\uD83D\uDDD2: The check might be limited and would have a minimum cooldown of 20 seconds!"));
//            return;
//        }
//
//        ScheduledExecutorService service = this.service.get();
//
//        ScheduledFuture<Object> future = null;
//
//        try {
//            try {
//                String importString = "import " +
//                        packageImports.stream().collect(Collectors.joining(".*\nimport ")) + ".*\n import " +
//                        classImports.stream().collect(Collectors.joining("\n")) + "\n";
//
//                String script = importString + event.getMessage().getContentRaw().split("\\s+",2)[1];
//
//                int timeout = 5;
//
//                if (isRanByBotOwner) {
//                    timeout = 60;
//
//                    engine.put("commandManager", AirUtils.commandManager);
//
//                    engine.put("message", event.getMessage());
//                    engine.put("channel", event.getMessage().getTextChannel());
//                    engine.put("guild", event.getGuild());
//                    engine.put("member", event.getMember());
//                    engine.put("user", event.getAuthor());
//                    engine.put("jda", event.getJDA());
//                    engine.put("shardManager", event.getJDA().asBot().getShardManager());
//                    engine.put("event", event);
//
//                    engine.put("args", args);
//
//                    future = service.schedule(
//                            () -> engine.eval(script), 0, TimeUnit.MILLISECONDS);
//                } else {
//                    protected_.setVariable("user", new UserDelegate(event.getAuthor()));
//                    protected_.setVariable("guild", new GuildDelegate(event.getGuild()));
//                    protected_.setVariable("jda", new JDADelegate(event.getJDA()));
//                    protected_.setVariable("member", new MemberDelegate(event.getMember()));
//                    protected_.setVariable("channel", new TextChannelDelegate(event.getChannel()));
//                    if (event.getChannel().getParent() != null)
//                        protected_.setVariable("category", new CategoryDelegate(event.getChannel().getParent()));
//
//                    future = service.schedule(() -> {
//                        filter.register();
//                        return protected_.evaluate(script);
//                    }, 0, TimeUnit.MILLISECONDS);
//                }
//
//                Object out = future.get(timeout, TimeUnit.SECONDS);
//
//                if (out != null && !String.valueOf(out).isEmpty()) {
//                    if (isRanByBotOwner)
//                        (new MessageBuilder())
//                                .append(out.toString())
//                                .buildAll(MessageBuilder.SplitPolicy.ANYWHERE)
//                                .forEach(it -> sendMsg(event, it));
//                    else {
//                        if (filter.containsMentions(out.toString())) {
//                            sendMsg(event, "**ERROR:** Mentioning people!");
//                            sendError(event.getMessage());
//                        } else {
//                            sendMsg(event, "**" + event.getAuthor().getName()
//                                               + ":** " + out.toString()
//                                                          .replaceAll("@here", "@h\u0435re")
//                                                          .replaceAll("@everyone", "@\u0435veryone"));
//                        }
//                    }
//                } else {
//                    sendSuccess(event.getMessage());
//                }
//
//            } catch (ExecutionException e1) {
//                event.getChannel().sendMessage("ERROR: " + e1.getCause().toString()).queue();
//                //e.printStackTrace();
//                sendError(event.getMessage());
//            } catch (TimeoutException | InterruptedException e2) {
//                future.cancel(true);
//                event.getChannel().sendMessage("ERROR: " + e2.toString()).queue();
//                //e.printStackTrace();
//                if (!future.isCancelled()) future.cancel(true);
//                sendError(event.getMessage());
//            } catch (IllegalArgumentException | VRCubeException e3) {
//                sendMsg(event, "ERROR: " + e3.getClass().getName() + ": " + e3.getLocalizedMessage());
//                sendError(event.getMessage());
//                // Debuging System.out.println(EarthUtils.throwableToJSONObject(e3).toString(4));
//            } catch (ArrayIndexOutOfBoundsException e4) {
//                sendSuccess(event.getMessage());
//            }
//        } catch (Throwable thr) {
//            if (Settings.useJSON)
//                sendErrorJSON(event.getMessage(), thr, true);
//            else {
//                sendMsg(event, "ERROR: " + thr.toString());
//                thr.printStackTrace();
//            }
//        } finally {
//            filter.unregister();
//
//            services.remove(service);
//
//            // Just in case
//            service.shutdown();
//        }
//
//        // Garbage collect
//        System.gc();
//    }
//
//    public void shutdown() {
//        services.forEach(ScheduledExecutorService::shutdownNow);
//        services.clear();
//    }
//
//    @Override
//    public String help() {
//        return "A simple eval command";
//    }
//
//    @Override
//    public String getName() {
//        return "eval";
//    }
//
//    @Override
//    public String[] getAliases() {
//        return new String[]{"eval™", "evaluate", "evan", "eva;"};
//    }
//
//    public boolean toggleFilter() {
//        boolean ret = runIfNotOwner;
//        runIfNotOwner = !runIfNotOwner;
//        return ret;
//    }
//
//    public boolean setFilter(boolean status) {
//        boolean ret = runIfNotOwner;
//        runIfNotOwner = status;
//        return ret;
//    }
//}

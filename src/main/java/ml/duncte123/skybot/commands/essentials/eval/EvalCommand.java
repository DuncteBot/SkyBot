/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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
 *
 */

package ml.duncte123.skybot.commands.essentials.eval;

import Java.lang.VRCubeException;
import groovy.lang.GroovyShell;
import ml.duncte123.skybot.commands.essentials.eval.filter.EvalFilter;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.delegate.GuildDelegate;
import ml.duncte123.skybot.objects.delegate.JDADelegate;
import ml.duncte123.skybot.objects.delegate.UserDelegate;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.groovy.sandbox.SandboxTransformer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class EvalCommand extends Command {
    
    private GroovyShell protected_;
    private ScriptEngine engine;
    private List<String> packageImports;
    private List<String> classImports;
    private List<ScheduledExecutorService> services = new ArrayList<>();
    private int evalCounter = 0;
    private Supplier<ScheduledExecutorService> service =
            () -> {
                ScheduledExecutorService service
                        = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Eval-Thread-" + evalCounter++));
                services.add(service);
                return service;
            };
    private EvalFilter filter = new EvalFilter();
    
    /**
     * This initialises the engine
     */
    public EvalCommand() {
        this.category = CommandCategory.UNLISTED;
        // The GroovyShell is for the public eval
        protected_ = new GroovyShell(
                            new CompilerConfiguration()
                                    .addCompilationCustomizers(new SandboxTransformer()));
        engine = new ScriptEngineManager(protected_.getClassLoader()).getEngineByName("groovy");
        packageImports = Arrays.asList(
                "java.io",
                "java.lang",
                "java.util",
                "net.dv8tion.jda.core",
                "net.dv8tion.jda.core.entities",
                "net.dv8tion.jda.core.entities.impl",
                "net.dv8tion.jda.core.managers",
                "net.dv8tion.jda.core.managers.impl",
                "net.dv8tion.jda.core.utils",
                "ml.duncte123.skybot.utils");
        classImports = Arrays.asList(
                "ml.duncte123.skybot.objects.FakeInterface",
                "Java.lang.VRCubeException"
        );
    }
    
    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        boolean isRanByBotOwner = Arrays.asList(Settings.wbkxwkZPaG4ni5lm8laY).contains(
                event.getAuthor().getId()) ||
                                          event.getAuthor().getId().equals(Settings.ownerId);
        
        if (!isRanByBotOwner && !hasUpvoted(event.getAuthor())) {
            sendError(event.getMessage());
            sendEmbed(event,
                    EmbedUtils.embedMessage("This command is a hidden command, hidden commands are not available to user that have not upvoted the bot, " +
                            "Please consider to give this bot an upvote over at " +
                            "[https://discordbots.org/bot/210363111729790977](https://discordbots.org/bot/210363111729790977)"));
            return;
        }
        
        ScheduledExecutorService service = this.service.get();
        
        ScheduledFuture<Object> future = null;
        
        try {
            try {
                StringBuilder importStringBuilder = new StringBuilder();
                for (final String s : packageImports) {
                    importStringBuilder.append("import ").append(s).append(".*;\n");
                }
                for (final String s : classImports) {
                    importStringBuilder.append("import ").append(s).append(";\n");
                }
                
                String script = importStringBuilder.toString() +
                                    event.getMessage().getRawContent()
                                        .substring(event.getMessage().getRawContent()
                                                   .split(" ")[0].length());
                
                int timeout = 5;
                if (isRanByBotOwner) {
                    timeout = 60;
                    
                    engine.put("commandmanager", AirUtils.commandManager);
                    
                    engine.put("message", event.getMessage());
                    engine.put("channel", event.getMessage().getTextChannel());
                    engine.put("guild", event.getGuild());
                    engine.put("member", event.getMember());
                    engine.put("jda", event.getJDA());
                    engine.put("shardmanager", event.getJDA().asBot().getShardManager());
                    engine.put("event", event);
                    
                    engine.put("args", args);
                    
                    future = service.schedule(
                            () -> engine.eval(script), 0, TimeUnit.MILLISECONDS);
                } else {
                    
                    if (filter.filterArrays(script))
                        throw new VRCubeException("Arrays are not allowed");
                    if (filter.filterLoops(script))
                        throw new VRCubeException("Loops are not allowed");
                    
                    protected_.setVariable("user", new UserDelegate(event.getAuthor()));
                    protected_.setVariable("guild", new GuildDelegate(event.getGuild()));
                    protected_.setVariable("jda", new JDADelegate(event.getJDA()));
                    
                    future = service.schedule(() -> {
                        filter.register();
                        return protected_.evaluate(script);
                    }, 0, TimeUnit.MILLISECONDS);
                }
                
                Object out = future.get(timeout, TimeUnit.SECONDS);
                
                if (out != null && !String.valueOf(out).isEmpty()) {
                    if (isRanByBotOwner)
                        (new MessageBuilder())
                                .append(out.toString())
                                .buildAll(MessageBuilder.SplitPolicy.ANYWHERE)
                                .forEach(it -> event.getChannel().sendMessage(it).queue());
                    else {
                        if (filter.containsMentions(out.toString())) {
                            sendMsg(event, "**ERROR:** Mentioning people!");
                            sendError(event.getMessage());
                        } else {
                            sendMsg(event, "**" + event.getAuthor().getName()
                                               + ":** " + out.toString()
                                                          .replaceAll("@here", "@h\u0435re")
                                                          .replaceAll("@everyone", "@\u0435veryone"));
                        }
                    }
                } else {
                    sendSuccess(event.getMessage());
                }
                
            } catch (ExecutionException e1) {
                event.getChannel().sendMessage("ERROR: " + e1.getCause().toString()).queue();
                //e.printStackTrace();
                sendError(event.getMessage());
            } catch (TimeoutException | InterruptedException e2) {
                future.cancel(true);
                event.getChannel().sendMessage("ERROR: " + e2.toString()).queue();
                //e.printStackTrace();
                if (!future.isCancelled()) future.cancel(true);
                sendError(event.getMessage());
            } catch (IllegalArgumentException | VRCubeException e3) {
                sendMsg(event, "ERROR: " + e3.getClass().getName() + ": " + e3.getMessage());
                sendError(event.getMessage());
            }
        } catch (Throwable thr) {
            sendMsg(event, "ERROR: " + thr.toString());
            thr.printStackTrace();
        } finally {
            filter.unregister();
            
            services.remove(service);
            
            // Just in case
            service.shutdown();
        }
        
        // Garbage collect
        System.gc();
    }
    
    public void shutdown() {
        services.forEach(ScheduledExecutorService::shutdownNow);
        services.clear();
    }
    
    @Override
    public String help() {
        return "A simple eval command";
    }
    
    @Override
    public String getName() {
        return "eval";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"eval™", "evaluate"};
    }
}

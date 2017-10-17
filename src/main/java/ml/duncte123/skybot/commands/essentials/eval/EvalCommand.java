/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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

package ml.duncte123.skybot.commands.essentials.eval;

import groovy.lang.GroovyShell;
import ml.duncte123.skybot.commands.essentials.eval.filter.EvalFilter;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.delegate.JDA.JDADelegate;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.groovy.sandbox.SandboxTransformer;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class EvalCommand extends Command {

    private ScriptEngine engine;
    private GroovyShell sh;
    private List<String> packageImports;
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Eval-Thread"));
    private EvalFilter filter = new EvalFilter();

    /**
     * This initialises the engine
     */
    public EvalCommand() {
        //the GroovyShell is for the public eval
        sh = new GroovyShell(
                new CompilerConfiguration().addCompilationCustomizers(new SandboxTransformer())
        );
        //ScriptEngine for owner eval
        engine = new ScriptEngineManager(sh.getClassLoader()).getEngineByName("groovy");
        packageImports =  Arrays.asList("java.io",
                "java.lang",
                "java.util",
                "net.dv8tion.jda.core",
                "net.dv8tion.jda.core.entities",
                "net.dv8tion.jda.core.entities.impl",
                "net.dv8tion.jda.core.managers",
                "net.dv8tion.jda.core.managers.impl",
                "net.dv8tion.jda.core.utils",
                "ml.duncte123.skybot.utils");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        try {
            //this.service = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Eval-Thread"));

            Bindings bindings = engine.createBindings();

            bindings.put("commands", AirUtils.commandSetup.getCommands());

            bindings.put("message", event.getMessage());
            bindings.put("channel", event.getMessage().getTextChannel());
            bindings.put("guild", event.getGuild());
            bindings.put("member", event.getMember());
            bindings.put("jda", new JDADelegate(event.getJDA()));
            bindings.put("event", event);

            bindings.put("args", args);

            StringBuilder importStringBuilder = new StringBuilder();
            for (final String s : packageImports) {
                importStringBuilder.append("import ").append(s).append(".*;");
            }

            String script = importStringBuilder.toString() +
                    event.getMessage().getRawContent().substring(event.getMessage().getRawContent().split(" ")[0].length())
                            .replaceAll("getToken", "getSelfUser");

            //ScheduledFuture<Object> future = service.schedule(() -> engine.eval(script), 0, TimeUnit.MILLISECONDS);
            ScheduledFuture<?> future;
            int timeout = 5;
            if(event.getAuthor().getId().equals(Settings.ownerId)) {
                timeout = 10;
                future = service.schedule(() -> engine.eval(script, bindings), 0, TimeUnit.MILLISECONDS);
            } else {

                if(filter.filterArrays(script))
                    throw new IllegalArgumentException("Arrays are not allowed");
                if(filter.filterLoops(script))
                    throw new IllegalArgumentException("Loops are not allowed");

                if(script.contains("println")) { //CC VRCube
                    sendError(event.getMessage());
                    return;
                }

                future = service.schedule(() -> {
                    filter.register();
                    return sh.evaluate(script);
                }, 0, TimeUnit.MILLISECONDS);
                //sendError(event.getMessage());
                //return;
            }

            Object out = null;

            try {
                out = future.get(timeout, TimeUnit.SECONDS);
            }
            catch (ExecutionException e)  {
                event.getChannel().sendMessage("Error: " + e.getCause().toString()).queue();
                //e.printStackTrace();
                sendError(event.getMessage());
                return;
            }
            catch (TimeoutException | InterruptedException e) {
                service.awaitTermination(4, TimeUnit.SECONDS);
                event.getChannel().sendMessage("Error: " + e.toString()).queue();
                //e.printStackTrace();
                if(!future.isCancelled()) future.cancel(true);
                sendError(event.getMessage());
                return;
            }

            future.cancel(true);

            if (out != null && !String.valueOf(out).isEmpty() ) {
                sendMsg(event, out.toString());
            } else {
                sendSuccess(event.getMessage());
            }

        }
        /*catch (ScriptException e) {
            event.getChannel().sendMessage("Error: " + e.getMessage()).queue();
            sendError(event.getMessage());
            return;
        }*/
        catch (Exception e1) {
            event.getChannel().sendMessage("Error: " + e1.getMessage()).queue();
            sendError(event.getMessage());
            //e1.printStackTrace();/
        }
        //service.shutdown();
        System.gc();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        return "A simple eval command";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "eval";
    }
}

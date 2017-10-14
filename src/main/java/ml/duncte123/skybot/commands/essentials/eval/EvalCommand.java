package ml.duncte123.skybot.commands.essentials.eval;

import groovy.lang.GroovyShell;
import ml.duncte123.skybot.commands.essentials.eval.filter.EvalFilter;
import ml.duncte123.skybot.objects.command.Command;
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
    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Eval-Thread"));
    private EvalFilter filter = new EvalFilter();

    /**
     * This initialises the engine
     */
    public EvalCommand() {
        //engine = new ScriptEngineManager().getEngineByName("groovy");
        sh = new GroovyShell(
                new CompilerConfiguration().addCompilationCustomizers(new SandboxTransformer())
        );
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

            Bindings bindings = engine.createBindings();

            bindings.put("commands", AirUtils.commandSetup.getCommands());

            bindings.put("message", event.getMessage());
            bindings.put("channel", event.getChannel());
            bindings.put("guild", event.getGuild());
            bindings.put("member", event.getMember());
            bindings.put("jda", event.getJDA());
            bindings.put("event", event);

            bindings.put("args", args);

            StringBuilder importStringBuilder = new StringBuilder();
            for (final String s : packageImports) {
                //importStringBuilder.append("import ").append(s).append(".*;");
            }

            String script = importStringBuilder.toString() +
                    event.getMessage().getRawContent().substring(event.getMessage().getRawContent().split(" ")[0].length())
                            .replaceAll("getToken", "getSelfUser");

            //ScheduledFuture<Object> future = service.schedule(() -> engine.eval(script), 0, TimeUnit.MILLISECONDS);
            ScheduledFuture<Object> future;
            if(event.getAuthor().getId().equals(Settings.ownerId)) {
                future = service.schedule(() -> engine.eval(script, bindings), 0, TimeUnit.MILLISECONDS);
            } else {
                future = service.schedule(() -> {
                    filter.register();
                    return sh.evaluate(script);
                }, 0, TimeUnit.MILLISECONDS);
            }

            Object out = null;
            int timeout = 10;

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
                future.cancel(true);
                event.getChannel().sendMessage("Error: " + e.toString()).queue();
                //e.printStackTrace();
                sendError(event.getMessage());
                return;
            }

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

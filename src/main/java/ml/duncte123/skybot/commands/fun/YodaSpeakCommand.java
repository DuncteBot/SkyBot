package ml.duncte123.skybot.commands.fun;

import me.duncte123.botCommons.web.WebUtils;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static ml.duncte123.skybot.utils.MessageUtils.sendMsg;

public class YodaSpeakCommand extends Command {
    @Override
    public void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event) {

        if(args.length < 1) {
            sendMsg(event, "Correct usage: `" + PREFIX + getName() + " <A sentence.>`");
            return;
        }

        WebUtils.ins.prepareRaw(new Request.Builder()
                .url("https://apis.duncte123.me/yoda?sentence=" + StringUtils.join(args, "+"))
                .get()
                .addHeader("X-User-id", event.getJDA().getSelfUser().getId())
                .addHeader("X-client-token", event.getJDA().getToken())
                .addHeader("Accept", "text/plain")
                .addHeader("User-Agent", WebUtils.getUserAgent())
                .build(), Response::body).async(
                (body) -> {
                    try {
                        final String text = body.string();
                        logger.debug("Yoda response: " + text);
                        sendMsg(event, text);
                    } catch (IOException e) {
                        e.printStackTrace();
                        sendMsg(event, "Yoda is asleep tell my developers to wake him up");
                    }
                },
                error -> {
                    error.printStackTrace();
                    sendMsg(event, "Yoda is asleep tell my developers to wake him up");
                }
        );

    }

    @Override
    public String help() {
        return "Convert your sentences into yoda speak.\n" +
                "Usage: `" + PREFIX + getName() + " <A sentence.>`";
    }

    @Override
    public String getName() {
        return "yoda";
    }
}

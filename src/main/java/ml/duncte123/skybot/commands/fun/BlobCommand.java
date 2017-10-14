package ml.duncte123.skybot.commands.fun;

import org.apache.commons.lang3.StringUtils;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.Settings;
import ml.duncte123.skybot.utils.URLConnectionReader;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class BlobCommand extends Command {
    /**
     * {@inheritDoc}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        String blob = "blobnomcookie";

        if(args.length > 0) {
            blob = StringUtils.join(args);
        }

            Response response = URLConnectionReader.getRequest("https://i.duncte123.ml/blob/" + blob + ".png");

            ResponseBody responseBody = response.body();

            if(responseBody.contentLength() <= 0) {
                sendMsg(event, "This blob was not found on the server!!!");
                if(response != null) {
                    response.close();
                }
                return;
            }

            event.getChannel().sendFile(responseBody.byteStream(), "blob.png", null).queue(
                    unused -> {
                        if(response != null) {
                            response.close();
                        }
                    }
            );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        return "Gives you a blob.\n" +
                "Usage: `" + Settings.prefix+getName() + " [blob name]`";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "blob";
    }
}

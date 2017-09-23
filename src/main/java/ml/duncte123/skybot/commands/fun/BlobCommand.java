package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class BlobCommand extends Command {
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        String blob = "blobnomcookie";

        if(args.length > 0) {
            blob = StringUtils.join(args);
        }

        try {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://i.duncte123.ml/blob/" + blob + ".png")
                    .get()
                    .build();

            Response response = client.newCall(request).execute();

            //InputStream blobFile = response.body().byteStream();
            ResponseBody responseBody = response.body();

            if(responseBody.contentLength() <= 0) {
                sendMsg(event, "This blob was not found on the server!!!");
                return;
            }

            event.getChannel().sendFile(responseBody.byteStream(), "blob.png", null).queue();

            //response.close();
        }
        catch (IOException ioe) {
            sendMsg(event, "ERROR: " + ioe.getMessage());
        }
        catch (NullPointerException nulle) {
            nulle.printStackTrace();
            sendMsg(event, "This blob was not found on the server!!! <:regional_indicator_b1nzy:334221955911647234>");
        }

    }

    @Override
    public String help() {
        return "Gives you a blob.\n" +
                "Usage: `" + Settings.prefix+getName() + " [blob name]`";
    }

    @Override
    public String getName() {
        return "blob";
    }
}

package ml.duncte123.skybot.commands.essentials;

import com.github.natanbc.reliqua.request.RequestException;
import me.duncte123.botCommons.web.WebUtils;
import me.duncte123.botCommons.web.WebUtilsErrorUtils;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

public class GithubReleaseCommand extends Command {

    private static final String CREATE_RELEASE = "https://api.github.com/repos/DuncteBot/SkyBot/releases?access_token="
            + AirUtils.CONFIG.getString("apis.github");

    private static final String UPLOAD_ASSET = "https://%s/repos/DuncteBot/SkyBot/releases/%s/assets?name=&s?access_token="
            + AirUtils.CONFIG.getString("apis.github");

    @Override
    public void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event) {
        if (!isDev(event.getAuthor())
                && !Settings.OWNER_ID.equals(event.getAuthor().getId())) {
            MessageUtils.sendError(event.getMessage());
            MessageUtils.sendMsg(event, "You must be the bot owner to run this command!");

            return;
        }

        // The running JAR file
        /*File running = new File(SkyBot.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        String name = "v" + running.getName().split("_")[0];*/
        String fullJarName = System.getProperty("java.class.path");
        String firstPart = fullJarName.split("-")[1];
        String name = "v" + firstPart.split("_")[0];

        // The message from after the {prefix}{invoke} syntax
        String message = event.getMessage().getContentDisplay();
        message = message.substring(message.indexOf(invoke) + 1);

        /*
         * Format is:
         *
         * Skybot <version>, release authored by <author>
         *
         * <given message>
         */
        message = String.format("SkyBot %s, release authored by %s%n%n%s",
                name,
                event.getAuthor().getName() + '#' + event.getAuthor().getDiscriminator(),
                message);

        JSONObject releaseOut = new JSONObject()
                .put("tag_name", name)
                .put("name", name)
                .put("target_commitish", "dev")
                .put("body", message)
                //Set it as draft until the asset is uploaded
                .put("draft", true)
                .put("prerelease", false);

        try {
            JSONObject releaseIn = WebUtils.ins.preparePost(CREATE_RELEASE,
                    Collections.singletonMap("User-Agent", "SkyBot-Release-Uploader"),
                    WebUtils.EncodingType.APPLICATION_JSON)
                    .build(success -> new JSONObject(success.body().source()), WebUtilsErrorUtils::handleError).execute();

            if (releaseIn == null)
                return;

            // Now upload the asset
        }
        catch (RequestException e) {
            MessageUtils.sendError(event.getMessage());
            MessageUtils.sendMsg(event, "An error occurred creating a release, it has been logged in the console");
        }
    }

    @Override
    public String help() {
        return "Releases a new version of the bot to GitHub, if configured";
    }

    @Override
    public String getName() {
        return "release";
    }
}

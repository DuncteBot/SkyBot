package ml.duncte123.skybot.commands.essentials;

import com.github.natanbc.reliqua.request.RequestException;
import me.duncte123.botCommons.web.WebUtils;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.utils.MiscUtil;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class GithubReleaseCommand extends Command {

    private static final String GITHUB_API = "https://api.github.com";
    private static final String REPO_PART = "/repos/DuncteBot/SkyBot";

    private static final String CREATE_RELEASE = GITHUB_API + REPO_PART + "/releases?access_token="
            + AirUtils.CONFIG.getString("apis.github");

    private static final String UPDATE_RELEASE = GITHUB_API + REPO_PART + "/releases/%s?access_token="
            + AirUtils.CONFIG.getString("apis.github");

    private static final String UPLOAD_ASSET = "https://uploads.github.com" + REPO_PART + "/releases/%s/assets?name=%s&access_token="
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
        message = message.substring(message.indexOf(invoke) + invoke.length() + 1);

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
            //You meant to post the json ramid?
            JSONObject releaseIn = WebUtils.ins.postJSON(CREATE_RELEASE, releaseOut,
                    mapper -> new JSONObject(mapper.body().string())).execute();

            //I don't think that this is what you meant to do
            /*JSONObject releaseIn = WebUtils.ins.preparePost(CREATE_RELEASE,
                    Collections.singletonMap("User-Agent", "SkyBot-Release-Uploader"),
                    WebUtils.EncodingType.APPLICATION_JSON)
                    .build(success -> new JSONObject(success.body().source()), WebUtilsErrorUtils::handleError).execute();*/

            if (releaseIn == null)
                return;

            System.out.println(releaseIn.toString(4));
            String releaseId = releaseIn.getString("id");

            // Now upload the asset

            FileInputStream jarStream = new FileInputStream(fullJarName);
            //MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            MediaType type = MediaType.parse("application/zip");
            RequestBody body = MiscUtil.createRequestBody(type, jarStream);
            //builder.addFormDataPart("file", fullJarName, body);

            Request request = new Request.Builder()
                    .post(body)
                    .header("User-Agent", WebUtils.getUserAgent())
                    .url(String.format(UPLOAD_ASSET, releaseId, fullJarName))
                    .build();

            WebUtils.ins.prepareRaw(request, mapper -> new JSONObject(mapper.body().string())).async( asset -> {
                for (int i = 0; i < 10; i++)
                    System.out.println();
                System.out.println(asset.toString(4));
            });



        }
        catch (RequestException | FileNotFoundException e) {
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

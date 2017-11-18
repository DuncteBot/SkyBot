package me.ramidzkh.skybot_updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class GithubRequester {

    public static String getLatestDownloadUrl(OkHttpClient client)
    throws IOException {
        Request request = new Request.Builder()
                .url("https://api.github.com/repos/duncte123/SkyBot/releases")
                .header("User-Agent", "Skybot auto updater")
                .build();
        Response response = client.newCall(request).execute();
        
        JsonArray releases = new JsonParser().parse(response.body().source().readUtf8()).getAsJsonArray();
        
        return ((JsonObject) releases.get(0)).get("assets").getAsJsonArray().get(0).getAsJsonObject()
                       .get("browser_download_url").getAsString();
    }

    public static void download(String url, OutputStream out)
    throws IOException {
        InputStream in = new URL(url).openStream();
        
        byte[] buf = new byte[1024];
        int len;
        
        while((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
    }

    public static void downloadLatest(OkHttpClient client, OutputStream out)
    throws IOException {
        download(getLatestDownloadUrl(client), out);
    }
}

package ml.duncte123.skybot.utils;

import okhttp3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class WebUtils {

    /**
     * Reads contents from a website and returns it to a string
     * @param url The url to read
     * @return The text contents
     * @throws IOException When something broke
     */
    public static String getText(String url) throws IOException {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        connection.addRequestProperty("User-Agent", "Binzy's personal pc");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();

        return response.toString();
    }

    /**
     * This makes a get request to the specified website
     * @param url The website to post to
     * @param accept What we will accept, {@link AcceptType AcceptType}
     * @return The {@link okhttp3.Response Response} from the webserver
     */
    public static Response getRequest(String url, AcceptType accept) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "DiscordBot (https://bot.duncte123.ml/)")
                .addHeader("Accept", accept.getType())
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            return client.newCall(request).execute();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This makes a post request to the specified website
     * @param url The website to post to
     * @return The {@link okhttp3.Response Response} from the webserver
     */
    public static Response getRequest(String url) {

        return getRequest(url, AcceptType.TEXT_JSON);
    }

    /**
     * This makes a post request to the specified website
     * @param url The website to post to
     * @param postFields the params for the post
     * @param accept What we will accept, {@link AcceptType AcceptType}
     * @return The {@link okhttp3.Response Response} from the webserver
     */
    public static Response postRequest(String url, Map<String, String> postFields, AcceptType accept) {

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse(accept.getType());

        StringBuilder postParams = new StringBuilder();

        for (String field : postFields.keySet()) {
            postParams.append(field).append("=").append(postFields.get(field)).append("&");
        }

        RequestBody body = RequestBody.create(mediaType, postParams.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("User-Agent", "DiscordBot (https://bot.duncte123.ml/)")
                .addHeader("Accept", accept.getType())
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            return client.newCall(request).execute();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This makes a post request to the specified website
     * @param url The website to post to
     * @param postFields the params for the post
     * @return The {@link okhttp3.Response Response} from the webserver
     */
    public static Response postRequest(String url, Map<String, String> postFields) {
        return postRequest(url, postFields, AcceptType.TEXT_JSON);
    }

    /**
     * This makes a post request to the specified website
     * @param url The website to post to
     * @param accept What we will accept, {@link AcceptType AcceptType}
     * @return The {@link okhttp3.Response Response} from the webserver
     */
    public static Response postRequest(String url, AcceptType accept) {
        return postRequest(url, new HashMap<>(), accept);
    }

    /**
     * This makes a post request to the specified website
     * @param url The website to post to
     * @return The {@link okhttp3.Response Response} from the webserver
     */
    public static Response postRequest(String url) {
        return postRequest(url,AcceptType.TEXT_JSON);
    }

    /**
     * This holds some variables that we will accept
     */
    public enum AcceptType {
        TEXT_PLAIN("text/plain"),
        TEXT_JSON("application/json"),
        TEXT_HTML("text/html"),
        TEXT_XML("application/xml");

        private String type;

        AcceptType(String type){
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
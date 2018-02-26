/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.utils;

import kotlin.Deprecated;
import kotlin.DeprecationLevel;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.config.Config;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebUtilsJava {

    private static final String USER_AGENT = "Mozilla/5.0 dunctebot (SkyBot v" + Settings.VERSION + ", https://bot.duncte123.me/)";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ScheduledExecutorService service
            = Executors.newScheduledThreadPool(2, r -> new Thread(r, "Web-Thread"));

    /**
     * Reads contents from a website and returns it to a string
     *
     * @param url The url to read
     * @return The text contents
     * @throws IOException When something broke
     */
    @Deprecated(message = "Well deprecated class", level = DeprecationLevel.ERROR)
    public static String getText(String url) throws IOException {
        return getRequest(url).body().string();
    }

    /**
     * Reads json data from a web page and returns it
     *
     * @param url The url to read
     * @return The text contents
     * @throws IOException When something broke
     */
    @Deprecated(message = "Well deprecated class", level = DeprecationLevel.ERROR)
    public static JSONObject getJSONObject(String url) throws IOException {
        return new JSONObject(getText(url));
    }

    /**
     * Reads json data from a web page and returns it
     *
     * @param url The url to read
     * @return The text contents
     * @throws IOException When something broke
     */
    @Deprecated(message = "Well deprecated class", level = DeprecationLevel.ERROR)
    public static JSONArray getJSONArray(String url) throws IOException {
        return new JSONArray(getText(url));
    }

    /**
     * Reads the contents of a url into an InputStream
     * @param url the url to read
     * @return the InputStream of the url
     */
    @Deprecated(message = "Well deprecated class", level = DeprecationLevel.ERROR)
    public static InputStream getInputStream(String url) {
        return getRequest(url).body().byteStream();
    }

    /**
     * This makes a get request to the specified website
     *
     * @param url    The website to post to
     * @param accept What we will accept, {@link AcceptType AcceptType}
     * @return The {@link Response} from the webserver
     */
    @Deprecated(message = "Well deprecated class", level = DeprecationLevel.ERROR)
    public static Response getRequest(String url, AcceptType accept) {
        return executeRequest(
                new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("User-Agent", USER_AGENT)
                        .addHeader("Accept", accept.getType())
                        .addHeader("cache-control", "no-cache")
                        .build()
        );
    }

    /**
     * This makes a post request to the specified website
     *
     * @param url The website to post to
     * @return The {@link Response} from the webserver
     */
    @Deprecated(message = "Well deprecated class", level = DeprecationLevel.ERROR)
    public static Response getRequest(String url) {
        return getRequest(url, AcceptType.TEXT_HTML);
    }

    /**
     * This makes a post request to the specified website
     *
     * @param url        The website to post to
     * @param postFields the params for the post (param name, param value)
     * @param accept     What we will accept, {@link AcceptType AcceptType}
     * @return The {@link Response} from the webserver
     */
    @Deprecated(message = "Well deprecated class", level = DeprecationLevel.ERROR)
    public static Response postRequest(String url, Map<String, Object> postFields, AcceptType accept) {
        StringBuilder postParams = new StringBuilder();

        for (Map.Entry<String, Object> entry : postFields.entrySet()) {
            postParams.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        return executeRequest(
                new Request.Builder()
                        .url(url)
                        .post(RequestBody.create(MediaType.parse(AcceptType.URLENCODED.getType()), Config.replaceLast(postParams.toString(), "\\&", "")))
                        .addHeader("User-Agent", USER_AGENT)
                        .addHeader("Accept", accept.getType())
                        .addHeader("cache-control", "no-cache")
                        .build()
        );
    }

    /**
     * This makes a post request to the specified website
     *
     * @param url        The website to post to
     * @param postFields the params for the post
     * @return The {@link Response} from the webserver
     */
    @Deprecated(message = "Well deprecated class", level = DeprecationLevel.ERROR)
    public static Response postRequest(String url, Map<String, Object> postFields) {
        return postRequest(url, postFields, AcceptType.URLENCODED);
    }

    /**
     * This makes a post request to the specified website
     *
     * @param url    The website to post to
     * @param accept What we will accept, {@link AcceptType AcceptType}
     * @return The {@link Response} from the webserver
     */
    @Deprecated(message = "Well deprecated class", level = DeprecationLevel.ERROR)
    public static Response postRequest(String url, AcceptType accept) {
        return postRequest(url, new HashMap<>(), accept);
    }

    /**
     * This makes a post request to the specified website
     *
     * @param url The website to post to
     * @return The {@link Response} from the webserver
     */
    @Deprecated(message = "Well deprecated class", level = DeprecationLevel.ERROR)
    public static Response postRequest(String url) {
        return postRequest(url, AcceptType.TEXT_JSON);
    }

    /**
     * This allows for JSON post requests to a website
     * @param url the website to post the json to
     * @param data the JSON data to post
     * @return The {@link Response} from the webserver
     */
    @Deprecated(message = "Well deprecated class", level = DeprecationLevel.ERROR)
    public static Response postJSON(String url, JSONObject data) {
        return executeRequest(
                new Request.Builder()
                        .url(url)
                        .post(RequestBody.create(MediaType.parse("application/json"), data.toString()))
                        .addHeader("User-Agent", USER_AGENT)
                        .build()
        );
    }

    /**
     * This translates a string into a different language
     * @param sourceLang the source language (example: "nl")
     * @param targetLang the target language (example: "en")
     * @param input the user inpur (example: "Dit is een test")
     * @return the output of the api
     * THe examples above will output the following <code>["This is a test","Dit is een test",null,null,1]</code>
     */
    @Deprecated(message = "Well deprecated class", level = DeprecationLevel.ERROR)
    public static JSONArray translate(String sourceLang, String targetLang, String input) {
        try {
            return getJSONArray(
                    "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" + sourceLang + "&tl=" + targetLang + "&dt=t&q=" + input
            ).getJSONArray(0).getJSONArray(0);
        }
        catch (IOException e) {
            return new JSONArray()
                    .put(input)
                    .put("null")
                    .put("")
                    .put("")
                    .put(0);
        }
    }

    /**
     * Executes a web request
     * @param request the {@link Request Request} to execute
     * @return the {@link Response Response} from the web server
     */
    public static Response executeRequest(Request request) {
        try {
            return service.schedule(() ->
                            client.newCall(request).execute()
                    , 0L, TimeUnit.MICROSECONDS).get();
        } catch (InterruptedException | ExecutionException e) {
            //e.printStackTrace();
            return null;
        }
    }

    /**
     * This holds some variables that we will accept
     */
    public enum AcceptType {
        TEXT_PLAIN("text/plain"),
        TEXT_JSON("application/json"),
        TEXT_HTML("text/html"),
        TEXT_XML("application/xml"),
        URLENCODED("application/x-www-form-urlencoded");

        private String type;

        AcceptType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
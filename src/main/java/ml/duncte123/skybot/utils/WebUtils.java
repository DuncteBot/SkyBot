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

public class WebUtils {

    private static String USER_AGENT = "Mozilla/5.0 dunctebot (SkyBot v" + Settings.version + ", https://bot.duncte123.me/)";
    private static final OkHttpClient client = new OkHttpClient();
    public static final ScheduledExecutorService service
            = Executors.newScheduledThreadPool(2, r -> new Thread(r, "Web-Thread"));

    /**
     * Reads contents from a website and returns it to a string
     *
     * @param url The url to read
     * @return The text contents
     * @throws IOException When something broke
     */
    public static String getText(String url) throws IOException {
        return getRequest(url).body().string();
    }

    /**
     * Reads the contents of a url into an InputStream
     * @param url the url to read
     * @return the InputStream of the url
     * @throws IOException when things break
     */
    public static InputStream getInputStream(String url) throws IOException {
        return getRequest(url).body().byteStream();
    }

    /**
     * This makes a get request to the specified website
     *
     * @param url    The website to post to
     * @param accept What we will accept, {@link AcceptType AcceptType}
     * @return The {@link Response} from the webserver
     */
    public static Response getRequest(String url, AcceptType accept) {
        try {
            return service.schedule(() -> {
                try {
                    return client.newCall(new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("User-Agent", USER_AGENT)
                        .addHeader("Accept", accept.getType())
                        .addHeader("cache-control", "no-cache")
                        .build()).execute();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }, 0L, TimeUnit.MICROSECONDS).get();
        } catch (InterruptedException | ExecutionException e) {
            //e.printStackTrace();
            return null;
        }
    }

    /**
     * This makes a post request to the specified website
     *
     * @param url The website to post to
     * @return The {@link Response} from the webserver
     */
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
    public static Response postRequest(String url, Map<String, Object> postFields, AcceptType accept) {
        StringBuilder postParams = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : postFields.entrySet()) {
            postParams.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        try {
            return service.schedule(() -> {
                try {
                    return client.newCall(new Request.Builder()
                            .url(url)
                            .post(RequestBody.create(MediaType.parse(AcceptType.URLENCODED.getType()), Config.replaceLast(postParams.toString(), "\\&", "")))
                            .addHeader("User-Agent", USER_AGENT)
                            .addHeader("Accept", accept.getType())
                            .addHeader("cache-control", "no-cache")
                            .build()).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }, 0L, TimeUnit.MICROSECONDS).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This makes a post request to the specified website
     *
     * @param url        The website to post to
     * @param postFields the params for the post
     * @return The {@link Response} from the webserver
     */
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
    public static Response postRequest(String url, AcceptType accept) {
        return postRequest(url, new HashMap<>(), accept);
    }

    /**
     * This makes a post request to the specified website
     *
     * @param url The website to post to
     * @return The {@link Response} from the webserver
     */
    public static Response postRequest(String url) {
        return postRequest(url, AcceptType.TEXT_JSON);
    }

    /**
     * This allows for JSON post requests to a website
     * @param url the website to post the json to
     * @param data the JSON data to post
     * @return The {@link Response} from the webserver
     */
    public static Response postJSON(String url, JSONObject data) {
        try {
            return service.schedule(() -> {
                try {
                    return client.newCall(new Request.Builder()
                            .url(url)
                            .post(RequestBody.create(MediaType.parse("application/json"), data.toString()))
                            .addHeader("User-Agent", USER_AGENT)
                            .build()).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }, 0L, TimeUnit.MICROSECONDS).get();
        } catch (InterruptedException | ExecutionException e) {
            //e.printStackTrace();
            return null;
        }
    }

    /**
     * Shortens a URL with the <a href="https://goo.gl" target="">goo.gl</a> api
     *
     * @param url The URL to shorten
     * @return The shortened URL. <code>null</code> if any error occurred
     */
    public static String shortenUrl(String url) {
        try {
            JSONObject jo = new JSONObject();

            jo.put("longUrl", url);

            String returnData = postJSON("https://www.googleapis.com/urlshortener/v1/url?key="
                    + AirUtils.config.getString("apis.googl", "Google api key"), jo).body().string();

            JSONObject returnJSON = new JSONObject(returnData);
            return returnJSON.get("id").toString();

        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This translates a string into a different language
     * @param sourceLang the source language (example: "nl")
     * @param targetLang the target language (example: "en")
     * @param input the user inpur (example: "Dit is een test")
     * @return the output of the api
     * THe examples above will output the following <code>["This is a test","Dit is een test",null,null,1]</code>
     */
    public static JSONArray translate(String sourceLang, String targetLang, String input) {
        try {
            return new JSONArray(
                    getText("https://translate.googleapis.com/translate_a/single?client=gtx&sl=" + sourceLang + "&tl=" + targetLang + "&dt=t&q=" + input)
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
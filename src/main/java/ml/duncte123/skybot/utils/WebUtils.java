/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WebUtils {

    private static String USER_AGENT = "Mozilla/5.0 dunctebot (SkyBot v" + Settings.version + ", https://bot.duncte123.me/)";
    //private static final OkHttpClient client;
    private static final ThreadLocal<OkHttpClient> client = ThreadLocal.withInitial(
            () -> (new OkHttpClient.Builder())
                    .connectTimeout(10, TimeUnit.SECONDS)// connect timeout
                    .readTimeout(10, TimeUnit.SECONDS)// socket timeout
                    .build()
    );

    /**
     * Reads contents from a website and returns it to a string
     *
     * @param url The url to read
     * @return The text contents
     * @throws IOException When something broke
     */
    public static String getText(String url) throws IOException {
        return getRequest(url).body().source().readUtf8();
    }

    /**
     * This makes a get request to the specified website
     *
     * @param url    The website to post to
     * @param accept What we will accept, {@link AcceptType AcceptType}
     * @return The {@link Response} from the webserver
     */
    public static Response getRequest(String url, AcceptType accept) {
        
        Request request = new Request.Builder()
                                  .url(url)
                                  .get()
                                  .addHeader("User-Agent", USER_AGENT)
                                  .addHeader("Accept", accept.getType())
                                  .addHeader("cache-control", "no-cache")
                                  .build();
        
        try {
            return client.get().newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
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
        MediaType mediaType = MediaType.parse(AcceptType.URLENCODED.getType());
        
        StringBuilder postParams = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : postFields.entrySet()) {
            postParams.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        
        RequestBody body = RequestBody.create(mediaType, postParams.toString() + "dummy=param");
        
        Request request = new Request.Builder()
                                  .url(url)
                                  .post(body)
                                  .addHeader("User-Agent", USER_AGENT)
                                  .addHeader("Accept", accept.getType())
                                  .addHeader("cache-control", "no-cache")
                                  .build();
        
        try {
            return client.get().newCall(request).execute();
        } catch (IOException e) {
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
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), data.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("User-Agent", USER_AGENT)
                .build();

        try {
            return client.get().newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
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
                    + AirUtils.config.getString("apis.googl", "Google api key"), jo).body().source().readUtf8();

            JSONObject returnJSON = new JSONObject(returnData);
            return returnJSON.get("id").toString();

        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
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
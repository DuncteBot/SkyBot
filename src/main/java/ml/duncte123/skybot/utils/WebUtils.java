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

import com.github.natanbc.reliqua.Reliqua;
import com.github.natanbc.reliqua.request.PendingRequest;
import com.github.natanbc.reliqua.util.RequestMapper;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.config.Config;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class WebUtils extends Reliqua {

    public static final WebUtils ins = new WebUtils();
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; SkyBot/" + Settings.VERSION + "; +https://bot.duncte123.me;)";

    private WebUtils() {
        super(null, new OkHttpClient(), true);
    }

    public PendingRequest<String> getText(String url) throws NullPointerException {
        return prepareGet(url, ResponseBody::string);
    }

    public PendingRequest<JSONObject> getJSONObject(String url) throws NullPointerException {
        return prepareGet(url, EncodingType.APPLICATION_JSON, r -> new JSONObject(r.string()));
    }

    public PendingRequest<JSONArray> getJSONArray(String url) throws NullPointerException {
        return prepareGet(url, EncodingType.APPLICATION_JSON, (r) -> new JSONArray(r.string()));
    }

    public PendingRequest<InputStream> getInputStream(String url) throws NullPointerException {
        return prepareGet(url, ResponseBody::byteStream);
    }

    public <T> PendingRequest<T> prepareGet(String url, EncodingType accept, RequestMapper<T> mapper) {
        return createRequest(
                url,
                new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("User-Agent", USER_AGENT)
                        .addHeader("Accept", accept.getType()),
                200,
                mapper
        );
    }

    public <T> PendingRequest<T> prepareGet(String url, RequestMapper<T> mapper) {
        return prepareGet(url, EncodingType.TEXT_HTML, mapper);
    }

    public PendingRequest<String> preparePost(String url, Map<String, Object> postFields) {
        return preparePost(url, postFields, EncodingType.APPLICATION_URLENCODED, ResponseBody::string);
    }

    public PendingRequest<String> preparePost(String url, Map<String, Object> postFields, EncodingType type) {
        return preparePost(url, postFields, type, ResponseBody::string);
    }

    public PendingRequest<String> preparePost(String url, EncodingType accept) {
        return preparePost(url, new HashMap<>(), accept, ResponseBody::string);
    }

    public PendingRequest<String> preparePost(String url) {
        return preparePost(url, new HashMap<>(), EncodingType.APPLICATION_URLENCODED, ResponseBody::string);
    }

    public <T> PendingRequest<T> preparePost(String url, Map<String, Object> postFields, EncodingType accept, RequestMapper<T> mapper) {
        StringBuilder postParams = new StringBuilder();

        for (Map.Entry<String, Object> entry : postFields.entrySet()) {
            postParams.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        return createRequest(
                url,
                new Request.Builder()
                        .url(url)
                        .post(RequestBody.create(EncodingType.APPLICATION_URLENCODED.toMediaType(),
                                Config.replaceLast(postParams.toString(), "\\&", "")))
                        .addHeader("User-Agent", USER_AGENT)
                        .addHeader("Accept", accept.getType())
                        .addHeader("cache-control", "no-cache"),
                200,
                mapper
        );
    }

    public PendingRequest<String> postJSON(String url, JSONObject data) {
        return postJSON(url, data, ResponseBody::string);
    }

    public <T> PendingRequest<T> postJSON(String url, JSONObject data, RequestMapper<T> mapper) {
        return createRequest(
                url,
                new Request.Builder()
                        .url(url)
                        .post(RequestBody.create(EncodingType.APPLICATION_JSON.toMediaType(), data.toString()))
                        .addHeader("User-Agent", USER_AGENT),
                200,
                mapper
        );
    }

    public JSONArray translate(String sourceLang, String targetLang, String input) throws NullPointerException {
        return getJSONArray(
                "https://translate.googleapis.com/translate_a/single?client=gtx&sl=" + sourceLang + "&tl=" + targetLang + "&dt=t&q=" + input
        ).execute().getJSONArray(0).getJSONArray(0);
    }

    public PendingRequest<String> shortenUrl(String url) throws NullPointerException {
        return postJSON(
                "https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" +
                        AirUtils.CONFIG.getString("apis.googl", "Google api key"),
                new JSONObject()
                        .put("dynamicLinkInfo", new JSONObject()
                                .put("dynamicLinkDomain", "g57v2.app.goo.gl").put("link", url))
                        .put("suffix", new JSONObject("{\"option\": \"UNGUESSABLE\"}"))
                ,
                (r) -> new JSONObject(r.string()).getString("shortLink"));
    }

    public <T> PendingRequest<T> prepareRaw(Request request, RequestMapper<T> mapper) {
        return createRequest(request.url().toString(), request, 200, mapper);
    }

    private <T> PendingRequest<T> postRawToService(Service s, String raw, RequestMapper<T> mapper) {
        return createRequest(
                s.url,
                new Request.Builder()
                        .post(RequestBody.create(EncodingType.TEXT_PLAIN.toMediaType(), raw))
                        .url(s.url + "documents"),
                200,
                mapper
        );
    }

    public PendingRequest<String> leeks(String data) throws NullPointerException {
        Service leeks = Service.LEEKS;
        return postRawToService(leeks, data,
                (r) -> leeks.url + new JSONObject(r.string()).getString("key") + ".kt");
    }

    public PendingRequest<String> hastebin(String data) throws NullPointerException {
        Service hastebin = Service.HASTEBIN;
        return postRawToService(hastebin, data,
                (r) -> hastebin.url + new JSONObject(r.string()).getString("key") + ".kt");
    }

    public PendingRequest<String> wastebin(String data) throws NullPointerException {
        Service wastebin = Service.WASTEBIN;
        return postRawToService(wastebin, data,
                (r) -> wastebin.url + new JSONObject(r.string()).getString("key") + ".kt");
    }

    public enum EncodingType {
        TEXT_PLAIN("text/plain"),
        APPLICATION_JSON("application/json"),
        TEXT_HTML("text/html"),
        APPLICATION_XML("application/xml"),
        APPLICATION_URLENCODED("application/x-www-form-urlencoded");

        private String type;

        EncodingType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public MediaType toMediaType() {
            return MediaType.parse(type);
        }
    }

    public enum Service {
        HASTEBIN("https://hastebin.com/"),
        WASTEBIN("https://wastebin.party/"),
        LEEKS("https://haste.leeks.life/");

        private final String url;

        Service(String u) {
            this.url = u;
        }

        public String getUrl() {
            return url;
        }
    }
}
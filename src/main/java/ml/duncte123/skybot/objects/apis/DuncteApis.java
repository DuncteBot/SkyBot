/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.objects.apis;

import com.github.natanbc.reliqua.request.PendingRequest;
import me.duncte123.botcommons.web.WebUtils;
import me.duncte123.botcommons.web.WebUtilsErrorUtils;
import okhttp3.Request;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.List;

public class DuncteApis {

    private String apiKey;

    /**
     * Sets the api key
     *
     * @param apiKey
     *         the api key
     *
     * @return the current api class, useful for chaining
     */
    public DuncteApis setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * Returns the api key
     *
     * @return The api key
     *
     * @throws IllegalArgumentException
     *         when the api key is null
     */
    public String getApiKey() {

        if (apiKey == null) {
            throw new IllegalArgumentException("Api Key is null");
        }

        return apiKey;
    }

    public PendingRequest<JSONArray> getCustomCommands() {
        return WebUtils.ins.prepareRaw(defaultRequest("customcommands").build(),
            it -> {
                JSONObject json = WebUtilsErrorUtils.toJSONObject(it);

                return json.getJSONArray("data");
            }
        );
    }

    private Request.Builder defaultRequest(String path) {
        return WebUtils.defaultRequest()
//                .url("https://apis.duncte123.me/" + path)
                .url("http://duncte123-apis-lumen.local/" + path)
                .get()
                .addHeader("Authorization", getApiKey());
    }
}

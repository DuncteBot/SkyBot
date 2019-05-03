/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import com.github.natanbc.reliqua.request.RequestException;
import io.sentry.Sentry;
import me.duncte123.botcommons.web.WebUtils;
import me.duncte123.botcommons.web.WebUtilsErrorUtils;
import org.json.JSONObject;

public class PerspectiveApi {

    public static float checkProfanity(String text, String channelId, String apiKey) {
        try {
            final JSONObject json = makeRequest(text, channelId, apiKey);

            if (json.has("error")) {
                throw new Exception("Error while handling perspective api request: " + json);
            }

            final JSONObject score = json.getJSONObject("attributeScores").getJSONObject("SEVERE_TOXICITY")
                .getJSONObject("summaryScore");

            return score.getFloat("value");
        }
        catch (Exception e) {
            Sentry.capture(e);
            e.printStackTrace();

            return 0f;
        }
    }

    private static String genBody(String text, String channelId) {
        return "{\"comment\":{\"text\":\"" + text + "\"},\"requestedAttributes\":{\"SEVERE_TOXICITY\":{}},\"sessionId\":\"" + channelId + "\"}";
    }

    private static String genUrl(String apiKey) {
        return "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey;
    }

    private static JSONObject makeRequest(String text, String channelId, String apiKey) throws RequestException {
        return WebUtils.ins.postJSON(genUrl(apiKey), genBody(text, channelId), WebUtilsErrorUtils::toJSONObject).execute();
    }
}

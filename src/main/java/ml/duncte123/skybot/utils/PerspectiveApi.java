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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.natanbc.reliqua.request.RequestException;
import io.sentry.Sentry;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.core.exceptions.HttpException;

import java.util.Objects;

public class PerspectiveApi {

    public static float checkSevereToxicity(String text, String channelId, String apiKey, ObjectMapper mapper) {
        if (text.isEmpty()) {
            return 0f;
        }

        try {
            final JsonNode json = makeRequest(text, channelId, apiKey, mapper);

            if (json.has("error")) {
                final String error = json.get("error").get("message").asText();

                if (error.contains("does not support request languages")) {
                    return 0f;
                }

                throw new HttpException("Error while handling perspective api request: " + json);
            }

            final JsonNode score = json.get("attributeScores").get("SEVERE_TOXICITY")
                .get("summaryScore");

            return Float.valueOf(score.get("value").asText());
        }
        catch (Exception e) {
            Sentry.capture(e);
            e.printStackTrace();

            return 0f;
        }
    }

    private static String genBody(String text, String channelId) {
        return "{\"comment\":{\"text\":" +
            JSONQuoter.quote(text) +
            "},\"requestedAttributes\":{\"SEVERE_TOXICITY\":{}},\"sessionId\":\"" + channelId + "\"}";
    }

    private static String genUrl(String apiKey) {
        return "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey;
    }

    private static JsonNode makeRequest(String text, String channelId, String apiKey, ObjectMapper mapper) throws RequestException {
        return WebUtils.ins.postJSON(genUrl(apiKey), genBody(text, channelId),
            (it) -> mapper.readTree(Objects.requireNonNull(it.body()).byteStream())
        ).execute();
    }
}

/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import com.dunctebot.models.settings.ProfanityFilterType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.natanbc.reliqua.request.RequestException;
import io.sentry.Sentry;
import me.duncte123.botcommons.web.WebParserUtils;
import me.duncte123.botcommons.web.WebUtils;
import me.duncte123.botcommons.web.requests.JSONRequestBody;
import net.dv8tion.jda.api.exceptions.HttpException;

import java.util.Objects;

public class PerspectiveApi {

    private PerspectiveApi() {}

    public static float checkSwearFilter(String text, String channelId, String apiKey, ProfanityFilterType filterType, ObjectMapper mapper) {
        if (text.isEmpty()) {
            return 0f;
        }

        try {
            final JsonNode json = makeRequest(text, channelId, apiKey, filterType, mapper);

            if (json.has("error")) {
                final String error = json.get("error").get("message").asText();

                if ("Unable to detect language.".equals(error)) {
                    return 0f;
                }

                throw new HttpException("Error while handling perspective api request: " + json);
            }

            final JsonNode score = json.get("attributeScores")
                .get(filterType.getType())
                .get("summaryScore");

            return Float.parseFloat(score.get("value").asText());
        }
        catch (Exception e) {
            Sentry.capture(e);
            e.printStackTrace();

            return 0f;
        }
    }

    private static JSONRequestBody genBody(String text, String channelId, ProfanityFilterType filterType, ObjectMapper mapper) {
        final ObjectNode mainNode = mapper.createObjectNode();
        final ObjectNode commentNode = mapper.createObjectNode()
            .put("text", text);

        final ObjectNode requestedAttrs = mapper.createObjectNode();
        requestedAttrs.set(filterType.getType(), mapper.createObjectNode());

        mainNode.set("comment", commentNode);
        mainNode.set("requestedAttributes", requestedAttrs);
        mainNode.put("sessionId", channelId);

        try {
            return JSONRequestBody.fromJackson(mainNode);
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String genUrl(String apiKey) {
        return "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey;
    }

    private static JsonNode makeRequest(String text, String channelId, String apiKey, ProfanityFilterType filterType, ObjectMapper mapper) throws RequestException {
        return WebUtils.ins.postRequest(genUrl(apiKey), Objects.requireNonNull(genBody(text, channelId, filterType, mapper)))
            .build((it) -> WebParserUtils.toJSONObject(it, mapper), WebParserUtils::handleError)
            .execute();
    }
}

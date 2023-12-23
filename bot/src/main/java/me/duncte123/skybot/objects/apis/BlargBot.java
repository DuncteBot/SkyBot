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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.duncte123.skybot.objects.apis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.natanbc.reliqua.limiter.RateLimiter;
import com.github.natanbc.reliqua.request.PendingRequest;
import me.duncte123.botcommons.web.ContentType;
import me.duncte123.botcommons.web.WebParserUtils;
import me.duncte123.botcommons.web.WebUtils;
import me.duncte123.weebJava.helpers.IOHelper;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.web.WebUtils.defaultRequest;

public class BlargBot {

    private final String token;
    private final ObjectMapper mapper;

    public BlargBot(@Nonnull String token, @Nonnull ObjectMapper mapper) {
        this.token = token;
        this.mapper = mapper;
    }

    public PendingRequest<byte[]> getClint(String imageUrl) {
        return makeRequest("image/clint", mapper.createObjectNode().put("image", imageUrl));
    }

    public PendingRequest<byte[]> getLinus(String imageUrl) {
        return makeRequest("image/linus", mapper.createObjectNode().put("image", imageUrl));
    }

    public PendingRequest<byte[]> getDelete(String text) {
        return makeRequest("image/delete", mapper.createObjectNode().put("text", text));
    }

    public PendingRequest<byte[]> getPcCheck(String text) {
        return makeRequest("image/pccheck", mapper.createObjectNode().put("text", text));
    }

    public PendingRequest<byte[]> getShit(String text) {
        return getShit(text, false);
    }

    public PendingRequest<byte[]> getShit(String text, boolean plural) {
        return makeRequest("image/shit", mapper.createObjectNode().put("text", text).put("plural", plural));
    }

    public PendingRequest<byte[]> getTheSearch(String text) {
        return makeRequest("image/thesearch", mapper.createObjectNode().put("text", text));
    }


    private PendingRequest<byte[]> makeRequest(String path, JsonNode body) {
        return WebUtils.ins.prepareBuilder(
            defaultRequest()
                .url("https://api.blargbot.xyz/api/v1/" + path)
                .header("Content-Type", ContentType.JSON.getType())
                .post(RequestBody.create(jsonToString(body)))
                .addHeader("Authorization", token),
            (it) -> it.setRateLimiter(RateLimiter.directLimiter()),
            null
        ).build(IOHelper::read, WebParserUtils::handleError);
    }

    private byte[] jsonToString(JsonNode body) {
        try {
            return mapper.writeValueAsBytes(body);
        }
        catch (JsonProcessingException ignored) {
            return new byte[0]; // Should never happen
        }
    }
}

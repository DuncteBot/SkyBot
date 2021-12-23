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

package ml.duncte123.skybot.objects.apis.alexflipnote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.natanbc.reliqua.request.PendingRequest;
import me.duncte123.botcommons.web.WebUtils;
import me.duncte123.weebJava.helpers.IOHelper;
import me.duncte123.weebJava.helpers.QueryBuilder;
import okhttp3.Request;

import java.util.Objects;

import static me.duncte123.botcommons.web.WebUtils.defaultRequest;

public class Alexflipnote {

    private final ObjectMapper mapper;
    private final String apiKey;

    public Alexflipnote(ObjectMapper mapper, String apiKey) {
        this.mapper = mapper;
        this.apiKey = apiKey;
    }

    public PendingRequest<FlipnoteColourObj> getColour(String color) {
        return WebUtils.ins.prepareRaw(
            makeRequest("https://api.alexflipnote.dev/", "colour/" + color),
            (r) -> {
                final JsonNode node = mapper.readTree(Objects.requireNonNull(r.body()).byteStream());

                return mapper.readValue(node.traverse(), FlipnoteColourObj.class);
            }
        );
    }

    public PendingRequest<byte[]> getTrash(String face, String trash) {
        final QueryBuilder builder = new QueryBuilder().append("face", face).append("trash", trash);
        return WebUtils.ins.prepareRaw(
            makeRequest("trash" + builder.build()),
            IOHelper::read
        );
    }

    public PendingRequest<byte[]> getAchievement(String text) {
        final QueryBuilder builder = new QueryBuilder().append("text", text);
        return WebUtils.ins.prepareRaw(
            makeRequest("achievement" + builder.build()),
            IOHelper::read
        );
    }

    public PendingRequest<byte[]> getScroll(String text) {
        final QueryBuilder builder = new QueryBuilder().append("text", text);
        return WebUtils.ins.prepareRaw(
            makeRequest("scroll" + builder.build()),
            IOHelper::read
        );
    }

    public PendingRequest<byte[]> getDidYouMean(String input, String correction) {
        final QueryBuilder builder = new QueryBuilder().append("top", input).append("bottom", correction);
        return WebUtils.ins.prepareRaw(
            makeRequest("didyoumean" + builder.build()),
            IOHelper::read
        );
    }

    public PendingRequest<byte[]> getFacts(String text) {
        final QueryBuilder builder = new QueryBuilder().append("text", text);
        return WebUtils.ins.prepareRaw(
            makeRequest("facts" + builder.build()),
            IOHelper::read
        );
    }

    public PendingRequest<byte[]> getCaptcha(String text) {
        final QueryBuilder builder = new QueryBuilder().append("text", text);
        return WebUtils.ins.prepareRaw(
            makeRequest("captcha" + builder.build()),
            IOHelper::read
        );
    }

    public PendingRequest<byte[]> getJokeoverhead(String image) {
        final QueryBuilder builder = new QueryBuilder().append("image", image);
        return WebUtils.ins.prepareRaw(
            makeRequest("jokeoverhead" + builder.build()),
            IOHelper::read
        );
    }

    public PendingRequest<byte[]> getSalty(String image) {
        final QueryBuilder builder = new QueryBuilder().append("image", image);
        return WebUtils.ins.prepareRaw(
            makeRequest("salty" + builder.build()),
            IOHelper::read
        );
    }

    public PendingRequest<byte[]> getPornhub(String text1, String text2) {
        final QueryBuilder builder = new QueryBuilder().append("text", text1).append("text2", text2);
        return WebUtils.ins.prepareRaw(
            makeRequest("pornhub" + builder.build()),
            IOHelper::read
        );
    }

    private Request makeRequest(String path) {
        return makeRequest("https://flipnote.duncte.bot/", path);
    }

    private Request makeRequest(String url, String path) {
        return defaultRequest()
            .url(url + path)
            .addHeader("Authorization", this.apiKey)
            .get()
            .build();
    }
}

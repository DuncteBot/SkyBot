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

package ml.duncte123.skybot.objects.apis.alexflipnote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.natanbc.reliqua.request.PendingRequest;
import me.duncte123.botcommons.web.WebUtils;
import me.duncte123.botcommons.web.WebParserUtils;
import me.duncte123.weebJava.helpers.IOHelper;
import me.duncte123.weebJava.helpers.QueryBuilder;
import ml.duncte123.skybot.Author;
import okhttp3.Request;
import org.json.JSONObject;

import static me.duncte123.botcommons.web.WebUtils.defaultRequest;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class Alexflipnote {

    private final ObjectMapper mapper = new ObjectMapper();


    public PendingRequest<FlipnoteColourObj> getRandomColour() {
        return WebUtils.ins.prepareRaw(
            makeRequest("colour/random"),
            (r) -> {
                final JSONObject jsonObject = WebParserUtils.toJSONObject(r);
                jsonObject.put("integer", jsonObject.getInt("int"));
                return mapper.readValue(jsonObject.toString(), FlipnoteColourObj.class);
            }
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

    private Request makeRequest(String path) {
        return defaultRequest()
            .url("https://api.alexflipnote.dev/" + path)
            .get()
            .build();
    }
}

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

package ml.duncte123.skybot.objects.apis.alexflipnote;

import com.github.natanbc.reliqua.request.PendingRequest;
import com.google.gson.Gson;
import me.duncte123.botcommons.web.WebUtils;
import me.duncte123.botcommons.web.WebUtilsErrorUtils;
import me.duncte123.weebJava.helpers.IOHelper;
import me.duncte123.weebJava.helpers.QueryBuilder;
import ml.duncte123.skybot.Author;
import okhttp3.Request;
import org.json.JSONObject;

import static me.duncte123.botcommons.web.WebUtils.defaultRequest;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class Alexflipnote {

    private final Gson gson = new Gson();


    public PendingRequest<FlipnoteColourObj> getRandomColour() {
        return WebUtils.ins.prepareRaw(
            makeRequest("colour/random"),
            (r) -> {
                JSONObject jsonObject = WebUtilsErrorUtils.toJSONObject(r);
                jsonObject.put("integer", jsonObject.getInt("int"));
                return gson.fromJson(jsonObject.toString(), FlipnoteColourObj.class);
            }
        );
    }

    public PendingRequest<byte[]> getAchievement(String text) {
        QueryBuilder builder = new QueryBuilder().append("text", text);
        return WebUtils.ins.prepareRaw(
            makeRequest("achievement" + builder.build()),
            IOHelper::read
        );
    }

    public PendingRequest<byte[]> getDidYouMean(String input, String correction) {
        QueryBuilder builder = new QueryBuilder().append("top", input).append("bottom", correction);
        return WebUtils.ins.prepareRaw(
            makeRequest("didyoumean" + builder.build()),
            IOHelper::read
        );
    }


    private Request makeRequest(String path) {
        return defaultRequest()
            .url("https://api.alexflipnote.xyz/" + path)
            .get()
            .build();
    }
}

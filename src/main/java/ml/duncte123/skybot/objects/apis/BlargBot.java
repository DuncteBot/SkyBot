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
import me.duncte123.botCommons.web.WebUtils;
import me.duncte123.botCommons.web.WebUtils.EncodingType;
import me.duncte123.weebJava.helpers.IOHelper;
import ml.duncte123.skybot.Author;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static me.duncte123.botCommons.web.WebUtils.defaultRequest;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class BlargBot {

    private final String token;

    public BlargBot(@NotNull String token) {
        this.token = token;
    }

    public PendingRequest<byte[]> getClint(String imageUrl) {
        return makeRequest("image/clint", new JSONObject().put("image", imageUrl));
    }

    public PendingRequest<byte[]> getLinus(String imageUrl) {
        return makeRequest("image/linus", new JSONObject().put("image", imageUrl));
    }

    public PendingRequest<byte[]> getDelete(String text) {
        return makeRequest("image/delete", new JSONObject().put("text", text));
    }

    public PendingRequest<byte[]> getPcCheck(String text) {
        return makeRequest("image/pccheck", new JSONObject().put("text", text));
    }

    public PendingRequest<byte[]> getShit(String text) {
        return getShit(text, false);
    }

    public PendingRequest<byte[]> getShit(String text, boolean plural) {
        return makeRequest("image/shit", new JSONObject().put("text", text).put("plural", plural));
    }

    public PendingRequest<byte[]> getTheSearch(String text) {
        return makeRequest("image/thesearch", new JSONObject().put("text", text));
    }


    private PendingRequest<byte[]> makeRequest(String path, JSONObject body) {
        return WebUtils.ins.prepareRaw(
                defaultRequest()
                        .url("https://api.blargbot.xyz/api/v1/" + path)
                        .post(RequestBody.create(EncodingType.APPLICATION_JSON.toMediaType(), body.toString()))
                        .addHeader("Authorization", token)
                        .build(),
                IOHelper::read
        );
    }

}

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

import com.github.natanbc.reliqua.request.PendingRequest;
import me.duncte123.botcommons.web.WebUtils;
import me.duncte123.botcommons.web.WebUtilsErrorUtils;
import org.json.JSONObject;

import java.util.function.Consumer;

public class PerspectiveApi {

    public static void checkProfanity(String text, String apiKey, Consumer<Long> callback) {

        //

    }

    private static String genBody(String text) {
        return "{\"comment\":{\"text\":\"" + text + "\"},\"requestedAttributes\":{\"SEVERE_TOXICITY\":{}}}";
    }

    private static String genUrl(String apiKey) {
        return "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + apiKey;
    }

    private static PendingRequest<JSONObject> makeRequest(String text, String apiKey) {
        return WebUtils.ins.postJSON(genUrl(apiKey), genBody(text), WebUtilsErrorUtils::toJSONObject);
    }
}

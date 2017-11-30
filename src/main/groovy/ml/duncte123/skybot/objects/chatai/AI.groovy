/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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

package ml.duncte123.skybot.objects.chatai

import ml.duncte123.skybot.utils.WebUtils
import okhttp3.Response
import org.json.JSONObject

class AI {

    def base = "https://cleverbot.io/1.0/"
    def userKey
    def apiKey
    def nickname

    /**
     * This sets up the api for us to use
     * @param user the user token provided by <a href="https://cleverbot.io/">https://cleverbot.io/</a>
     * @param api the api token provided by <a href="https://cleverbot.io/">https://cleverbot.io/</a>
     */
    AI(String user, String api) {
        this.userKey = user
        this.apiKey = api
    }

    /**
     * This sets the name of the bot
     * @param nickname the nickname for the bot
     * @return the AI class, useful for chaining
     */
    AI setNick(String nickname) {
        this.nickname = nickname
        return this
    }

    /**
     * This creates the bot
     * @param callback a {@link ml.duncte123.skybot.objects.chatai.Callback Callback} that gives the return data from the api
     * @return the AI class, useful for chaining
     */
    AI create(Callback callback) {
        JSONObject postData = new JSONObject()
            .put("user", this.userKey)
            .put("key", this.apiKey)

        if(this.nickname != null)
            postData.put("nick", this.nickname)

        Response r = WebUtils.postJSON(this.base + "create", postData)

        try {
            JSONObject returnData = new JSONObject(r.body().source().readUtf8())
            this.nickname = returnData["nick"]
            callback.call(returnData)
        }
        catch (IOException | NullPointerException e) {
            e.printStackTrace()
            callback.call(new JSONObject()
                    .put("status", "failure")
                    .put("response", e.getMessage())
            )
        }

        return this
    }

    /**
     * this "asks" a question to the bot
     * @param input the text to send to the bot
     * @param callback a {@link Callback Callback} that gives the return data from the api
     */
    def ask(String input, Callback callback) {
        JSONObject postData = new JSONObject()
            .put("user", this.userKey)
            .put("key", this.apiKey)
            .put("nick", this.nickname)
            .put("text", input)


        Response r = WebUtils.postJSON(this.base + "ask", postData)

        try {
            callback.call(new JSONObject(r.body().source().readUtf8()))
        }
        catch (IOException | NullPointerException e) {
            e.printStackTrace()
            callback.call(new JSONObject()
                    .put("status", "failure")
                    .put("response", e.getMessage())
            )
        }
    }
}

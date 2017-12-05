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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.objects.chatai

import ch.qos.logback.classic.Level
import ml.duncte123.skybot.utils.WebUtils
import okhttp3.Response
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Consumer

class AI {

    def base = "https://cleverbot.io/1.0/"
    def userKey
    def apiKey
    def nickname
    def log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)

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
     * @param callback a {@link java.util.function.Consumer Consumer} that gives the return data from the api
     * @return the AI class, useful for chaining
     */
    AI create(Consumer<JSONObject> callback) {
        Map<String, Object> postData = new HashMap<>()
        postData.put("user", this.userKey)
        postData.put("key", this.apiKey)

        if(this.nickname != null)
            postData.put("nick", this.nickname)

        Response r = WebUtils.postRequest(this.base + "create", postData, WebUtils.AcceptType.TEXT_JSON)

        try {
            JSONObject returnData = new JSONObject(r.body().source().readUtf8())
            this.nickname = returnData["nick"]
            callback.accept(returnData)
        }
        catch (IOException | NullPointerException e) {
            //If the logger is set to debug, print the stacktrace
            if(log.getLevel() == Level.DEBUG)
                e.printStackTrace()

            callback.accept(new JSONObject()
                    .put("status", "failure")
                    .put("response", e.getMessage())
            )
        }

        return this
    }

    /**
     * this "asks" a question to the bot
     * @param input the text to send to the bot
     * @param callback a {@link java.util.function.Consumer Consumer} that gives the return data from the api
     */
    def ask(String input, Consumer<JSONObject> callback) {
        Map<String, Object> postData = new HashMap<>()
        postData.put("user", this.userKey)
        postData.put("key", this.apiKey)
        postData.put("nick", this.nickname)
        postData.put("text", input)


        Response r = WebUtils.postRequest(this.base + "ask", postData, WebUtils.AcceptType.TEXT_JSON)

        try {
            callback.accept(new JSONObject(r.body().source().readUtf8()))
        }
        catch (IOException | NullPointerException e) {
            //If the logger is set to debug, print the stacktrace
            if(log.getLevel() == Level.DEBUG)
                e.printStackTrace()

            callback.accept(new JSONObject()
                    .put("status", "failure")
                    .put("response", "Chat is unavailable at this moment in time, please try again later.")
            )
        }
    }
}

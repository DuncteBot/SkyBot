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

package ml.duncte123.skybot.entities.chatai

import ch.qos.logback.classic.Level
import ml.duncte123.skybot.utils.WebUtils
import okhttp3.Response
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.function.Consumer

/**
 * This sets up the api for us to use
 * @param user the user token provided by <a href="https://cleverbot.io/">https://cleverbot.io/</a>
 * @param api the api token provided by <a href="https://cleverbot.io/">https://cleverbot.io/</a>
 */
class AI(val user: String, val api: String) {

    private val base = "https://cleverbot.io/1.0/"
    var nickname: String? = null
    private val log = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger

    /**
     * This sets the name of the bot
     * @param nick the nickname for the bot
     * @return the AI class, useful for chaining
     */
    fun setNick(nick: String): AI {
        this.nickname = nick
        return this
    }

    /**
     * This creates the bot
     * @param callback a {@link java.util.function.Consumer Consumer} that gives the return data from the api
     * @return the AI class, useful for chaining
     */
    fun create(callback: Consumer<JSONObject>): AI {

        var postData: Map<String, Any?> = HashMap()
        postData += Pair("user", user)
        postData += Pair("key", api)

        if(nickname != null)
            postData += Pair("nick", nickname)

        val r: Response = WebUtils.postRequest(this.base + "create", postData, WebUtils.AcceptType.TEXT_JSON)

        try {
            val returnData = JSONObject(r.body()?.source()?.readUtf8())
            nickname = returnData["nick"] as String?
            callback.accept(returnData)
        }
        catch (e: IOException) {
            //If the logger is set to debug, print the stacktrace
            if(log.level == Level.DEBUG)
                e.printStackTrace()
        }
        catch (e: NullPointerException) {
            //If the logger is set to debug, print the stacktrace
            if(log.level == Level.DEBUG)
                e.printStackTrace()

            callback.accept(JSONObject()
                    .put("status", "failure")
                    .put("response", e.message)
            )
        }

        return this
    }

    fun ask(question: String, callback: Consumer<JSONObject>) {
        var postData: Map<String, Any?> = HashMap()
        postData += Pair("user", user)
        postData += Pair("key", api)
        postData += Pair("nick", nickname)
        postData += Pair("text", question)


        val r: Response = WebUtils.postRequest(this.base + "ask", postData, WebUtils.AcceptType.TEXT_JSON)

        try {
            callback.accept(JSONObject(r.body()?.source()?.readUtf8()))
        }
        catch (e: IOException) {
            //If the logger is set to debug, print the stacktrace
            if(log.level == Level.DEBUG)
                e.printStackTrace()
        }
        catch (e: NullPointerException) {
            //If the logger is set to debug, print the stacktrace
            if(log.level == Level.DEBUG)
                e.printStackTrace()

            callback.accept(JSONObject()
                    .put("status", "failure")
                    .put("response", e.message)
            )
        }
    }

}
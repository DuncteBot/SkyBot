/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.utils

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import ml.duncte123.skybot.Settings
import ml.duncte123.skybot.config.Config
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.ExecutionException

class WebUtils {

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 dunctebot (SkyBot v" + Settings.version + ", https://bot.duncte123.me/)"
        private val client = OkHttpClient()

        /**
         * Reads contents from a website and returns it to a string
         *
         * @param url The url to read
         * @return The text contents
         * @throws IOException When something broke
         */
        @Throws(IOException::class)
        @JvmStatic
        fun getText(url: String, action: String.() -> Unit) {
            getRequest(url) {
                action.invoke(this!!.body()!!.string())
            }
        }

        /**
         * Reads json data from a web page and returns it
         *
         * @param url The url to read
         * @return The text contents
         * @throws IOException When something broke
         */
        @Throws(IOException::class)
        @JvmStatic
        fun getJSONObject(url: String, action: JSONObject.() -> Unit) {
            getText(url) {
                action.invoke(JSONObject(this))
            }
        }

        /**
         * Reads json data from a web page and returns it
         *
         * @param url The url to read
         * @return The text contents
         * @throws IOException When something broke
         */
        @Throws(IOException::class)
        @JvmStatic
        fun getJSONArray(url: String, action: JSONArray.() -> Unit) {
            getText(url) {
                action.invoke(JSONArray(this))
            }
        }

        /**
         * Reads the contents of a url into an InputStream
         * @param url the url to read
         * @return the InputStream of the url
         * @throws IOException when things break
         */
        @Throws(IOException::class)
        @JvmStatic
        fun getInputStream(url: String, action: InputStream.() -> Unit) {
            getRequest(url) {
                action.invoke(this?.body()?.byteStream()!!)
            }
        }

        /**
         * This makes a get request to the specified website
         *
         * @param url    The website to post to
         * @param accept What we will accept, [AcceptType]
         * @return The [Response] from the webserver
         */
        @JvmStatic
        fun getRequest(url: String, accept: AcceptType = AcceptType.TEXT_HTML, action: Response?.() -> Unit) {
            runBlocking {
                action.invoke(executeRequest(
                        Request.Builder()
                                .url(url)
                                .get()
                                .addHeader("User-Agent", USER_AGENT)
                                .addHeader("Accept", accept.type)
                                .addHeader("cache-control", "no-cache")
                                .build()))
            }
        }

        /**
         * This makes a post request to the specified website
         *
         * @param url        The website to post to
         * @param postFields the params for the post (param name, param value)
         * @param accept     What we will accept, [AcceptType]
         * @return The [Response] from the webserver
         */
        @JvmStatic
        fun postRequest(url: String, postFields: Map<String, Any?>, accept: AcceptType = AcceptType.URLENCODED, action: Response?.() -> Unit) {
            val postParams = StringBuilder()

            for ((key, value) in postFields) {
                postParams.append(key).append("=").append(value).append("&")
            }
            runBlocking {
                action.invoke(executeRequest(
                        Request.Builder()
                                .url(url)
                                .post(RequestBody.create(MediaType.parse(AcceptType.URLENCODED.type), Config.replaceLast(postParams.toString(), "\\&", "")))
                                .addHeader("User-Agent", USER_AGENT)
                                .addHeader("Accept", accept.type)
                                .addHeader("cache-control", "no-cache")
                                .build()))
            }
        }

        /**
         * This makes a post request to the specified website
         *
         * @param url    The website to post to
         * @param accept What we will accept, [AcceptType]
         * @return The [Response] from the webserver
         */
        @JvmStatic
        fun postRequest(url: String, accept: AcceptType = AcceptType.TEXT_JSON, action: Response?.() -> Unit) {
            return postRequest(url, HashMap(), accept, action)
        }

        /**
         * This allows for JSON post requests to a website
         * @param url the website to post the json to
         * @param data the JSON data to post
         * @return The [Response] from the webserver
         */
        @JvmStatic
        fun postJSON(url: String, data: JSONObject, action: Response?.() -> Unit) {
            runBlocking {
                action.invoke(executeRequest(
                        Request.Builder()
                                .url(url)
                                .post(RequestBody.create(MediaType.parse("application/json"), data.toString()))
                                .addHeader("User-Agent", USER_AGENT)
                                .build()))
            }
        }

        /**
         * Shortens a URL with the [goo.gl](https://goo.gl) api
         *
         * @param url The URL to shorten
         * @return The shortened URL. `null` if any error occurred
         */
        @JvmStatic
        fun shortenUrl(url: String, action: String?.() -> Unit) {
            try {
                val jo = JSONObject()

                jo.put("longUrl", url)

                postJSON("https://www.googleapis.com/urlshortener/v1/url?key=" + AirUtils.config.getString("apis.googl", "Google api key"), jo) {
                    action.invoke(JSONObject(this!!.body()!!.string()).get("id").toString())
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        /**
         * This translates a string into a different language
         * @param sourceLang the source language (example: "nl")
         * @param targetLang the target language (example: "en")
         * @param input the user inpur (example: "Dit is een test")
         * @return the output of the api
         * THe examples above will output the following `["This is a test","Dit is een test",null,null,1]`
         */
        @JvmStatic
        fun translate(sourceLang: String, targetLang: String, input: String): JSONArray {
            var json: JSONArray? = null
            getJSONArray("https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sourceLang&tl=$targetLang&dt=t&q=$input") {
                json = this.getJSONArray(0).getJSONArray(0)
            }
            return json!!
        }

        /**
         * Executes a web request
         * @param request the {@link Request Request} to execute
         * @returns the [Response] from the web server
         */
        @JvmStatic
        suspend fun executeRequest(request: Request): Response? {
            return try {
                async { client.newCall(request).execute() }.await()
            } catch (e: InterruptedException) {
                //e.printStackTrace();
                null
            } catch (e: ExecutionException) {
                null
            }

        }

        @JvmStatic
        fun execCustomRequest(request: Request, action: Response?.() -> Unit) {
            runBlocking {
                action.invoke(executeRequest(request))
            }
        }
    }
    /**
     * This holds some variables that we will accept
     */
    enum class AcceptType(val type: String) {
        TEXT_PLAIN("text/plain"),
        TEXT_JSON("application/json"),
        TEXT_HTML("text/html"),
        TEXT_XML("application/xml"),
        URLENCODED("application/x-www-form-urlencoded")
    }
}
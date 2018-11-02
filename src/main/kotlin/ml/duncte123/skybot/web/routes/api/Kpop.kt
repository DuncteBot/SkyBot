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

package ml.duncte123.skybot.web.routes.api

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.utils.ApiUtils
import ml.duncte123.skybot.web.WebHolder
import org.json.JSONObject
import spark.Spark.*
import spark.kotlin.*
import java.sql.SQLException

@Author(nickname = "duncte123", author = "Duncan Sterken")
class Kpop(private val holder: WebHolder) {

    init {
        path("/api") {
            get("/kpop") {
                val search = request.queryParamOrDefault("search", "")
                try {
                    return@get ApiUtils.getRandomKpopMember(holder.database, search).toJson()
                        .put("status", "success")
                        .put("code", response.status())
                } catch (e: SQLException) {
                    response.status(404)
                    return@get JSONObject()
                        .put("status", "faiure")
                        .put("message", "Nothing found")
                        .put("code", response.status())
                }
            }
        }
    }
}

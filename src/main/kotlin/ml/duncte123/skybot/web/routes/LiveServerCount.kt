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

package ml.duncte123.skybot.web.routes

import ml.duncte123.skybot.Author
import ml.duncte123.skybot.web.WebHolder
import spark.ModelAndView
import spark.kotlin.get

@Author(nickname = "duncte123", author = "Duncan Sterken")
class LiveServerCount(private val holder: WebHolder) {

    init {
        get("/liveServerCount") {
            holder.engine.render(ModelAndView(mapOf("nothing" to "something"),
                "static/liveServerCount.twig"))
        }
    }

}

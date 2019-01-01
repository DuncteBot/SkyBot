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

package ml.duncte123.skybot.web.controllers

import com.jagrosh.jdautilities.oauth2.OAuth2Client
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.web.WebRouter
import spark.Request
import spark.Response

@Author(nickname = "duncte123", author = "Duncan Sterken")
object Callback {

    fun handle(request: Request, response: Response, oAuth2Client: OAuth2Client): Any {

        if (!request.queryParams().contains("code")) {
            return response.redirect("/")
        }

        val sesid: String = request.session().attribute(WebRouter.SESSION_ID)

        val oauthses = oAuth2Client.startSession(
            request.queryParams("code"),
            request.queryParams("state"),
            sesid
        ).complete()

        val userId = oAuth2Client.getUser(oauthses).complete().id

        request.session(true).attribute(WebRouter.USER_SESSION, "$sesid${WebRouter.SPLITTER}$userId")

        return response.redirect("/dashboard")
    }

}

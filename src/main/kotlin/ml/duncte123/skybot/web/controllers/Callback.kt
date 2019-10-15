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
import com.jagrosh.jdautilities.oauth2.Scope
import com.jagrosh.jdautilities.oauth2.exceptions.InvalidStateException
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.web.WebRouter
import spark.Request
import spark.Response

@Author(nickname = "duncte123", author = "Duncan Sterken")
object Callback {

    fun handle(request: Request, response: Response, oAuth2Client: OAuth2Client): Any {

        if (!request.queryParams().contains("code") || !request.queryParams().contains("state")) {
            return response.redirect(WebRouter.HOMEPAGE)
        }

        return try {
            val session = request.session()

            if (session.attribute<String?>(WebRouter.SESSION_ID) == null){
                return response.redirect(WebRouter.HOMEPAGE)
            }

            val sesid: String? = request.session().attribute(WebRouter.SESSION_ID)
            val oauthses = oAuth2Client.startSession(
                request.queryParams("code"),
                request.queryParams("state"),
                sesid,
                Scope.IDENTIFY, Scope.GUILDS
            ).complete()

            val userId = oAuth2Client.getUser(oauthses).complete().id

            session.attribute(WebRouter.USER_ID, userId)

            if (session.attributes().contains(WebRouter.OLD_PAGE)) {
                return response.redirect(session.attribute(WebRouter.OLD_PAGE))
            }

            response.redirect("/")
        } catch (stateEx: InvalidStateException) {
            "<h1>${stateEx.message}</h1><br /><a href=\"${WebRouter.HOMEPAGE}\">Click here to go back home</a>"
        }
    }

}

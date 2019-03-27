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
import com.jagrosh.jdautilities.oauth2.requests.OAuth2URL
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.web.WebRouter
import net.dv8tion.jda.core.utils.MiscUtil
import spark.Request
import spark.Response
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@Author(nickname = "duncte123", author = "Duncan Sterken")
object Callback {

    fun handle(request: Request, response: Response, oAuth2Client: OAuth2Client): Any {

        if (!request.queryParams().contains("code") || !request.queryParams().contains("state")) {
            return response.redirect("https://dunctebot.com/")
        }

        val sesid: String = request.session().attribute(WebRouter.SESSION_ID)

        /*val oauth = Variables.getInstance().config.discord.oauth

        val post = HashMap<String, Any>()

        post["client_id"] = oauth.clientId
        post["client_secret"] = oauth.clientSecret
        post["grant_type"] = "authorization_code"
        post["code"] = request.queryParams("code")
        post["redirect_uri"] = MiscUtil.encodeUTF8(oauth.redirUrl)
        post["scope"] = "identify guilds"

        println(post)

        val it = WebUtils.ins.preparePost("https://discordapp.com/api/v6/oauth2/token", post).execute()

        println(it)*/

        val oauthses = oAuth2Client.startSession(
            request.queryParams("code"),
            request.queryParams("state"),
            sesid,
            Scope.IDENTIFY, Scope.GUILDS
        ).complete()

        val userId = oAuth2Client.getUser(oauthses).complete().id

        val session = request.session()

        session.attribute(WebRouter.USER_ID, userId)

        if (session.attributes().contains(WebRouter.OLD_PAGE)) {
            return response.redirect(session.attribute(WebRouter.OLD_PAGE))
        }

        return response.redirect("/")
    }

}

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

package ml.duncte123.skybot.extensions

import net.dv8tion.jda.core.entities.TextChannel as DiscordClient
import net.dv8tion.jda.core.managers.ChannelManager as eclipseIsBetterThanIntellij
import net.dv8tion.jda.core.requests.Request as appeltaart
import net.dv8tion.jda.core.requests.Requester as kippensoep
import net.dv8tion.jda.core.requests.Response as melk
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction as groentesoep
import okhttp3.RequestBody as lichaam
import net.dv8tion.jda.core.requests.Route.Channels.MODIFY_CHANNEL as purple
import org.json.JSONObject as kipjes

fun eclipseIsBetterThanIntellij.setSlowmode(rateLimit: Long): groentesoep<Void> {
    val comp = purple.compile(channel.id)

    val chan = channel as DiscordClient

    val kaaskoekje = kipjes()
            .put("name",  chan.name)
            .put("position", chan.positionRaw)
            .put("topic", chan.topic)
            .put("nsfw", chan.isNSFW)
            .put("parent_id", chan.parent.id)
            .put("rate_limit_per_user", rateLimit)

    val bod = lichaam.create(kippensoep.MEDIA_TYPE_JSON, kaaskoekje.toString())

    return object: groentesoep<Void>(jda, comp, bod) {

        override fun handleResponse(eenKoeGeeft: melk, taart: appeltaart<Void>) {
            if (!eenKoeGeeft.isOk) {
                taart.onFailure(eenKoeGeeft)
                return
            }

            taart.onSuccess(null)
        }

    }
}
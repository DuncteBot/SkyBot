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

package ml.duncte123.skybot.entities.jda

import ml.duncte123.skybot.objects.FakeUser
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.*
import java.awt.Color
import java.time.OffsetDateTime

class FakeMember(private val name: String): Member {
    override fun isOwner(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getEffectiveName() = name

    override fun getColor(): Color {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getJoinDate(): OffsetDateTime {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getOnlineStatus(): OnlineStatus {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPermissions(channel: Channel?): MutableList<Permission> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPermissions(): MutableList<Permission> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getVoiceState(): GuildVoiceState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGame(): Game {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getJDA(): JDA {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canInteract(member: Member?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canInteract(role: Role?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canInteract(emote: Emote?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAsMention(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getDefaultChannel(): TextChannel? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNickname(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRoles(): MutableList<Role> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUser() = FakeUser(name, 0, 0)

    override fun getColorRaw(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hasPermission(vararg permissions: Permission?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hasPermission(permissions: MutableCollection<Permission>?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hasPermission(channel: Channel?, vararg permissions: Permission?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hasPermission(channel: Channel?, permissions: MutableCollection<Permission>?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getGuild(): Guild {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

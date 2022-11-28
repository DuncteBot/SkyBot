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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.entities.jda

import ml.duncte123.skybot.objects.user.FakeUser
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.*
import net.dv8tion.jda.api.entities.channel.attribute.*
import net.dv8tion.jda.api.entities.channel.middleman.*
import net.dv8tion.jda.api.entities.channel.concrete.*
import java.awt.Color
import java.time.OffsetDateTime
import java.util.*

class FakeMember(private val name: String) : Member {
    private val internalUser = FakeUser(name, 0, 0)

    override fun getEffectiveName() = name

    override fun getUser() = internalUser

    override fun getAvatarId() = internalUser.avatarId

    override fun canInteract(member: Member): Boolean {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun canInteract(role: Role): Boolean {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun canInteract(emote: Emote): Boolean {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getTimeJoined(): OffsetDateTime {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getActivities(): MutableList<Activity> {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getIdLong(): Long {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getTimeBoosted(): OffsetDateTime? {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun isOwner(): Boolean {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getColor(): Color {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getOnlineStatus(): OnlineStatus {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getVoiceState(): GuildVoiceState {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getJDA(): JDA {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getAsMention(): String {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getDefaultChannel(): TextChannel? {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getNickname(): String {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getRoles(): MutableList<Role> {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getColorRaw(): Int {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun hasPermission(vararg permissions: Permission?): Boolean {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getGuild(): Guild {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getOnlineStatus(type: ClientType): OnlineStatus {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun hasPermission(permissions: MutableCollection<Permission>): Boolean {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun hasPermission(channel: GuildChannel, vararg permissions: Permission?): Boolean {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun hasPermission(channel: GuildChannel, permissions: MutableCollection<Permission>): Boolean {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getPermissions(): EnumSet<Permission> {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getPermissions(channel: GuildChannel): EnumSet<Permission> {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getPermissionsExplicit(): EnumSet<Permission> {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getPermissionsExplicit(channel: GuildChannel): EnumSet<Permission> {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun getActiveClients(): EnumSet<ClientType> {
        throw NotImplementedError("An operation is not implemented: not implemented")
    }

    override fun hasTimeJoined() = false

    override fun canSync(targetChannel: GuildChannel, syncSource: GuildChannel) = false

    override fun canSync(channel: GuildChannel) = false

    override fun isPending() = false
}

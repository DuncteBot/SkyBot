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

package ml.duncte123.skybot.extensions

import com.dunctebot.models.settings.GuildSetting
import com.dunctebot.models.utils.Utils.ratelimmitChecks
import com.dunctebot.models.utils.Utils.toLong
import ml.duncte123.skybot.objects.api.Reminder
import ml.duncte123.skybot.utils.AirUtils
import java.sql.ResultSet
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.TemporalAccessor

fun TemporalAccessor.toSQL() = java.sql.Date(Instant.from(this).toEpochMilli())
fun java.sql.Date.asInstant() = OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.time), ZoneOffset.UTC)
fun String.toDate() = AirUtils.fromDatabaseFormat(this).toSQL()

fun ResultSet.toReminder() = Reminder(
    this.getInt("id"),
    this.getLong("user_id"),
    this.getString("reminder"),
    this.getDate("remind_create_date").asInstant(),
    this.getDate("remind_date").asInstant(),
    this.getLong("channel_id"),
    this.getLong("message_id"),
    this.getLong("guild_id"),
    this.getBoolean("in_channel")
)

fun ResultSet.toGuildSetting() = GuildSetting(this.getLong("guild_id"))
    .setEnableJoinMessage(this.getBoolean("enableJoinMessage"))
    .setEnableSwearFilter(this.getBoolean("enableSwearFilter"))
    .setCustomJoinMessage(this.getString("customWelcomeMessage"))
    .setCustomPrefix(this.getString("prefix"))
    .setLogChannel(toLong(this.getString("logChannelId")))
    .setWelcomeLeaveChannel(toLong(this.getString("welcomeLeaveChannel")))
    .setCustomLeaveMessage(this.getString("customLeaveMessage"))
    .setAutoroleRole(toLong(this.getString("autoRole")))
    .setServerDesc(this.getString("serverDesc"))
    .setAnnounceTracks(this.getBoolean("announceNextTrack"))
    .setAutoDeHoist(this.getBoolean("autoDeHoist"))
    .setFilterInvites(this.getBoolean("filterInvites"))
    .setEnableSpamFilter(this.getBoolean("spamFilterState"))
    .setMuteRoleId(toLong(this.getString("muteRoleId")))
    .setRatelimits(ratelimmitChecks(this.getString("ratelimits")))
    .setKickState(this.getBoolean("kickInsteadState"))
    .setLeaveTimeout(this.getInt("leave_timeout"))
    .setSpamThreshold(this.getInt("spam_threshold"))
    .setBanLogging(this.getBoolean("logBan"))
    .setUnbanLogging(this.getBoolean("logUnban"))
    .setKickLogging(this.getBoolean("logKick"))
    .setMuteLogging(this.getBoolean("logMute"))
    .setWarnLogging(this.getBoolean("logWarn"))
    .setFilterType(this.getString("profanity_type"))
    .setAiSensitivity(this.getFloat("aiSensitivity"))
    .setAllowAllToStop(this.getBoolean("allow_all_to_stop"))
    .setFilterInvites(this.getBoolean("invite_logging"))
//    .setBlacklistedWords(getBlackListsForGuild(guildId))
//    .setWarnActions(getWarnActionsForGuild(guildId))
    .setEmbedColor(this.getInt("embed_color"))
    .setYoungAccountBanEnabled(this.getBoolean("youngAccountBanEnabled"))
    .setYoungAccountThreshold(this.getInt("youngAccountThreshold"))
    .setMemberLogging(this.getBoolean("logMember"))
    .setMessageLogging(this.getBoolean("message_logging"))

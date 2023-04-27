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
import com.dunctebot.models.utils.DateUtils
import com.dunctebot.models.utils.DateUtils.DB_ZONE_ID
import com.dunctebot.models.utils.DateUtils.getSqlTimestamp
import com.dunctebot.models.utils.Utils.ratelimmitChecks
import com.dunctebot.models.utils.Utils.toLong
import ml.duncte123.skybot.objects.api.Reminder
import java.sql.ResultSet
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun OffsetDateTime.toSQL() = getSqlTimestamp(this)
// TODO: still an hour in the past?
fun java.sql.Timestamp.asInstant() = OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.time), DB_ZONE_ID)
fun String.toDate() = DateUtils.fromMysqlFormat(this).toSQL()
fun String.toJavaDate() = DateUtils.fromMysqlFormat(this)

fun ResultSet.toReminder() = Reminder(
    this.getInt("id"),
    this.getLong("user_id"),
    this.getString("reminder"),
    this.getTimestamp("created_at").asInstant(),
    this.getTimestamp("remind_on").asInstant(),
    this.getLong("channel_id"),
    this.getLong("message_id"),
    this.getLong("guild_id"),
    this.getBoolean("in_channel")
)

fun ResultSet.toReminderMySQL() = Reminder(
    this.getInt("id"),
    this.getString("user_id").toLong(),
    this.getString("reminder"),
    this.getString("remind_create_date").toJavaDate(),
    this.getString("remind_date").toJavaDate(),
    this.getString("channel_id").toLong(),
    this.getString("message_id").toLong(),
    this.getString("guild_id").toLong(),
    this.getBoolean("in_channel")
)

fun ResultSet.toGuildSettingMySQL() = GuildSetting(this.getString("guildId").toLong())
    .setCustomPrefix(this.getString("prefix"))
    .setAutoroleRole(toLong(this.getString("autoRole")))
    // .setEmbedColor(this.getInt("embed_color")) // NOTE: this is in a different table
    .setLeaveTimeout(this.getInt("leave_timeout"))
    .setAnnounceTracks(this.getBoolean("announceNextTrack"))
    .setAllowAllToStop(this.getBoolean("allowAllToStop"))
    .setServerDesc(this.getString("serverDesc"))
    // Join/leave
    .setWelcomeLeaveChannel(toLong(this.getString("welcomeLeaveChannel")))
    .setEnableJoinMessage(this.getBoolean("enableJoinMessage"))
    .setEnableLeaveMessage(this.getBoolean("enableLeaveMessage"))
    .setCustomJoinMessage(this.getString("customWelcomeMessage"))
    .setCustomLeaveMessage(this.getString("customLeaveMessage"))
    // moderation
    .setLogChannel(toLong(this.getString("logChannelId")))
    .setMuteRoleId(toLong(this.getString("muteRoleId")))
    .setEnableSwearFilter(this.getBoolean("enableSwearFilter"))
    .setFilterType(this.getString("filterType"))
    .setAiSensitivity(this.getFloat("aiSensitivity"))
    .setAutoDeHoist(this.getBoolean("autoDeHoist"))
    .setFilterInvites(this.getBoolean("filterInvites"))
    .setEnableSpamFilter(this.getBoolean("spamFilterState"))
    .setKickState(this.getBoolean("kickInsteadState"))
    .setRatelimits(ratelimmitChecks(this.getString("ratelimits")))
    .setSpamThreshold(this.getInt("spam_threshold"))
    .setYoungAccountBanEnabled(this.getBoolean("young_account_ban_enabled"))
    .setYoungAccountThreshold(this.getInt("young_account_threshold"))
    // logging
    .setBanLogging(this.getBoolean("banLogging"))
    .setUnbanLogging(this.getBoolean("unbanLogging"))
    .setMuteLogging(this.getBoolean("muteLogging"))
    .setKickLogging(this.getBoolean("kickLogging"))
    .setWarnLogging(this.getBoolean("warnLogging"))
    .setMemberLogging(this.getBoolean("memberLogging"))
    .setInviteLogging(this.getBoolean("invite_logging"))
    .setMessageLogging(this.getBoolean("message_logging"))

fun ResultSet.toGuildSetting() = GuildSetting(this.getLong("guild_id"))
    .setCustomPrefix(this.getString("prefix"))
    .setAutoroleRole(toLong(this.getString("auto_role_id")))
    .setEmbedColor(this.getInt("embed_color"))
    .setLeaveTimeout(this.getInt("voice_leave_timeout_seconds"))
    .setAnnounceTracks(this.getBoolean("announce_track_enabled"))
    .setAllowAllToStop(this.getBoolean("allow_all_to_stop"))
    .setServerDesc(this.getString("server_description"))
    // Join/leave
    .setWelcomeLeaveChannel(toLong(this.getString("join_leave_channel_id")))
    .setEnableJoinMessage(this.getBoolean("join_message_enabled"))
    .setEnableLeaveMessage(this.getBoolean("leave_message_enabled"))
    .setCustomJoinMessage(this.getString("join_message"))
    .setCustomLeaveMessage(this.getString("leave_message"))
    // moderation
    .setLogChannel(toLong(this.getString("log_channel_id")))
    .setMuteRoleId(toLong(this.getString("mute_role_id")))
    .setEnableSwearFilter(this.getBoolean("swear_filter_enabled"))
    .setFilterType(this.getString("swear_filter_type"))
    .setAiSensitivity(this.getFloat("swear_sensitivity"))
    .setAutoDeHoist(this.getBoolean("auto_dehoist_enabled"))
    .setFilterInvites(this.getBoolean("invite_filter_enabled"))
    .setEnableSpamFilter(this.getBoolean("spam_filter_state"))
    .setKickState(this.getBoolean("kick_instead_state"))
    .setRatelimits(ratelimmitChecks(this.getString("ratelimits")))
    .setSpamThreshold(this.getInt("spam_threshold"))
    .setYoungAccountBanEnabled(this.getBoolean("ban_young_account_enabled"))
    .setYoungAccountThreshold(this.getInt("ban_young_account_threshold_days"))
    // logging
    .setBanLogging(this.getBoolean("ban_logging_enabled"))
    .setUnbanLogging(this.getBoolean("unban_logging_enabled"))
    .setMuteLogging(this.getBoolean("mute_logging_enabled"))
    .setKickLogging(this.getBoolean("kick_logging_enabled"))
    .setWarnLogging(this.getBoolean("warn_logging_enabled"))
    .setMemberLogging(this.getBoolean("member_logging_enabled"))
    .setInviteLogging(this.getBoolean("invite_logging_enabled"))
    .setMessageLogging(this.getBoolean("message_logging_enabled"))

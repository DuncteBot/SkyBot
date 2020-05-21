/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.commands.uncategorized

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.*
import me.duncte123.weebJava.types.StatusType
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Authors
import ml.duncte123.skybot.entities.jda.DunctebotGuild
import ml.duncte123.skybot.extensions.getStaticAvatarUrl
import ml.duncte123.skybot.extensions.parseTimes
import ml.duncte123.skybot.extensions.toEmoji
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.FinderUtils
import ml.duncte123.skybot.utils.GuildUtils
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.stream.Collectors

@Authors(authors = [
    Author(nickname = "Sanduhr32", author = "Maurice R S"),
    Author(nickname = "duncte123", author = "Duncan Sterken")
])
class UserinfoCommand : Command() {
    private val nitroUserLink = "**[Nitro User:](https://github.com/DuncteBot/SkyBot/issues/201#issuecomment-486182959 \"Click for more info on the nitro user check\")**"

    init {
        this.shouldLoadMembers = true
        this.name = "userinfo"
        this.aliases = arrayOf("user", "i", "whois", "ui", "retrieveuserinfo")
        this.help = "Get some information about yourself or from another user"
        this.usage = "[@user]"
        this.cooldown = 30
    }

    override fun execute(ctx: CommandContext) {
        val event = ctx.event
        val args = ctx.args

        if (ctx.invoke.toLowerCase() == "retrieveuserinfo") {
            if (args.isEmpty()) {
                sendMsg(ctx, "Missing arguments for retrieving user information")

                return
            }

            ctx.jda.retrieveUserById(args[0]).queue({
                renderUserEmbed(event, it, ctx.guild, ctx.variables.prettyTime)
            }, {
                sendMsg(ctx, "Could not get user info: ${it.message}")
            })

            return
        }

        var u: User? = null
        var m: Member? = null

        if (args.isEmpty()) {
            u = event.author
            m = event.member
        } else {
            val members = FinderUtils.searchMembers(ctx.argsRaw, ctx)
            var users = members.stream().map { it.user }.collect(Collectors.toList())

            if (users.isEmpty()) {
                users = FinderUtils.searchUsers(ctx.argsRaw, ctx)

                if (users.isNotEmpty()) {
                    u = users[0]
                    m = ctx.guild.getMember(u)
                }

            } else {
                u = users[0]
                m = ctx.guild.getMember(u)
            }
        }

        if (u != null && m == null) {
            renderUserEmbed(event, u, ctx.guild, ctx.variables.prettyTime)

            return
        }

        if (m == null) {
            sendMsg(event, "This user could not be found.")
            return
        }

        renderMemberEmbed(event, m, ctx)
    }

    private fun renderUserEmbed(event: GuildMessageReceivedEvent, user: User, guild: DunctebotGuild, prettyTime: PrettyTime) {
        val times = prettyTime.parseTimes(user)

        val embed = EmbedUtils.defaultEmbed()
            .setColor(guild.getColor())
            .setThumbnail(user.getStaticAvatarUrl())
            .setDescription("""User info for ${user.asMention}
                        |
                        |**User Tag:** ${user.asTag.escapeMarkDown()}
                        |**User Id:** ${user.id}
                        |**Display Name:** ${user.name.escapeMarkDown()}
                        |**Account Created:** ${times.first} (${times.second})
                        |$nitroUserLink ${user.isNitro().toEmoji()}
                        |**Bot Account:** ${user.isBot.toEmoji()}
                        |
                        |_Use `${guild.getSettings().customPrefix}avatar [user]` to get a user's avatar_
                    """.trimMargin())

        sendEmbed(event.channel, embed)
    }

    private fun generateJoinOrder(guild: Guild, member: Member) = buildString {
        val joins = guild.memberCache.applyStream {
            it.sorted(Comparator.comparing(Member::getTimeJoined)).collect(Collectors.toList())
        }!!

        var index = joins.indexOf(member)
        index -= 3

        if (index < 0) {
            index = 0
        }

        appendln()

        if (joins[index] == member) {
            append("[${joins[index].effectiveName.escapeMarkDown()}](https://patreon.com/DuncteBot)")
        } else {
            append(joins[index].effectiveName.escapeMarkDown())
        }

        for (i in index + 1 until index + 7) {
            if (i >= joins.size) {
                break
            }

            val mbr = joins[i]
            var usrName = mbr.effectiveName.escapeMarkDown()

            if (mbr == member) {
                usrName = "[$usrName](https://patreon.com/DuncteBot)"
            }

            append(" \\> ")
            append(usrName)
        }
    }

    private fun renderMemberEmbed(event: GuildMessageReceivedEvent, member: Member, ctx: CommandContext) {
        val prettyTime = ctx.variables.prettyTime
        val user = member.user
        val guild = ctx.guild

        val userTimes = prettyTime.parseTimes(user)
        val memberTimes = prettyTime.parseTimes(member)

        val boostingSinceMsg = if (member.timeBoosted == null) {
            ""
        } else {
            val boostTime = member.timeBoosted!!
            val boostTimes = prettyTime.parseTimes(boostTime)

            "\n**Boosting since:** ${boostTime.toBoostEmote()} ${boostTimes.first} (${boostTimes.second})"
        }

        val embed = EmbedUtils.defaultEmbed()
            .setColor(member.color)
            .setThumbnail(user.getStaticAvatarUrl())
            .setDescription("""User info for ${member.asMention}
                        |
                        |**User Tag:** ${user.asTag.escapeMarkDown()}
                        |**User Id:** ${user.id}
                        |**Display Name:** ${member.effectiveName.escapeMarkDown()}
                        |**Account Created:** ${userTimes.first} (${userTimes.second})
                        |$nitroUserLink ${user.isNitro().toEmoji()}
                        |**Joined Server:** ${memberTimes.first} (${memberTimes.second})
                        |**Join position:** #${GuildUtils.getMemberJoinPosition(member)}
                        |**Join Order:** ${generateJoinOrder(guild, member)}
                        |**Bot Account:** ${user.isBot.toEmoji()}
                        |**Boosting:** ${(member.timeBoosted != null).toEmoji()}$boostingSinceMsg
                        |
                        |_Use `${ctx.prefix}avatar [user]` to get a user's avatar_
                    """.trimMargin())

        // If we don't have permission to send files or our weebSh key is null
        if (!ctx.selfMember.hasPermission(event.channel, Permission.MESSAGE_ATTACH_FILES)
            || ctx.config.apis.weebSh == null) {
            sendEmbedRaw(event.channel, embed.build(), null)
            return
        }

        ctx.weebApi.generateDiscordStatus(toWeebshStatus(member),
            user.getStaticAvatarUrl() + "?size=256").async {
            event.channel.sendFile(it, "stat.png")
                .embed(embed.setThumbnail("attachment://stat.png").build())
                .queue(null) {
                    sendEmbedRaw(event.channel, embed.setThumbnail(user.effectiveAvatarUrl).build(), null)
                }
        }
    }


    private fun toWeebshStatus(member: Member): StatusType {
        if (member.activities.isNotEmpty() && member.activities.any { it.type == Activity.ActivityType.STREAMING }) {
            return StatusType.STREAMING
        }

        return when (member.onlineStatus) {
            OnlineStatus.ONLINE -> StatusType.ONLINE
            OnlineStatus.DO_NOT_DISTURB -> StatusType.DND
            OnlineStatus.IDLE -> StatusType.IDLE
            else -> StatusType.OFFLINE
        }
    }

    private fun User.isNitro() = this.avatarId != null && (this.avatarId as String).startsWith("a_")

    /*private fun OnlineStatus.toEmote() = when (this) {
        OnlineStatus.ONLINE -> "<:online2:464520569975603200>"
        OnlineStatus.IDLE -> "<:away2:464520569862357002>"
        OnlineStatus.DO_NOT_DISTURB -> "<:dnd2:464520569560498197>"

        else -> "<:offline2:464520569929334784>"
    }*/

    private fun OffsetDateTime.toBoostEmote(): String {
        return when (this.until(OffsetDateTime.now(), ChronoUnit.MONTHS)) {
            0L, 1L -> {
                "<:booster:585764032162562058>"
            }
            2L -> {
                "<:booster2:585764446253744128>"
            }
            3L -> {
                "<:booster3:585764446220189716>"
            }
            else -> {
                "<:booster4:585764446178246657>"
            }
        }
    }

    private fun String.escapeMarkDown(): String {
        return this.replace("_", "\\_")
            .replace("*", "\\*")
            .replace("`", "\\`")
            .replace("|", "\\|")
            .replace(">", "\\?")
            .replace("~", "\\~")
    }

}

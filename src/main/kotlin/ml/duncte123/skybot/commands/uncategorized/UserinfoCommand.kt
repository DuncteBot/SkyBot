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

package ml.duncte123.skybot.commands.uncategorized

import com.jagrosh.jdautilities.commons.utils.FinderUtil
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.*
import me.duncte123.weebJava.types.StatusType
import ml.duncte123.skybot.Author
import ml.duncte123.skybot.Authors
import ml.duncte123.skybot.entities.jda.DunctebotGuild
import ml.duncte123.skybot.extensions.toEmoji
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.GuildUtils
import net.dv8tion.jda.core.OnlineStatus
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.ocpsoft.prettytime.PrettyTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors

@Authors(authors = [
    Author(nickname = "Sanduhr32", author = "Maurice R S"),
    Author(nickname = "duncte123", author = "Duncan Sterken")
])
class UserinfoCommand : Command() {
    private val nitroUserLink = "**[Nitro User:](https://github.com/DuncteBot/SkyBot/issues/201#issuecomment-486182959 \"Click for more info on the nitro user check\")**"

    override fun executeCommand(ctx: CommandContext) {
        val event = ctx.event
        val args = ctx.args

        if (ctx.invoke == "retrieveuserinfo") {
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
            val members = FinderUtil.findMembers(ctx.argsRaw, ctx.guild)
            var users = members.stream().map { it.user }.collect(Collectors.toList())

            if (users.isEmpty()) {
                users = FinderUtil.findUsers(ctx.argsRaw, ctx.jda)

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

        val createTime = user.creationTime
        val createTimeDate = Date.from(createTime.toInstant())
        val createTimeFormat = createTime.format(DateTimeFormatter.RFC_1123_DATE_TIME)
        val createTimeHuman = prettyTime.format(createTimeDate)

        val embed = EmbedUtils.defaultEmbed()
            .setColor(guild.getColor())
            .setThumbnail(user.effectiveAvatarUrl.replace(".gif", ".png"))
            .setDescription("""User info for ${user.asMention}
                        |
                        |**Username + Discriminator:** ${user.asTag}
                        |**User Id:** ${user.id}
                        |**Display Name:** ${user.name}
                        |**Account Created:** $createTimeFormat ($createTimeHuman)
                        |$nitroUserLink ${isNitro(user)}
                        |**Bot Account:** ${user.isBot.toEmoji()}
                        |
                        |_Use `${guild.getSettings().customPrefix}avatar [user]` to get a user's avatar_
                    """.trimMargin())

        sendEmbed(event.channel, embed)
    }


    private fun renderMemberEmbed(event: GuildMessageReceivedEvent, m: Member, ctx: CommandContext) {

        val prettyTime = ctx.variables.prettyTime
        val u = m.user
        val joinOrder = StringBuilder()
        val joins = event.guild.memberCache.stream().sorted(
            Comparator.comparing<Member, OffsetDateTime> { it.joinDate }
        ).collect(Collectors.toList())

        var index = joins.indexOf(m)
        index -= 3

        if (index < 0) {
            index = 0
        }

        joinOrder.append("\n")

        if (joins[index] == m) {
            joinOrder.append("[${joins[index].effectiveName}](https://dunctebot.com/)")
        } else {
            joinOrder.append(joins[index].effectiveName)
        }

        for (i in index + 1 until index + 7) {
            if (i >= joins.size) {
                break
            }

            val usr = joins[i]
            var usrName = usr.effectiveName

            if (usr == m) {
                usrName = "[$usrName](https://dunctebot.com/)"
            }

            joinOrder.append(" > ").append(usrName)
        }

        val createTime = u.creationTime
        val createTimeDate = Date.from(createTime.toInstant())
        val createTimeFormat = createTime.format(DateTimeFormatter.RFC_1123_DATE_TIME)
        val createTimeHuman = prettyTime.format(createTimeDate)

        val joinTime = m.joinDate
        val joinTimeDate = Date.from(joinTime.toInstant())
        val joinTimeFormat = joinTime.format(DateTimeFormatter.RFC_1123_DATE_TIME)
        val joinTimeHuman = prettyTime.format(joinTimeDate)

        val mStatus = m.onlineStatus

        val embed = EmbedUtils.defaultEmbed()
            .setColor(m.color)
            .setThumbnail(u.effectiveAvatarUrl.replace(".gif", ".png"))
            .setDescription("""User info for ${m.asMention}
                        |
                        |**Username + Discriminator:** ${u.asTag}
                        |**User Id:** ${u.id}
                        |**Display Name:** ${m.effectiveName}
                        |**Account Created:** $createTimeFormat ($createTimeHuman)
                        |$nitroUserLink ${isNitro(u).toEmoji()}
                        |**Joined Server:** $joinTimeFormat ($joinTimeHuman)
                        |**Join position:** #${GuildUtils.getMemberJoinPosition(m)}
                        |**Join Order:** $joinOrder
                        |**Online Status:** ${convertStatus(mStatus)} ${mStatus.key}
                        |**Bot Account:** ${u.isBot.toEmoji()}
                        |
                        |_Use `${ctx.prefix}avatar [user]` to get a user's avatar_
                    """.trimMargin())

        if (!event.guild.selfMember.hasPermission(event.channel, Permission.MESSAGE_ATTACH_FILES)
            || ctx.config.apis.weebSh.wolketoken == null) {
            sendEmbedRaw(event.channel, embed.build(), null)
            return
        }

        ctx.weebApi.generateDiscordStatus(toWeebshStatus(m),
            u.effectiveAvatarUrl.replace("gif", "png") + "?size=256").async {
            event.channel.sendFile(it, "stat.png")
                .embed(embed.setThumbnail("attachment://stat.png").build())
                .queue(null) {
                    sendEmbedRaw(event.channel, embed.setThumbnail(u.effectiveAvatarUrl).build(), null)
                }
        }
    }

    override fun help(prefix: String): String? = "Get information from yourself or from another user.\n" +
        "Usage: `$prefix$name [username]`"

    override fun getName() = "userinfo"

    override fun getAliases() = arrayOf("user", "i", "whois", "ui", "retrieveuserinfo")

    private fun toWeebshStatus(member: Member): StatusType {
        if (member.game != null && member.game.type == Game.GameType.STREAMING) {
            return StatusType.STREAMING
        }

        return when (member.onlineStatus) {
            OnlineStatus.ONLINE -> StatusType.ONLINE
            OnlineStatus.OFFLINE -> StatusType.OFFLINE
            OnlineStatus.DO_NOT_DISTURB -> StatusType.DND
            OnlineStatus.IDLE -> StatusType.IDLE
            OnlineStatus.INVISIBLE -> StatusType.OFFLINE
            else -> StatusType.ONLINE
        }
    }

    private fun isNitro(user: User): Boolean {
        return user.avatarId != null && user.avatarId.startsWith("a_")
    }

    private fun convertStatus(status: OnlineStatus): String {
        return when (status) {
            OnlineStatus.ONLINE -> "<:online2:464520569975603200>"
            OnlineStatus.IDLE -> "<:away2:464520569862357002>"
            OnlineStatus.DO_NOT_DISTURB -> "<:dnd2:464520569560498197>"

            else -> "<:offline2:464520569929334784>"
        }
    }

}

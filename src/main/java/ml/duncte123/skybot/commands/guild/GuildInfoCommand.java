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

package ml.duncte123.skybot.commands.guild;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.messaging.MessageUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.GuildUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class GuildInfoCommand extends Command {

    //https://stackoverflow.com/a/1915107/4453592
    private static final String INVITE_STRING_TEMPLATE = "**Invite:** [discord.gg/%1$s](https://discord.gg/%1$s)";

    public GuildInfoCommand() {
        this.name = "guildinfo";
        this.aliases = new String[]{
            "serverinfo",
            "server",
            "guild",
            "gi",
            "si",
        };
        this.helpFunction = (prefix, invoke) -> "Shows some stats about the server";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        try {
            final Guild g = event.getGuild();

            if (g.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
                if (!g.getFeatures().contains("VANITY_URL")) {
                    g.retrieveInvites().queue((invites) -> {

                        if (invites.isEmpty()) {
                            sendGuildInfoEmbed(event, ctx, "");
                            return;
                        }

                        final Invite invite = invites.get(0);
                        sendGuildInfoEmbed(event, ctx, String.format(INVITE_STRING_TEMPLATE, invite.getCode()));
                    });
                } else {
                    sendGuildInfoEmbed(event, ctx, String.format(INVITE_STRING_TEMPLATE, g.getVanityUrl()));
                }
            } else {
                sendGuildInfoEmbed(event, ctx, "");
            }

        }
        catch (Exception e) {
            logger.error("GuildInfoCommand", e);
            MessageUtils.sendMsg(event, "OOPS, something went wrong: " + e.getMessage());
        }
    }

    private void sendGuildInfoEmbed(GuildMessageReceivedEvent event, CommandContext ctx, String inviteString) {
        final Guild g = event.getGuild();
        final double[] ratio = GuildUtils.getBotRatio(g);
        final EmbedBuilder eb = EmbedUtils.defaultEmbed();
        final GuildSettings settings = ctx.getGuildSettings();

        final OffsetDateTime createTime = g.getTimeCreated();
        final Date createTimeDate = Date.from(createTime.toInstant());
        final String createTimeFormat = createTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
        final String createTimeHuman = ctx.getVariables().getPrettyTime().format(createTimeDate);

        if (settings.getServerDesc() != null && !"".equals(settings.getServerDesc())) {
            eb.addField("Server Description", settings.getServerDesc() + "\n\u200B", false);
        }

        String owner = "Unknown";

        if (g.getOwner() != null) {
            owner = g.getOwner().getEffectiveName();
        }

        eb.setThumbnail(event.getGuild().getIconUrl())
            .addField("Basic Info", "**Owner:** " + owner + "\n" +
                "**Name:** " + g.getName() + "\n" +
                "**Prefix:** " + settings.getCustomPrefix() + "\n" +
                "**Region:** " + g.getRegion().getName() + "\n" +
                "**Created at:** " + String.format("%s (%s)", createTimeFormat, createTimeHuman) + "\n" +
                "**Verification level:** " + GuildUtils.verificationLvlToName(g.getVerificationLevel()) + "\n" +
                inviteString + "\n\u200B", false)
            .addField("Member Stats", "**Total members:** " + g.getMemberCount() + "\n" +
                "**(Possible) Nitro users:** " + GuildUtils.countAnimatedAvatars(g) + "\n" +
                "**Bot to user ratio:** " + ratio[1] + "% is a bot and " + ratio[0] + "% is a user", false);

        sendEmbed(event, eb.build());
    }

}

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

package ml.duncte123.skybot.commands.guild;

import me.duncte123.botCommons.messaging.MessageUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.GuildUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;

import static ml.duncte123.skybot.utils.MessageUtils.sendEmbed;

/**
 * Created by Duncan on 2-7-2017.
 */
@Authors(authors = {
        @Author(nickname = "Sanduhr32", author = "Maurice R S"),
        @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class GuildInfoCommand extends Command {

    //https://stackoverflow.com/a/1915107/4453592
    private static final String INVITE_STRING_TEMPLATE = "**Invite:** [discord.gg/%1$s](https://discord.gg/%1$s)";

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {
        GuildMessageReceivedEvent event = ctx.getEvent();
        try {
            Guild g = event.getGuild();

            if (g.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
                if (!g.getFeatures().contains("VANITY_URL")) {
                    g.getInvites().queue(invites ->
                            invites.stream().findFirst().ifPresent(invite ->
                                    sendGuildInfoEmbed(event, ctx, String.format(INVITE_STRING_TEMPLATE, invite.getCode()))
                            )
                    );
                } else {
                    g.getVanityUrl().queue(invite ->
                            sendGuildInfoEmbed(event, ctx, String.format(INVITE_STRING_TEMPLATE, invite))
                    );
                }
            } else {
                sendGuildInfoEmbed(event, ctx, "");
            }

        } catch (Exception e) {
            MessageUtils.sendMsg(event, "OOPS, something went wrong: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String help() {
        return "Show some stats";
    }

    @Override
    public String getName() {
        return "guildinfo";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"serverinfo", "server", "guild"};
    }

    private void sendGuildInfoEmbed(GuildMessageReceivedEvent event, CommandContext ctx, String inviteString) {
        Guild g = event.getGuild();
        double[] ratio = GuildUtils.getBotRatio(g);
        EmbedBuilder eb = EmbedUtils.defaultEmbed();
        GuildSettings settings = ctx.getGuildSettings();
        if (settings.getServerDesc() != null && !"".equals(settings.getServerDesc())) {
            eb.addField("Server Description", settings.getServerDesc() + "\n", false);
        }
        eb.setThumbnail(event.getGuild().getIconUrl() != null ? event.getGuild().getIconUrl() : "https://i.duncte123.ml/blob/b1nzyblob.png")
                .addField("Basic Info", "**Owner:** " + g.getOwner().getEffectiveName() + "\n" +
                        "**Name:** " + g.getName() + "\n" +
                        "**Prefix:** " + settings.getCustomPrefix() + "\n" +
                        "**Region:** " + g.getRegion().getName() + "\n" +
                        "**Created at:** " + g.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME) + "\n" +
                        "**Verification level:** " + GuildUtils.verificationLvlToName(g.getVerificationLevel()) + "\n" +
                        inviteString, false)
                .addField("Member Stats", "**Total members:** " + g.getMemberCache().size() + "\n" +
                        "**(Possible) Nitro users:** " + GuildUtils.countAnimatedAvatars(g) + "\n" +
                        "**Bot to user ratio:** " + ratio[1] + "% is a bot and " + ratio[0] + "% is a user", false);

        sendEmbed(event, eb.build());
    }

}
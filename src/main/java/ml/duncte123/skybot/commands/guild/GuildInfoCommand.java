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

import kotlin.Pair;
import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.extensions.PrettyTimeKt;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.GuildUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;

import javax.annotation.Nonnull;

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
        this.help = "Shows some stats about the server";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final Guild g = ctx.getJDAGuild();

            // If the guild has the VANITY_URL feature enabled or the vanityCode is not null the guild has a vanity url
            // We do this check before fetching the invites so that we can display the vanity url if they have one
            if (g.getFeatures().contains("VANITY_URL") || g.getVanityCode() != null) {
                sendGuildInfoEmbed(ctx, String.format(INVITE_STRING_TEMPLATE, g.getVanityCode()));
                return;
            }

            // Check if the selfuser has the manage server permissions
            // We can only fetch the invites when we have those permissions
            if (g.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
                // Fetch the invites for the guild
                g.retrieveInvites().queue((invites) -> {

                    // If there are no invites we will put give an empty string
                    if (invites.isEmpty()) {
                        sendGuildInfoEmbed(ctx, null);
                        return;
                    }

                    // Get the first invite and format that with the code
                    final Invite invite = invites.get(0);
                    sendGuildInfoEmbed(ctx, String.format(INVITE_STRING_TEMPLATE, invite.getCode()));
                });

                return;
            }

            // If there is no vanity url and we are not allowed to fetch invites
            // we will default to the empty string for the invite so that we don't crash
            sendGuildInfoEmbed(ctx, "");
    }

    private void sendGuildInfoEmbed(CommandContext ctx, String inviteString) {
        final Guild g = ctx.getJDAGuild();
        final double[] ratio = GuildUtils.getBotRatio(g);
        final EmbedBuilder eb = EmbedUtils.getDefaultEmbed();
        final GuildSettings settings = ctx.getGuildSettings();
        final Pair<String, String> times = PrettyTimeKt.parseTimes(ctx.getVariables().getPrettyTime(), g);

        if (settings.getServerDesc() != null && !"".equals(settings.getServerDesc())) {
            eb.addField("Server Description", settings.getServerDesc() + "\n\u200B", false);
        }

        String owner = "Unknown";

        if (g.getOwner() != null) {
            owner = g.getOwner().getEffectiveName();
        }

        eb.setThumbnail(g.getIconUrl())
            .addField("Basic Info", "**Owner:** " + owner + "\n" +
                "**Name:** " + g.getName() + "\n" +
                "**Prefix:** " + settings.getCustomPrefix() + "\n" +
                "**Region:** " + g.getRegion().getName() + "\n" +
                "**Created at:** " + String.format("%s (%s)", times.getFirst(), times.getSecond()) + "\n" +
                "**Verification level:** " + GuildUtils.verificationLvlToName(g.getVerificationLevel()) + "\n" +
                inviteString + "\n\u200B", false)
            .addField("Member Stats", "**Total members:** " + g.getMemberCount() + "\n" +
                "**(Possible) Nitro users:** " + GuildUtils.countAnimatedAvatars(g) + "\n" +
                "**Bot to user ratio:** " + ratio[1] + "% is a bot and " + ratio[0] + "% is a user", false);

        sendEmbed(ctx, eb);
    }

}

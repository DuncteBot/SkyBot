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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.guild;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.time.format.DateTimeFormatter;

/**
 * Created by Duncan on 2-7-2017.
 */

public class GuildInfoCommand extends Command {

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        Guild g = event.getGuild();
        GuildSettings settings = GuildSettingsUtils.getGuild(event.getGuild());
        try {
            final String inviteStringTemplate = "[discord.gg/%s](https://discord.gg/%s)";
            final String[] inviteString = new String[1];

            if (g.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
                if (!g.getFeatures().contains("VANITY_URL")) {
                    g.getInvites().complete().parallelStream().findFirst().ifPresent(inv -> inviteString[0] = String.format(inviteStringTemplate, inv.getCode(), inv.getCode()));
                } else {
                    String vanity = g.getVanityUrl().complete();
                    inviteString[0] = String.format(inviteStringTemplate, vanity, vanity);
                }
            }

            double[] ratio = AirUtils.getBotRatio(g);
            EmbedBuilder eb = EmbedUtils.defaultEmbed()
                    .addField("Basic Info", "**Owner:** " + g.getOwner().getEffectiveName() + "\n" +
                            "**Name:** " + g.getName() + "\n" +
                            "**Prefix:** " + settings.getCustomPrefix() + "\n" +
                            "**Region:** " + g.getRegion().getName() + "\n" +
                            "**Created at:** " + g.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME) + "\n" +
                            "**Verification level:** " + AirUtils.verificationLvlToName(g.getVerificationLevel()) + "\n" +
                            "**Invite:** " + inviteString[0], false)
                    .addField("Member Stats", "**Total members:** " + g.getMemberCache().size() + "\n" +
                            "**(Possible) Nitro users:** " + AirUtils.countAnimatedAvatars(g).get() + "\n" +
                            "**Bot to user ratio:** " + ratio[1] + "% is a bot and " + ratio[0] + "% is a user (total users " + g.getMemberCache().size() + ")", false);
            //If the guild doesn't have a icon we show a nice blob
            eb.setThumbnail(event.getGuild().getIconUrl() != null ? event.getGuild().getIconUrl() : "https://i.duncte123.ml/blob/b1nzyblob.png");

            sendEmbed(event, eb.build());
        } catch (Exception e) {
            sendMsg(event, "OOPS, something went wrong: " + e.getMessage());
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
}

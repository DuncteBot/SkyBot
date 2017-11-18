/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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
import net.dv8tion.jda.core.entities.MessageEmbed;
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

            double[] ratio = AirUtils.getBotRatio(g);
            EmbedBuilder eb = EmbedUtils.defaultEmbed()
                                      .addField("Guild Owner", g.getOwner().getEffectiveName(), true)
                                      .addField("Total Members", g.getMembers().size() + "", true)
                                      .addField("Verification Level", AirUtils.verificationLvlToName(g.getVerificationLevel()), true)
                                      .addField("Guild Name", g.getName(), true)
                                      .addField("Guild prefix", settings.getCustomPrefix(), true)
                                      .addField("Guild Creation Time", g.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                                      .addField("Guild Region", g.getRegion().getName(), true)
                                      .addField("Bot to user ratio", ratio[1] + "% of this guild is a bot (total users " + g.getMembers().size() + ")", true);
            if (g.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
                eb.addField("Guild Invite",
                        "[https://discord.gg/" + g.getInvites().complete().get(0).getCode() +
                                "](https://discord.gg/" + g.getInvites().complete().get(0).getCode() + ")",
                        true);
            }
            //If the guild doesn't have a icon we show a nice blob
            eb.setThumbnail(event.getGuild().getIconUrl() != null ? event.getGuild().getIconUrl() : "https://i.duncte123.ml/blob/b1nzyblob.png");

            MessageEmbed messageEmbed = eb.build();

            sendEmbed(event, messageEmbed);
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
        return new String[]{"serverinfo", "server"};
    }
}

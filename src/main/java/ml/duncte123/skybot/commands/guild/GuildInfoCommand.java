/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ml.duncte123.skybot.commands.guild;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
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

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event){
        Guild g = event.getGuild();
        GuildSettings settings = getSettings(g.getId());
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
                    .addField("Bot to user ratio", ratio[1] + "% of this guild is a bot (total users "+g.getMembers().size()+")", true);
                    if(g.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) {
                        eb.addField("Guild Invite",
                            "[https://discord.gg/" + g.getInvites().complete().get(0).getCode() +
                                "](https://discord.gg/" + g.getInvites().complete().get(0).getCode() + ")",
                                true);
                    }
                    //If the guild doesn't have a icon we show a nice blob
                   eb.setThumbnail(event.getGuild().getIconUrl() != null  ? event.getGuild().getIconUrl() : "https://i.duncte123.ml/blob/b1nzyblob.png");

            MessageEmbed messageEmbed = eb.build();

            sendEmbed(event, messageEmbed);
        }
        catch (Exception e){
            sendMsg(event, "OOPS, something went wrong: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
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

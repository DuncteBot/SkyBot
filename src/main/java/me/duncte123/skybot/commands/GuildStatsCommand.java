package me.duncte123.skybot.commands;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.time.format.DateTimeFormatter;

/**
 * Created by Duncan on 2-7-2017.
 */

public class GuildStatsCommand implements Command {


    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event){
        Guild g = event.getGuild();
        try {
            event.getChannel().sendMessage(AirUtils.defaultEmbed()
                    .addField("Guild Owner", g.getOwner().getEffectiveName(), false)
                    .addField("Total Members", g.getMembers().size() + "", false)
                    .addField("Verification Level", SkyBot.verificationLvlToName(g.getVerificationLevel()), false)
                    .addField("Guild Name", g.getName(), false)
                    .addField("Guild Creation Time", g.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME), false)
                    .addField("Guild Region", g.getRegion().getName(), false)
                    .addField("Guild Invite",
                            "[https://discord.gg/" + g.getInvites().complete(false).get(0).getCode() +
                                    "](https://discord.gg/" + g.getInvites().complete(false).get(0).getCode() + ")",
                            false)
                    .setThumbnail(event.getGuild().getIconUrl())
                    .build()).queue();
        }
        catch (Exception e){
            event.getChannel().sendMessage("OOPS, something went wrong: " + e.getMessage()).queue();
        }
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "Show some stats";
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return;
    }
}

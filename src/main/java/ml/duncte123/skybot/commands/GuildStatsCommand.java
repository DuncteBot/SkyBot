package ml.duncte123.skybot.commands;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.time.format.DateTimeFormatter;

/**
 * Created by Duncan on 2-7-2017.
 */

public class GuildStatsCommand extends Command {


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
                    .addField("Guild Owner", g.getOwner().getEffectiveName(), true)
                    .addField("Total Members", g.getMembers().size() + "", true)
                    .addField("Verification Level", AirUtils.verificationLvlToName(g.getVerificationLevel()), true)
                    .addField("Guild Name", g.getName(), true)
                    .addField("Guild Creation Time", g.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                    .addField("Guild Region", g.getRegion().getName(), true)
                    .addField("Guild Invite",
                            "[https://discord.gg/" + g.getInvites().complete().get(0).getCode() +
                                    "](https://discord.gg/" + g.getInvites().complete().get(0).getCode() + ")",
                            true)
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
}

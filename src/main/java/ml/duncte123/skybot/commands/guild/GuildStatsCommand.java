package ml.duncte123.skybot.commands.guild;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.time.format.DateTimeFormatter;

/**
 * Created by Duncan on 2-7-2017.
 */

public class GuildStatsCommand extends Command {

    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     */
    @Override
    public void action(String[] args, GuildMessageReceivedEvent event){
        Guild g = event.getGuild();
        try {

            EmbedBuilder eb = AirUtils.defaultEmbed()
                    .addField("Guild Owner", g.getOwner().getEffectiveName(), true)
                    .addField("Total Members", g.getMembers().size() + "", true)
                    .addField("Verification Level", AirUtils.verificationLvlToName(g.getVerificationLevel()), true)
                    .addField("Guild Name", g.getName(), true)
                    .addField("Guild Creation Time", g.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                    .addField("Guild Region", g.getRegion().getName(), true);
                    if(PermissionUtil.checkPermission(g.getSelfMember(), Permission.MANAGE_SERVER)) {
                        eb.addField("Guild Invite",
                            "[https://discord.gg/" + g.getInvites().complete().get(0).getCode() +
                                "](https://discord.gg/" + g.getInvites().complete().get(0).getCode() + ")",
                                true);
                    }
                   eb.setThumbnail(event.getGuild().getIconUrl());

            MessageEmbed messageEmbed = eb.build();

            if(!PermissionUtil.checkPermission(event.getGuild().getSelfMember(), Permission.MESSAGE_EMBED_LINKS)) {
                event.getChannel().sendMessage(AirUtils.embedToMessage(messageEmbed)).queue();
                return;
            }
            event.getChannel().sendMessage(messageEmbed).queue();
        }
        catch (Exception e){
            event.getChannel().sendMessage("OOPS, something went wrong: " + e.getMessage()).queue();
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
}

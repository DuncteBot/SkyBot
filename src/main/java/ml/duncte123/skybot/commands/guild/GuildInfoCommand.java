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
import net.dv8tion.jda.core.utils.PermissionUtil;

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
        GuildSettings settings = AirUtils.guildSettings.get(g.getId());
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
                    if(PermissionUtil.checkPermission(g.getSelfMember(), Permission.MANAGE_SERVER)) {
                        eb.addField("Guild Invite",
                            "[https://discord.gg/" + g.getInvites().complete().get(0).getCode() +
                                "](https://discord.gg/" + g.getInvites().complete().get(0).getCode() + ")",
                                true);
                    }
                    //If the guild doesn't have a icon we show a nice blob
                   eb.setThumbnail(event.getGuild().getIconUrl() != null  ? event.getGuild().getIconUrl() : "https://i.duncte123.ml/blob/b1nzyblob.png");

            MessageEmbed messageEmbed = eb.build();

            sendEmbed(messageEmbed, event);
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

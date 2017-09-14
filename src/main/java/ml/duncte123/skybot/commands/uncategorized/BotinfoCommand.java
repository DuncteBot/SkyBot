package ml.duncte123.skybot.commands.uncategorized;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;

/**
 * Created by Duncan on 11-7-2017.
 */
public class BotinfoCommand extends Command {

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {
        User u = event.getJDA().getSelfUser();
        Member m = event.getGuild().getMemberById(u.getId());

        MessageEmbed eb = AirUtils.defaultEmbed()
                .setDescription("Here is some information about me \uD83D\uDE09")
                .setThumbnail(u.getEffectiveAvatarUrl())
                .addField("Created by", "duncte123#1245", true)
                .addField("Version", Config.version, true)
                .addField("Lib info", "JDA version: " + JDAInfo.VERSION + "\nLavaPlayer version: " + PlayerLibrary.VERSION, false)
                .addField("Github repo", "[https://github.com/duncte123/SkyBot](https://github.com/duncte123/SkyBot)", false)
                .addField("Donate", "If you want to help me out and support the bot please consider to [https://paypal.me/duncte123](donate) any amount.", false)
                .build();
        if(!PermissionUtil.checkPermission(event.getGuild().getSelfMember(), Permission.MESSAGE_EMBED_LINKS)) {
            event.getChannel().sendMessage(AirUtils.embedToMessage(eb)).queue();
            return;
        }
        event.getChannel().sendMessage(eb).queue();
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Get's some info about the bot";
    }

    @Override
    public String getName() {
        return "botinfo";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"about"};
    }
}

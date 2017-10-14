package ml.duncte123.skybot.commands.uncategorized;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

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
    	User u = event.getAuthor();
    	
        MessageEmbed eb = EmbedUtils.defaultEmbed()
                .setDescription("Here is some information about me \uD83D\uDE09")
                .setThumbnail(u.getEffectiveAvatarUrl())
                .addField("General info", "**Creator:** duncte123#1245\n" +
                        "**Invite:** [You can invite me by clicking here](https://bots.discord.pw/bots/210363111729790977)\n" +
                        "**Github:** [https://github.com/duncte123/SkyBot](https://github.com/duncte123/SkyBot)\n" +
                        "**Guilds:** " + event.getJDA().asBot().getShardManager().getGuildCache().size() + "\n" +
                        "**Bot version:** " + Settings.version, true)
                .addField("Lib info", "JDA version: " + JDAInfo.VERSION + "\nLavaPlayer version: " + PlayerLibrary.VERSION, false)
                .addField("Donate", "If you want to help me out and support the bot please consider to [donate](https://paypal.me/duncte123) any amount.", false)
                .build();
        sendEmbed(event, eb);
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Gets some info about the bot\nUsage: `"+ Settings.prefix+getName()+"`";
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

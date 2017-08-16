package ml.duncte123.skybot.commands;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.time.format.DateTimeFormatter;

/**
 * Created by Duncan on 11-7-2017.
 */
public class BotinfoCommand extends Command {



    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        User u = event.getJDA().getSelfUser();
        Member m = event.getGuild().getMemberById(u.getId());

        EmbedBuilder eb = AirUtils.defaultEmbed()
                .setDescription("Bot information.")
                .setThumbnail(u.getEffectiveAvatarUrl())
                .addField("Username + Discriminator", u.getName() + "#" + u.getDiscriminator(), true)
                .addField("Bot Id", u.getId(), true)
                .addField("Game", m.getGame().getName(), true)
                .addField("Nickname", (m.getNickname() == null ? "**_NO NICKNAME_**" : m.getNickname()), true)
                .addField("Created", u.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                .addField("Joined", m.getJoinDate().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                .addField("Online Status", m.getOnlineStatus().name(), true)
                .addField("Lib info", "JDA version: " + JDAInfo.VERSION + "\nLavaPlayer version: " + PlayerLibrary.VERSION, false)
                .addField("Github repo", "[https://github.com/duncte123/SkyBot](https://github.com/duncte123/SkyBot)", false);
        event.getChannel().sendMessage(eb.build()).queue();
    }

    @Override
    public String help() {
        return "Get's some info about the bot";
    }
}

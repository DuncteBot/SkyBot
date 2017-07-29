package me.duncte123.skybot.commands;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.time.format.DateTimeFormatter;

/**
 * Created by Duncan on 11-7-2017.
 */
public class BotinfoCommand implements Command {



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
                .setImage(u.getEffectiveAvatarUrl())
                .addField("Username + Discriminator", u.getName() + "#" + u.getDiscriminator(), false)
                .addField("Bot Id", u.getId(), false)
                .addField("Game", m.getGame().getName(), false)
                .addField("Nickname", (m.getNickname() == null ? "**_NO NICKNAME_**" : m.getNickname()), false)
                .addField("Created", u.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME), false)
                .addField("Joined", m.getJoinDate().format(DateTimeFormatter.RFC_1123_DATE_TIME), false)
                .addField("Online Status", m.getOnlineStatus().name(), false)
                .addField("Lib info", "JDA version: " + JDAInfo.VERSION + "\nLavaPlayer version: " + PlayerLibrary.VERSION, false)
                .addField("Github repo", "[https://github.com/duncte123/SkyBot](https://github.com/duncte123/SkyBot)", false);
        event.getChannel().sendMessage(eb.build()).queue();
    }

    @Override
    public String help() {
        return "Get's some info about the bot";
    }

    @Override
    public void executed(boolean safe, MessageReceivedEvent event) {
        return;
    }
}

package me.duncte123.skybot.commands;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;

/**
 * Created by Duncan on 9-7-2017.
 */
public class UserinfoCommand extends Command {

    User u;
    Member m;

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {

        u = event.getAuthor();
        m = event.getGuild().getMemberById(u.getId());

        if (args.length >= 1) {
            if(event.getMessage().getMentionedUsers().size() >= 1) {
                u = event.getMessage().getMentionedUsers().get(0);
                m = event.getGuild().getMemberById(u.getId());
            } else {
                try {
                    m = event.getGuild().getMembersByEffectiveName(StringUtils.join(args, " "), true).get(0);
                    u = m.getUser();
                }
                catch (Exception e) {
                    try{
                        m = event.getGuild().getMemberById(StringUtils.join(args, " "));
                        u = m.getUser();
                    }
                    catch (Exception ex) {
                        event.getChannel().sendMessage("This user could not be found.").queue();
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
      
        EmbedBuilder eb = AirUtils.defaultEmbed()
                .setColor(m.getColor())
                .setDescription("Userinfo for " + u.getName() + "#" + u.getDiscriminator())
                .setThumbnail(u.getEffectiveAvatarUrl())
                .addField("Username", u.getName(), true)
                .addField("Discriminator", u.getDiscriminator(), true)
                .addField("User Id", u.getId(), true)
                .addField("Playing", (m.getGame() == null ? "**_NOTING_**" : m.getGame().getName()), true)
                .addField("Nickname", (m.getNickname() == null ? "**_NO NICKNAME_**" : m.getNickname()), true)
                .addField("Created", u.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                .addField("Joined", m.getJoinDate().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                .addField("Online Status", m.getOnlineStatus().name(), true)
                .addField("Is a bot", u.isBot() + "", true)
                .addField("Is fake", u.isFake() + "", true);
        event.getChannel().sendMessage(eb.build()).queue();
    }

    @Override
    public String help() {
        return "Get the userinfo from yourself or from another user.";
    }
}

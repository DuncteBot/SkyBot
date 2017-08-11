package me.duncte123.skybot.commands;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by Duncan on 9-7-2017.
 */
public class UserinfoCommand extends Command {

    User u;
    Member m;

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {

        if (args.length == 0) {
            u = event.getAuthor();
            m = event.getGuild().getMemberById(u.getId());
        } else {
            List<User> mentioned = event.getMessage().getMentionedUsers();
            if (!mentioned.isEmpty()) {
                m = event.getGuild().getMember(mentioned.get(0));
            } else {
                String name = StringUtils.join(args, " ");

                List<Member> members = event.getGuild().getMembersByName(name, true);
                if (members.isEmpty()) {
                    members = event.getGuild().getMembersByNickname(name, true);
                }
                m = members.isEmpty() ? null : members.get(0);
            }
        }

        if (m == null) {
            event.getChannel().sendMessage("This user could not be found.").queue();
            return false;
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {

        u = m.getUser();
      
        EmbedBuilder eb = AirUtils.defaultEmbed()
                .setColor(m.getColor())
                .setDescription("Userinfo for " + u.getName() + "#" + u.getDiscriminator())
                .setThumbnail(u.getEffectiveAvatarUrl())
                .addField("Username + Discriminator", u.getName() + "#" + u.getDiscriminator(), true)
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

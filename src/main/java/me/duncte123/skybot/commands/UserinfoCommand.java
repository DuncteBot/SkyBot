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
                    m = event.getGuild().getMembersByName(StringUtils.join(args, " "), true).get(0);
                    u = m.getUser();
                }
                catch (Exception e) {
                    event.getChannel().sendMessage("This user could not be found.").queue();
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
      
        EmbedBuilder eb = AirUtils.defaultEmbed()
                .setDescription("Userinfor for " + u.getName() + "#" + u.getDiscriminator())
                .setThumbnail(u.getEffectiveAvatarUrl())
                .setImage(u.getEffectiveAvatarUrl())
                .addField("Username", u.getName(), false)
                .addField("Discriminator", u.getDiscriminator(), false)
                .addField("User Id", u.getId(), false)
                .addField("Playing", (m.getGame() == null ? "**_NOTING_**" : m.getGame().getName()), false)
                .addField("Nickname", (m.getNickname() == null ? "**_NO NICKNAME_**" : m.getNickname()), false)
                .addField("Created", u.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME), false)
                .addField("Joined", m.getJoinDate().format(DateTimeFormatter.RFC_1123_DATE_TIME), false)
                .addField("Online Status", m.getOnlineStatus().name(), false)
                .addField("Is a bot", u.isBot() + "", false)
                .addField("Is fake", u.isFake() + "", false);
        event.getChannel().sendMessage(eb.build()).queue();
    }

    @Override
    public String help() {
        return "Get the userinfo from yourself or from another user.";
    }
}

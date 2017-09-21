package ml.duncte123.skybot.commands.uncategorized;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created by Duncan on 9-7-2017.
 */
public class UserinfoCommand extends Command {

    /**
     * this is the user object
     */
    User u;
    /**
     * this is the member object
     */
    Member m;

    /**
     * This is the executeCommand of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

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
            return;
        }

        u = m.getUser();
      
        MessageEmbed eb = AirUtils.defaultEmbed()
                .setColor(m.getColor())
                .setDescription("Userinfo for " + u.getName() + "#" + u.getDiscriminator())
                .setThumbnail(u.getEffectiveAvatarUrl())
                .addField("Username + Discriminator", u.getName() + "#" + u.getDiscriminator(), true)
                .addField("User Id", u.getId(), true)
                .addField("Playing", (m.getGame() == null ? "**_nothing_**" : m.getGame().getName()), true)
                .addField("Nickname", (m.getNickname() == null ? "**_no nickname_**" : m.getNickname()), true)
                .addField("Created", u.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                .addField("Joined", m.getJoinDate().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                .addField("Online Status", AirUtils.convertStatus(m.getOnlineStatus()) + " "  + m.getOnlineStatus().name().toLowerCase(), true)
                .addField("Is a bot", (u.isBot() ? "Yep, this user is a bot" : "Nope, this user is not a bot") + "", true)
                .build();

        sendEmbed(eb, event);
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Get information from yourself or from another user.\nUsage: `"+ Config.prefix+getName()+" [username]`";
    }

    @Override
    public String getName() {
        return "userinfo";
    }
}

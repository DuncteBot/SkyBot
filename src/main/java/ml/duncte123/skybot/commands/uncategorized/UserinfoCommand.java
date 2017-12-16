/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.uncategorized;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Duncan on 9-7-2017.
 */
public class UserinfoCommand extends Command {
    
    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        User u;
        Member m;
        
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
        
        StringBuilder joinOrder = new StringBuilder();
        List<Member> joins = event.getGuild().getMemberCache().parallelStream().collect(Collectors.toList());
        joins.sort(Comparator.comparing(Member::getJoinDate));
        int index = joins.indexOf(m);
        index -= 3;
        if (index < 0)
            index = 0;
        joinOrder.append("\n");
        if (joins.get(index).equals(m))
            joinOrder.append("[").append(joins.get(index).getEffectiveName()).append("](https://bot.duncte123.me/)");
        else
            joinOrder.append(joins.get(index).getEffectiveName());
        for (int i = index + 1; i < index + 7; i++) {
            if (i >= joins.size())
                break;
            Member usr = joins.get(i);
            String name = usr.getEffectiveName();
            if (usr.equals(m))
                name = "[" + name + "](https://bot.duncte123.me/)";
            joinOrder.append(" > ").append(name);
        }
        
        //TODO: make request to discord profiles
        
        MessageEmbed eb = EmbedUtils.defaultEmbed()
                                  .setColor(m.getColor())
                                  .setDescription("User info for " + u.getName() + "#" + u.getDiscriminator())
                                  .setThumbnail(u.getEffectiveAvatarUrl())
                                  .addField("Username + Discriminator", String.format("%#s", u), true)
                                  .addField("User Id", u.getId(), true)
                                  .addField("Status", AirUtils.gameToString(m.getGame()), true)
                                  .addField("Nickname", (m.getNickname() == null ? "**_no nickname_**" : m.getNickname()), true)
                                  .addField("Created", u.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                                  .addField("Joined", m.getJoinDate().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                                  .addField("Join order", joinOrder.toString(), true)
                                  .addField("Online Status", AirUtils.convertStatus(m.getOnlineStatus()) + " " + m.getOnlineStatus().name().toLowerCase().replaceAll("_", " "), true)
                                  .addField("Is a bot", (u.isBot() ? "Yep, this user is a bot" : "Nope, this user is not a bot"), true)
                                  .build();
        
        sendEmbed(event, eb);
    }
    
    @Override
    public String help() {
        return "Get information from yourself or from another user.\nUsage: `" + Settings.prefix + getName() + " [username]`";
    }
    
    @Override
    public String getName() {
        return "userinfo";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"user", "i"};
    }
}

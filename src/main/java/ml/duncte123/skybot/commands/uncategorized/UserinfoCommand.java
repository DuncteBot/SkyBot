/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

        StringBuilder joinOrder = new StringBuilder();

        //event.getGuild().getMember(u).j
        List<Member> joins = event.getGuild().getMemberCache().stream().collect(Collectors.toList());
        //joins.sort((Member a, Member b) -> a.getJoinDate().compareTo(b.getJoinDate()));
        joins.sort( Comparator.comparing(Member::getJoinDate));
        int index = joins.indexOf(m);
        index-=3;
        if(index<0)
            index=0;
        joinOrder.append("\n"+"Join Order: ");
        if(joins.get(index).equals(m))
            joinOrder.append("[**").append(joins.get(index).getEffectiveName()).append("**](.)");
        else
            joinOrder.append(joins.get(index).getEffectiveName());
        for(int i=index+1;i<index+7;i++) {
            if(i>=joins.size())
                break;
            Member usr = joins.get(i);
            String name = usr.getEffectiveName();
            if(usr.equals(m))
                name="[**"+name+"**](.)";
            joinOrder.append(" > ").append(name);
        }
      
        MessageEmbed eb = EmbedUtils.defaultEmbed()
                .setColor(m.getColor())
                .setDescription("User info for " + u.getName() + "#" + u.getDiscriminator())
                .setThumbnail(u.getEffectiveAvatarUrl())
                .addField("Username + Discriminator", u.getName() + "#" + u.getDiscriminator(), true)
                .addField("User Id", u.getId(), true)
                .addField("Playing", (m.getGame() == null ? "**_nothing_**" : m.getGame().getName()), true)
                .addField("Nickname", (m.getNickname() == null ? "**_no nickname_**" : m.getNickname()), true)
                .addField("Created", u.getCreationTime().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                .addField("Joined", m.getJoinDate().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                .addField("Join order", joinOrder.toString(), true)
                .addField("Online Status", AirUtils.convertStatus(m.getOnlineStatus()) + " "  + m.getOnlineStatus().name().toLowerCase(), true)
                .addField("Is a bot", (u.isBot() ? "Yep, this user is a bot" : "Nope, this user is not a bot") + "", true)
                .build();

        sendEmbed(event, eb);
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        return "Get information from yourself or from another user.\nUsage: `"+ Settings.prefix+getName()+" [username]`";
    }

    @Override
    public String getName() {
        return "userinfo";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"user", "i"};
    }
}

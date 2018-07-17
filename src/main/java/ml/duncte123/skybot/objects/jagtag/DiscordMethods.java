/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.objects.jagtag;

import com.jagrosh.jagtag.Method;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ml.duncte123.skybot.utils.AirUtils.RAND;

public class DiscordMethods {

    public static Collection<Method> getMethods() {
        return Arrays.asList(
                new Method("user", (env) -> {
                    User u = env.get("user");
                    return u.getName();
                }),

                new Method("nick", (env) -> {
                    User u = env.get("user");
                    TextChannel tc = env.get("channel");
                    return tc.getGuild().getMember(u).getEffectiveName();
                }),

                new Method("discrim", (env) -> {
                    User u = env.get("user");
                    return u.getDiscriminator();
                }),

                new Method("avatar", (env) -> {
                    User u = env.get("user");
                    return u.getEffectiveAvatarUrl();
                }),

                new Method("userid", (env) -> {
                    User u = env.get("user");
                    return u.getId();
                }),

                new Method("atuser", (env) -> {
                    User u = env.get("user");
                    return u.getAsMention();
                }),

                new Method("server", (env) -> {
                    TextChannel tc = env.get("channel");
                    return tc.getGuild().getName();
                }),

                new Method("serverid", (env) -> {
                    TextChannel tc = env.get("channel");
                    return tc.getGuild().getId();
                }),

                new Method("servercount", (env) -> {
                    TextChannel tc = env.get("channel");
                    return String.valueOf(tc.getGuild().getMemberCache().size());
                }),

                new Method("servericon", (env) -> {
                    TextChannel tc = env.get("channel");
                    return tc.getGuild().getIconUrl();
                }),

                new Method("channel", (env) -> {
                    TextChannel tc = env.get("channel");
                    return tc.getAsMention();
                }),

                new Method("channelid", (env) -> {
                    TextChannel tc = env.get("channel");
                    return tc.getId();
                }),

                new Method("randuser", (env) -> {
                    TextChannel tc = env.get("channel");
                    List<Member> members = tc.getGuild().getMemberCache().asList();
                    Member m = members.get(RAND.nextInt(members.size()));
                    return m.getEffectiveName();
                }),

                new Method("randonline", (env) -> {
                    TextChannel tc = env.get("channel");
                    List<Member> members = tc.getGuild().getMemberCache().stream().filter( it -> it.getOnlineStatus()
                            .equals(OnlineStatus.ONLINE) ).collect(Collectors.toList());
                    Member m = members.get(RAND.nextInt(members.size()));
                    return m.getEffectiveName();
                }),

                new Method("randchannel", (env) -> {
                    TextChannel tc = env.get("channel");
                    List<TextChannel> channels = tc.getGuild().getTextChannelCache().asList();
                    return channels.get(RAND.nextInt(channels.size())).getAsMention();
                })
        );
    }

}

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
import net.dv8tion.jda.core.entities.*;

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
                    Guild guild = env.get("guild");
                    return guild.getName();
                }),

                new Method("serverid", (env) -> {
                    Guild guild = env.get("guild");
                    return guild.getId();
                }),

                new Method("servercount", (env) -> {
                    Guild guild = env.get("guild");
                    return String.valueOf(guild.getMemberCache().size());
                }),

                new Method("servericon", (env) -> {
                    Guild guild = env.get("guild");
                    return guild.getIconUrl();
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
                    Guild guild = env.get("guild");
                    List<Member> members = guild.getMemberCache().asList();
                    Member m = members.get(RAND.nextInt(members.size()));
                    return m.getEffectiveName();
                }),

                new Method("randatuser", (env) -> {
                    Guild guild = env.get("guild");
                    List<Member> members = guild.getMemberCache().asList();
                    Member m = members.get(RAND.nextInt(members.size()));
                    return m.getAsMention();
                }),

                new Method("randonline", (env) -> {
                    Guild guild = env.get("guild");
                    List<Member> members = guild.getMemberCache().stream().filter( it -> it.getOnlineStatus()
                            .equals(OnlineStatus.ONLINE) ).collect(Collectors.toList());
                    if (members.isEmpty()) {
                        return "";
                    }
                    if (members.size() == 1) {
                        return members.get(0).getEffectiveName();
                    }
                    Member m = members.get(RAND.nextInt(members.size()));
                    return m.getEffectiveName();
                }),

                new Method("randatonline", (env) -> {
                    Guild guild = env.get("guild");
                    List<Member> members = guild.getMemberCache().stream().filter( it -> it.getOnlineStatus()
                            .equals(OnlineStatus.ONLINE) ).collect(Collectors.toList());
                    if (members.isEmpty()) {
                        return "";
                    }
                    if (members.size() == 1) {
                        return members.get(0).getAsMention();
                    }
                    Member m = members.get(RAND.nextInt(members.size()));
                    return m.getAsMention();
                }),

                new Method("randchannel", (env) -> {
                    Guild guild = env.get("guild");
                    List<TextChannel> channels = guild.getTextChannelCache().asList();
                    if (channels.isEmpty()) {
                        return "";
                    }
                    if (channels.size() == 1) {
                        return channels.get(0).getAsMention();
                    }
                    return channels.get(RAND.nextInt(channels.size())).getAsMention();
                }),

                new Method("randemote", (env) -> {
                    Guild guild = env.get("guild");
                    List<Emote> emotes = guild.getEmoteCache().asList();
                    if (emotes.isEmpty()) {
                        return "";
                    }
                    if (emotes.size() == 1) {
                        return emotes.get(0).getAsMention();
                    }
                    return emotes.get(RAND.nextInt(emotes.size())).getAsMention();
                })
        );
    }

}

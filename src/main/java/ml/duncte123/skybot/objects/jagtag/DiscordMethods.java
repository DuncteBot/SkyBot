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

import com.jagrosh.jagtag.Environment;
import com.jagrosh.jagtag.Method;
import com.jagrosh.jagtag.ParseException;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.MiscUtil;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DiscordMethods {

    public static Collection<Method> getMethods() {
        return Arrays.asList(
                new Method("user", (env) -> {
                    User u = env.get("user");
                    return u.getName();
                }, (env, in) -> {
                    if (in[0].equals(""))
                        return "";
                    List<Member> members = null;
                    Guild g = env.get("guild");
                    if (g != null)
                        members = FinderUtil.findMembers(in[0], g);
                    if (members == null || members.isEmpty())
                        throw new ParseException(String.format("Your input `%s` returned no members", in[0]));
                    return members.get(0).getUser().getName();
                }),

                new Method("nick", (env) -> {
                    User u = env.get("user");
                    Guild g = env.get("guild");
                    return g.getMember(u).getEffectiveName();
                }, (env, in) -> {
                    if (in[0].equals(""))
                        return "";
                    List<Member> members = null;
                    Guild g = env.get("guild");
                    if (g != null)
                        members = FinderUtil.findMembers(in[0], g);
                    if (members == null || members.isEmpty())
                        throw new ParseException(String.format("Your input `%s` returned no members", in[0]));
                    return members.get(0).getEffectiveName();
                }),

                new Method("discrim", (env) -> {
                    User u = env.get("user");
                    return u.getDiscriminator();
                }, (env, in) -> {
                    if (in[0].equals(""))
                        return "";
                    List<Member> members = null;
                    Guild g = env.get("guild");
                    if (g != null)
                        members = FinderUtil.findMembers(in[0], g);
                    if (members == null || members.isEmpty())
                        throw new ParseException(String.format("Your input `%s` returned no members", in[0]));
                    return members.get(0).getUser().getDiscriminator();
                }),

                new Method("avatar", (env) -> {
                    User u = env.get("user");
                    return u.getEffectiveAvatarUrl() + "?size=2048";
                }, (env, in) -> {
                    if (in[0].equals(""))
                        return "";
                    List<Member> members = null;
                    Guild g = env.get("guild");
                    if (g != null)
                        members = FinderUtil.findMembers(in[0], g);
                    if (members == null || members.isEmpty())
                        throw new ParseException(String.format("Your input `%s` returned no members", in[0]));
                    return members.get(0).getUser().getEffectiveAvatarUrl() + "?size=2048";
                }),

                new Method("creation", (env, in) -> {
                    long id;
                    try {
                        id = Long.parseUnsignedLong(in[0]);
                    } catch (NumberFormatException ignored) {
                        throw new ParseException(String.format("Your input `%s` is not a valid long id", in[0]));
                    }
                    return MiscUtil.getCreationTime(id).format(DateTimeFormatter.RFC_1123_DATE_TIME);
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
                }, (env, in) -> {
                    if (in[0].equals(""))
                        return "";

                    return getFirstTextChannel(env, in).getAsMention();
                }),

                new Method("channelid", (env) -> {
                    TextChannel tc = env.get("channel");
                    return tc.getId();
                }, (env, in) -> {
                    if (in[0].equals(""))
                        return "";

                    return getFirstTextChannel(env, in).getId();
                }),

                new Method("randuser", (env) -> {
                    Guild guild = env.get("guild");
                    List<Member> members = guild.getMemberCache().asList();
                    int randNum = (int) Math.round(Math.random() * members.size()) + 1;
                    Member m = members.get(randNum);
                    return m.getEffectiveName();
                }),

                new Method("randatuser", (env) -> {
                    Guild guild = env.get("guild");
                    List<Member> members = guild.getMemberCache().asList();
                    int randNum = (int) Math.round(Math.random() * members.size()) + 1;
                    Member m = members.get(randNum);
                    return m.getAsMention();
                }),

                new Method("randonline", (env) -> {
                    Guild guild = env.get("guild");
                    List<Member> members = guild.getMemberCache().stream().filter(it -> it.getOnlineStatus()
                            .equals(OnlineStatus.ONLINE)).collect(Collectors.toList());
                    if (members.isEmpty()) {
                        return "";
                    }
                    if (members.size() == 1) {
                        return members.get(0).getEffectiveName();
                    }
                    int randNum = (int) Math.round(Math.random() * members.size()) + 1;
                    Member m = members.get(randNum);
                    return m.getEffectiveName();
                }),

                new Method("randatonline", (env) -> {
                    Guild guild = env.get("guild");
                    List<Member> members = guild.getMemberCache().stream().filter(it -> it.getOnlineStatus()
                            .equals(OnlineStatus.ONLINE)).collect(Collectors.toList());
                    if (members.isEmpty()) {
                        return "";
                    }
                    if (members.size() == 1) {
                        return members.get(0).getAsMention();
                    }
                    int randNum = (int) Math.round(Math.random() * members.size()) + 1;
                    Member m = members.get(randNum);
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
                    int randNum = (int) Math.round(Math.random() * channels.size()) + 1;
                    return channels.get(randNum).getAsMention();
                })
        );
    }

    @NotNull
    private static TextChannel getFirstTextChannel(Environment env, String[] in) throws ParseException {
        List<TextChannel> channels = null;
        Guild g = env.get("guild");
        if (g != null)
            channels = FinderUtil.findTextChannels(in[0], g);
        if (channels == null || channels.isEmpty())
            throw new ParseException(String.format("Your input `%s` returned no channels", in[0]));
        return channels.get(0);
    }
}

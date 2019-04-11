/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.MiscUtil;
import javax.annotation.Nonnull;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class DiscordMethods {

    public static Collection<Method> getMethods() {
        return List.of(
            new Method("user", (env) -> {
                final User u = env.get("user");

                return u.getName();
            },
                (env, in) -> getMemberFromInput(env, in).getUser().getName()
            ),

            new Method("nick", (env) -> {
                final User u = env.get("user");
                final Guild g = env.get("guild");

                if (g.getMember(u) == null) {
                    return u.getAsTag();
                }

                return g.getMember(u).getEffectiveName();
            },
                (env, in) -> getMemberFromInput(env, in).getEffectiveName()
            ),

            new Method("discrim", (env) -> {
                final User u = env.get("user");

                return u.getDiscriminator();
            },
                (env, in) -> getMemberFromInput(env, in).getUser().getDiscriminator()
            ),

            new Method("avatar", (env) -> {
                final User u = env.get("user");

                return u.getEffectiveAvatarUrl() + "?size=2048";
            },
                (env, in) -> getMemberFromInput(env, in).getUser().getEffectiveAvatarUrl() + "?size=2048"
            ),

            new Method("creation", (env, in) -> {
                long id;

                try {
                    id = Long.parseUnsignedLong(in[0]);
                }
                catch (NumberFormatException ignored) {
                    throw new ParseException(String.format("Your input `%s` is not a valid long id", in[0]));
                }

                return MiscUtil.getCreationTime(id).format(DateTimeFormatter.RFC_1123_DATE_TIME);
            }),

            new Method("userid", (env) -> {
                final User u = env.get("user");

                return u.getId();
            }),

            new Method("atuser", (env) -> {
                final User u = env.get("user");

                return u.getAsMention();
            }),

            new Method("server", (env) -> {
                final Guild guild = env.get("guild");

                return guild.getName();
            }),

            new Method("serverid", (env) -> {
                final Guild guild = env.get("guild");

                return guild.getId();
            }),

            new Method("servercount", (env) -> {
                final Guild guild = env.get("guild");

                return String.valueOf(guild.getMemberCache().size());
            }),

            new Method("servericon", (env) -> {
                final Guild guild = env.get("guild");

                return guild.getIconUrl();
            }),

            new Method("channel", (env) -> {
                final TextChannel tc = env.get("channel");

                if (tc == null) {
                    return "";
                }

                return tc.getAsMention();
            }, (env, in) -> {
                if (in[0].equals("")) {
                    return "";
                }

                return getFirstTextChannel(env, in).getAsMention();
            }),

            new Method("channelid", (env) -> {
                final TextChannel tc = env.get("channel");

                if (tc == null) {
                    return "";
                }

                return tc.getId();
            }, (env, in) -> {
                if (in[0].equals("")) {
                    return "";
                }

                return getFirstTextChannel(env, in).getId();
            }),

            new Method("randuser",
                (env) -> getRandomMember(env).getEffectiveName()
            ),

            new Method("randatuser",
                (env) -> getRandomMember(env).getAsMention()
            ),

            new Method("randonline",
                (env) -> getRandomOnlineMember(env).getEffectiveName()
            ),

            new Method("randatonline",
                (env) -> getRandomOnlineMember(env).getAsMention()
            ),

            new Method("randchannel", (env) -> {
                final Guild guild = env.get("guild");
                final List<TextChannel> channels = guild.getTextChannels();

                if (channels.size() == 1) {
                    return channels.get(0).getAsMention();
                }

                final int randNum = (int) Math.round(Math.random() * channels.size()) + 1;

                return channels.get(randNum).getAsMention();
            }),

            new Method("deleteinvoke", (env) -> {
                if (env.containsKey("messageId")) {

                    final TextChannel channel = env.get("channel");

                    if (channel != null) {
                        final String messageId = env.get("messageId");

                        channel.deleteMessageById(messageId).queue(null, (failure) -> {});
                    }
                }

                return "";
            }),

            new Method("addrole", (env, in) -> {
                final Guild guild = env.get("guild");

                final List<Role> foundRoles = FinderUtil.findRoles(in[0], guild);

                if (foundRoles.isEmpty()) {
                    throw new ParseException("No roles found for input");
                }

                final List<Member> foundMembers = FinderUtil.findMembers(in[1], guild);

                if (foundMembers.isEmpty()) {
                    throw new ParseException("No members found for input");
                }

                final Member selfMember = guild.getSelfMember();
                final Member targetMember = foundMembers.get(0);
                final Role targetRole = foundRoles.get(0);

                if (!selfMember.canInteract(targetMember) || !selfMember.canInteract(targetRole)) {
                    throw new ParseException("Cannot interact with target member or target role");
                }

                guild.getController().addSingleRoleToMember(targetMember, targetRole).queue();

                return "";
            }, "|user:")

            /*,

            new Method("embed", (env, input) -> {
                try {
                    final JSONObject jsonObject = new JSONObject(input[0]);
                    jsonObject.put("type", "rich");
                    env.put("embed", jsonObject);
                }
                catch (JSONException e) {
                    throw new ParseException("The embed input is not valid JSON");
                }
                return "";
            })*/
        );
    }

    @Nonnull
    private static TextChannel getFirstTextChannel(Environment env, String[] in) throws ParseException {
        final Guild g = env.get("guild");
        List<TextChannel> channels = null;

        if (g != null) {
            channels = FinderUtil.findTextChannels(in[0], g);
        }

        if (channels == null || channels.isEmpty()) {
            throw new ParseException(String.format("Your input `%s` returned no channels", in[0]));
        }

        return channels.get(0);
    }

    @Nonnull
    private static Member getMemberFromInput(Environment env, String[] in) throws ParseException {

        if (in[0].equals("")) {
            throw new ParseException("Input for member cannot be empty");
        }

        List<Member> members = null;
        final Guild g = env.get("guild");

        if (g != null) {
            members = FinderUtil.findMembers(in[0], g);
        }

        if (members == null || members.isEmpty()) {
            throw new ParseException(String.format("Your input `%s` returned no members", in[0]));
        }


        return members.get(0);
    }

    private static Member getRandomMember(Environment env) throws ParseException {
        return getRandomMember(env, (m) -> true);
    }

    private static Member getRandomOnlineMember(Environment environment) throws ParseException {
        return getRandomMember(environment, (member) -> member.getOnlineStatus() == OnlineStatus.ONLINE);
    }

    private static Member getRandomMember(Environment env, Predicate<? super Member> filter) throws ParseException {
        final Guild guild = env.get("guild");
        final List<Member> members = guild.getMemberCache().stream().filter(filter).collect(Collectors.toList());

        if (members.isEmpty()) {
            throw new ParseException("No members found");
        }

        if (members.size() == 1) {
            return members.get(0);
        }

        final int randNum = (int) Math.round(Math.random() * members.size()) + 1;

        return members.get(randNum);
    }
}

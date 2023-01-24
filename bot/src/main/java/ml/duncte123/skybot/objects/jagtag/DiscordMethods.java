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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.objects.jagtag;

import com.jagrosh.jagtag.Environment;
import com.jagrosh.jagtag.Method;
import com.jagrosh.jagtag.ParseException;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.utils.TimeUtil;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Nonnull;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS;
import static net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE;

@SuppressWarnings({"PMD.NPathComplexity", "PMD.ExcessiveMethodLength"})
public class DiscordMethods {
    private DiscordMethods() {}

    public static Collection<Method> getMethods() {
        return List.of(
            new Method("user", (env) -> {
                final User user = env.get("user");

                return user.getName();
            },
                (env, in) -> getMemberFromInput(env, in).getUser().getName()
            ),

            new Method("usertag", (env) -> {
                final User user = env.get("user");

                return user.getAsTag();
            },
                (env, in) -> getMemberFromInput(env, in).getUser().getAsTag()
            ),

            new Method("nick", (env) -> {
                final User user = env.get("user");
                final Guild guild = env.get("guild");
                final Member member = guild.getMember(user);

                if (member == null) {
                    return user.getAsTag();
                }

                return member.getEffectiveName();
            },
                (env, in) -> getMemberFromInput(env, in).getEffectiveName()
            ),

            new Method("discrim", (env) -> {
                final User user = env.get("user");

                return user.getDiscriminator();
            },
                (env, in) -> getMemberFromInput(env, in).getUser().getDiscriminator()
            ),

            new Method("avatar", (env) -> {
                final User user = env.get("user");

                return user.getEffectiveAvatarUrl() + "?size=4096";
            },
                (env, in) -> getMemberFromInput(env, in).getUser().getEffectiveAvatarUrl() + "?size=4096"
            ),

            new Method("creation", (env, in) -> {
                long idLong;

                try {
                    idLong = Long.parseUnsignedLong(in[0]);
                }
                catch (NumberFormatException e) {
                    throw new ParseException(String.format("Your input `%s` is not a valid long id", in[0]), e);
                }

                return TimeUtil.getTimeCreated(idLong).format(DateTimeFormatter.RFC_1123_DATE_TIME);
            }),

            new Method("userid", (env) -> {
                final User user = env.get("user");

                return user.getId();
            }),

            new Method("atuser", (env) -> {
                final User user = env.get("user");

                return user.getAsMention();
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

                return String.valueOf(guild.getMemberCount());
            }),

            new Method("servericon", (env) -> {
                final Guild guild = env.get("guild");

                return guild.getIconUrl();
            }),

            new Method("channel", (env) -> {
                final TextChannel channel = env.get("channel");

                if (channel == null) {
                    return "";
                }

                return channel.getAsMention();
            }, (env, in) -> {
                if ("".equals(in[0])) {
                    return "";
                }

                return getFirstTextChannel(env, in).getAsMention();
            }),

            new Method("channelid", (env) -> {
                final TextChannel channel = env.get("channel");

                if (channel == null) {
                    return "";
                }

                return channel.getId();
            }, (env, in) -> {
                if ("".equals(in[0])) {
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

                        channel.deleteMessageById(messageId)
                            .queue(null, new ErrorHandler().ignore(UNKNOWN_MESSAGE, MISSING_PERMISSIONS));
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

                guild.addRoleToMember(targetMember, targetRole).queue();

                return "";
            }, "|user:"),
            new Method("embed", (env, input) -> {
                try {

                    final DataObject jsonObject = DataObject.fromJson(input[0]);
                    jsonObject.put("type", "rich");
                    env.put("embed", jsonObject);
                }
                catch (ParsingException e) {
                    throw new ParseException("The embed input is not valid JSON", e);
                }
                return "";
            })
        );
    }

    @Nonnull
    private static TextChannel getFirstTextChannel(Environment env, String[] input) throws ParseException {
        final Guild guild = env.get("guild");
        List<TextChannel> channels = null;

        if (guild != null) {
            channels = FinderUtil.findTextChannels(input[0], guild);
        }

        if (channels == null || channels.isEmpty()) {
            throw new ParseException(String.format("Your input `%s` returned no channels", input[0]));
        }

        return channels.get(0);
    }

    @Nonnull
    private static Member getMemberFromInput(Environment env, String[] input) throws ParseException {

        if ("".equals(input[0])) {
            throw new ParseException("Input for member cannot be empty");
        }

        List<Member> members = null;
        final Guild guild = env.get("guild");

        if (guild != null) {
            members = FinderUtil.findMembers(input[0], guild);
        }

        if (members == null || members.isEmpty()) {
            throw new ParseException(String.format("Your input `%s` returned no members", input[0]));
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
        final List<Member> members = guild.getMemberCache().applyStream(
            (s) -> s.filter(filter).collect(Collectors.toList())
        );

        //noinspection ConstantConditions
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

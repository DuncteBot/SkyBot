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

package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.objects.command.Command;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.Guild.VerificationLevel;
import net.dv8tion.jda.core.utils.cache.MemberCacheView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class GuildUtils {

    private static Logger logger = LoggerFactory.getLogger(GuildUtils.class);

    /**
     * Returns an array with the member counts of the guild
     * 0 = the total users
     * 1 = the total bots
     * 2 = the total members
     *
     * @param g
     *         The {@link Guild Guild} to count the users in
     *
     * @return an array with the member counts of the guild
     * [0] = users
     * [1] = bots
     * [2] = total
     */
    public static long[] getBotAndUserCount(Guild g) {
        final MemberCacheView memberCache = g.getMemberCache();
        final long totalCount = memberCache.size();
        final long botCount = memberCache.stream().filter(it -> it.getUser().isBot()).count();
        final long userCount = totalCount - botCount;

        return new long[]{userCount, botCount, totalCount};
    }

    /**
     * This will calculate the bot to user ratio
     * 0 = users percentage
     * 1 = bot percentage
     *
     * @param g
     *         the {@link Guild} that we want to check
     *
     * @return the percentage of users and the percentage of bots in a nice compact array
     * [0] = users percentage
     * [1] = bot percentage
     */
    public static double[] getBotRatio(Guild g) {

        final long[] counts = getBotAndUserCount(g);
        final double totalCount = counts[2];
        final double userCount = counts[0];
        final double botCount = counts[1];

        //percent in users
        final double userCountP = (userCount / totalCount) * 100;

        //percent in bots
        final double botCountP = (botCount / totalCount) * 100;

        logger.debug("In the guild {}({} Members), {}% are users, {}% are bots",
            g.getName(),
            totalCount,
            userCountP,
            botCountP
        );

        return new double[]{Math.round(userCountP), Math.round(botCountP)};
    }

    /**
     * This counts the users in a guild that have an animated avatar
     *
     * @param g
     *         the guild to count it in
     *
     * @return the amount users that have a animated avatar
     */
    public static long countAnimatedAvatars(Guild g) {

        return g.getMemberCache().stream()
            .map(Member::getUser)
            .map(User::getAvatarId)
            .filter(Objects::nonNull)
            .filter(it -> it.startsWith("a_")).count();
    }

    /**
     * This will get the first channel of a guild that we can write in/should be able to write in
     *
     * @param guild
     *         The guild that we want to get the main channel from
     *
     * @return the Text channel that we can send our messages in.
     */
    public static TextChannel getPublicChannel(Guild guild) {

        final TextChannel pubChann = guild.getTextChannelCache().getElementById(guild.getId());

        if (pubChann == null || !pubChann.canTalk()) {

            return guild.getTextChannelCache().stream().filter(TextChannel::canTalk).findFirst().orElse(null);
        }

        return pubChann;
    }

    /**
     * This will convert the VerificationLevel from the guild to how it is displayed in the settings
     *
     * @param lvl
     *         The level to convert
     *
     * @return The converted verification level
     */
    // Null safety
    public static String verificationLvlToName(VerificationLevel lvl) {

        if (lvl == null) {
            return "None";
        }

        switch (lvl) {
            case LOW:
                return "Low";
            case MEDIUM:
                return "Medium";
            case HIGH:
                return "(╯°□°）╯︵ ┻━┻";
            case VERY_HIGH:
                return "┻━┻彡 ヽ(ಠ益ಠ)ノ彡┻━┻";
            default:
                return "None";
        }
    }

    public static int getMemberJoinPosition(Member member) {
        return member.getGuild().getMemberCache().stream().sorted(Comparator.comparing(Member::getJoinDate))
            .collect(Collectors.toList()).indexOf(member) + 1;
    }

    public static void reloadOneGuildPatrons(@Nonnull ShardManager manager, @Nonnull DatabaseAdapter adapter) {
        logger.info("(Re)loading one guild patrons");

        final Guild supportGuild = manager.getGuildById(Command.supportGuildId);
        final Role oneGuildRole = supportGuild.getRoleById(Command.oneGuildPatronsRole);

        adapter.loadOneGuildPatrons(
            (patrons) -> {
                patrons.forEachEntry((userId, guildId) -> {

                    final Member memberInServer = supportGuild.getMemberById(userId);

                    if (memberInServer != null && memberInServer.getRoles().contains(oneGuildRole)) {
                        Command.oneGuildPatrons.put(userId, guildId);
                    }

                    return true;
                });

                logger.info("Found {} one guild patrons", Command.oneGuildPatrons.keySet().size());

                return null;
            }
        );
    }

    public static void removeOneGuildPatron(long userId, @Nonnull DatabaseAdapter adapter) {
        adapter.removeOneGuildPatron(userId);
    }

    public static void addOneGuildPatron(long userId, long guildId, @Nonnull Variables variables) {
        variables.getDatabaseAdapter().addOneGuildPatrons(userId, guildId, (user, guild) -> {
            final SkyBot instance = SkyBot.getInstance();
            final Guild dbGuild = instance.getShardManager().getGuildById(Command.supportGuildId);

            if (dbGuild == null) {
                return null;
            }

            final Member newPatron = dbGuild.getMemberById(userId);

            if (newPatron == null) {
                return null;
            }

            final boolean hasRole = newPatron.getRoles().stream()
                .map(Role::getIdLong)
                .anyMatch((role) -> role == Command.oneGuildPatronsRole);

            if (hasRole) {
                Command.oneGuildPatrons.put(userId, guildId);
            }

            return null;
        });
    }
}

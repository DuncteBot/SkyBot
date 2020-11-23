/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.sentry.Sentry;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.objects.GuildMemberInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Guild.VerificationLevel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class GuildUtils {
    public static final Cache<Long, GuildMemberInfo> GUILD_MEMBER_COUNTS = Caffeine.newBuilder()
        .expireAfterAccess(5, TimeUnit.HOURS)
        .build();
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildUtils.class);

    private GuildUtils() {}

    /**
     * Returns an array with the member counts of the guild
     * 0 = the total users
     * 1 = the total bots
     * 2 = the total members
     *
     * @param guild
     *     The {@link Guild Guild} to count the users in
     *
     * @return an array with the member counts of the guild
     * [0] = users
     * [1] = bots
     * [2] = total
     */
    public static long[] getBotAndUserCount(Guild guild) {
        if (!GUILD_MEMBER_COUNTS.asMap().containsKey(guild.getIdLong())) {
            try {
                GUILD_MEMBER_COUNTS.put(guild.getIdLong(), GuildMemberInfo.init(guild));
            }
            catch (ExecutionException | InterruptedException e) {
                Sentry.capture(e);
                LOGGER.error("Failed to fetch member counts", e);
                // backup if we fail to fetch
                GUILD_MEMBER_COUNTS.put(guild.getIdLong(), new GuildMemberInfo());
            }
        }

        final GuildMemberInfo guildCount = GUILD_MEMBER_COUNTS.getIfPresent(guild.getIdLong());

        // This should never happen
        if (guildCount == null) {
            return new long[]{0L, 0L, 0L};
        }

        final long totalCount = guild.getMemberCount();

        return new long[]{guildCount.users, guildCount.bots, totalCount};
    }

    /**
     * This will calculate the bot to user ratio
     * 0 = users percentage
     * 1 = bot percentage
     *
     * @param guild
     *     the {@link Guild} that we want to check
     *
     * @return the percentage of users and the percentage of bots in a nice compact array
     * [0] = users percentage
     * [1] = bot percentage
     */
    public static double[] getBotRatio(Guild guild) {

        final long[] counts = getBotAndUserCount(guild);
        final double totalCount = counts[2];
        final double userCount = counts[0];
        final double botCount = counts[1];

        //percent in users
        final double userCountP = (userCount / totalCount) * 100;

        //percent in bots
        final double botCountP = (botCount / totalCount) * 100;

        LOGGER.debug("In the guild {}({} Members), {}% are users, {}% are bots",
            guild.getName(),
            totalCount,
            userCountP,
            botCountP
        );

        return new double[]{
            // https://stackoverflow.com/a/11701527
            Math.round(userCountP * 100.0) / 100.0,
            Math.round(botCountP * 100.0) / 100.0
        };
    }

    public static long getNitroUserCountCache(Guild guild) {
        final GuildMemberInfo guildCount = GUILD_MEMBER_COUNTS.getIfPresent(guild.getIdLong());

        // This should never happen
        if (guildCount == null) {
            return 0L;
        }

        return guildCount.nitroUsers;
    }

    public static long countAnimatedAvatars(List<Member> members) {
        return members.stream()
            .map(Member::getUser)
            .map(User::getAvatarId)
            .filter(Objects::nonNull)
            .filter(it -> it.startsWith("a_"))
            .count();
    }

    public static TextChannel getPublicChannel(Guild guild) {

        final TextChannel pubChann = guild.getTextChannelCache().getElementById(guild.getId());

        if (pubChann == null || !pubChann.canTalk()) {

            return guild.getTextChannelCache().applyStream(
                (s) -> s.filter(TextChannel::canTalk).findFirst().orElse(null)
            );
        }

        return pubChann;
    }

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

    /*public static long getMemberJoinPosition(Member member) {
        //noinspection ConstantConditions
        return member.getGuild().getMemberCache().applyStream(
            (s) -> s.sorted(Comparator.comparing(Member::getTimeJoined))
                .takeWhile((it) -> !it.equals(member))
                .count() + 1
        );
    }*/

    public static void loadAllPatrons(@Nonnull DatabaseAdapter adapter) {
        LOGGER.info("(Re)loading patrons");

        adapter.loadAllPatrons((data) -> {
            data.getPatrons().forEach(
                (patron) -> CommandUtils.PATRONS.add(patron.getUserId())
            );

            LOGGER.info("Loaded {} normal patrons", CommandUtils.PATRONS.size());

            data.getTagPatrons().forEach(
                (patron) -> CommandUtils.TAG_PATRONS.add(patron.getUserId())
            );

            LOGGER.info("Loaded {} tag patrons", CommandUtils.TAG_PATRONS.size());

            data.getOneGuildPatrons().forEach((patron) -> {
                final long userId = patron.getUserId();
                // The guild id is never null here, and if it is something is terribly wrong
                @SuppressWarnings("ConstantConditions") final long guildId = patron.getGuildId();

                CommandUtils.ONEGUILD_PATRONS.put(userId, guildId);
            });

            LOGGER.info("Loaded {} one guild patrons", CommandUtils.ONEGUILD_PATRONS.size());

            data.getGuildPatrons().forEach(
                (patron) -> CommandUtils.GUILD_PATRONS.add(patron.getUserId())
            );

            LOGGER.info("Loaded {} guild patrons", CommandUtils.GUILD_PATRONS.size());

            return null;
        });
    }
}

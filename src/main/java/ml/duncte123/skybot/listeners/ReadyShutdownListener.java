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

package ml.duncte123.skybot.listeners;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.CommandUtils;
import ml.duncte123.skybot.utils.GuildUtils;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ReadyShutdownListener extends MessageListener {
    private boolean unbanTimerRunning = false;
    private boolean isCacheCleanerActive = false;
    private boolean reminderCheckActive = false;
    private byte shardsReady = 0;

    public ReadyShutdownListener(Variables variables) {
        super(variables);
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof ReadyEvent) {
            this.onReady((ReadyEvent) event);
        } else if (event instanceof GuildMessageUpdateEvent) {
            this.onGuildMessageUpdate((GuildMessageUpdateEvent) event);
        } else if (event instanceof GuildMessageReceivedEvent) {
            this.onGuildMessageReceived((GuildMessageReceivedEvent) event);
        }
    }

    private void onReady(ReadyEvent event) {
        final JDA jda = event.getJDA();
        logger.info("Logged in as {} (Shard {})", jda.getSelfUser().getAsTag(), jda.getShardInfo().getShardId());

        //Start the timers if they have not been started yet
        if (!unbanTimerRunning) {
            logger.info("Starting the unban timer.");
            //Register the timer for the auto unbans
            systemPool.scheduleAtFixedRate(() -> ModerationUtils.checkUnbans(variables), 2, 2, TimeUnit.MINUTES);
            unbanTimerRunning = true;
        }

        if (!isCacheCleanerActive) {
            logger.info("Starting spam-cache-cleaner!");
            systemPool.scheduleAtFixedRate(spamFilter::clearMessages, 20, 13, TimeUnit.SECONDS);
            isCacheCleanerActive = true;
        }

        if (!reminderCheckActive) {
            systemPool.scheduleAtFixedRate(
                () -> variables.getDatabaseAdapter().getExpiredReminders((reminders) -> {
                    AirUtils.handleExpiredReminders(reminders, variables.getDatabaseAdapter(), variables.getPrettyTime());

                    return null;
                }), 2, 2, TimeUnit.MINUTES);
            reminderCheckActive = true;
        }

        shardsReady++;
        final ShardManager manager = jda.getShardManager();
        if (shardsReady == manager.getShardsTotal()) {

            loadPatrons(manager);
        }
    }

    private void loadPatrons(@Nonnull ShardManager manager) {
        logger.info("Collecting patrons");

        final Guild supportGuild = manager.getGuildById(Settings.SUPPORT_GUILD_ID);

        if (supportGuild == null) {
            logger.error("Could not find support guild");

            return;
        }

        supportGuild.getMembersWithRoles(supportGuild.getRoleById(Settings.PATRONS_ROLE))
            .stream()
            .map(Member::getUser)
            .map(User::getIdLong)
            .forEach(CommandUtils.patrons::add);

        logger.info("Found {} normal patrons", CommandUtils.patrons.size());

        final List<User> guildPatronsList = supportGuild.getMembersWithRoles(supportGuild.getRoleById(Settings.GUILD_PATRONS_ROLE))
            .stream().map(Member::getUser).collect(Collectors.toList());

        final TLongList patronGuildsTrove = new TLongArrayList();

        guildPatronsList.forEach((patron) -> {
            final List<Long> guilds = manager.getMutualGuilds(patron).stream()
                .filter((it) -> it.getOwner().equals(it.getMember(patron)) ||
                    it.getMember(patron).hasPermission(Permission.ADMINISTRATOR))
                .map(Guild::getIdLong)
                .collect(Collectors.toList());

            patronGuildsTrove.addAll(guilds);
        });

        CommandUtils.guildPatrons.addAll(patronGuildsTrove);

        logger.info("Found {} guild patrons", patronGuildsTrove.size());

        supportGuild.getMembersWithRoles(supportGuild.getRoleById(Settings.TAG_PATRONS_ROLE))
            .stream()
            .map(Member::getUser)
            .map(User::getIdLong)
            .forEach(CommandUtils.tagPatrons::add);

        logger.info("Found {} tag patrons", CommandUtils.tagPatrons.size());

        GuildUtils.reloadOneGuildPatrons(manager, variables.getDatabaseAdapter());
    }
}

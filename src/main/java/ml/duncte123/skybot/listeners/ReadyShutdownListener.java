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
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.MusicCommand;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.GuildUtils;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ReadyShutdownListener extends MessageListener {

    private final ScheduledExecutorService systemPool = Executors.newScheduledThreadPool(3,
        r -> new Thread(r, "Bot-Service-Thread"));
    private boolean unbanTimerRunning = false;
    private boolean isCacheCleanerActive = false;
    private short shardsReady = 0;

    @Override
    public void onReady(ReadyEvent event) {
        final JDA jda = event.getJDA();
        logger.info("Logged in as {} (Shard {})", jda.getSelfUser().getAsTag(), jda.getShardInfo().getShardId());

        //Start the timers if they have not been started yet
        if (!unbanTimerRunning) {
            logger.info("Starting the unban timer.");
            //Register the timer for the auto unbans
            systemPool.scheduleAtFixedRate(() -> ModerationUtils.checkUnbans(variables), 5, 5, TimeUnit.MINUTES);
            unbanTimerRunning = true;
        }

        if (!isCacheCleanerActive) {
            logger.info("Starting spam-cache-cleaner!");
            systemPool.scheduleAtFixedRate(spamFilter::clearMessages, 20, 13, TimeUnit.SECONDS);
            isCacheCleanerActive = true;
        }

        shardsReady++;
        final ShardManager manager = jda.asBot().getShardManager();
        if (shardsReady == manager.getShardsTotal()) {

            loadPatrons(manager);
        }
    }

    private void loadPatrons(@NotNull ShardManager manager) {
        logger.info("Collecting patrons");

        final Guild supportGuild = manager.getGuildById(Command.supportGuildId);

        final List<Long> patronsList = supportGuild.getMembersWithRoles(supportGuild.getRoleById(Command.patronsRole))
            .stream().map(Member::getUser).map(User::getIdLong).collect(Collectors.toList());

        Command.patrons.addAll(patronsList);

        logger.info("Found {} normal patrons", Command.patrons.size());

        final List<User> guildPatronsList = supportGuild.getMembersWithRoles(supportGuild.getRoleById(Command.guildPatronsRole))
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

        Command.guildPatrons.addAll(patronGuildsTrove);

        logger.info("Found {} guild patrons", patronGuildsTrove.size());

        GuildUtils.reloadOneGuildPatrons(manager, variables.getDatabaseAdapter());
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        if (!shuttingDown) {
            return;
        }

        MusicCommand.shutdown();

        //Kill other things
        //((EvalCommand) AirUtils.COMMAND_MANAGER.getCommand("eval")).shutdown();
        if (unbanTimerRunning && isCacheCleanerActive) {
            this.systemPool.shutdown();
        }

        AirUtils.stop(variables.getDatabase(), variables.getAudioUtils());
        variables.getCommandManager().commandThread.shutdown();

        /*
         * Only shut down if we are not updating
         */
        if (!isUpdating) {
            System.exit(0);
        }
    }
}

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

package ml.duncte123.skybot.listeners;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.utils.CommandUtils;
import ml.duncte123.skybot.utils.GuildUtils;
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
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.handle.SocketHandler;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ReadyShutdownListener extends MessageListener {
    private boolean arePoolsRunning = false;
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
        killEvents(jda);

        //Start the timers if they have not been started yet
        if (!arePoolsRunning) {
            logger.info("Starting spam-cache-cleaner!");
            systemPool.scheduleAtFixedRate(spamFilter::clearMessages, 20, 13, TimeUnit.SECONDS);

            if (!variables.useApi()) {
                this.startSQLiteTimers();
            }

            arePoolsRunning = true;
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
                .filter((it) -> it.getOwnerIdLong() == patron.getIdLong() ||
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

    //TODO: Remove when intends are added
    @Deprecated
    private void killEvents(JDA jda) {
        final JDAImpl api = (JDAImpl) jda;
        final SocketHandler.NOPHandler nopHandler = new SocketHandler.NOPHandler(api);
        final var handlers = api.getClient().getHandlers();

        handlers.put("TYPING_START", nopHandler);
        handlers.put("MESSAGE_REACTION_ADD", nopHandler);
    }

    private void startSQLiteTimers() {
        // This is ran on the systemPool to not hold the event thread from getting new events
        // Reflection is used because the class is removed at compile time
        systemPool.execute(() -> {
            try {
                // Get a new class instance or whatever you call this
                // Basically this is SQLiteTimers.class
                // A new instance would be new SQLiteTimers()
                final Class<?> aClass = Class.forName("ml.duncte123.skybot.database.SQLiteTimers");
                final Method[] methods = aClass.getDeclaredMethods();

                // Loop over all the methods that start with "start"
                for (final Method method : methods) {
                    if (!method.getName().startsWith("start")) {
                        continue;
                    }

                    // Invoke the method statically
                    method.invoke(null, variables);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
    }

    // might work some day
    /*private void customGame(JDA jda) {
        DataObject gameObj = DataObject.empty();
        gameObj.put("name", jda.getShardInfo().toString());
        gameObj.put("type", Activity.ActivityType.DEFAULT.getKey());

        DataObject object = DataObject.empty();

        object.put("game", gameObj);
        object.put("afk", false);
        object.put("status", OnlineStatus.ONLINE.getKey());
        object.put("since", System.currentTimeMillis());
        object.put("guild_id", "191245668617158656");
        object.put("client_status", DataObject.empty()
            .put("mobile", "online")
        );

        ((JDAImpl) jda).getClient().send(DataObject.empty()
            .put("d", object)
            .put("op", WebSocketCode.PRESENCE).toString());
    }*/
}

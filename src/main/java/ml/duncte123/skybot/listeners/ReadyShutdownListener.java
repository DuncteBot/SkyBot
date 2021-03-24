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

package ml.duncte123.skybot.listeners;

import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.utils.GuildUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReadyShutdownListener extends MessageListener {
    // Using an atomic boolean because multiple shards are writing to it
    private final AtomicBoolean arePoolsRunning = new AtomicBoolean(false);

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
        LOGGER.info("Logged in as {} (Shard {})", jda.getSelfUser().getAsTag(), jda.getShardInfo().getShardId());

        //Start the timers if they have not been started yet
        if (!arePoolsRunning.get()) {
            LOGGER.info("Starting spam-cache-cleaner!");
            systemPool.scheduleAtFixedRate(spamFilter::clearMessages, 20, 13, TimeUnit.SECONDS);

            // auto poster for guild info (post every day)
            //noinspection ConstantConditions
            systemPool.scheduleAtFixedRate(
                () -> variables.getApis().sendServerCountToLists(jda.getShardManager()),
                1,
                1,
                TimeUnit.DAYS
            );

            if (!variables.useApi()) {
                this.startSQLiteTimers();
            }

            arePoolsRunning.set(true);

            // Load the patrons here so that they are loaded once
            GuildUtils.loadAllPatrons(variables.getDatabaseAdapter());
        }
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
}

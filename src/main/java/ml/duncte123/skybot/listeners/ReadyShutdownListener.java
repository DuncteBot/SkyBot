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

import me.duncte123.botcommons.BotCommons;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.GuildUtils;
import ml.duncte123.skybot.web.WebSocketClient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReadyShutdownListener extends MessageListener {
    // Using an atomic boolean because multiple shards are writing to it
    private final AtomicBoolean arePoolsRunning = new AtomicBoolean(false);
    private final Thread shutdownHook;

    public ReadyShutdownListener(Variables variables) {
        super(variables);

        this.shutdownHook = new Thread(() -> {
            LOGGER.info("Shutting down via shutdown hook");
            SkyBot.getInstance().getShardManager().shutdown();
            this.onShutdown();
        });
        this.shutdownHook.setName("DuncteBot shutdown hook");

        Runtime.getRuntime().addShutdownHook(this.shutdownHook);
    }

    @Override
    public void onEvent(@Nonnull GenericEvent event) {
        if (event instanceof GuildMessageUpdateEvent messageUpdate) {
            this.onGuildMessageUpdate(messageUpdate);
        } else if (event instanceof GuildMessageReceivedEvent messageReceived) {
            this.onGuildMessageReceived(messageReceived);
        } else if (event instanceof GuildMessageDeleteEvent delete) {
            this.onGuildMessageDelete(delete);
        } else if (event instanceof MessageBulkDeleteEvent bulkDelete) {
            this.onMessageBulkDelete(bulkDelete);
        } else if (event instanceof ReadyEvent ready) {
            this.onReady(ready);
        } else if (event instanceof ShutdownEvent) {
            this.onShutdown();
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

            arePoolsRunning.set(true);

            // Load the patrons here so that they are loaded once
            GuildUtils.loadAllPatrons(variables.getDatabase());
        }
    }

    private void onShutdown() {
        if (!arePoolsRunning.get()) {
            return;
        }

        // little hack to make sure this method only gets called once
        arePoolsRunning.set(false);

        try {
            Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
        } catch (final Exception ignored) {}

        BotCommons.shutdown();

        this.redis.shutdown();
        LOGGER.info("Redis shutdown");

        AirUtils.stop(this.variables.getAudioUtils());
        LOGGER.info("Music system shutdown");

        // Kill all threads
        this.systemPool.shutdown();
        LOGGER.info("System pool shutdown");

        // kill the websocket
        final WebSocketClient client = SkyBot.getInstance().getWebsocketClient();

        if (client != null) {
            client.shutdown();
            LOGGER.info("Websocket client shutdown");
        }

        // shut down weeb.java
        this.variables.getWeebApi().shutdown();
        LOGGER.info("Weeb.java shutdown");
        LOGGER.info("Bot and JDA shutdown cleanly");
    }
}

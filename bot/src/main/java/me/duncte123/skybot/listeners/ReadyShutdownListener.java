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

package me.duncte123.skybot.listeners;

import me.duncte123.botcommons.BotCommons;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.Variables;
import me.duncte123.skybot.database.DataTimers;
import me.duncte123.skybot.utils.AirUtils;
import me.duncte123.skybot.utils.GuildUtils;
import me.duncte123.skybot.web.WebSocketClient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.duncte123.skybot.utils.ThreadUtils.runOnVirtual;

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
        switch (event) {
            case MessageUpdateEvent messageUpdate -> this.onGuildMessageUpdate(messageUpdate);
            case MessageReceivedEvent messageReceived -> this.onGuildMessageReceived(messageReceived);
            case SlashCommandInteractionEvent slashEvent -> this.onSlashCommandInteraction(slashEvent);
            case MessageDeleteEvent delete -> this.onGuildMessageDelete(delete);
            case MessageBulkDeleteEvent bulkDelete -> this.onMessageBulkDelete(bulkDelete);
            case ReadyEvent ready -> this.onReady(ready);
            case ShutdownEvent shutdownEvent -> this.onShutdown();
            default -> {
            }
        }
    }

    private void onReady(ReadyEvent event) {
        final JDA jda = event.getJDA();
        LOGGER.info("Logged in as {} (Shard {})", jda.getSelfUser().getAsTag(), jda.getShardInfo().getShardId());

        //Start the timers if they have not been started yet
        if (!arePoolsRunning.get()) {
            LOGGER.info("Starting spam-cache-cleaner!");
            SkyBot.SYSTEM_POOL.scheduleAtFixedRate(() -> runOnVirtual(spamFilter::clearMessages), 20, 13, TimeUnit.SECONDS);

            // Reset our activity every day
            SkyBot.SYSTEM_POOL.scheduleAtFixedRate(
                () -> jda.getShardManager().setActivityProvider(SkyBot.ACTIVITY_PROVIDER),
                1, 1, TimeUnit.DAYS
            );

            if (
                "psql".equals(this.variables.getConfig().useDatabase) ||
                    "mysql".equals(this.variables.getConfig().useDatabase)
            ) {
                DataTimers.startReminderTimer(this.variables, LOGGER);
                DataTimers.startWarningTimer(this.variables, LOGGER);
                DataTimers.startUnbanTimer(this.variables, LOGGER);
            }

            arePoolsRunning.set(true);

            // Load the patrons here so that they are loaded once
            GuildUtils.loadAllPatrons(variables.getDatabase());

            // Nice first attempt :)
            jda
                //.getGuildById(191245668617158656L)
                .updateCommands()
                .addCommands(this.commandManager.getAllSlashCommands())
                .queue();
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
        SkyBot.SYSTEM_POOL.shutdown();
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

        try {
            this.variables.getDatabase().close();
        } catch (Exception e) {
            LOGGER.error("Failed to close database", e);
        }

        LOGGER.info("Bot and JDA shutdown cleanly");
    }
}

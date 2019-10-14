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

package ml.duncte123.skybot;

import fredboat.audio.player.LavalinkManager;
import io.sentry.Sentry;
import me.duncte123.botcommons.text.TextColor;
import ml.duncte123.skybot.commands.mod.DeHoistListener;
import ml.duncte123.skybot.listeners.GuildListener;
import ml.duncte123.skybot.listeners.GuildMemberListener;
import ml.duncte123.skybot.listeners.ReadyShutdownListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.IEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken"),
    @Author(nickname = "ramidzkh", author = "Ramid Khan")
})
public class EventManager implements IEventManager {

    public static int restartingShard = -32; // -32 = none, -1 = all, id = id;
    public static boolean shouldFakeBlock = false;
    private static final Logger logger = LoggerFactory.getLogger(EventManager.class);
    private final ReactionHandler reactionHandler = new ReactionHandler();
    private final List<EventListener> listeners = new ArrayList<>();

    EventManager(Variables variables) {
        final GuildMemberListener guildMemberListener = new GuildMemberListener(variables);
        final GuildListener guildListener = new GuildListener(variables);
        final ReadyShutdownListener readyShutdownListener = new ReadyShutdownListener(variables); // Extends the message listener
        final DeHoistListener deHoistListener = new DeHoistListener(variables);
        final ShardWatcher shardWatcher = new ShardWatcher();

        this.listeners.add(guildMemberListener);
        this.listeners.add(guildListener);
        this.listeners.add(readyShutdownListener);
        this.listeners.add(deHoistListener);
        this.listeners.add(reactionHandler);
        this.listeners.add(shardWatcher);

        if (LavalinkManager.ins.isEnabled()) {
            this.listeners.add(LavalinkManager.ins.getLavalink());
        }
    }

    @Override
    public void register(@Nonnull Object listener) {
        throw new IllegalArgumentException();
    }

    @Override
    public void unregister(@Nonnull Object listener) {
        throw new IllegalArgumentException();
    }

    @Override
    public void handle(@Nonnull GenericEvent event) {
        final JDA.ShardInfo shardInfo = event.getJDA().getShardInfo();

        if (shouldFakeBlock) {
            //noinspection ConstantConditions
            if (shardInfo == null) {
                logger.warn(TextColor.RED + "Shard booting up (Event {})." + TextColor.RESET, event.getClass().getSimpleName());
                return;
            }

            if (restartingShard == -1 || restartingShard == shardInfo.getShardId()) {
                return;
            }
        }

        for (final EventListener listener : this.listeners) {
            try {
                listener.onEvent(event);
            }
            catch (Throwable thr) {
                Sentry.capture(thr);

                logger.error("Error while handling event {}({}); {}",
                    event.getClass().getName(),
                    listener.getClass().getSimpleName(),
                    thr.getLocalizedMessage());
                logger.error("", thr);
            }
        }
    }

    @Override
    @Nonnull
    public List<Object> getRegisteredListeners() {
        return Collections.unmodifiableList(this.listeners);
    }

    public ReactionHandler getReactionHandler() {
        return this.reactionHandler;
    }
}

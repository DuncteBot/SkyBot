/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import me.duncte123.botCommons.text.TextColor;
import ml.duncte123.skybot.commands.mod.DeHoistListener;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.IEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A single event listener container
 */
@Authors(authors = {
        @Author(nickname = "Sanduhr32", author = "Maurice R S"),
        @Author(nickname = "duncte123", author = "Duncan Sterken"),
        @Author(nickname = "ramidzkh", author = "Ramid Khan")
})
public class EventManager
        implements IEventManager {

    private static final Logger logger = LoggerFactory.getLogger(EventManager.class);
    public static int restartingShard = -32; // -32 = none, -1 = all, id = id;
    public static boolean shouldFakeBlock;
    private final BotListener botListener;
    private final DeHoistListener deHoistListener;
    private final ReactionHandler reactionHandler = new ReactionHandler();

    EventManager(Variables variables) {
        this.botListener = new BotListener(variables);
        this.deHoistListener = new DeHoistListener(variables);
    }


    @Override
    public void register(Object listener) {
        throw new IllegalArgumentException();
    }

    @Override
    public void unregister(Object listener) {
        throw new IllegalArgumentException();
    }

    @Override
    public void handle(Event event) {
        try {
            JDA.ShardInfo shardInfo = event.getJDA().getShardInfo();
            if (shouldFakeBlock) {
                if (shardInfo == null) {
                    logger.warn(TextColor.RED + "Shard booting up." + TextColor.RESET);
                    return;
                }
                if (restartingShard == -1 || restartingShard == shardInfo.getShardId())
                    return;
            }

            for (EventListener listener : getRegisteredListenersClass()) {
                listener.onEvent(event);
            }

        } catch (Throwable thr) {
            logger.error("Error while handling event " + event.getClass().getName() + "; " + thr.getLocalizedMessage(), thr);
        }
    }

    @Override
    public List<Object> getRegisteredListeners() {
        return getRegisteredListenersClass()
                .stream().map(it -> (Object) it).collect(Collectors.toList());
    }

    /**
     * Returns our reaction handler
     * @return our reaction handler
     */
    public ReactionHandler getReactionHandler() {
        return reactionHandler;
    }

    private List<EventListener> getRegisteredListenersClass() {
        if (LavalinkManager.ins.isEnabled())
            return Arrays.asList(LavalinkManager.ins.getLavalink(), botListener, deHoistListener, reactionHandler);
        else
            return Arrays.asList(botListener, deHoistListener, reactionHandler);
    }

}

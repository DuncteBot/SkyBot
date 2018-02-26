/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import ml.duncte123.skybot.commands.mod.DeHoistListener;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.IEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * A single event listener container
 */
public class EventManager
implements IEventManager {

    private static final Logger logger = LoggerFactory.getLogger(EventManager.class);

    private final BotListener botListener = new BotListener();
    private final DeHoistListener deHoistListener = new DeHoistListener();
    
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
            if(LavalinkManager.ins.isEnabled())
                LavalinkManager.ins.getLavalink().onEvent(event);
            botListener.onEvent(event);
            deHoistListener.onEvent(event);
        } catch (Throwable thr) {
            logger.warn("Error while handling event " + event.getClass().getName() + "; " + thr.getLocalizedMessage(), thr);
        }
    }
    
    @Override
    public List<Object> getRegisteredListeners() {
        if(LavalinkManager.ins.isEnabled())
            return Arrays.asList(LavalinkManager.ins.getLavalink(), botListener, deHoistListener);
        else
            return Arrays.asList(botListener, deHoistListener);
    }
}

package ml.duncte123.skybot;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.IEventManager;

import java.util.Arrays;
import java.util.List;

/**
 * A single event listener container
 */
class EventManager
implements IEventManager {

    private BotListener botListener = new BotListener();
    
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
        botListener.onEvent(event);
    }
    
    @Override
    public List<Object> getRegisteredListeners() {
        return Arrays.asList(botListener);
    }
}

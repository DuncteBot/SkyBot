package ml.duncte123.skybot;

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
        try {
            botListener.onEvent(event);
        } catch (Throwable thr) {
            logger.warn("Error while handling event " + event + "; " + thr.getLocalizedMessage(), thr);
        }
    }
    
    @Override
    public List<Object> getRegisteredListeners() {
        return Arrays.asList(botListener);
    }
}

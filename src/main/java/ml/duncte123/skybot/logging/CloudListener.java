package ml.duncte123.skybot.logging;

import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.slf4j.event.Level;

public class CloudListener implements SimpleLog.LogListener{
    @Override
    public void onLog(SimpleLog log, SimpleLog.Level logLevel, Object message) {
        if(logLevel.getPriority() > 2) {
            AirUtils.log(log.name, Level.valueOf(logLevel.name()), message);
        }
    }

    @Override
    public void onError(SimpleLog log, Throwable err) {
        AirUtils.logger.error(err.getMessage(), err);
    }
}

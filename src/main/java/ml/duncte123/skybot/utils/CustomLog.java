package ml.duncte123.skybot.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.*;


    /**
     *
     * All the credits for this file go to the developers of the jda lib (https://github.com/DV8FromTheWorld/JDA)
     *
     */
public class CustomLog {
    /**
     * The global LOG-level that is used as standard if not overwritten
     */
    public static Level LEVEL = Level.INFO;

    /**
     * If this boolean is set to true, if there is no console present, messages are shown as message-dialogs
     */
    public static boolean ENABLE_GUI = false;

    private static final String FORMAT = "[%time%] [%level%] [%name%]: %text%";
    private static final String MSGFORMAT = "%text%";
    private static final SimpleDateFormat DFORMAT = new SimpleDateFormat("dd-mm-yyyy HH:mm:ss");

    private static final Map<String, CustomLog> LOGS = new HashMap<>();
    private static final Set<LogListener> listeners = new HashSet<>();

    /**
     * Will get the LOG with the given LOG-name or create one if it didn't exist
     *
     * @param name the name of the LOG
     * @return CustomLog with given LOG-name
     */
    public static CustomLog getLog(String name) {
        synchronized (LOGS) {
            if(!LOGS.containsKey(name.toLowerCase())) {
                LOGS.put(name.toLowerCase(), new CustomLog(name));
            }
        }
        return LOGS.get(name.toLowerCase());
    }

    public final String name;
    private Level level = null;

    private CustomLog(String name) {
        this.name = name;
    }

    /**
     * Set the LOG-level
     * All messages with lower LOG-level will not be printed
     * If this level is set to null, the global Log-level ({@link net.dv8tion.jda.core.utils.SimpleLog#LEVEL}) will be used
     *
     * @param lev the new LOG-level
     */
    public void setLevel(Level lev) {
        this.level = lev;
    }

    /**
     * Gets the current logging-level of this Logger.
     * This might return null, if the global logging-level is used.
     *
     * @return the logging-level of this Logger or null
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Gets the effective logging-level of this Logger.
     * This considers the global logging-level.
     *
     * @return the effective logging-level of this Logger
     */
    public Level getEffectiveLevel() {
        return level == null ? CustomLog.LEVEL : level;
    }

    /**
     * Will LOG a message with given LOG-level
     *
     * @param level The level of the Log
     * @param msg   The message to LOG
     */
    public void log(Level level, Object msg) {
        synchronized (listeners) {
            for (LogListener listener : listeners) {
                listener.onLog(this, level, msg);
            }
        }
        String format = (ENABLE_GUI && !isConsolePresent()) ? MSGFORMAT : FORMAT;
        format = format.replace("%time%", DFORMAT.format(new Date())).replace("%level%", level.getTag()).replace("%name%", name).replace("%text%", String.valueOf(msg));
        if(level == CustomLog.Level.OFF || level.getPriority() < ((this.level == null) ? CustomLog.LEVEL.getPriority() : this.level.getPriority())) {
            //logToFiles(format, level);
        }
        else {
            print(format, level);
        }
    }

    public void log(Throwable ex) {
        synchronized (listeners) {
            for (LogListener listener : listeners) {
                listener.onError(this, ex);
            }
        }
        log(Level.FATAL, "Encountered an exception:");
        log(Level.FATAL, ExceptionUtils.getStackTrace(ex));
    }

    /**
     * Will LOG a message with trace level.
     *
     * @param msg the object, which should be logged
     */
    public void trace(Object msg) {
        log(Level.TRACE, msg);
    }

    /**
     * Will LOG a message with debug level
     *
     * @param msg the object, which should be logged
     */
    public void debug(Object msg) {
        log(Level.DEBUG, msg);
    }

    /**
     * Will LOG a message with info level
     *
     * @param msg the object, which should be logged
     */
    public void info(Object msg) {
        log(Level.INFO, msg);
    }

    /**
     * Will LOG a message with warning level
     *
     * @param msg the object, which should be logged
     */
    public void warn(Object msg) {
        log(Level.WARNING, msg);
    }

    /**
     * Will LOG a message with fatal level
     *
     * @param msg the object, which should be logged
     */
    public void fatal(Object msg) {
        log(Level.FATAL, msg);
    }

    /**
     * prints a message to the console or as message-box.
     *
     * @param msg   the message, that should be displayed
     * @param level the LOG level of the message
     */
    private void print(String msg, Level level) {
        if(ENABLE_GUI && !isConsolePresent()) {
            if(level.isError()) {
                JOptionPane.showMessageDialog(null, msg, "An Error occurred!", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, msg, level.getTag(), JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            if(level.isError()) {
                System.err.println(msg);
            } else {
                System.out.println(msg);
            }
        }
    }

    /**
     * Will return whether the program has a console present, or was launched without
     *
     * @return boolean true, if console is present
     */
    public static boolean isConsolePresent() {
        return System.console() != null;
    }

    /**
     * This interface has to be able to register (via {@link net.dv8tion.jda.core.utils.SimpleLog#addListener(net.dv8tion.jda.core.utils.SimpleLog.LogListener)}) and listen to log-messages.
     */
    public interface LogListener {
        /**
         * Called on any incoming log-messages (including stacktraces).
         * This is also called on log-messages that would normally not print to console due to log-level.
         * @param log
         *      the log this message was sent to
         * @param logLevel
         *      the level of the message sent
         * @param message
         *      the message as object
         */
        void onLog(CustomLog log, Level logLevel, Object message);

        /**
         * Called on any incoming error-message (Throwable).
         * Note: Throwables are also logged with FATAL level.
         * This is just a convenience Method to do special Throwable handling.
         * @param log
         *      the log this error was sent to
         * @param err
         *      the error as Throwable
         */
        void onError(CustomLog log, Throwable err);
    }

    /**
     * Enum containing all the LOG-levels
     */
    public enum Level {
        ALL("Finest", 0, false),
        TRACE("Trace", 1, false),
        DEBUG("Debug", 2, false),
        INFO("Info", 3, false),
        WARNING("Warning", 4, true),
        FATAL("Fatal", 5, true),
        OFF("NO-LOGGING", 6, true);

        private final String msg;
        private final int pri;
        private final boolean isError;

        Level(String message, int priority, boolean isError) {
            this.msg = message;
            this.pri = priority;
            this.isError = isError;
        }

        /**
         * Returns the Log-Tag (e.g. Fatal)
         *
         * @return the logTag
         */
        public String getTag() {
            return msg;
        }

        /**
         * Returns the numeric priority of this loglevel, with 0 being the lowest
         *
         * @return the level-priority
         */
        public int getPriority() {
            return pri;
        }

        /**
         * Returns whether this LOG-level should be treated like an error or not
         *
         * @return boolean true, if this LOG-level is an error-level
         */
        public boolean isError() {
            return isError;
        }
    }
}
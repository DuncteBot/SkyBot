package ml.duncte123.skybot.parsers;

import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;


public class CommandParser {

    /**
     * This will split an command into the command and the args
     * @param rw the raw text
     * @param e An instance of the {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     * @return Our {@link ml.duncte123.skybot.parsers.CommandParser.CommandContainer CommandContainer}
     */
    public CommandContainer parse(String rw, GuildMessageReceivedEvent e){
        final String[] split = rw.replaceFirst(Settings.prefix, "").split("\\s+");
        final String invoke = split[0].toLowerCase();
        final String[] args = Arrays.copyOfRange(split, 1, split.length);

        return new CommandContainer(invoke, args, e);
    }

     public class CommandContainer {
         public final String invoke;
         public final String[] args;
         public final GuildMessageReceivedEvent event;

         /**
          * Puts the contents of a command in a simple class
          * @param invoke The command that is ran
          * @param args The arguments from the command
          * @param e A ninstance of the {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
          */
         public CommandContainer(String invoke, String[] args, GuildMessageReceivedEvent e){
             this.invoke = invoke;
             this.args = args;
             this.event = e;
         }
     }

}

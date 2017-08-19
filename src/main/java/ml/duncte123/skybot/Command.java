package ml.duncte123.skybot;

import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public abstract class Command {

    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    public abstract boolean called(String[] args, GuildMessageReceivedEvent event);

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     */
    public abstract void action(String[] args, GuildMessageReceivedEvent event);

    /**
     * The usage instructions of the command
     * @return a String
     */
    public abstract String help();

    /**
     * This is always ran after the command is finished
     * @param success what {@link Command#executed(boolean, GuildMessageReceivedEvent)} returned
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     */
    public void executed(boolean success, GuildMessageReceivedEvent event) {  }

}

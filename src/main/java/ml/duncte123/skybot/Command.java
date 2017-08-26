package ml.duncte123.skybot;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public abstract class Command {

    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    public abstract boolean called(String[] args, GuildMessageReceivedEvent event);

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
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
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    public void executed(boolean success, GuildMessageReceivedEvent event) {  }

    /**
     * This is a shortcut for sending messages to a channel
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg the message to send
     */
    public void sendMsg(GuildMessageReceivedEvent event, String msg) {
       sendMsg(event, (new MessageBuilder()).append(msg).build());
    }

    /**
     * This is a shortcut for sending messages to a channel
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg the message to send
     */
    public void sendMsg(GuildMessageReceivedEvent event, MessageEmbed msg) {
        sendMsg(event, (new MessageBuilder()).setEmbed(msg).build());
    }

    /**
     * This is a shortcut for sending messages to a channel
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg the message to send
     */
    public void sendMsg(GuildMessageReceivedEvent event, Message msg) {
        event.getChannel().sendMessage(msg).queue();
    }

}

package ml.duncte123.skybot.commands;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CoinCommand extends Command {

    public final static String help = "flips a coin.";
    private final String coinUrl = "https://dshelmondgames.ml/img/coin/";
    private final String[] imagesArr = { "heads.png", "tails.png" };
    /**
     * This is a check to see if the command is save to execute
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     * @return true if we are the command is safe to run
     */
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * This is the action of the command, the thing you want the command to to needs to be in here
     * @param args The command agruments
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     */
    @Override
    public void action(String[] args, MessageReceivedEvent event) {

        event.getTextChannel().sendTyping().queue();
        event.getTextChannel().sendMessage("*Flips a coin*").queue();
        event.getTextChannel().sendMessage(AirUtils.embedImage(coinUrl+imagesArr[SkyBot.rand.nextInt(2)])).queue();
    }

    /**
     * The usage instructions of the command
     * @return a String
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }
}

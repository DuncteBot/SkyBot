package me.duncte123.skybot.commands;

import me.duncte123.skybot.Command;
import me.duncte123.skybot.SkyBot;
import me.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CoinCommand implements Command {

    public final static String help = "flips a coin.";
    private final String coinUrl = "https://dshelmondgames.ml/img/coin/";
    private final String[] imagesArr = { "heads.png", "tails.png" };

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {

        event.getTextChannel().sendTyping();
        event.getTextChannel().sendTyping();
        event.getTextChannel().sendMessage("*Flips a coin*").queue();
        event.getTextChannel().sendMessage(AirUtils.embedImage(coinUrl+imagesArr[SkyBot.rand.nextInt(2)])).queue();
    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return;

    }

}

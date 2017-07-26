package me.duncte123.skybot.utils;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.time.Instant;

public class Functions {

    public static MessageEmbed embedMessage(String message) {
        return defaultEmbed().setDescription(message).build();
    }

    public static MessageEmbed embedField(String title, String message){
        return defaultEmbed().addField(title, message, false).build();
    }

    public static MessageEmbed embedImage(String imageURL) {
        return defaultEmbed().setImage(imageURL).build();
    }

    public static EmbedBuilder defaultEmbed(){
        return new EmbedBuilder()
                .setColor(Config.defaultColour)
                .setFooter(Config.defaultName, Config.defaultIcon)
                .setTimestamp(Instant.now());
    }

    public static void modLog(User mod, User punishedUser, String punishment, String reason, String time, MessageReceivedEvent event){
        String length = "";
        if (!time.isEmpty()) { length = " lasting " + time + " "; }
        String punishedUserMention = "<@" + punishedUser.getId() + ">";
        MessageChannel modLogChannel = event.getGuild().getTextChannelsByName("modlog", true).get(0);
        modLogChannel.sendMessage(embedField(punishedUser.getName() + punishment, punishment
                + " by " + mod.getName() + length + " for " + reason)).queue(
                        msg -> msg.getTextChannel().sendMessage("_Relevant user: " + punishedUserMention + "_").queue()
        );
    }

    public static void modLog(User mod, User punishedUser, String punishment, String reason, MessageReceivedEvent event) {
        modLog(mod, punishedUser, punishment, reason, "", event);
    }
}

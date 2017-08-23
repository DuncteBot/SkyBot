package ml.duncte123.skybot.commands.music;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.utils.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.time.Instant;

public class LeaveCommand extends Command {

    public final static String help = "make the bot leave your channel.";

    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        boolean botInChannel = false;

        if(event.getGuild().getAudioManager().isConnected()){
            botInChannel = true;

            if(!event.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(event.getMember())){
                EmbedBuilder eb = new EmbedBuilder()
                        .setColor(Config.defaultColour)
                .addField(SkyBot.au.embedTitle, "I'm sorry, but you have to be in the same channel as me to use any music related commands", false);
                eb.setFooter(Config.defaultName, Config.defaultIcon)
                .setTimestamp(Instant.now());
                event.getChannel().sendMessage(eb.build()).queue();
                return false;
            }

        }else{
            EmbedBuilder eb = new EmbedBuilder()
                    .setColor(Config.defaultColour)
                    .addField(SkyBot.au.embedTitle, "I'm not in a channel atm", false)
                    .setFooter(Config.defaultName, Config.defaultIcon)
                    .setTimestamp(Instant.now());
            event.getChannel().sendMessage(eb.build()).queue();
        }


        return botInChannel;
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        SkyBot.au.getMusicManager(event.getGuild()).player.stopTrack();
        event.getGuild().getAudioManager().setSendingHandler(null);
        event.getGuild().getAudioManager().closeAudioConnection();
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(Config.defaultColour)
                .addField(SkyBot.au.embedTitle, "Leaving your channel", false)
                .setFooter(Config.defaultName, Config.defaultIcon)
                .setTimestamp(Instant.now());
        event.getChannel().sendMessage(eb.build()).queue();

    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }
}

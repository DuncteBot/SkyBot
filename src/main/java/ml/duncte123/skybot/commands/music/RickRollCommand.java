package ml.duncte123.skybot.commands.music;

import ml.duncte123.skybot.Command;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.utils.AudioUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class RickRollCommand extends Command {

    @Override
    public boolean called(String[] args, GuildMessageReceivedEvent event) {
        // TODO Auto-generated method stub
        return event.getGuild().getAudioManager().isConnected();
    }

    @Override
    public void action(String[] args, GuildMessageReceivedEvent event) {
        AudioUtils au = SkyBot.au;
        GuildMusicManager mng = au.getMusicManager(event.getGuild());

        au.loadAndPlay(mng, event.getChannel(), "/root/Desktop/music/rick.mp3", false);

    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "";
    }

}

/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.musicJava;

import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.audio.TrackScheduler;
import ml.duncte123.skybot.objects.command.MusicCommand;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;

public class RepeatCommand extends MusicCommand {

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        Guild guild = event.getGuild();
        GuildMusicManager musicManager = getMusicManager(guild);
        AudioManager audioManager = getAudioManager(guild);
        TrackScheduler scheduler = musicManager.scheduler;

        if(!audioManager.isConnected()){
            sendMsg(event, "I'm not in a voice channel, use `"+ Settings.prefix+"join` to make me join a channel");
            return;
        }

        if(!audioManager.getConnectedChannel().getMembers().contains(event.getMember())){
            sendMsg(event, "I'm sorry, but you have to be in the same channel as me to use any music related commands");
            return;
        }

        scheduler.setRepeating(!scheduler.isRepeating());

        sendMsg(event, "Player was set to: **" + (scheduler.isRepeating() ? "repeat" : "not repeat") + "**");

    }

    @Override
    public String help() {
        // TODO Auto-generated method stub
        return "Makes the player repeat the currently playing song";
    }

    @Override
    public String getName() {
        return "repeat";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"loop"};
    }
}

/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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
 *
 */

package ml.duncte123.skybot.objects.command;

import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;

public abstract class MusicCommand extends Command {

    public MusicCommand() {
        this.category = CommandCategory.MUSIC;
    }

    /**
     * Returns the autio utils
     * @return the audio utils
     */
    protected AudioUtils getAu() {
        return AirUtils.audioUtils;
    }

    /**
     * This is a shortcut for getting the music manager
     * @param guild the guild to get the music manager for
     * @return the {@link GuildMusicManager GuildMusicManager} for that guild
     */
    protected GuildMusicManager getMusicManager(Guild guild) {
        return AirUtils.audioUtils.getMusicManager(guild);
    }

    /**
     * This is a shortcut for getting the audio manager
     * @param guild the guild to get the audio manager for
     * @return the {@link net.dv8tion.jda.core.managers.AudioManager AudioManager} from the guild
     */
    protected AudioManager getAudioManager(Guild guild) {
        return guild.getAudioManager();
    }

    /**
     * This performs some checks that we need for the music
     * @param event The current {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if the checks pass
     */
    protected boolean channelChecks(GuildMessageReceivedEvent event) {
        AudioManager audioManager = getAudioManager(event.getGuild());

        if(!audioManager.isConnected()){
            sendMsg(event, "I'm not in a voice channel, use `"+ Settings.prefix+"join` to make me join a channel");
            return false;
        }

        if(!audioManager.getConnectedChannel().getMembers().contains(event.getMember())){
            sendMsg(event, "I'm sorry, but you have to be in the same channel as me to use any music related commands");
            return false;
        }
        return true;
    }

}

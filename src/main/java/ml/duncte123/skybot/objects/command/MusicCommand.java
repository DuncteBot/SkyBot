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

package ml.duncte123.skybot.objects.command;

import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.AudioUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.managers.AudioManager;

public abstract class MusicCommand extends Command {

    /**
     * This holds the audio utils class
     */
    protected AudioUtils au = AirUtils.audioUtils;

    /**
     * This is a shortcut for getting the music manager
     * @param guild the guild to get the music manager for
     * @return the {@link ml.duncte123.skybot.audio.GuildMusicManager GuildMusicManager} for that guild
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

}

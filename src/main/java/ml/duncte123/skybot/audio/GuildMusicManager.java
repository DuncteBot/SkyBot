/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.audio;

import lavalink.client.io.Link;
import lavalink.client.player.IPlayer;

public class GuildMusicManager {

    /**
     * This is our player
     */
    public final IPlayer player;

    /**
     * This is the scheduler
     */
    public final TrackScheduler scheduler;

    /**
     * This is what actually sends the audio
     */
    public final AudioPlayerSenderHandler sendHandler;

    /**
     * Constructor
     *
     * @param manager The {@link com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager AudioPlayerManager} for the corresponding guild
     */
    public GuildMusicManager(Link manager) {
        player = manager.getPlayer();
        scheduler = new TrackScheduler(player, this);
        sendHandler = new AudioPlayerSenderHandler(player);
        player.addListener(scheduler);
    }

    /**
     * This will get our sendings handler
     *
     * @return The {@link AudioPlayerSenderHandler thing} that sends our audio
     */
    public AudioPlayerSenderHandler getSendHandler() {
        return sendHandler;
    }
}

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

import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavaplayerPlayerWrapper;
import net.dv8tion.jda.core.audio.AudioSendHandler;

public class AudioPlayerSenderHandler implements AudioSendHandler {

    /**
     * This is our audio player
     */
    private final IPlayer audioPlayer;

    /**
     * I don't know what this does but it seems important
     */
    private AudioFrame lastFrame;

    public AudioPlayerSenderHandler(IPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    /**
     * Checks if the player can provide the song
     *
     * @return true if we can provide something
     */
    @Override
    public boolean canProvide() {
        System.out.println("Handler: before wrapper");
        LavaplayerPlayerWrapper lavaplayerPlayer = (LavaplayerPlayerWrapper) audioPlayer;
        System.out.println("Handler: after wrapper");
        if (lastFrame == null) {
            lastFrame = lavaplayerPlayer.provide();
        }
        return lastFrame != null;
    }

    /**
     * This <em>should</em> gives us our audio
     *
     * @return The audio in some nice bytes
     */
    @Override
    public byte[] provide20MsAudio() {
        return lastFrame.data;
    }

    /**
     * "Checks" if this audio is opus
     *
     * @return always true
     */
    @Override
    public boolean isOpus() {
        System.out.println("Handler: isOpus");
        return true;
    }
}

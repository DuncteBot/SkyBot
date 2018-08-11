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

import fredboat.audio.player.LavalinkManager;
import lavalink.client.player.IPlayer;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

public class GuildMusicManager {

    /**
     * This is our player
     */
    public final IPlayer player;
    /**
     * This is the scheduler
     */
    public final TrackScheduler scheduler;
    private final GuildSettings settings;
    /**
     * This is what actually sends the audio
     */
    private final AudioPlayerSenderHandler sendHandler;
    /**
     * This is the text channel were we will announce our songs
     */
    public long latestChannel = -1;

    /**
     * Constructor
     *
     * @param g The guild that we wannt the manager for
     */
    public GuildMusicManager(Guild g) {
        player = LavalinkManager.ins.createPlayer(g.getIdLong());
        scheduler = new TrackScheduler(player, this);
        sendHandler = new AudioPlayerSenderHandler(player);
        player.addListener(scheduler);
        this.settings = GuildSettingsUtils.getGuild(g);
    }

    /**
     * This will get our sendings handler
     *
     * @return The {@link AudioPlayerSenderHandler thing} that sends our audio
     */
    public AudioPlayerSenderHandler getSendHandler() {
        return sendHandler;
    }

    boolean isAnnounceTracks() {
        return settings.isAnnounceTracks();
    }

    TextChannel getLatestChannel() {
        if (this.latestChannel == -1 || this.latestChannel == 0)
            return null;
        return SkyBot.getInstance().getShardManager().getTextChannelById(this.latestChannel);
    }
}

/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.concurrent.atomic.AtomicLong;

@Author(nickname = "duncte123", author = "Duncan Sterken")
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
     * This is the text channel were we will announce our songs
     */
    private final AtomicLong lastChannel = new AtomicLong(-1);
    private final GuildSettings settings;

    /**
     * Constructor
     *
     * @param g
     *         The guild that we want the manager for
     */
    public GuildMusicManager(Guild g, Variables variables) {
        this.player = LavalinkManager.ins.createPlayer(g.getIdLong());
        this.scheduler = new TrackScheduler(this.player, this, variables);
        this.player.addListener(this.scheduler);
        this.settings = GuildSettingsUtils.getGuild(g, variables);
    }

    /**
     * This will get our sending handler
     *
     * @return The {@link AudioPlayerSenderHandler thing} that sends our audio
     */
    public AudioPlayerSenderHandler getSendHandler() {
        return new AudioPlayerSenderHandler(this.player);
    }

    boolean isAnnounceTracks() {
        return this.settings.isAnnounceTracks();
    }

    public long getLastChannel() {
        return this.lastChannel.get();
    }

    public void setLastChannel(long lastChannel) {
        this.lastChannel.set(lastChannel);
    }

    TextChannel getLatestChannel() {
        final long last = this.getLastChannel();

        if (last == -1 || last == 0) {
            return null;
        }

        return SkyBot.getInstance().getShardManager().getTextChannelById(last);
    }
}

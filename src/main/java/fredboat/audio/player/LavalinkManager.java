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

package fredboat.audio.player;

import com.afollestad.ason.Ason;
import com.afollestad.ason.AsonArray;
import lavalink.client.io.Lavalink;
import lavalink.client.io.Link;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavaplayerPlayerWrapper;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.audio.LavalinkNode;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.AudioUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.sf.json.JSONArray;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


/**
 * This class has been taken from
 * https://github.com/Frederikam/FredBoat/blob/master/FredBoat/src/main/java/fredboat/audio/player/LavalinkManager.java\
 * and has been modified to fit my needs
 */
public class LavalinkManager {

    public static final LavalinkManager ins = new LavalinkManager();

    private LavalinkManager() {
    }

    private Lavalink lavalink = null;

    public void start() {
        if (!isEnabled()) return;

        String userId = getIdFromToken(AirUtils.CONFIG.getString("discord.token"));

        lavalink = new Lavalink(
                userId,
                AirUtils.CONFIG.getInt("discord.totalShards", 1),
                shardId -> SkyBot.getInstance().getShardManager().getShardById(shardId)
        );
        List<LavalinkNode> defaultNodes = new ArrayList<>();
        defaultNodes.add(new LavalinkNode("ws://localhost","youshallnotpass"));
        List<Ason> nodes = AirUtils.CONFIG.getArray("lavalink.nodes", defaultNodes );
        List<LavalinkNode> nodeList = new ArrayList<>();

        nodes.forEach(it -> nodeList.add( Ason.deserialize(it, LavalinkNode.class) ));

        nodeList.forEach(it ->
                lavalink.addNode(toURI(it.wsurl), it.pass)
        );
    }

    public boolean isEnabled() {
        return AirUtils.CONFIG.getBoolean("lavalink.enable", false);
    }

    public IPlayer createPlayer(String guildId) {
        return isEnabled()
                ? lavalink.getLink(guildId).getPlayer()
                : new LavaplayerPlayerWrapper(AudioUtils.ins.getPlayerManager().createPlayer());
    }

    public void openConnection(VoiceChannel channel) {
        if (isEnabled()) {
            lavalink.getLink(channel.getGuild()).connect(channel);
        } else {
            channel.getGuild().getAudioManager().openAudioConnection(channel);
        }
    }

    public boolean isConnected(Guild g) {
        return isEnabled() ?
                lavalink.getLink(g).getState() == Link.State.CONNECTED :
                g.getAudioManager().isConnected();
    }

    public void closeConnection(Guild guild) {
        if (isEnabled()) {
            lavalink.getLink(guild).disconnect();
        } else {
            guild.getAudioManager().closeAudioConnection();
        }
    }

    public VoiceChannel getConnectedChannel(@NotNull Guild guild) {
        //NOTE: never use the local audio manager, since the audio connection may be remote
        // there is also no reason to look the channel up remotely from lavalink, if we have access to a real guild
        // object here, since we can use the voice state of ourselves (and lavalink 1.x is buggy in keeping up with the
        // current voice channel if the bot is moved around in the client)
        return guild.getSelfMember().getVoiceState().getChannel();
    }

    public Lavalink getLavalink() {
        return lavalink;
    }

    /**
     * This is a simple util function that extracts the bot id from the token
     * @param token the token of your bot
     * @return the client id of the bot
     */
    private String getIdFromToken(String token) {
        return new String(
                Base64.getDecoder().decode(
                        token.split("\\.")[0]
                )
        );
    }

    private URI toURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}
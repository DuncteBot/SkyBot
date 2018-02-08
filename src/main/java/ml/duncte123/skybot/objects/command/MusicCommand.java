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

package ml.duncte123.skybot.objects.command;

import gnu.trove.map.TLongLongMap;
import gnu.trove.map.hash.TLongLongHashMap;
import lavalink.client.io.Link;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.SinceSkybot;
import ml.duncte123.skybot.SkyBot;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class MusicCommand extends Command {

    @SinceSkybot(version = "3.54.2")
    public static TLongLongMap cooldowns = new TLongLongHashMap();
    @SinceSkybot(version = "3.54.2")
    private static ScheduledExecutorService service = Executors.newScheduledThreadPool(1,
            r -> new Thread(r, "MusicCooldown - Thread"));

    public MusicCommand() {
        this.category = CommandCategory.MUSIC;
    }

    static {
        service.scheduleWithFixedDelay(() ->
                        cooldowns.forEachEntry((a, b) -> {
                            if (b > 0) {
                                cooldowns.put(a, (b - 200));
                                return true;
                            } else if (b == 0) {
                                cooldowns.remove(a);
                            }
                            return true;
                        })
                , 0, 200, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the autio utils
     *
     * @return the audio utils
     */
    protected AudioUtils getAu() {
        return AirUtils.audioUtils;
    }

    /**
     * This is a shortcut for getting the music manager
     *
     * @param guild the guild to get the music manager for
     * @return the {@link GuildMusicManager GuildMusicManager} for that guild
     */
    //@Deprecated(message = "Use #getLink(guild)")
    protected GuildMusicManager getMusicManager(Guild guild) {
        return AirUtils.audioUtils.getMusicManager(guild);
    }

    public static boolean isConnected(Guild g) {
        return isLavaLinkEnabled() ?
                getLink(g).getState() == Link.State.CONNECTED :
                g.getAudioManager().isConnected();
    }

    private static boolean isLavaLinkEnabled() {
        return AirUtils.config.getBoolean("lavalink.enable");
    }

    protected static void openAudioConnection(VoiceChannel vc) {
        if(isLavaLinkEnabled())
            getLink(vc.getGuild()).connect(vc);
        else
            vc.getGuild().getAudioManager().openAudioConnection(vc);
    }

    public static void closeAudioConnection(Guild g) {
        if(isLavaLinkEnabled())
            getLink(g).disconnect();
        else
            g.getAudioManager().closeAudioConnection();
    }

    /**
     * This is a shortcut for getting the the link
     *
     * @param guild the guild to get the audio manager for
     * @return the {@link Link Link} for the guild
     */
    protected static Link getLink(Guild guild) {
        return SkyBot.getInstance().getLavalink().getLink(guild);
    }

    /**
     * This performs some checks that we need for the music
     *
     * @param event The current {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if the checks pass
     */
    protected boolean channelChecks(GuildMessageReceivedEvent event) {
        Link audioManager = getLink(event.getGuild());

//        if (audioManager.getState() != Link.State.CONNECTED) {
        if (!isConnected(event.getGuild())) {
            MessageUtils.sendMsg(event, "I'm not in a voice channel, use `" + PREFIX + "join` to make me join a channel");
            return false;
        }

        if (audioManager.getChannel() != null && !audioManager.getChannel().equals(event.getMember().getVoiceState().getChannel())) {
            MessageUtils.sendMsg(event, "I'm sorry, but you have to be in the same channel as me to use any music related commands");
            return false;
        }
        return true;
    }

    /**
     * @param guildId the {@link Guild} id that should receive the cooldown.
     */
    @SinceSkybot(version = "3.54.2")
    @Author(nickname = "Sanduhr32", author = "Maurice R S")
    public static void addCooldown(long guildId) {
        cooldowns.put(guildId, 12600);
    }

    /**
     * This method shuts down the service that cares for the dynamic cooldown decreasing.
     */
    @SinceSkybot(version = "3.54.2")
    @Author(nickname = "Sanduhr32", author = "Maurice R S")
    public static void shutdown() {
        service.shutdown();
    }
}

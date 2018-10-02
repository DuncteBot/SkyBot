/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import fredboat.audio.player.LavalinkManager;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.hash.TLongLongHashMap;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.SinceSkybot;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.utils.AudioUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.concurrent.TimeUnit;

import static me.duncte123.botCommons.messaging.MessageUtils.sendMsg;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public abstract class MusicCommand extends Command {

    @SinceSkybot(version = "3.54.2")
    public static TLongLongMap cooldowns = new TLongLongHashMap();

    static {
        commandService.scheduleWithFixedDelay(() ->
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

    public MusicCommand() {
        this.category = CommandCategory.MUSIC;
    }

    /**
     * This is a shortcut for getting the the link
     *
     * @return the {@link LavalinkManager LavalinkManager}
     */
    protected static LavalinkManager getLavalinkManager() {
        return LavalinkManager.ins;
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
        commandService.shutdown();
    }


    /**
     * This is a shortcut for getting the music manager
     *
     * @param guild the guild to get the music manager for
     * @return the {@link GuildMusicManager GuildMusicManager} for that guild
     */
    //@Deprecated(message = "Use #getLavalinkManager(guild)")
    protected GuildMusicManager getMusicManager(Guild guild, AudioUtils audioUtils) {
        return audioUtils.getMusicManager(guild);
    }

    /**
     * This performs some checks that we need for the music and may suppress error messages.
     *
     * @param event The current {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param reply whether the bot replies that you should make it join first
     * @return true if the checks pass
     */
    protected boolean channelChecks(GuildMessageReceivedEvent event, AudioUtils audioUtils, boolean reply) {

        if (!event.getMember().getVoiceState().inVoiceChannel()) {
            sendMsg(event, "Please join a voice channel first");
            return false;
        }

        LavalinkManager lavalinkManager = getLavalinkManager();
        if (!lavalinkManager.isConnected(event.getGuild())) {
            if (reply) {
                sendMsg(event, "I'm not in a voice channel, use `" + PREFIX + "join` to make me join a channel\n\n" +
                    "Want to have the bot automatically join your channel? Consider becoming a patron.");
            }
            return false;
        }

        if (lavalinkManager.getConnectedChannel(event.getGuild()) != null &&
            !lavalinkManager.getConnectedChannel(event.getGuild()).getMembers().contains(event.getMember())) {
            if (reply) {
                sendMsg(event, "I'm sorry, but you have to be in the same channel as me to use any music related commands");
            }
            return false;
        }
        getMusicManager(event.getGuild(), audioUtils).latestChannel = event.getChannel().getIdLong();
        return true;
    }

    /**
     * This performs some checks that we need for the music and will always reply with error messages.
     *
     * @param event The current {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return true if the checks pass
     */
    protected boolean channelChecks(GuildMessageReceivedEvent event, AudioUtils audioUtils) {
        return channelChecks(event, audioUtils, true);
    }

    protected boolean prejoinChecks(GuildMessageReceivedEvent event) {
        if (isUserOrGuildPatron(event, false)) {
            //If the member is not connected
            if (!event.getMember().getVoiceState().inVoiceChannel()) {
                sendMsg(event, "Please join a voice channel first.");
                return false;
            }
            return !getLavalinkManager().isConnected(event.getGuild());
        }
        return false;
    }

    /*protected boolean isOwner(GuildMessageReceivedEvent event) {
        return isDev(event.getAuthor()) || event.getAuthor().getId().equals(Settings.OWNER_ID);
    }*/

    protected boolean hasCoolDown(Guild guild) {
        return cooldowns.containsKey(guild.getIdLong()) && cooldowns.get(guild.getIdLong()) > 0;
    }
}

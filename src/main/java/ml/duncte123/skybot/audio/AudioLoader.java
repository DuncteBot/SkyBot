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

package ml.duncte123.skybot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.commands.music.RadioCommand;
import ml.duncte123.skybot.objects.RadioStream;
import ml.duncte123.skybot.objects.TrackUserData;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.duncte123.botcommons.messaging.EmbedUtils.embedField;
import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

public class AudioLoader implements AudioLoadResultHandler {

    private final CommandContext ctx;
    private final TextChannel channel;
    private final User requester;
    private final GuildMusicManager mng;
    private final boolean announce;
    private final boolean addPlaylist;
    private final String trackUrl;
    private final AudioUtils audioUtils;

    public AudioLoader(CommandContext ctx, GuildMusicManager mng, boolean announce,
                       boolean addPlaylist, String trackUrl, AudioUtils audioUtils) {
        this.ctx = ctx;
        this.channel = ctx.getChannel();
        this.requester = ctx.getAuthor();
        this.mng = mng;
        this.announce = announce;
        this.addPlaylist = addPlaylist;
        this.trackUrl = trackUrl;
        this.audioUtils = audioUtils;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        String title = track.getInfo().title;
        title = getSteamTitle(track, title, ctx.getCommandManager());

        track.setUserData(new TrackUserData(requester.getIdLong()));

        mng.scheduler.queue(track);

        if (announce) {
            String msg = "Adding to queue: " + title;
            if (mng.player.getPlayingTrack() == null) {
                msg += "\nand the Player has started playing;";
            }

            sendEmbed(channel, embedField(audioUtils.embedTitle, msg));
        }
    }

    static String getSteamTitle(AudioTrack track, String title, CommandManager commandManager) {
        if (track.getInfo().isStream) {
            Optional<RadioStream> stream = ((RadioCommand) commandManager.getCommand("radio"))
                .getRadioStreams().stream().filter(s -> s.getUrl().equals(track.getInfo().uri)).findFirst();

            if (stream.isPresent()) {
                title = stream.get().getName();
            }
        }

        return title;
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        AudioTrack firstTrack = playlist.getSelectedTrack();
        List<AudioTrack> tracks = new ArrayList<>();

        for (final AudioTrack track : playlist.getTracks()) {
            track.setUserData(new TrackUserData(requester.getIdLong()));
            tracks.add(track);
        }

        if (tracks.isEmpty()) {
            sendEmbed(channel, embedField(audioUtils.embedTitle, "Error: This playlist is empty."));
            return;

        } else if (firstTrack == null) {
            firstTrack = playlist.getTracks().get(0);
        }

        if (addPlaylist) {
            tracks.forEach(mng.scheduler::queue);
        } else {
            mng.scheduler.queue(firstTrack);
        }

        if (announce) {
            String msg;

            if (addPlaylist) {
                msg = "Adding **" + playlist.getTracks().size() + "** tracks to queue from playlist: " + playlist.getName();

                if (mng.player.getPlayingTrack() == null) {
                    msg += "\nand the Player has started playing;";
                }
            } else {
                String prefix = GuildSettingsUtils.getGuild(channel.getGuild(), ctx.getVariables()).getCustomPrefix();
                msg = "**Hint:** Use `" + prefix + "pplay <playlist link>` to add a playlist." +
                    "\n\nAdding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")";

                if (mng.player.getPlayingTrack() == null) {
                    msg += "\nand the Player has started playing;";
                }
            }
            sendEmbed(channel, embedField(audioUtils.embedTitle, msg));
        }
    }

    @Override
    public void noMatches() {
        if (announce) {
            sendEmbed(channel, embedField(audioUtils.embedTitle, "Nothing found by _" + trackUrl + "_"));
        }
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        if (!announce) {
            return;
        }

        if (exception.getMessage().endsWith("Playback on other websites has been disabled by the video owner.")) {
            sendEmbed(channel, embedField(audioUtils.embedTitle, "Could not play: " + trackUrl
                + "\nExternal playback of this video was blocked by YouTube."));
            return;
        }

        Throwable root = ExceptionUtils.getRootCause(exception);

        if (root == null) {
            // It can return null so shush
            // noinspection UnusedAssignment
            root = exception;
            return;
        }

        sendEmbed(channel, embedField(audioUtils.embedTitle, "Could not play: " + root.getMessage()
            + "\nIf this happens often try another link or join our [support guild](https://discord.gg/NKM9Xtk) for more!"));

    }
}

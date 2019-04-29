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
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.duncte123.botcommons.messaging.EmbedUtils.embedField;
import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

public class AudioLoader implements AudioLoadResultHandler {

    private final CommandContext ctx;
    private final TextChannel channel;
    private final long requester;
    private final GuildMusicManager mng;
    private final boolean announce;
    private final String trackUrl;
    private final AudioUtils audioUtils;

    public AudioLoader(CommandContext ctx, GuildMusicManager mng, boolean announce, String trackUrl, AudioUtils audioUtils) {
        this.ctx = ctx;
        this.channel = ctx.getChannel();
        this.requester = ctx.getAuthor().getIdLong();
        this.mng = mng;
        this.announce = announce;
        this.trackUrl = trackUrl;
        this.audioUtils = audioUtils;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        final String title = getSteamTitle(track, track.getInfo().title, this.ctx.getCommandManager());

        track.setUserData(new TrackUserData(this.requester));
        this.mng.scheduler.queue(track);

        if (this.announce) {
            String msg = "Adding to queue: " + title;
            if (this.mng.player.getPlayingTrack() == null) {
                msg += "\nand the Player has started playing;";
            }

            sendEmbed(this.channel, embedField(this.audioUtils.embedTitle, msg));
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
//        AudioTrack firstTrack = playlist.getSelectedTrack();
        final List<AudioTrack> tracks = new ArrayList<>();
        final TrackUserData userData = new TrackUserData(this.requester);

        for (final AudioTrack track : playlist.getTracks()) {
            track.setUserData(userData);
            tracks.add(track);
        }

        if (tracks.isEmpty()) {
            sendEmbed(this.channel, embedField(this.audioUtils.embedTitle, "Error: This playlist is empty."));

            return;
        } /*else if (firstTrack == null) {
            firstTrack = playlist.getTracks().get(0);
        }*/

        tracks.forEach(this.mng.scheduler::queue);

        /*if (this.addPlaylist) {
            tracks.forEach(this.mng.scheduler::queue);
        } else {
            this.mng.scheduler.queue(firstTrack);
        }*/

        if (this.announce) {
            String msg = "Adding **" + playlist.getTracks().size() + "** tracks to queue from playlist: " + playlist.getName();

            /*if (this.addPlaylist) {
                msg = "Adding **" + playlist.getTracks().size() + "** tracks to queue from playlist: " + playlist.getName();
            } else {
                final String prefix = GuildSettingsUtils.getGuild(this.channel.getGuild(), this.ctx.getVariables()).getCustomPrefix();

                msg = "**Hint:** Use `" + prefix + "pplay <playlist link>` to add a playlist." +
                    "\n\nAdding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")";

            }*/

            if (this.mng.player.getPlayingTrack() == null) {
                msg += "\nand the Player has started playing;";
            }

            sendEmbed(this.channel, embedField(this.audioUtils.embedTitle, msg));
        }
    }

    @Override
    public void noMatches() {
        if (this.announce) {
            sendEmbed(this.channel, embedField(this.audioUtils.embedTitle, "Nothing found by _" + this.trackUrl + "_"));
        }
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        if (!this.announce) {
            return;
        }

        if (exception.getMessage().endsWith("Playback on other websites has been disabled by the video owner.")) {
            sendEmbed(this.channel, embedField(this.audioUtils.embedTitle, "Could not play: " + this.trackUrl
                + "\nExternal playback of this video was blocked by YouTube."));
            return;
        }

        @Nullable Throwable root = ExceptionUtils.getRootCause(exception);

        if (root == null) {
            // It can return null so shush
            // noinspection UnusedAssignment
            root = exception;

            return;
        }

        sendEmbed(this.channel, embedField(this.audioUtils.embedTitle, "Could not play: " + root.getMessage()
            + "\nIf this happens often try another link or join our [support guild](https://discord.gg/NKM9Xtk) for more!"));

    }

    static String getSteamTitle(AudioTrack track, String rawTitle, CommandManager commandManager) {
        String title = rawTitle;

        if (track.getInfo().isStream) {
            final Optional<RadioStream> stream = ((RadioCommand) commandManager.getCommand("radio"))
                .getRadioStreams().stream().filter(s -> s.getUrl().equals(track.getInfo().uri)).findFirst();

            if (stream.isPresent()) {
                title = stream.get().getName();
            }
        }

        return title;
    }
}

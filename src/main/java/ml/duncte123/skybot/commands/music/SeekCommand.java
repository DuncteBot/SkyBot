/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.player.IPlayer;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.MusicCommand;
import net.dv8tion.jda.internal.utils.Helpers;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SeekCommand extends MusicCommand {

    private static final Pattern TIME_REGEX = Pattern.compile("(\\d{2}):(\\d{2})");

    public SeekCommand() {
        this.name = "seek";
        this.aliases = new String[]{
            "jump",
            "jumpto",
            "jp",
        };
        this.help = "Seek in the currently playing track";
        this.usage = "<minutes:seconds>` / `{prefix}seek [-]<seconds>";
        this.extraInfo = "Examples: `{prefix}seek 04:20`\n" +
                "`{prefix}seek 00:50`\n" +
                "`{prefix}seek 120`";
    }

    @Override
    public void run(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();

        if (args.isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final IPlayer player = getMusicManager(ctx.getGuild(), ctx.getAudioUtils()).player;
        final AudioTrack currentTrack = player.getPlayingTrack();

        if (currentTrack == null) {
            sendMsg(ctx, "The player is currently not playing anything");
            return;
        }

        if (!currentTrack.isSeekable()) {
            sendMsg(ctx, "This track is not seekable");
            return;
        }

        final String arg0 = args.get(0);
        final String seekTime = arg0.replaceFirst("-", "");
        final Matcher matcher = TIME_REGEX.matcher(seekTime);

        if (matcher.matches()) {
            final long minutes = Long.parseLong(matcher.group(1)) * 60 * 1000;
            final long seconds = Long.parseLong(matcher.group(2)) * 1000;

            final long finalTime = minutes + seconds;

            player.seekTo(finalTime);
            sendNowPlaying(ctx);
            return;
        }

        if (!Helpers.isNumeric(seekTime)) {
            sendMsg(ctx, "Invalid time format");
            return;
        }

        // To hopefully prevent race conditions
        final Supplier<Long> trackDuration = () -> player.getPlayingTrack().getDuration();

        int seconds = Integer.parseInt(seekTime) * 1000;

        // FIXME: Odd looking code
        if (seconds >= trackDuration.get()) {
            if (arg0.charAt(0) == '-') {
                sendMsg(ctx, "You're trying to skip more than the length of the track into the negatives?");
                return;
            }

            // No need to announce as we just skip to the end
            player.seekTo(trackDuration.get());
            return;
        }

        if (arg0.charAt(0) == '-') {
            seconds = ~seconds;
        }

        final Supplier<Long> trackPosition = () -> player.getPlayingTrack().getPosition();
        final long currentPosition = trackPosition.get();
        final long newPosition = currentPosition + seconds;

        if (newPosition < 0) {
            sendMsg(ctx, String.format("%s is not above 0", newPosition));
            return;
        }

        player.seekTo(newPosition);

        if (newPosition < trackDuration.get()) {
            sendNowPlaying(ctx);
        }

    }

    private void sendNowPlaying(CommandContext ctx) {
        try {
            // Race condition
            Thread.sleep(700);
        }
        catch (InterruptedException ignored) {
        }

        Objects.requireNonNull(ctx.getCommandManager().getCommand("nowplaying")).executeCommand(ctx);
    }
}

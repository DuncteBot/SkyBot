/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.duncte123.skybot.commands.music;

import dev.arbjerg.lavalink.client.protocol.Track;
import me.duncte123.skybot.Variables;
import me.duncte123.skybot.audio.LocalPlayer;
import me.duncte123.skybot.objects.command.CommandContext;
import me.duncte123.skybot.objects.command.MusicCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.internal.utils.Helpers;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class SeekCommand extends MusicCommand {

    private static final Pattern TIME_REGEX = Pattern.compile("(\\d{2}):(\\d{2})");

    public SeekCommand() {
        this.requiresArgs = true;
        this.name = "seek";
        this.aliases = new String[]{
            "jump",
            "jumpto",
            "jp",
        };
        this.help = "Seek in the currently playing track";
        this.usage = "<minutes:seconds>` / `{prefix}seek [-]<seconds>";
        this.extraInfo = """
            Examples: `{prefix}seek 04:20`
            `{prefix}seek 00:50`
            `{prefix}seek 120`""";
    }

    @Override
    public void run(@NotNull CommandContext ctx) {
        try {
            this.run0(ctx);
        } catch (NumberFormatException e) {
            sendMsg(ctx, "Your input \"" + ctx.getArgs().get(0) + "\" is not a valid number.");
        }
    }

    public void run0(@Nonnull CommandContext ctx) throws NumberFormatException {
        final var player = ctx.getAudioUtils().getMusicManager(ctx.getGuildId()).getPlayer();
        final Track currentTrack = player.getCurrentTrack();

        if (currentTrack == null) {
            sendMsg(ctx, "The player is currently not playing anything");
            return;
        }

        if (!currentTrack.getInfo().isSeekable()) {
            sendMsg(ctx, "This track is not seekable");
            return;
        }

        final List<String> args = ctx.getArgs();
        final String arg0 = args.get(0);
        final String seekTime = arg0.replaceFirst("-", "");
        final Matcher matcher = TIME_REGEX.matcher(seekTime);

        if (matcher.matches()) {
            handleTimeSkip(ctx, player, matcher);
            return;
        }

        if (!Helpers.isNumeric(seekTime)) {
            sendMsg(ctx, "Invalid time format");
            return;
        }

        // To hopefully prevent race conditions
        final Supplier<Long> trackDuration = () -> player.getCurrentTrack().getInfo().getLength();

        int seconds = Integer.parseInt(seekTime) * 1000;

        if (seconds >= trackDuration.get()) {
            handleOverSkip(ctx, player, arg0, trackDuration);
            return;
        }

        // ~ is the NOT operator (inverts all the bits)
        if (arg0.charAt(0) == '-') {
            seconds = ~seconds;
        }

        final Supplier<Long> trackPosition = player::getPosition;
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

    @Override
    public void handleEvent(@NotNull SlashCommandInteractionEvent event, @NotNull Variables variables) {
        event.reply("Slash command not supported yet, sorry. Please report this issue.").queue();
    }

    private void handleOverSkip(@NotNull CommandContext ctx, LocalPlayer player, String arg0, Supplier<Long> trackDuration) {
        if (arg0.charAt(0) == '-') {
            sendMsg(ctx, "You're trying to skip more than the length of the track into the negatives?");
            return;
        }

        // No need to announce as we just skip to the end
        player.seekTo(trackDuration.get());
    }

    private void handleTimeSkip(@NotNull CommandContext ctx, LocalPlayer player, Matcher matcher) {
        final long minutes = Long.parseLong(matcher.group(1)) * 60 * 1000;
        final long seconds = Long.parseLong(matcher.group(2)) * 1000;

        final long finalTime = minutes + seconds;

        player.seekTo(finalTime);
        sendNowPlaying(ctx);
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

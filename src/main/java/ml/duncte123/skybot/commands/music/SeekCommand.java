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

package ml.duncte123.skybot.commands.music;

import lavalink.client.player.IPlayer;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.MusicCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SeekCommand extends MusicCommand {

    private static final Pattern TIME_REGEX = Pattern.compile("(\\d{2}):(\\d{2})");

    @Override
    public void run(@Nonnull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();

        if (args.isEmpty()) {
            sendMsg(event, "Missing arguments, check `" + ctx.getPrefix() + "help " + getName() + "`");
            return;
        }

        final Matcher matcher = TIME_REGEX.matcher(args.get(0));

        if (!matcher.matches()) {
            sendMsg(event, "Invalid time format");
            return;
        }

        final IPlayer player = getMusicManager(ctx.getGuild(), ctx.getAudioUtils()).player;

        if (player.getPlayingTrack() == null) {
            sendMsg(event, "The player is currently not playing anything");
            return;
        }

        if (!player.getPlayingTrack().isSeekable()) {
            sendMsg(event, "This track is not seekable");
            return;
        }

        final long minutes = Integer.parseInt(matcher.group(1)) * 60 * 1000;
        final long seconds = Integer.parseInt(matcher.group(2)) * 1000;

        final long finalTime = minutes + seconds;

        player.seekTo(finalTime);

        try {
            Thread.sleep(500);
        }
        catch (InterruptedException ignored) {
        }

        ctx.getCommandManager().getCommand("nowplaying").executeCommand(ctx);

    }

    @Override
    public String getName() {
        return "seek";
    }

    @Override
    public String help(String prefix) {
        return "seek in the currently playing track\n" +
            "Usage: `" + prefix + getName() + " <minutes:seconds>`\n" +
            "Examples: `" + prefix + getName() + " 04:20`\n" +
            "`" + prefix + getName() + " 00:50`\n";
    }
}

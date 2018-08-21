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

package ml.duncte123.skybot.commands.music;

import lavalink.client.player.IPlayer;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.MusicCommand;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.duncte123.botCommons.messaging.MessageUtils.sendMsg;

public class SeekCommand extends MusicCommand {

    private static final Pattern TIME_REGEX = Pattern.compile("(\\d{2})\\:(\\d{2})");

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();

        if (!channelChecks(event, ctx.getAudioUtils()))
            return;

        List<String> args = ctx.getArgs();

        if (args.isEmpty()) {
            sendMsg(event, "Missing arguments, check `" + PREFIX + "help " + getName() + "`");
            return;
        }

        Matcher matcher = TIME_REGEX.matcher(args.get(0));

        if (!matcher.matches()) {
            sendMsg(event, "Invalid time format");
            return;
        }
        IPlayer player = getMusicManager(ctx.getGuild(), ctx.getAudioUtils()).player;

        if (player.getPlayingTrack() == null) {
            sendMsg(event, "The player is currently not playing anything");
            return;
        }

        long minutes = Integer.parseInt(matcher.group(1)) * 60 * 1000;
        long seconds = Integer.parseInt(matcher.group(2)) * 1000;

        long finalTime = minutes + seconds;

        player.seekTo(finalTime);

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        ctx.getCommandManager().getCommand("nowplaying").executeCommand(ctx);

    }

    @Override
    public String getName() {
        return "seek";
    }

    @Override
    public String help() {
        return "seek in the currently playing track\n" +
                "Usage: `" + PREFIX + getName() + " <minutes:seconds>`\n" +
                "Examples: `" + PREFIX + getName() + " 04:20`\n" +
                "`" + PREFIX + getName() + " 00:50`\n";
    }
}

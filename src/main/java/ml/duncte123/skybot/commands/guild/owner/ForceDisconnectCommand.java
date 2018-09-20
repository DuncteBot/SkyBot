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

package ml.duncte123.skybot.commands.guild.owner;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.MusicCommand;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import static me.duncte123.botCommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class ForceDisconnectCommand extends MusicCommand {
    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();

        if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            Guild g = event.getGuild();
            GuildMusicManager manager = getMusicManager(g, ctx.getAudioUtils());

            manager.player.stopTrack();
            manager.scheduler.queue.clear();
            getLavalinkManager().closeConnection(g);
            sendMsg(event, "Successfully send the disconnect signal to the server");
        } else {
            sendMsg(event, "You need administrator perms to run this command.");
        }
    }

    @Override
    public String help() {
        return "Force disconnects the bot from music for when the bot is stuck";
    }

    @Override
    public String getName() {
        return "forcedisconnect";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"forceleave"};
    }
}

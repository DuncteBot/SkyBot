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

package ml.duncte123.skybot.commands.guild.owner;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.audio.GuildMusicManager;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.MusicCommand;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class ForceDisconnectCommand extends MusicCommand {
    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();

        if (!ctx.getMember().hasPermission(Permission.ADMINISTRATOR) && !isDev(ctx.getAuthor())) {
            sendMsg(event, "You need administrator perms to run this command.");
            return;
        }

        final Guild g = event.getGuild();
        final GuildMusicManager manager = getMusicManager(g, ctx.getAudioUtils());

        manager.player.stopTrack();
        manager.scheduler.queue.clear();
        getLavalinkManager().closeConnection(g);

        sendMsg(event, "Successfully sent the disconnect signal to the server");

    }

    @Override
    public String help(String prefix) {
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

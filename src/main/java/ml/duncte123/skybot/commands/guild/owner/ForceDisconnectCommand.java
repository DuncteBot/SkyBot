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

package ml.duncte123.skybot.commands.guild.owner;

import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.MusicCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.CommandUtils.isDev;

public class ForceDisconnectCommand extends MusicCommand {

    public ForceDisconnectCommand() {
        this.name = "forcedisconnect";
        this.aliases = new String[]{
            "forceleave",
        };
        this.help = "Force disconnects the bot from music for when the bot is stuck";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if (!ctx.getMember().hasPermission(Permission.ADMINISTRATOR) && !isDev(ctx.getAuthor())) {
            sendMsg(ctx, "You need administrator perms to run this command.");
            return;
        }

        final Guild guild = ctx.getGuild();

        ctx.getAudioUtils().removeMusicManager(guild);
        getLavalinkManager().closeConnection(guild);

        sendMsg(ctx, "Successfully sent the disconnect signal to the server");

    }
}

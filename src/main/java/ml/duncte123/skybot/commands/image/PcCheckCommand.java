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

package ml.duncte123.skybot.commands.image;

import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class PcCheckCommand extends ImageCommandBase {

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();

        if (!doAllChecks(event, ctx.getArgs())) {
            return;
        }

        String reason = ctx.getRawArgs();

        for (User user : event.getMessage().getMentionedUsers()) {
            reason = reason.replaceAll(user.getAsMention(), String.format("%#s", user));
        }

        for (TextChannel channel : event.getMessage().getMentionedChannels()) {
            reason = reason.replaceAll(channel.getAsMention(), String.format("%#s", channel));
        }

        for (Role role : event.getMessage().getMentionedRoles()) {
            reason = reason.replaceAll(role.getAsMention(), String.format("@%s", role.getName()));
        }

        ctx.getBlargbot().getPcCheck(reason).async((image) -> handleBasicImage(event, image));
    }

    @Override
    public String help() {
        return "WHoops get your pc checked\n" +
                "Usage: `db!pccheck <reason>`";
    }

    @Override
    public String getName() {
        return "pccheck";
    }
}

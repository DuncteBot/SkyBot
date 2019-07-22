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

package ml.duncte123.skybot.commands.guild.mod;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.ModerationUtils.modLog;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class UnbanCommand extends ModBaseCommand {

    public UnbanCommand() {
        this.name = "unban";
        this.aliases = new String[]{
            "ban't",
            "pardon",
        };
        this.helpFunction = (invoke, prefix) -> "Removes the ban for a user";
        this.usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + " <user> [-r reason]`";
        this.botPermissions = new Permission[]{
            Permission.BAN_MEMBERS,
        };
        this.flags = new Flag[]{
            new Flag(
                'r',
                "reason",
                "Sets the reason for this ban"
            ),
        };
    }

    @Override
    public void run(@Nonnull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final String argsJoined = ctx.getArgsJoined();
        final User mod = ctx.getAuthor();

        try {
            event.getGuild().getBanList().queue(list -> {

                for (final Guild.Ban ban : list) {
                    final User bannedUser = ban.getUser();
                    final String userFormatted = bannedUser.getAsTag();


                    if (bannedUser.getName().equalsIgnoreCase(argsJoined) || bannedUser.getId().equals(argsJoined) ||
                        userFormatted.equalsIgnoreCase(argsJoined)) {

                        event.getGuild().getController().unban(bannedUser)
                            .reason("Unbanned by " + mod.getAsTag())
                            .queue();

                        sendMsg(event, "User " + userFormatted + " unbanned.");
                        modLog(mod, ban.getUser(), "unbanned", ctx.getGuild());
                        return;
                    }
                }
                sendMsg(event, "This user is not banned");
            });

        }
        catch (Exception e) {
            e.printStackTrace();
            sendMsg(event, "ERROR: " + e.getMessage());
        }
    }
}

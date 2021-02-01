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

package ml.duncte123.skybot.commands.guild.mod;

import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.ModerationUtils.modLog;

public class UnbanCommand extends ModBaseCommand {

    public UnbanCommand() {
        this.requiresArgs = true;
        this.name = "unban";
        this.aliases = new String[]{
            "ban't",
            "pardon",
        };
        this.help = "Removes the ban for a user";
        this.usage = "<user> [-r reason]";
        this.botPermissions = new Permission[]{
            Permission.BAN_MEMBERS,
        };
        this.flags = new Flag[]{
            new Flag(
                'r',
                "reason",
                "Sets the reason for this unban"
            ),
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final var flags = ctx.getParsedFlags(this);
        final String argsJoined = String.join(" ", flags.get("undefined"));
        final User mod = ctx.getAuthor();

        ctx.getJDAGuild().retrieveBanList().queue((list) -> {
            for (final Guild.Ban ban : list) {
                final User bannedUser = ban.getUser();
                final String userFormatted = bannedUser.getAsTag();


                if (bannedUser.getName().equalsIgnoreCase(argsJoined) || bannedUser.getId().equals(argsJoined) ||
                    userFormatted.equalsIgnoreCase(argsJoined)) {

                    String reason = "Unbanned by " + mod.getAsTag();

                    if (flags.containsKey("r")) {
                        reason = reason + ": " + String.join(" ", flags.get("r"));
                    }

                    ctx.getJDAGuild().unban(bannedUser)
                        .reason(reason)
                        .queue();

                    sendMsg(ctx, "User " + userFormatted + " unbanned.");
                    modLog(mod, ban.getUser(), "unbanned", null, null, ctx.getGuild());
                    return;
                }
            }

            sendMsg(ctx, "This user is not banned");
        });
    }
}

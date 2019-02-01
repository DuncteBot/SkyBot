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
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static ml.duncte123.skybot.utils.ModerationUtils.canInteract;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class TempMuteCommand extends TempBanCommand {

    @Override
    public void run(@NotNull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();
        final List<Member> mentioned = ctx.getMentionedMembers();
        final GuildSettings settings = ctx.getGuildSettings();

        if (mentioned.isEmpty() || args.size() < 2) {
            sendMsg(event, "Usage is `" + Settings.PREFIX + getName() + " <@user> <time><m/h/d/w/M/Y> [Reason]`");
            return;
        }


        if (settings.getMuteRoleId() <= 0) {
            sendMsg(event, "No mute/spamrole is set, use `db!spamrole <Role>` to set it");
            return;
        }

        final User author = event.getAuthor();
        final Member mod = ctx.getMember();
        final Member toMute = event.getMessage().getMentionedMembers().get(0);
        final User mutee = toMute.getUser();
        final Role role = event.getGuild().getRoleById(settings.getMuteRoleId());
        final Member self = ctx.getSelfMember();

        if (!canInteract(mod, toMute, "mute", ctx.getChannel())) {
            return;
        }

        if (role == null) {
            sendMsg(event, "The current mute role does not exist on this server, please contact your server administrator about this.");
            return;
        }

        if (!self.canInteract(role)) {
            sendMsg(event, "I cannot mute this member, is the mute role above mine?");
            return;
        }

        final String reason = String.join(" ", args.subList(2, args.size()));
        final String[] timeParts = args.get(1).split("(?<=\\D)+(?=\\d)+|(?<=\\d)+(?=\\D)+");

        if (!AirUtils.isInt(timeParts[0])) {
            sendMsg(event, "Usage is `" + Settings.PREFIX + getName() + " <@user> <time><m/h/d/w/M/Y> [Reason]`");
            return;
        }

        final CalculateBanTime muteTime = new CalculateBanTime(event, timeParts).invoke();

        if (muteTime.hasError()) {
            return;
        }

        final String fReason = reason.isEmpty() ? "No reason was provided" : reason;
        final String finalDate = muteTime.getFinalUnbanDate();

        ctx.getDatabaseAdapter().createMute(
            author.getIdLong(),
            mutee.getIdLong(),
            mutee.getAsTag(),
            finalDate,
            event.getGuild().getIdLong()
        );


        event.getGuild().getController().addSingleRoleToMember(toMute, role)
            .reason("Muted by " + author.getAsTag() + ": " + fReason)
            .queue(success -> {
                    ModerationUtils.modLog(author, mutee, "muted", fReason, args.get(1), ctx.getGuild());
                    sendSuccess(event.getMessage());
                }
            );

    }

    @Override
    public String getName() {
        return "tempmute";
    }

    @Override
    public String help() {
        return "Temporally mutes a user on the guild\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <@user> <time><m/h/d/w/M/Y> [Reason]`";
    }
}

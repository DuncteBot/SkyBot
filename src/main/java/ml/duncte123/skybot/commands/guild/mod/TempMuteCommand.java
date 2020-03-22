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

import me.duncte123.durationparser.Duration;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static ml.duncte123.skybot.commands.guild.mod.TempBanCommand.getDuration;
import static ml.duncte123.skybot.utils.ModerationUtils.canInteract;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class TempMuteCommand extends ModBaseCommand {

    public TempMuteCommand() {
        this.shouldLoadMembers = true;
        this.requiresArgs = true;
        this.requiredArgCount = 2;
        this.name = "tempmute";
        this.help = "Temporally mutes a user in the server, this will override any existing tempmutes for the user";
        this.usage = "<@user> <time><w/d/h/m/s> [-r reason]";
        this.botPermissions = new Permission[]{
            Permission.MANAGE_SERVER,
            Permission.MANAGE_ROLES,
        };
        this.flags = new Flag[]{
            new Flag(
                'r',
                "reason",
                "Sets the reason for this mute"
            ),
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();
        final GuildSettings settings = ctx.getGuildSettings();
        final List<Member> mentioned = ctx.getMentionedArg(0);

        if (mentioned.isEmpty()) {
            sendMsg(ctx, "I could not find any members with name " + args.get(0));
            return;
        }

        if (settings.getMuteRoleId() <= 0) {
            sendMsg(event, "No mute/spamrole is set, use `" + ctx.getPrefix() + "spamrole <Role>` to set it");
            return;
        }

        final User author = event.getAuthor();
        final Member mod = ctx.getMember();
        final Member toMute = mentioned.get(0);
        final User mutee = toMute.getUser();
        final Guild guild = event.getGuild();
        final Role role = guild.getRoleById(settings.getMuteRoleId());
        final Member self = ctx.getSelfMember();

        if (canNotProceed(ctx, event, mod, toMute, role, self)) {
            return;
        }

        String reason = "No reason given";
        final var flags = ctx.getParsedFlags(this);

        if (flags.containsKey("r")) {
            reason = String.join(" ", flags.get("r"));
        }

        final Duration duration = getDuration(args.get(1), getName(), event, ctx.getPrefix());

        if (duration == null) {
            return;
        }

        final String fReason = reason;
        final String finalDate = AirUtils.getDatabaseDateFormat(duration);

        ctx.getDatabaseAdapter().createMute(
            author.getIdLong(),
            mutee.getIdLong(),
            mutee.getAsTag(),
            finalDate,
            guild.getIdLong(),
            (mute) -> {
                if (mute != null) {
                    final String modId = mute.getModId();
                    final User oldMuteMod = guild.getJDA().getUserById(modId);
                    String modName = "Unknown";

                    if (oldMuteMod != null) {
                        modName = oldMuteMod.getAsTag();
                    }

                    sendMsg(event, String.format(
                        "Previously created muted for %#s removed, mute was created by %s",
                        mutee,
                        modName
                    ));
                }

                return null;
            }
        );


        guild.addRoleToMember(toMute, role)
            .reason("Muted by " + author.getAsTag() + ": " + fReason)
            .queue(success -> {
                    ModerationUtils.modLog(author, mutee, "muted", fReason, duration.toString(), ctx.getGuild());
                    sendSuccess(event.getMessage());
                }
            );
    }

    static boolean canNotProceed(@Nonnull CommandContext ctx, GuildMessageReceivedEvent event, Member mod, Member toMute, Role role, Member self) {
        if (role == null) {
            sendMsg(event, "The current mute role does not exist on this server, please contact your server administrator about this.");

            return true;
        }

        if (!canInteract(mod, toMute, "mute", ctx.getChannel())) {
            return true;
        }

        if (!self.canInteract(role)) {
            sendMsg(event, "I cannot mute this member, is the mute role above mine?");

            return true;
        }

        return false;
    }
}

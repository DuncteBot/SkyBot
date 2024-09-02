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

package me.duncte123.skybot.commands.guild.mod;

import me.duncte123.botcommons.messaging.MessageUtils;
import me.duncte123.skybot.Variables;
import me.duncte123.skybot.entities.jda.DunctebotGuild;
import me.duncte123.skybot.objects.command.CommandContext;
import me.duncte123.skybot.objects.command.Flag;
import me.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.skybot.utils.ModerationUtils.canInteract;

public class KickCommand extends ModBaseCommand {

    public KickCommand() {
        this.requiresArgs = true;
        this.name = "kick";
        this.aliases = new String[]{"yeet"};
        this.help = "Kicks a user from the server";
        this.usage = "<@user> [-r reason]";
        this.userPermissions = new Permission[]{
            Permission.KICK_MEMBERS,
        };
        this.botPermissions = new Permission[]{
            Permission.KICK_MEMBERS,
        };
        this.flags = new Flag[]{
            new Flag(
                'r',
                "reason",
                "Sets the reason for this kick"
            ),
        };
    }

    @Override
    protected void configureSlashSupport(@NotNull SlashCommandData baseData) {
        baseData.addOptions(
            new OptionData(
                OptionType.USER,
                "user",
                "The user to kick.",
                true
            ),
            new OptionData(
                OptionType.STRING,
                "reason",
                "Reason for kicking",
                false
            )
        );
    }

    @Override
    public void handleEvent(@NotNull SlashCommandInteractionEvent event, @NotNull DunctebotGuild guild, @NotNull Variables variables) {
        final var toKickMember = event.getOption("user").getAsMember();
        final var moderator = event.getMember();

        if (!canInteract(moderator, toKickMember, "kick", event.getChannel())) {
            return;
        }

        final var reason = Optional.ofNullable(event.getOption("reason"))
            .map(OptionMapping::getAsString)
            .orElse("No reason given");

        guild.kick(toKickMember)
            .reason(String.format("%#s: %s", moderator, reason))
            .queue(
                (ignored) -> {
                    ModerationUtils.modLog(moderator.getUser(), toKickMember.getUser(), "kicked", reason, null, guild);
                    event.reply("User has been kicked").queue();
                }
            );
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<Member> mentioned = ctx.getMentionedArg(0);
        final List<String> args = ctx.getArgs();

        if (mentioned.isEmpty()) {
            sendMsg(ctx, "I could not find any members with name " + args.get(0));
            return;
        }

        final Member toKickMember = mentioned.get(0);
        final Member member = ctx.getMember();

        if (!canInteract(member, toKickMember, "kick", ctx.getChannel())) {
            return;
        }

        final AuditableRestAction<Void> kickAction = ctx.getGuild()
            .kick(toKickMember)
            .reason("Kicked by " + ctx.getAuthor().getAsTag());

        String reason = null;
        final var flags = ctx.getParsedFlags(this);

        if (flags.containsKey("r")) {
            reason = String.join(" ", flags.get("r"));
            kickAction.reason("Kicked by " + String.format("%#s: %s", ctx.getAuthor(), reason));
        } else if (args.size() > 1) {
            final var example = "\nExample: `%skick %s -r %s`".formatted(
                ctx.getPrefix(), args.get(0), String.join(" ", args.subList(1, args.size()))
            );

            sendMsg(ctx, "Hint: if you want to set a reason, use the `-r` flag" + example);
        }

        final String finalReason = reason;

        kickAction.queue(
            (ignored) -> {
                ModerationUtils.modLog(ctx.getAuthor(), toKickMember.getUser(), "kicked", finalReason, null, ctx.getGuild());
                MessageUtils.sendSuccess(ctx.getMessage());
            }
        );
    }
}

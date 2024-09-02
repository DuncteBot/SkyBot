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

import me.duncte123.skybot.Variables;
import me.duncte123.skybot.entities.jda.DunctebotGuild;
import me.duncte123.skybot.objects.command.CommandContext;
import me.duncte123.skybot.objects.command.Flag;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static me.duncte123.skybot.utils.ModerationUtils.canInteract;
import static me.duncte123.skybot.utils.ModerationUtils.modLog;

public class BanCommand extends ModBaseCommand {

    public BanCommand() {
        this.requiresArgs = true;
        this.name = "ban";
        this.aliases = new String[]{
            "dabon",
            "naenae",
        };
        this.help = "Bans a user from the server **(THIS WILL DELETE MESSAGES)**";
        this.usage = "<@user> [-r Reason] [--nodel]";
        this.botPermissions = new Permission[]{
            Permission.BAN_MEMBERS,
        };
        this.flags = new Flag[]{
            new Flag(
                'r',
                "reason",
                "Sets the reason for this ban"
            ),
            new Flag(
                "nodel",
                "Prevents the deletion of any messages"
            ),
        };
    }

    @Override
    protected void configureSlashSupport(@NotNull SlashCommandData baseData) {
        baseData.addOptions(
            new OptionData(
                OptionType.USER,
                "user",
                "The user that you want to ban.",
                true
            ),
            new OptionData(
                OptionType.STRING,
                "reason",
                "The reason for this ban",
                false
            ),
            new OptionData(
                OptionType.BOOLEAN,
                "nodel",
                "Prevents the deletion of any messages",
                false
            )
        );
    }

    @Override
    public void handleEvent(@NotNull SlashCommandInteractionEvent event, @NotNull DunctebotGuild guild, @NotNull Variables variables) {
        final var toBanMember = event.getOption("user").getAsMember();
        final var moderator = event.getMember();

        if (!canInteract(moderator, toBanMember, "ban", event.getChannel())) {
            return;
        }

        final var reason = Optional.ofNullable(event.getOption("reason"))
            .map(OptionMapping::getAsString)
            .orElse("No reason given");
        final var nodel = Optional.ofNullable(event.getOption("nodel"))
            .map(OptionMapping::getAsBoolean)
            .orElse(false);

        final int delDays = nodel ? 0 : 1;
        final var modUser = moderator.getUser();

        guild.ban(toBanMember, delDays, TimeUnit.DAYS)
            .reason(String.format("%#s: %s", modUser, reason))
            .queue(
                (m) -> {
                    modLog(modUser, toBanMember.getUser(), "banned", reason, null, guild);
                    event.reply("User has been banned").queue();
                }
            );
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final List<Member> mentioned = ctx.getMentionedArg(0);

        if (mentioned.isEmpty()) {
            sendMsg(ctx, "I could not find any members with name " + args.get(0));
            return;
        }

        final Member toBanMember = mentioned.get(0);

        if (!canInteract(ctx.getMember(), toBanMember, "ban", ctx.getChannel())) {
            return;
        }

        String reason = "No reason given";
        final var flags = ctx.getParsedFlags(this);

        if (flags.containsKey("r")) {
            reason = String.join(" ", flags.get("r"));
        } else if (args.size() > 1 && !"--nodel".equals(args.get(1))) {
            final var example = "\nExample: `%sban %s -r %s`".formatted(
                ctx.getPrefix(), args.get(0), String.join(" ", args.subList(1, args.size()))
            );

            sendMsg(ctx, "Hint: if you want to set a reason, use the `-r` flag" + example);
        }

        final String fReason = reason;
        final int delDays = flags.containsKey("nodel") ? 0 : 1;
        final User toBan = toBanMember.getUser();

        ctx.getGuild().ban(toBan, delDays, TimeUnit.DAYS)
            .reason(String.format("%#s: %s", ctx.getAuthor(), fReason))
            .queue(
            (m) -> {
                modLog(ctx.getAuthor(), toBan, "banned", fReason, null, ctx.getGuild());
                sendSuccess(ctx.getMessage());
            }
        );
    }
}

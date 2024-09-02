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

import com.dunctebot.models.settings.GuildSetting;
import me.duncte123.skybot.Variables;
import me.duncte123.skybot.entities.jda.DunctebotGuild;
import me.duncte123.skybot.objects.command.CommandContext;
import me.duncte123.skybot.objects.command.Flag;
import me.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
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

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static me.duncte123.skybot.commands.guild.mod.TempMuteCommand.canNotProceed;

public class MuteCommand extends ModBaseCommand {

    public MuteCommand() {
        this.requiresArgs = true;
        this.name = "mute";
        this.help = "Mutes a user in the server";
        this.usage = "<@user> [-r reason]";
        this.userPermissions = new Permission[]{
            Permission.KICK_MEMBERS,
        };
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
    protected void configureSlashSupport(@NotNull SlashCommandData baseData) {
        baseData.addOptions(
            new OptionData(
                OptionType.USER,
                "user",
                "The user to mute.",
                true
            ),
            new OptionData(
                OptionType.STRING,
                "reason",
                "Reason for muting",
                false
            )
        );
    }

    @Override
    public void handleEvent(@NotNull SlashCommandInteractionEvent event, @NotNull DunctebotGuild guild, @NotNull Variables variables) {
        final GuildSetting settings = guild.getSettings();

        if (settings.getMuteRoleId() <= 0) {
            event.reply("No mute/spamrole is currently set. Use `/settings muteRole set:@role` to set one")
                .setEphemeral(true)
                .queue();
            return;
        }

        final Member mod = event.getMember();
        final Member self = guild.getSelfMember();
        final Member toMute = event.getOption("user").getAsMember();
        final Role role = guild.getRoleById(settings.getMuteRoleId());

        if (canNotProceed(event, mod, toMute, role, self)) {
            return;
        }

        event.deferReply().queue();

        final var reason = Optional.ofNullable(event.getOption("reason"))
            .map(OptionMapping::getAsString)
            .orElse("No reason given");

        final User user = event.getUser();

        guild.addRoleToMember(toMute, role)
            .reason("Muted by " + String.format("%#s: %s", user, reason)).queue(success -> {
                    ModerationUtils.modLog(user, toMute.getUser(), "muted", null, null, guild);
                    event.getHook()
                        .editOriginal("User has been muted")
                        .queue();
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

        final GuildSetting settings = ctx.getGuildSettings();

        if (settings.getMuteRoleId() <= 0) {
            sendMsg(ctx, "No mute/spamrole is set, use `" + ctx.getPrefix() + "muterole <Role>` to set it");
            return;
        }

        final Member mod = ctx.getMember();
        final Member self = ctx.getSelfMember();
        final Member toMute = mentioned.get(0);
        final Role role = ctx.getGuild().getRoleById(settings.getMuteRoleId());

        if (canNotProceed(ctx, mod, toMute, role, self)) {
            return;
        }

        String reason = "No reason given";
        final var flags = ctx.getParsedFlags(this);

        if (flags.containsKey("r")) {
            reason = String.join(" ", flags.get("r"));
        } else if (args.size() > 1) {
            final var example = "\nExample: `%smute %s -r %s`".formatted(
                ctx.getPrefix(), args.get(0), String.join(" ", args.subList(1, args.size()))
            );

            sendMsg(ctx, "Hint: if you want to set a reason, use the `-r` flag" + example);
        }

        ctx.getGuild().addRoleToMember(toMute, role)
            .reason("Muted by " + String.format("%#s: %s", ctx.getAuthor(), reason)).queue(success -> {
                ModerationUtils.modLog(ctx.getAuthor(), toMute.getUser(), "muted", null, null, ctx.getGuild());
                sendSuccess(ctx.getMessage());
            }
        );

    }
}

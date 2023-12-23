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

package me.duncte123.skybot.commands.guild.owner;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.duncte123.skybot.objects.command.Command;
import me.duncte123.skybot.objects.command.CommandCategory;
import me.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static me.duncte123.skybot.commands.guild.owner.UnlockEmoteCommand.cannotInteractWithEmote;

public class LockEmoteCommand extends Command {

    public LockEmoteCommand() {
        this.category = CommandCategory.ADMINISTRATION;
        this.name = "lockemote";
        this.help = """
            Lock an emote to some roles.
            _Please note that you can't use the emote anymore if you don't have any of the specified roles,
            even if you have administrator permission_""";
        this.usage = "<emote> <@role...>";
        this.userPermissions = new Permission[]{
            Permission.ADMINISTRATOR,
        };
        this.botPermissions = new Permission[]{
            Permission.MANAGE_GUILD_EXPRESSIONS,
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if (ctx.getArgs().isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final Message message = ctx.getMessage();
        final List<CustomEmoji> mentionedEmotes = message.getReactions()
            .stream()
            .map(MessageReaction::getEmoji)
            .filter((it) -> it.getType() == Emoji.Type.CUSTOM)
            .map(EmojiUnion::asCustom)
            .toList();
        final List<Role> mentionedRoles = new ArrayList<>(message.getMentions().getRoles());

        if (mentionedRoles.isEmpty()) {
            // Loop over the args and check if there are roles found in text
            ctx.getArgs().forEach(
                (arg) -> mentionedRoles.addAll(FinderUtil.findRoles(arg, ctx.getGuild()))
            );
        }

        if (mentionedEmotes.isEmpty() || mentionedRoles.isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final RichCustomEmoji emote = (RichCustomEmoji) mentionedEmotes.get(0);

        if (cannotInteractWithEmote(ctx, emote)) {
            return;
        }

        emote.getManager().setRoles(new HashSet<>(mentionedRoles)).queue();
        sendSuccess(message);
        final List<String> roleNames = mentionedRoles.stream().map(Role::getName).collect(Collectors.toList());

        sendMsg(ctx, "The emote " + emote.getAsMention() + " has been locked to users that have the " +
            "following roles: `" + String.join("`, `", roleNames) + "`");
    }
}

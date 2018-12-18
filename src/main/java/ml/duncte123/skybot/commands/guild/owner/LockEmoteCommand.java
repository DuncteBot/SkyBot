/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.commands.guild.owner;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;
import static ml.duncte123.skybot.commands.guild.owner.UnlockEmoteCommand.cannotInteractWithEmote;

public class LockEmoteCommand extends Command {

    public LockEmoteCommand() {
        this.category = CommandCategory.MOD_ADMIN;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();
        final Message message = ctx.getMessage();

        if (!ctx.getMember().hasPermission(Permission.ADMINISTRATOR) && !isDev(ctx.getAuthor())) {
            sendMsg(event, "You need administrator perms to run this command.");
            return;
        }

        if (!ctx.getSelfMember().hasPermission(Permission.MANAGE_EMOTES)) {
            sendMsg(event, "I need the manage emotes permission in order to lock the emotes to roles");
            return;
        }

        if (ctx.getArgs().isEmpty()) {
            sendMsg(event, "Correct usage: `" + Settings.PREFIX + getName() + " <emote> <@role...>`");
            return;
        }

        final List<Emote> mentionedEmotes = message.getEmotes();
        final List<Role> mentionedRoles = new ArrayList<>(message.getMentionedRoles());

        if (mentionedRoles.isEmpty()) {
            // Loop over the args and check if there are roles found in text
            ctx.getArgs().forEach(
                (arg) -> mentionedRoles.addAll(FinderUtil.findRoles(arg, ctx.getGuild()))
            );
        }

        if (mentionedEmotes.isEmpty() || mentionedRoles.isEmpty()) {
            sendMsg(event, "Correct usage: `" + Settings.PREFIX + getName() + " <emote> <@role...>`");
            return;
        }

        final Emote emote = mentionedEmotes.get(0);

        if (cannotInteractWithEmote(event, emote)) return;

        emote.getManager().setRoles(new HashSet<>(mentionedRoles)).queue();
        sendSuccess(message);
        List<String> roleNames = mentionedRoles.stream().map(Role::getName).collect(Collectors.toList());
        sendMsg(event, "The emote " + emote.getAsMention() + " has been locked to users that have the " +
            "following roles: `" + String.join("`, `", roleNames) + "`");
    }

    @Override
    public String getName() {
        return "lockemote";
    }

    @Override
    public String help() {
        return "Lock an emote to some roles.\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <emote> <@role...>`\n" +
            "Please note that you can't use the emote anymore if you don't have any of the specified roles,\n" +
            "even if you have administrator permission";
    }
}

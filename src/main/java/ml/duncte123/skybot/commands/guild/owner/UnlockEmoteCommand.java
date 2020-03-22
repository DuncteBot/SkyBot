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

package ml.duncte123.skybot.commands.guild.owner;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendSuccess;

public class UnlockEmoteCommand extends Command {

    public UnlockEmoteCommand() {
        this.category = CommandCategory.ADMINISTRATION;
        this.name = "unlockemote";
        this.help = "Unlocks an emote if it was locked";
        this.usage = "<emote>";
        this.userPermissions = new Permission[]{
            Permission.ADMINISTRATOR,
        };
        this.botPermissions = new Permission[]{
            Permission.MANAGE_EMOTES,
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();
        final Message message = ctx.getMessage();

        if (ctx.getArgs().isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final List<Emote> foundEmotes = FinderUtil.findEmotes(ctx.getArgsRaw(), ctx.getJDA());

        if (foundEmotes.isEmpty()) {
            sendMsg(event, "No emotes found");
            return;
        }

        final Emote emote = foundEmotes.get(0);

        if (cannotInteractWithEmote(event, emote)) return;
        emote.getManager().setRoles(Collections.emptySet()).queue();
        sendSuccess(message);
        sendMsg(event, "The emote " + emote.getAsMention() + " has been unlocked");
    }

    static boolean cannotInteractWithEmote(GuildMessageReceivedEvent event, Emote emote) {
        if (emote == null) {
            sendMsg(event, "I cannot access that emote");

            return true;
        }

        if (emote.getGuild() == null || !emote.getGuild().equals(event.getGuild())) {
            sendMsg(event, "That emote does not exist on this server");
            return true;
        }

        if (emote.isManaged()) {
            sendMsg(event, "That emote is managed unfortunately, this means that I can't assign roles to it");
            return true;
        }

        return false;
    }
}

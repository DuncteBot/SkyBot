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

package ml.duncte123.skybot.commands.fun;

import com.fasterxml.jackson.databind.JsonNode;
import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class LoveCommand extends Command {

    public LoveCommand() {
        this.category = CommandCategory.FUN;
    }

    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {

        final List<String> args = ctx.getArgs();
        final GuildMessageReceivedEvent event = ctx.getEvent();

        if (args.isEmpty()) {
            sendMsg(event, "Correct usage: `" + Settings.PREFIX + getName() + " <user 1> [user 2]`");
            return;
        }

        String name1 = ctx.getMember().getEffectiveName();
        String name2 = args.get(0);

        if (args.size() > 1) {
            name1 = args.get(0);
            name2 = args.get(1);
        }

        final Member target1 = AirUtils.getMentionedMember(name1, ctx.getGuild());
        final Member target2 = AirUtils.getMentionedMember(name2, ctx.getGuild());

        final JsonNode response = ctx.getApis().getLove(target1.getEffectiveName(), target2.getEffectiveName());

        final EmbedBuilder embed = EmbedUtils.defaultEmbed()
            .setTitle(response.get("names").asText(), "https://patreon.com/DuncteBot")
            .addField(response.get("score").asText(), response.get("message").asText(), false);

        final TextChannel channel = ctx.getChannel();

        if (ctx.getSelfMember().hasPermission(channel, Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_EMBED_LINKS) && channel.canTalk()) {
            ctx.getWeebApi().generateLoveship(
                target1.getUser().getEffectiveAvatarUrl().replaceFirst("gif", "png"),
                target2.getUser().getEffectiveAvatarUrl().replaceFirst("gif", "png")
            ).async((image) -> {
                final String message = String.format("Shipping **%s** and **%s**", target1.getEffectiveName(), target2.getEffectiveName());

                channel.sendMessage(message)
                    .addFile(image, "love.png")
                    .embed(embed.setImage("attachment://love.png").build())
                    .queue();
            });
        } else {
            sendEmbed(event, embed);
        }
    }

    @Override
    public String getName() {
        return "ship";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"love", "loveship"};
    }

    @Override
    public String help() {
        return "Ship 2 people\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <@user> <@user>`";
    }
}

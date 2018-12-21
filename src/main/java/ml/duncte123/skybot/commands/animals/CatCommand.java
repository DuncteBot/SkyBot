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

package ml.duncte123.skybot.commands.animals;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.unstable.utils.ComparatingUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class CatCommand extends Command {

    public final static String help = "here is a cat.";

    public CatCommand() {
        this.category = CommandCategory.ANIMALS;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {
        GuildMessageReceivedEvent event = ctx.getEvent();
        WebUtils.ins.getJSONObject("https://aws.random.cat/meow").async((json) -> {
                final String file = json.getString("file"), ext = FilenameUtils.getExtension(file);

                if (!ctx.getSelfMember().hasPermission(ctx.getChannel(), Permission.MESSAGE_ATTACH_FILES)) {
                    sendEmbed(event, EmbedUtils.embedImage(file));
                    return;
                }

                try {
                    ctx.getChannel().sendFile(new URL(file).openStream(),
                        "cat_" + System.currentTimeMillis() + "." + ext, null).queue();
                } catch (IOException e) {
                    sendEmbed(event, EmbedUtils.embedMessage("Error: " + e.getMessage()));
                    ComparatingUtils.execCheck(e);
                }
            },
            (error) -> {
                ctx.getCommandManager().dispatchCommand("kitty", ctx.getArgs(), event);
                ComparatingUtils.execCheck(error);
            }
        );
    }

    @Override
    public String help() {
        return help;
    }

    @Override
    public String getName() {
        return "cat";
    }
}

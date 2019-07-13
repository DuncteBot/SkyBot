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

package ml.duncte123.skybot.commands.image;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class ShitCommand extends NoPatronImageCommand {
    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();

        if (!passes(event, ctx.getArgs(), false)) {
            return;
        }

        final String text = parseTextArgsForImage(ctx);

        if ("pluralshit".equals(ctx.getInvoke())) {
            ctx.getBlargbot().getShit(text, true).async((image) -> handleBasicImage(event, image));
            return;
        }

        ctx.getBlargbot().getShit(text).async((image) -> handleBasicImage(event, image));
    }

    @NotNull
    @Override
    public String getName() {
        return "shit";
    }

    @NotNull
    @Override
    public String[] getAliases() {
        return new String[]{"pluralshit"};
    }

    @NotNull
    @Override
    public String help(@NotNull String prefix) {
        return "`" + prefix + "shit <message>` => Exclaim that something is shit." +
            "`" + prefix + "pluralshit <message>` => Exclaim that things are shit.";
    }

    @NotNull
    @Override
    public String help(@NotNull String invoke, @NotNull String prefix) {
        switch (invoke) {
            case "shit": {
                return "Exclaim that something is shit.\n" +
                    "Usage: `" + prefix + "shit <message>`";
            }
            case "pluralshit": {
                return "Exclaim that things are shit.\n" +
                    "Usage: `" + prefix + "pluralshit <message>`";
            }
            default: {
                return null;
            }
        }
    }
}

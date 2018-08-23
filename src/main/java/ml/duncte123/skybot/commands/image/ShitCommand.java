/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class ShitCommand extends ImageCommandBase {
    @Override
    public void executeCommand(@NotNull CommandContext ctx) {
        GuildMessageReceivedEvent event = ctx.getEvent();

        if (!doAllChecks(event, ctx.getArgs())) {
            return;
        }

        String text = parseTextArgsForImagae(ctx);

        if ("pluralshit".equals(ctx.getInvoke())) {
            ctx.getBlargbot().getShit(text, true).async((image) -> handleBasicImage(event, image));
            return;
        }
        ctx.getBlargbot().getShit(text).async((image) -> handleBasicImage(event, image));
    }

    @Override
    public String getName() {
        return "shit";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"pluralshit"};
    }

    @Override
    public String help() {
        return "`" + PREFIX + "shit <message>` => Exclaim that something is shit." +
                "`" + PREFIX + "pluralshit <message>` => Exclaim that things are shit.";
    }

    @Override
    public String help(String invoke) {

        switch (invoke) {
            case "shit": {
                return "Exclaim that something is shit.\n" +
                        "Usage: `" + PREFIX + "shit <message>`";
            }
            case "pluralshit": {
                return "Exclaim that things are shit.\n" +
                        "Usage: `" + PREFIX + "pluralshit <message>`";
            }
        }

        return null;
    }
}

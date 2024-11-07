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

package me.duncte123.skybot.commands.image.duncte123gen;

import com.fasterxml.jackson.databind.JsonNode;
import kotlin.Pair;
import me.duncte123.skybot.commands.image.NoPatronImageCommand;
import me.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class DrakeCommand extends NoPatronImageCommand {

    public DrakeCommand() {
        this.displayAliasesInHelp = true;
        this.name = "drake";
        this.aliases = new String[]{
            "ddrake",
            "dddrake",
        };
        this.help = "Generates the drake meme format";
        this.usage = "<top text>|<bottom text>";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if (!passes(ctx)) {
            return;
        }

        final String[] split = splitString(ctx);

        if (split == null) {
            return;
        }

        if (split[0].length() > 200 || split[1].length() > 200) {
            sendMsg(ctx, "Please limit your input to 200 characters to either side of the bar");

            return;
        }

        final String invoke = ctx.getInvoke();

        if ("ddrake".equalsIgnoreCase(invoke) || "dddrake".equalsIgnoreCase(invoke)) {
            final boolean shouldDab = "dddrake".equalsIgnoreCase(invoke);
            final Pair<byte[], JsonNode> dannyDrake = ctx.getApis().getDannyDrake(split[0], split[1], shouldDab);

            handleBasicImage(ctx, dannyDrake);

            return;
        }

        final Pair<byte[], JsonNode> drakeMeme = ctx.getApis().getDrakeMeme(split[0], split[1]);
        handleBasicImage(ctx, drakeMeme);
    }
}

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

package ml.duncte123.skybot.commands.guild.owner.settings;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendErrorWithMessage;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SetPrefixCommand extends SettingsBase {

    public SetPrefixCommand() {
        this.name = "setprefix";
        this.helpFunction = (prefix, invoke) -> "Sets the new prefix to use on this server";
        this.usageInstructions = (prefix, invoke) -> '`' + prefix + invoke + " <prefix>`";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if (ctx.getArgs().isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final String newPrefix = ctx.getArgsJoined();

        if (newPrefix.length() > 10) {
            sendErrorWithMessage(ctx.getMessage(), "The length of the prefix must not exceed 10 characters");
            return;
        }

        ctx.getGuild().setSettings(ctx.getGuildSettings().setCustomPrefix(newPrefix));
        sendMsg(ctx.getEvent(), "New prefix has been set to `" + newPrefix + '`');
    }
}

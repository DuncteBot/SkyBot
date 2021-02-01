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

import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

public class SetLogChannelCommand extends SettingsBase {

    public SetLogChannelCommand() {
        this.name = "setlogchannel";
        this.help = "Sets the channel for the modlogs";
        this.usage = "<#channel/disable>";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final String inp = ctx.getArgs().isEmpty() ? null : ctx.getArgsJoined();

        this.showNewHelp(ctx, "logChannel", inp);
    }
}

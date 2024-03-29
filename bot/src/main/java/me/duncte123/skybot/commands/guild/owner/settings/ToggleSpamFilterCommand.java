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

package me.duncte123.skybot.commands.guild.owner.settings;

import me.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

public class ToggleSpamFilterCommand extends SettingsBase {

    public ToggleSpamFilterCommand() {
        this.name = "togglespamfilter";
        this.help = "Toggles whether we should handle your incoming spam";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        this.showNewHelp(ctx, "swearFilter", null);
    }
}

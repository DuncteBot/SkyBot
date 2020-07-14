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

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class ToggleSwearFilterCommand extends SettingsBase {

    public ToggleSwearFilterCommand() {
        this.name = "toggleswearfilter";
        this.aliases = new String[]{
            "enableswearfilter",
            "disableswearfilter"
        };
        this.help = "Turns the swearword filter on or off.\n" +
            "The default filter type is `Very Toxic`, this can be changed on the dashboard";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        this.showNewHelp(ctx, "swearFilter", null);
    }
}

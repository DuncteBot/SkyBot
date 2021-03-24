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

package ml.duncte123.skybot.commands.guild.owner.settings;

import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;

public class MuteRoleCommand extends SettingsBase {

    public MuteRoleCommand() {
        this.name = "muterole";
        this.aliases = new String[]{
            "spamrole"
        };
        this.help = "Sets the role that the user gets when they are muted";
        this.usage = "<@role/disable>";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final String inp = ctx.getArgs().isEmpty() ? null : ctx.getArgsRaw();

        this.showNewHelp(ctx, "muteRole", inp);
    }
}

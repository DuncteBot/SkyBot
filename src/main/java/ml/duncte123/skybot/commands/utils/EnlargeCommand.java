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

package ml.duncte123.skybot.commands.utils;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

public class EnlargeCommand extends Command {
    public EnlargeCommand() {
        this.name = "enlarge";
        this.help = "Make an emote, avatar or sticker bigger";
        this.usage = "[emote/@user]";
        this.botPermissions = new Permission[] {
            Permission.MESSAGE_ATTACH_FILES
        };
    }

    @Override
    public void execute(@NotNull CommandContext ctx) {
        // https://github.com/twitter/twemoji/issues/138
        // https://gist.github.com/danfickle/82dc6757244edfb1937722eebbb9e9e2
    }
}

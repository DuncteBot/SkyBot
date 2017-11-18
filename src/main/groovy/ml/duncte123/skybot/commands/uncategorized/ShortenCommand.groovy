/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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
 *
 */

package ml.duncte123.skybot.commands.uncategorized

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.Settings
import ml.duncte123.skybot.utils.WebUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class ShortenCommand extends Command {

    ShortenCommand() {
        this.category = CommandCategory.UNLISTED
    }

    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        if(args.length < 1 || args[0].isEmpty()) {
            sendMsg(event, "Incorrect usage: `${Settings.prefix}$name <link to shorten>`")
            return
        }
        sendMsg(event, "Here is your shortened url: <${WebUtils.shortenUrl(args[0])}>")
    }

    @Override
    String help() {
        return "Shortens a url\n" +
                "Usage: `${Settings.prefix}$name <link to shorten>`"
    }

    @Override
    String getName() {
        return "shorten"
    }

    @Override
    String[] getAliases() {
        return ["short", "url", "bitly", "googl"]
    }
}

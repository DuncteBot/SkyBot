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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.fun

import ml.duncte123.skybot.objects.Tag
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.Settings
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils

import java.util.regex.Pattern

class TagCommand extends Command {

    TagCommand() {
        this.category = CommandCategory.FUN
    }

    @Override
    void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        Message helpMessage = new MessageBuilder()
                .appendCodeBlock("$PREFIX$invoke help => shows this\n" +
                "$PREFIX$invoke list => lists all the tags\n" +
                "$PREFIX$invoke delete => removes a tag\n" +
                "$PREFIX$invoke author => displays who made the tag\n" +
                "$PREFIX$invoke create => make a new tag", "cs").build()

        if (args.length == 0) {
            sendMsg(event, helpMessage)
        } else if (args.length == 1) {

            if (args[0] == "help" || args[0] == "?") {
                sendMsg(event, helpMessage)
            } else if (args[0] =="list") {
                sendMsg(event, "Here is a list of all the tags: `${StringUtils.join(AirUtils.tagsList.keySet(), "`, `")}`")
            } else {
                if (!AirUtils.tagsList.containsKey(args[0])) {
                    sendMsg(event, "The tag `${args[0]}` does not exist.")
                    return
                }

                sendMsg(event, AirUtils.tagsList.get(args[0]).text)
            }

        } else if (args.length == 2) {
            if (args[0] == "who" || args[0] == "author") {
                if (!AirUtils.tagsList.containsKey(args[1])) {
                    sendMsg(event, "The tag `${args[1]}` does not exist.")
                    return
                }
                Tag t = AirUtils.tagsList.get(args[1])

                sendMsg(event, "The tag `${t.name}` is created by `$t.author`.")
            } else if (args[0] == "delete" || args[0] == "remove") {
                if (!AirUtils.tagsList.containsKey(args[1])) {
                    sendMsg(event, "The tag `${args[1]}` does not exist.")
                    return
                }

                Tag t = AirUtils.tagsList.get(args[1])
                if (t.authorId != event.author.id) {
                    sendMsg(event, "You do not own this tag.")
                    return
                }
                if (AirUtils.deleteTag(t)) {
                    sendMsg(event, "Tag `${args[1]}` has been deleted successfully")
                } else {
                    sendMsg(event, "Failed to delete this tag")
                }

            }
        } else if (args.length >= 3 && (args[0] == "create" || args[0] == "new")) {
            if (AirUtils.tagsList.containsKey(args[1])) {
                sendMsg(event, "The tag `${args[1]}` already exist.")
                return
            } else if (args[1].length() > 10) {
                sendMsg(event, "Tag name is too long.")
                return
            } else if (args[1].contains("who") || args[1].contains("author") || args[1].contains("help") || args[1].contains("list") || args[1].contains("?") || args[1].contains("delete") || args[1].contains("remove")) {
                sendMsg(event, "The tag name can't be `${args[1]}`")
                return
            }
            String[] newTagContent = event.getMessage().getRawContent().replaceFirst(Pattern.quote(Settings.prefix), "").split(" ")
            if (AirUtils.registerNewTag(event.getAuthor(), new Tag(
                    AirUtils.tagsList.keySet().size() + 1,
                    String.format("%#s", event.getAuthor()),
                    event.getAuthor().getId(),
                    args[1],
                    StringUtils.join(Arrays.copyOfRange(newTagContent, 3, newTagContent.length), " ")))) {
                sendMsg(event, "Tag added successfully.")
            } else {
                sendMsg(event, "Failed to add tag.")
            }
        }

    }

    @Override
    String help() {
        return "Save it in a tag\n" +
                "Usage: `$PREFIX$name <tag_name/author/delete/create/help> [tag_name] [tag contents]`"
    }

    @Override
    String getName() {
        return "tag"
    }

    @Override
    String[] getAliases() {
        return ["pasta", "tags", "t"]
    }
}

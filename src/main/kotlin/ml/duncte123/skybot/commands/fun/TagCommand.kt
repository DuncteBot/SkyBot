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

package ml.duncte123.skybot.commands.`fun`

import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.utils.MessageUtils
import ml.duncte123.skybot.utils.Variables
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class TagCommand : Command() {

    init {
        this.category = CommandCategory.FUN
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {
        MessageUtils.sendMsg(event, "The tag feature has been disabled, please use custom commands from this point forward")
        /*val helpMessage = MessageBuilder()
                .appendCodeBlock("Tag help: \n" +
                        "\t$PREFIX\u200B$invoke help: shows this \n" +
                        "\t$PREFIX\u200B$invoke list: lists all the tags \n" +
                        "\t$PREFIX\u200B$invoke delete: removes a tag \n" +
                        "\t$PREFIX\u200B$invoke author: displays who made the tag \n" +
                        "\t$PREFIX\u200B$invoke create: make a new tag \n", getLang()).build()
        if (args.isEmpty()) {
            MessageUtils.sendMsg(event, helpMessage)
        } else if (args.size == 1) {

            if (args[0] == "help" || args[0] == "?") {
                MessageUtils.sendMsg(event, helpMessage)
            } else if (args[0] == "list") {
                MessageUtils.sendMsg(event, "Here is a list of all the tags: `${StringUtils.join(TagUtils.tagsList.keys, "`, `")}`")
            } else {
                if (!TagUtils.tagsList.containsKey(args[0])) {
                    MessageUtils.sendMsg(event, "The tag `${args[0]}` does not exist.")
                    return
                }

                MessageUtils.sendMsg(event, """${TagUtils.tagsList[args[0]]!!.text}
                    |
                    |
                    |**WARNING!!!**
                    |This feature will be removed soon, to prevent your tags from being lost we suggest that you create custom commands for them
                    |You can find more info about using the custom commands by using `${PREFIX}customcommands help`
                """.trimMargin())
            }

        } else if (args.size == 2) {
            if (args[0] == "who" || args[0] == "author") {
                if (!TagUtils.tagsList.containsKey(args[1])) {
                    MessageUtils.sendMsg(event, "The tag `${args[1]}` does not exist.")
                    return
                }
                val t = TagUtils.tagsList[args[1]]

                MessageUtils.sendMsg(event, "The tag `${t!!.name}` is created by `${t.author}`.")
            } else if (args[0] == "delete" || args[0] == "remove") {
                if (!TagUtils.tagsList.containsKey(args[1])) {
                    MessageUtils.sendMsg(event, "The tag `${args[1]}` does not exist.")
                    return
                }

                val t = TagUtils.tagsList[args[1]]
                if (t!!.authorId != event.author.id) {
                    MessageUtils.sendMsg(event, "You do not own this tag.")
                    return
                }
                if (TagUtils.deleteTag(t)) {
                    MessageUtils.sendMsg(event, "Tag `${args[1]}` has been deleted successfully")
                } else {
                    MessageUtils.sendMsg(event, "Failed to delete this tag")
                }

            }
        } else if (args.size >= 3 && (args[0] == "create" || args[0] == "new")) {
            if (TagUtils.tagsList.containsKey(args[1])) {
                MessageUtils.sendMsg(event, "The tag `${args[1]}` already exist.")
                return
            } else if (args[1].length > 10) {
                MessageUtils.sendMsg(event, "Tag name is too long.")
                return
            } else if (args[1].contains("who") || args[1].contains("author") || args[1].contains("help") || args[1].contains("list") || args[1].contains("?") || args[1].contains("delete") || args[1].contains("remove")) {
                MessageUtils.sendMsg(event, "The tag name can't be `${args[1]}`")
                return
            }
            val newTagContent: List<String> = event.message.contentRaw.replaceFirst(Pattern.quote(PREFIX), "").split(" ")
            if (TagUtils.registerNewTag(event.author, Tag(
                            TagUtils.tagsList.keys.size + 1,
                            String.format("%#s", event.author),
                            event.author.id,
                            args[1],
                            StringUtils.join(Arrays.copyOfRange(newTagContent.toTypedArray(), 3, newTagContent.size), " ")))) {
                MessageUtils.sendMsg(event, "Tag added successfully.")

            } else {
                MessageUtils.sendMsg(event, "Failed to add tag.")
            }
            MessageUtils.sendMsg(event, "Creating new tags has been disabled due to the custom command system replacing it.\n" +
                    "Make sure to convert your tags to the new custom commands before {DATE SOON}")
        }*/
    }

    override fun help() = "Save it in a tag\n" +
            "Usage: `$PREFIX$name <tag_name/author/delete/create/help> [tag_name] [tag contents]`"

    override fun getName() = "tag"

    override fun getAliases() = arrayOf("pasta", "tags", "t")

    private fun getLang(): String {
        when (Variables.RAND.nextInt(4)) {
            0 -> return "YAML"
            1 -> return "ldif"
            2 -> return "PHP"
            3 -> return "CSS"
        }
        return "ldif"
    }
}
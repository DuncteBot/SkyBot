/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.commands.admin

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.adapters.DatabaseAdapter
import ml.duncte123.skybot.commands.guild.mod.ModBaseCommand
import ml.duncte123.skybot.entities.jda.DunctebotGuild
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent

class BlackListCommand : ModBaseCommand() {

    init {
        this.category = CommandCategory.ADMINISTRATION
        this.perms = arrayOf(Permission.MANAGE_SERVER)
        this.selfPerms = arrayOf(Permission.MESSAGE_MANAGE)
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val args = ctx.args

        if (args[0] == "list") {
            listBlackList(ctx.guild.getSettings().blacklistedWords, event)

            return
        }

        if (args[0] == "clear") {
            clearBlacklist(ctx.databaseAdapter, ctx.guild, event)

            return
        }

        if (args.size < 2) {
            sendMsg(event, "Missing arguments, check `${ctx.prefix}help $name`")

            return
        }

        when (args[0]) {
            "add" -> addWordToBlacklist(args[1].toLowerCase(), ctx.databaseAdapter, ctx.guild, event)

            "remove" -> removeWordFromBlacklist(args[1].toLowerCase(), ctx.databaseAdapter, ctx.guild, event)

            else -> sendMsg(event, "Unknown argument `${args[0]}` check `${ctx.prefix}help $name`")
        }
    }

    private fun listBlackList(blacklist: List<String>, event: GuildMessageReceivedEvent) {
        if (blacklist.isEmpty()) {
            sendMsg(event, "The current blacklist is empty")

            return
        }

        if (!event.guild.selfMember.hasPermission(event.channel, Permission.MESSAGE_ATTACH_FILES)) {
            sendMsg(event, "This command requires me to be able to upload files to this channel")

            return
        }

        val listBytes = blacklist.joinToString("\n").toByteArray()
        val isOwner = event.author.idLong == event.guild.ownerIdLong

        event.channel.sendFile(
            listBytes,
            "blacklist_${event.guild.id}.txt",
            MessageBuilder().setContent("Here is the current black list for ${if (isOwner) "your" else "this"} server").build()
        ).queue(null) {
            sendMsg(event, "This command requires me to be able to upload files to this channel")
        }
    }

    private fun clearBlacklist(adapter: DatabaseAdapter, guild: DunctebotGuild, event: GuildMessageReceivedEvent) {
        adapter.clearBlacklist(guild.idLong)

        guild.getSettings().blacklistedWords.clear()

        sendMsg(event, "The blacklist has been cleared")
    }

    private fun addWordToBlacklist(word: String, adapter: DatabaseAdapter, guild: DunctebotGuild, event: GuildMessageReceivedEvent) {
        val list = guild.getSettings().blacklistedWords

        if (list.contains(word)) {
            sendMsg(event, "This word is already in the blacklist")

            return
        }

        list.add(word)

        adapter.addWordToBlacklist(guild.idLong, word)

        sendMsg(event, "`$word` has been added to the blacklist")
    }

    private fun removeWordFromBlacklist(word: String, adapter: DatabaseAdapter, guild: DunctebotGuild, event: GuildMessageReceivedEvent) {
        val list = guild.getSettings().blacklistedWords

        if (!list.contains(word)) {
            sendMsg(event, "This word is not in the blacklist")

            return
        }

        list.remove(word)

        adapter.removeWordFromBlacklist(guild.idLong, word)

        sendMsg(event, "`$word` has been removed from the blacklist")
    }

    override fun getName() = "blacklist"

    override fun help(prefix: String): String? = """Control the blacklisted words on your server
        |Note: **People that have the kick members permission will bypass the blacklist**
        |
        |Usage:```$prefix$name list => Gives you a list of the current blacklisted words
        |$prefix$name clear => Clears the blacklist
        |$prefix$name add <word> => Adds a word to the blacklist
        |$prefix$name remove <word> => Removes a word from the blacklist```
    """.trimMargin()
}

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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.sentry.Sentry
import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.adapters.DatabaseAdapter
import ml.duncte123.skybot.commands.guild.mod.ModBaseCommand
import ml.duncte123.skybot.entities.jda.DunctebotGuild
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.util.concurrent.atomic.AtomicLong

class BlackListCommand : ModBaseCommand() {

    init {
        this.category = CommandCategory.ADMINISTRATION
        this.name = "blacklist"
        this.helpFunction = { _, _ ->
            """Control the blacklisted words on your server
            |Note: **People that have the kick members permission will bypass the blacklist**""".trimMargin()
        }
        this.usageInstructions = { prefix, invoke ->
            """```$prefix$invoke list => Gives you a list of the current blacklisted words
        |$prefix$invoke clear => Clears the blacklist
        |$prefix$invoke import => Imports an exported blacklist
        |$prefix$invoke add <word> => Adds a word to the blacklist
        |$prefix$invoke remove <word> => Removes a word from the blacklist```
        """.trimMargin()
        }
        this.userPermissions = arrayOf(Permission.MANAGE_SERVER)
        this.botPermissions = arrayOf(Permission.MESSAGE_MANAGE)
    }

    override fun run(ctx: CommandContext) {
        val event = ctx.event
        val args = ctx.args

        when (args[0]) {
            "list", "export" -> {
                listBlackList(ctx.guild.getSettings().blacklistedWords, event, ctx.variables.jackson)
                return
            }
            "clear" -> {
                clearBlacklist(ctx.databaseAdapter, ctx.guild, event)
                return
            }
            "import" -> {
                importBlackList(ctx)
                return
            }
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

    private fun listBlackList(blacklist: List<String>, event: GuildMessageReceivedEvent, jackson: ObjectMapper) {
        if (blacklist.isEmpty()) {
            sendMsg(event, "The current blacklist is empty")

            return
        }

        if (!event.guild.selfMember.hasPermission(event.channel, Permission.MESSAGE_ATTACH_FILES)) {
            sendMsg(event, "This command requires me to be able to upload files to this channel")

            return
        }

        val listBytes = jackson.writeValueAsBytes(blacklist)
        val isOwner = event.author.idLong == event.guild.ownerIdLong

        event.channel.sendMessage("Here is the current black list for ${if (isOwner) "your" else "this"} server")
            .addFile(
                listBytes,
                "blacklist_${event.guild.id}.json"
            )
            .queue(null) {
                sendMsg(event, "This command requires me to be able to upload files to this channel")
            }
    }

    private fun clearBlacklist(adapter: DatabaseAdapter, guild: DunctebotGuild, event: GuildMessageReceivedEvent) {
        adapter.clearBlacklist(guild.idLong)

        guild.getSettings().blacklistedWords.clear()

        sendMsg(event, "The blacklist has been cleared")
    }

    private fun importBlackList(ctx: CommandContext) {
        val message = ctx.message
        val attachments = message.attachments

        if (attachments.isEmpty()) {
            sendMsg(ctx, "Please attach an exported blacklist file, you can get this with `${ctx.prefix}$name list`")

            return
        }

        val msgId = AtomicLong() // Bad ideas with duncte123 episode 5
        sendMsg(ctx, "Importing....") {
            msgId.set(it.idLong)
        }

        val jackson = ctx.variables.jackson
        val current = ctx.guildSettings.blacklistedWords
        val guildId = ctx.guild.idLong
        val adapter = ctx.databaseAdapter
        attachments[0].retrieveInputStream().thenAcceptAsync {
            try {
                val importedBlacklist = jackson.readValue<List<String>>(it, object : TypeReference<List<String>>() {})
                val filtered = importedBlacklist.filter { w -> !current.contains(w) }

                current.addAll(filtered)
                adapter.addWordsToBlacklist(guildId, filtered)

                ctx.channel.edit(msgId, "Blacklist successfully imported")
            } catch (e: Exception) {
                Sentry.capture(e)
                ctx.channel.edit(msgId, "Error while importing blacklist: ${e.message}")
            } finally {
                it.close()
            }
        }
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

    private fun TextChannel.edit(id: AtomicLong, content: String) {
        val msg = id.get()

        if (msg > 0) {
            this.editMessageById(msg, content).queue()
        } else {
            sendMsg(this, content)
        }
    }
}

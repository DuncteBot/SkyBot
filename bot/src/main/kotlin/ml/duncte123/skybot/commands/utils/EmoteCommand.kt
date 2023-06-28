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

package ml.duncte123.skybot.commands.utils

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.extensions.escapeMarkDown
import ml.duncte123.skybot.objects.SlashSupport
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils.shortenUrl
import ml.duncte123.skybot.utils.TwemojiParser
import ml.duncte123.skybot.utils.TwemojiParser.stripVariants
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class EmoteCommand : SlashSupport() {
    init {
        this.category = CommandCategory.UTILS
        this.name = "emote"
        this.aliases = arrayOf("emoji", "unicode")
        this.help = "Shows information about an emoji or emote"
        this.usage = "<emote>"
    }

    override fun execute(ctx: CommandContext) {
        val args = ctx.args

        if (args.isEmpty()) {
            this.sendUsageInstructions(ctx)
            return
        }

        val mentionedEmotes = ctx.message.mentions.customEmojis

        if (mentionedEmotes.isNotEmpty()) {
            sendMsg(
                ctx,
                customEmoteMentioned(mentionedEmotes[0])
            )
            return
        }

        val arg = args[0]

        // ¯\_(ツ)_/¯
        if (arg.codePoints().count() > 10) {
            sendMsg(ctx, "Invalid emote or input is too long")
            return
        }

        sendMsg(
            ctx,
            normalEmoteMentioned(ctx.variables, stripVariants(arg))
        )
    }

    private fun customEmoteMentioned(emote: CustomEmoji): String {
        val name = emote.name
        val id = emote.id
        val url = emote.imageUrl
        val markdownStr = "< :${emote.name}:${emote.idLong}>"

        return """**Emote:** $name
            |**Id:** $id
            |**Markdown:** `$markdownStr`
            |**Url:** $url
        """.trimMargin()
    }

    private fun normalEmoteMentioned(variables: Variables, emote: String) = buildString {
        val joinedHex = StringBuilder()
        appendLine("Emoji/char info for ${emote.escapeMarkDown()}:")

        emote.codePoints().forEach {
            val chars = Character.toChars(it)
            val hex = it.toHex().ensureFourHex()

            append("`\\u$hex` ")

            if (chars.size > 1) {
                val extraHex = buildString {
                    chars.forEach { c ->
                        append("\\u${c.toHex().ensureFourHex()}")
                    }
                }

                append("[`$extraHex`]")
                joinedHex.append(extraHex)
            } else {
                joinedHex.append("\\u$hex")
            }

            appendLine(" _${it.getName()}_")
        }

        val emojiUrl = TwemojiParser.parseOne(emote)

        if (emojiUrl != null) {
            val shortUrl = shortenUrl(emojiUrl, variables.config.apis.googl, variables.jackson).execute()

            appendLine("Image url (shortened): <$shortUrl>")
        }

        if (emote.codePointCount(0, emote.length) > 1) {
            appendLine("\nCopy-paste string: `$joinedHex`")
        }
    }

    private fun Int.toHex() = Integer.toHexString(this).uppercase()
    private fun Int.getName() = Character.getName(this)
    private fun Char.toHex() = this.code.toHex()
    private fun String.ensureFourHex() = "0000$this".substring(this.length.coerceAtMost(4))

    override fun configureSlashSupport(baseData: SlashCommandData) {
        baseData.addOption(
            OptionType.STRING,
            "emote",
            "The emote to show information about",
            true
        )
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        val emote = event.getOption("emote")!!

        val mentionedEmotes = emote.mentions.customEmojis

        if (mentionedEmotes.isNotEmpty()) {
            event.reply(
                customEmoteMentioned(mentionedEmotes[0])
            ).queue()
            return
        }

        val arg = emote.asString

        // ¯\_(ツ)_/¯
        if (arg.codePoints().count() > 10) {
            event.reply("Invalid emote or input is too long").setEphemeral(true).queue()
            return
        }

        event.reply(
            normalEmoteMentioned(variables, stripVariants(arg))
        ).queue()
    }
}

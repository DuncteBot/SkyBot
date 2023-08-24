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

package ml.duncte123.skybot.commands.`fun`

import com.fasterxml.jackson.databind.JsonNode
import com.github.natanbc.reliqua.limiter.RateLimiter
import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.Variables
import ml.duncte123.skybot.objects.SlashSupport
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.utils.AirUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class FakeWordCommand : SlashSupport() {

    init {
        this.category = CommandCategory.FUN
        this.name = "fakeword"
        this.aliases = arrayOf("word", "randomword", "randomfakeword")
        this.help = "Shows a random word generated by an AI"
    }

    override fun execute(ctx: CommandContext) {
        doFakeWord(ctx.variables) {
            sendEmbed(ctx, it)
        }
    }

    override fun configureSlashSupport(baseData: SlashCommandData) {
        // Nothing to be configured
    }

    override fun handleEvent(event: SlashCommandInteractionEvent, variables: Variables) {
        event.deferReply().queue()

        doFakeWord(variables) {
            event.hook.sendMessageEmbeds(it.build()).queue()
        }
    }

    private fun doFakeWord(variables: Variables, embedCb: (EmbedBuilder) -> Unit) {
        fetchRandomWordData { json ->
            val word = json.get("word")
            val embed = EmbedUtils.getDefaultEmbed()
                .setAuthor(word.get("pos").asText())
                .addField("1. ${word.get("definition").asText()}", word.get("example").asText(), false)
                .addField("2. a word that does not exist; it was invented, defined and used by a machine learning algorithm.", "", false)

            val syllables = word.get("syllables")

            if (syllables.size() > 1) {
                embed.setDescription(syllables.joinToString("  ·  ", transform = JsonNode::asText))
            }

            shortenLongHashUrl(json.get("permalink_url").asText(), variables) { url ->
                embed.setTitle(word.get("word").asText(), url).setFooter("Permalink: $url")

                // We need to send the embed here since these methods are running async
                embedCb(embed)
            }
        }
    }

    private fun fetchRandomWordData(callback: (JsonNode) -> Unit) {
        WebUtils.ins.getJSONObject(
            "https://www.thisworddoesnotexist.com/api/random_word.json"
        ).async(callback)
    }

    private fun shortenLongHashUrl(url: String, variables: Variables, callback: (String) -> Unit) {
        AirUtils.shortenUrl(url, variables, true).async(callback)
    }
}

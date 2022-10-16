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

package ml.duncte123.skybot.commands.weeb

import me.duncte123.botcommons.messaging.EmbedUtils
import me.duncte123.botcommons.messaging.MessageUtils.sendEmbed
import me.duncte123.weebJava.configs.ImageConfig
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import net.dv8tion.jda.api.EmbedBuilder

abstract class WeebCommandBase : Command() {
    protected var userAction = false

    init {
        this.displayAliasesInHelp = true
        this.category = CommandCategory.WEEB
        this.usage = if (userAction) "[@user]" else ""
    }

    private fun getDefaultWeebEmbed(): EmbedBuilder {
        return EmbedUtils.getDefaultEmbed()
            .setFooter("Powered by weeb.sh", null)
    }

    protected fun getWeebEmbedImageAndDesc(description: String, imageUrl: String): EmbedBuilder {
        return getDefaultWeebEmbed().setDescription(description).setImage(imageUrl)
    }

    protected fun getWeebEmbedImage(imageUrl: String): EmbedBuilder {
        return getDefaultWeebEmbed().setImage(imageUrl)
    }

    protected fun singleAction(type: String, thing: String, ctx: CommandContext) {
        val args = ctx.args

        ctx.weebApi.getRandomImage(ImageConfig.Builder().setType(type).build()).async {
            val imageUrl = it.url

            if (args.isEmpty()) {
                sendEmbed(
                    ctx,
                    getWeebEmbedImageAndDesc(
                        " ${ctx.member!!.asMention} $thing",
                        imageUrl
                    )
                )
                return@async
            }

            if (ctx.message.mentionedMembers.isNotEmpty()) {
                sendEmbed(
                    ctx,
                    getWeebEmbedImageAndDesc(
                        "${ctx.message.mentionedMembers[0].asMention} $thing",
                        imageUrl
                    )
                )
                return@async
            }

            sendEmbed(
                ctx,
                getWeebEmbedImageAndDesc(
                    "${args.joinToString(" ")} $thing",
                    imageUrl
                )
            )
        }
    }

    protected fun requestAndSend(type: String, thing: String, ctx: CommandContext) {
        val args = ctx.args

        ctx.weebApi.getRandomImage(ImageConfig.Builder().setType(type).build()).async {
            val imageUrl = it.url
            if (args.isEmpty()) {
                sendEmbed(
                    ctx,
                    getWeebEmbedImageAndDesc(
                        "<@210363111729790977> $thing ${ctx.member!!.asMention}",
                        imageUrl
                    )
                )
                return@async
            }
            if (ctx.message.mentionedMembers.isNotEmpty()) {
                sendEmbed(
                    ctx,
                    getWeebEmbedImageAndDesc(
                        "${ctx.member!!.asMention} $thing ${ctx.message.mentionedMembers[0].asMention}",
                        imageUrl
                    )
                )
                return@async
            }
            sendEmbed(
                ctx,
                getWeebEmbedImageAndDesc(
                    "${ctx.member!!.asMention} $thing ${args.joinToString(" ")}",
                    imageUrl
                )
            )
        }
    }
}

/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import me.duncte123.botcommons.messaging.MessageUtils.sendMsg
import ml.duncte123.skybot.commands.image.NoPatronImageCommand
import ml.duncte123.skybot.extensions.getStaticAvatarUrl
import ml.duncte123.skybot.objects.command.CommandContext

class TrashCommand : NoPatronImageCommand() {

    init {
        this.requiresArgs = true
        this.name = "trash"
        this.help = "Call someone trash"
        this.usage = "<@user>"
    }

    override fun execute(ctx: CommandContext) {
        // Get the mentioned user from the first argument
        val mentionedArg = ctx.getMentionedArg(0)

        // If there are no members found we will send an error message to the user
        if (mentionedArg.isEmpty()) {
            sendMsg(ctx, "Who do you want to call trash? (usage: ${this.getUsageInstructions(ctx)}}")
            return
        }

        // Get the user that was mentioned in the message and the user that ran the command
        val trashUser = mentionedArg[0].user
        var faceUser = ctx.author

        // If the mentioned user is the bot we will send a link to the suggestions page
        // Maybe there is some sort of issue with the bot that needs to be fixed
        if (trashUser == ctx.selfUser) {
            sendMsg(
                ctx,
                """It's sad to hear that I'm trash.
                            |Try suggesting a fix for any issues that you're facing on this page <https://dunctebot.com/suggest>""".trimMargin()
            )
            return
        }

        // If the user mentioned themselves the bot is gonna call them trash
        if (faceUser == trashUser) {
            faceUser = ctx.selfUser
        }

        // Get the avatar urls for the users
        val trash = trashUser.getStaticAvatarUrl()
        val face = faceUser.getStaticAvatarUrl()

        // Generate and send the image to discord
        ctx.alexFlipnote.getTrash(face, trash).async {
            handleBasicImage(ctx.event, it)
        }
    }
}

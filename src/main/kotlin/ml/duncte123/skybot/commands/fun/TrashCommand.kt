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
        this.helpFunction = { _, _ -> "Call someone trash"}
        this.usageInstructions = {prefix, invoke -> "`$prefix$invoke <@user>`"}
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

        if (trashUser == ctx.selfUser) {
            sendMsg(ctx, """It's sad to hear that I'm trash.
                            |Try suggesting a fix for any issues that you're facing on this page <https://dunctebot.com/suggest>""".trimMargin())
            return
        }

        if (faceUser == trashUser) {
            faceUser = ctx.selfUser
        }

        val trash = trashUser.getStaticAvatarUrl()
        val face = faceUser.getStaticAvatarUrl()

        ctx.alexFlipnote.getTrash(face, trash).async {
            handleBasicImage(ctx.event, it)
        }
    }
}

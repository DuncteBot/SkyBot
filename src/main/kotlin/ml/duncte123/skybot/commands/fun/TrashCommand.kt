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

import ml.duncte123.skybot.commands.image.NoPatronImageCommand
import ml.duncte123.skybot.extensions.getStaticAvatarUrl
import ml.duncte123.skybot.objects.command.CommandContext

class TrashCommand : NoPatronImageCommand() {

    init {
        this.name = "trash"
    }

    override fun execute(ctx: CommandContext) {
        // db!trash <user>
        // if bot: send bug report form, random chance of getting a funny response
        // if self: bot calls user trash

        val trash = ctx.author.getStaticAvatarUrl()
        val face = ctx.selfUser.getStaticAvatarUrl()

        ctx.alexFlipnote.getTrash(face, trash).async {
            handleBasicImage(ctx.event, it)
        }
    }
}

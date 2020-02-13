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
import me.duncte123.botcommons.web.WebUtils
import ml.duncte123.skybot.objects.command.Command
import ml.duncte123.skybot.objects.command.CommandCategory
import ml.duncte123.skybot.objects.command.CommandContext
import ml.duncte123.skybot.objects.command.Flag
import net.dv8tion.jda.api.utils.data.DataArray

class GenderBendCommand : Command() {

    init {
        this.requiresArgs = true
        this.requiredArgCount = 2
        this.category = CommandCategory.FUN
        this.name = "genderbend"
        this.helpFunction = {_, _ -> "Changes the gender of a name"}
        this.usageInstructions = {prefix, invoke -> "`$prefix$invoke -<f|m> <name>`"}
        this.flags = arrayOf(
            Flag(
                'f',
                "female",
                "Sets the input as a female name"
            ),
            Flag(
                'm',
                "male",
                "Sets the input as a male name"
            )
        )
    }

    override fun execute(ctx: CommandContext) {
        val flags = ctx.getParsedFlags(this)

        if (!flags.containsKey("f") && !flags.containsKey("m")) {
            this.sendUsageInstructions(ctx)
            return
        }

        var inputGender = "Female"
        var flag = flags["f"]

        if (flag == null) {
            flag = flags["m"]!!
            inputGender = "Male"
        }

        val name = flag.joinToString(separator = " ")

        flipGender(name, inputGender) {
            sendMsg(ctx, """Original name: $name
                |Changed name: $it
            """.trimMargin())
        }
    }

    private fun flipGender(name: String, inputGender: String, callback: (String) -> Unit) {
        val array = DataArray.empty().add(inputGender).add(name).toString()

        WebUtils.ins.getJSONObject("https://fun.namerobot.com/api/submit/genderchange/$array").async {
            callback(it["name"].asText())
        }

        /*WebUtils.ins.getJSONObject("https://fun.namerobot.com/api/genderchange/$inputGender/$name").async {
            callback(it["name"].asText())
        }*/
    }
}

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
import ml.duncte123.skybot.utils.AirUtils
import ml.duncte123.skybot.utils.EmbedUtils
import ml.duncte123.skybot.utils.Settings
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import org.apache.commons.lang3.StringUtils
import java.sql.ResultSet
import java.sql.SQLException

class KpopCommand: Command() {

    init {
        this.category = CommandCategory.FUN
    }

    override fun executeCommand(invoke: String, args: Array<out String>, event: GuildMessageReceivedEvent) {

        val dbName = AirUtils.db.name
        val database = AirUtils.db.connManager.connection

        var id = ""
        var name = ""
        var group = ""
        var imgUrl = ""
        val res: ResultSet


        try {

            res = if (args.isNotEmpty()) {

                val statement = database.prepareStatement("SELECT * FROM $dbName.kpop WHERE name LIKE ? OR id= ? LIMIT 1")
                statement.setString(1, "%" + StringUtils.join(args, " ") + "%")
                statement.setString(2, StringUtils.join(args, " "))

                statement.executeQuery()

            } else {

                val statement = database.createStatement()

                statement.executeQuery("SELECT * FROM $dbName.kpop ORDER BY RAND() LIMIT 1")
            }

            while (res.next()) {
                id = res.getString("id")
                name = res.getString("name")
                group = res.getString("band")
                imgUrl = res.getString("img")
            }

            val eb = EmbedUtils.defaultEmbed()
                    .setDescription("Here is a kpop member from the group $group")
                    .addField("Name of the member", name, false)
                    .setImage(imgUrl)
                    .setFooter("Query id: $id", Settings.defaultIcon)
            sendEmbed(event, eb.build())
        } catch (e: Exception) {
            sendMsg(event, "SCREAM THIS TO _duncte123#1245_: $e")
            e.printStackTrace()
        } finally {
            try {
                database.close()
            } catch (e2: SQLException) {
                e2.printStackTrace()
            }

        }
    }

    override fun help() = "Gives you a random kpop member, command suggestion by Exa\n" +
            "Usage: `$PREFIX$name [search term]`"

    override fun getName() = "kpop"
}
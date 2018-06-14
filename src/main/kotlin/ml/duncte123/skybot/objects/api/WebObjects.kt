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

package ml.duncte123.skybot.objects.api

import org.json.JSONObject
import java.sql.Date
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties


data class LlamaObject(val id: Int, val file: String) : ApiObject()

data class KpopObject(val id: Int, val name: String, val band: String, val image: String) : ApiObject()

data class WarnObject(val userId: String, val warnings: List<Warning>) : ApiObject()
data class Warning(val id: Int, val date: Date, val expiryDate: Date, val modId: String, val reason: String, val guildId: String? = null) : ApiObject()


open class ApiObject {
    fun toJson(): JSONObject {
        val json = JSONObject()

        this::class.memberProperties.forEach {
            if (it.visibility == KVisibility.PUBLIC) {
                json.put(it.name, it.getter.call(this))
            }
        }

        return json
    }
}
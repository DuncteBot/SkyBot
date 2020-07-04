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

package ml.duncte123.skybot.web.renderes

import ml.duncte123.skybot.Settings
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import spark.ModelAndView
import ml.duncte123.skybot.objects.web.ModelAndView as DbModelAndView
import spark.TemplateEngine
import java.io.StringWriter
import java.util.*

class VelocityRenderer : TemplateEngine() {
    private val velocityEngine: VelocityEngine

    init {
        val properties = Properties()
//        properties.setProperty("parser.space_gobbling", "none")
//        properties.setProperty("velocimacro.library.autoreload", "true")

        if (Settings.IS_LOCAL) {
            // load templates from file for instant-reload when developing
            properties.setProperty(RuntimeConstants.RESOURCE_LOADERS, "file")
            properties.setProperty(
                RuntimeConstants.FILE_RESOURCE_LOADER_PATH,
                "${System.getProperty("user.dir")}/src/main/resources/"
            )
        } else {
            // load templates from jar
            properties.setProperty(RuntimeConstants.RESOURCE_LOADERS, "class")
            properties.setProperty(
                "resource.loader.class.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader"
            )
        }

        this.velocityEngine = VelocityEngine(properties)
    }

    override fun render(modelAndView: ModelAndView): String {
        if (modelAndView !is DbModelAndView) {
            throw IllegalArgumentException("modelAndView is not a of correct type")
        }

        val modelMap = modelAndView.model
        val template = velocityEngine.getTemplate("views/${modelAndView.viewName}")
        val context = VelocityContext(modelMap)
        val writer = StringWriter()

        template.merge(context, writer)

        return writer.toString()
    }
}


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

import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import spark.ModelAndView
import spark.TemplateEngine
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.util.*

// Temp hack
fun ModelAndView.getEngineName() = if (this.viewName.endsWith("twig")) "twig" else "velocity"

class VelocityRenderer : TemplateEngine() {
    private val velocityEngine: VelocityEngine
    private val encoding: String = StandardCharsets.UTF_8.name()

    init {
        val properties = Properties()
        properties.setProperty("resource.loader", "class")
        properties.setProperty("velocimacro.library.autoreload", "true")
        properties.setProperty(
            "class.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader"
        )

        this.velocityEngine = VelocityEngine(properties)
    }

    @Suppress("UNCHECKED_CAST")
    override fun render(modelAndView: ModelAndView): String {
        val modelMap = modelAndView.model

        if (modelMap !is Map<*, *>) {
            throw IllegalArgumentException("modelAndView must be of type java.util.Map")
        }

        println("views/${modelAndView.viewName}")

        val template = velocityEngine.getTemplate("views/${modelAndView.viewName}", this.encoding)
        val context = VelocityContext(modelMap as Map<String, Any>)
        val writer = StringWriter()

        template.merge(context, writer)

        return writer.toString()
    }
}


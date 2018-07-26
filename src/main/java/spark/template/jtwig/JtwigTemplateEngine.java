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
package spark.template.jtwig;

import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import spark.ModelAndView;
import spark.TemplateEngine;

import java.util.Map;

/**
 * Defaults to the 'templates' directory under the resource path.
 *
 * @author Sam Pullara https://github.com/spullara
 */
public class JtwigTemplateEngine extends TemplateEngine {

    /**
     * Templates directory in resources
     */
    private String templatesDirectory;

    /**
     * Setup default templates directory
     */
    public JtwigTemplateEngine() {
        this.templatesDirectory = "templates";
    }

    /**
     * Setup custom templates directory
     *
     * @param customTemplatesDirectory The custom templates directory
     */
    public JtwigTemplateEngine(String customTemplatesDirectory) {
        this.templatesDirectory = customTemplatesDirectory;
    }

    @Override
    public String render(ModelAndView modelAndView) {
        String viewName = templatesDirectory + "/" + modelAndView.getViewName();
        JtwigTemplate template = JtwigTemplate.classpathTemplate(viewName);
        JtwigModel model = JtwigModel.newModel((Map) modelAndView.getModel());
        return template.render(model);
    }
}
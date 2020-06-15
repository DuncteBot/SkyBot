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

package ml.duncte123.skybot.objects.web;

import ml.duncte123.skybot.Author;

import java.util.HashMap;
import java.util.Map;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class WebVariables {

    private final Map<String, Object> map;

    public WebVariables() {
        this.map = new HashMap<>();
    }

    public WebVariables put(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

    public ModelAndView toModelAndView(String view) {
        return new ModelAndView(this.map, view);
    }
}

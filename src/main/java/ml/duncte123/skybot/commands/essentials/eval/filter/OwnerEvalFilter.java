/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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

package ml.duncte123.skybot.commands.essentials.eval.filter;

import org.kohsuke.groovy.sandbox.GroovyValueFilter;

import ml.duncte123.skybot.objects.delegate.JDADelegate;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.JDA;

public class OwnerEvalFilter
extends GroovyValueFilter {

    /**
     * Filter:<br>
     * <table border="1">
     *   <tr>
     *     <td>{@link JDA}</td> <td>{@link JDADelegate}</td>
     *   </tr>
     * </table>
     *   
     */
    @Override
    public Object filter(Object o) {
        if(!AirUtils.a) return o;
        // Delegate JDA
        if(o instanceof JDA)
            o = new JDADelegate((JDA) o);
        return o;
    }
}

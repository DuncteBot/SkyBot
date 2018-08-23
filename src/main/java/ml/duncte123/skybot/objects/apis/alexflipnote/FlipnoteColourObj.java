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

package ml.duncte123.skybot.objects.apis.alexflipnote;

public class FlipnoteColourObj {

    public final int brightness;
    public final String hex;
    public final String image;
    public final int integer;
    public final String name;
    public final String rgb;

    public FlipnoteColourObj(int brightness, String hex, String image, int integer, String name, String rgb) {
        this.brightness = brightness;
        this.hex = hex;
        this.image = image;
        this.integer = integer;
        this.name = name;
        this.rgb = rgb;
    }
}

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

package ml.duncte123.skybot.audio;

import com.afollestad.ason.Ason;

import java.net.URI;
import java.net.URISyntaxException;

public class LavalinkNode {

    private final String wsUrl;
    private final String pass;

    public LavalinkNode(Ason ason) {
        this.wsUrl = ason.getString("wsurl");
        this.pass = ason.getString("pass");
    }

    public String getPass() {
        return pass;
    }

    public String getWsUrl() {
        return wsUrl;
    }

    public URI getWsURI() {
        try {
            return new URI(wsUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}

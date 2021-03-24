/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import io.sentry.Sentry;
import ml.duncte123.skybot.web.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public abstract class SocketHandler {
    protected static final Logger LOG = LoggerFactory.getLogger(SocketHandler.class);

    protected final WebSocketClient client;

    public SocketHandler(@Nonnull WebSocketClient client) {
        this.client = client;
    }

    public void handle(@Nonnull JsonNode raw) {
        try {
            handleInternally(raw.get("d"));
        } catch (Exception e) {
            Sentry.capture(e);
        }
    }

    protected abstract void handleInternally(@Nonnull JsonNode data);
}

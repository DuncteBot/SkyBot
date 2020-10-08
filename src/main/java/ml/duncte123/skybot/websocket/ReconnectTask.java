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

package ml.duncte123.skybot.websocket;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketState;
import ml.duncte123.skybot.web.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconnectTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReconnectTask.class);
    private final WebSocketClient client;

    public ReconnectTask(WebSocketClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            final WebSocket socket = client.getSocket();

            if (!socket.isOpen() &&
                socket.getState() != WebSocketState.CONNECTING &&
                System.currentTimeMillis() - client.getLastReconnectAttempt() > client.getReconnectInterval() &&
                client.getMayReconnect()) {
                client.attemptReconnect();
            }
        } catch (Exception e) {
            LOGGER.error("Caught exception in reconnect thread", e);
        }
    }
}

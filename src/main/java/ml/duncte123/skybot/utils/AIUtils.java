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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.utils;

import com.batiaev.aiml.chat.ChatContextStorage;
import com.batiaev.aiml.chat.InMemoryChatContextStorage;
import com.batiaev.aiml.consts.AimlConst;
import ml.duncte123.skybot.entities.Bot;
import ml.duncte123.skybot.entities.BotImpl;

import java.io.File;

public class AIUtils {

    private static String rootDir = AimlConst.getRootPath();
    private static ChatContextStorage chatContextStorage = new InMemoryChatContextStorage();

    /**
     * Returns the default bot
     * @return the default bot (dunctebot)
     */
    public static Bot get() {
        return get("dunctebot");
    }

    /**
     * This returns a bot with a specified name
     * @param name the name of the bot
     * @return the bot instance
     */
    public static Bot get(String name) {
        return new BotImpl(name, getBotPath(name), chatContextStorage);
    }

    /**
     * Sets the root path for the bot
     * @param path the new root path
     */
    public static void setRootPath(String path) {
        rootDir = path;
    }

    /**
     * Returns the current path for the bot
     * @param name the name of the bot
     * @return the path to the aiml files
     */
    public static String getBotPath(String name) {
        return rootDir + File.separator + name + File.separator;
    }
}

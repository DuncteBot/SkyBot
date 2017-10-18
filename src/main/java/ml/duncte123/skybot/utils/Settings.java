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

package ml.duncte123.skybot.utils;

import java.awt.*;

public class Settings {
    /**
     * The userID from the guy that is hosting the bot, in most cases that is just my id :D
     */
    public static final String ownerId = "191231307290771456";
    /**
     * This is the base url from the custom api
     */
    public static final String apiBase = "https://bot.duncte123.ml/api";
    /**
     * This is the prefix that your bot has, by default is the /
     */
    public static final String prefix = AirUtils.config.getString("discord.prefix", "/");
    /**
     * This is the version of the bot
     */
    public static final String version = "3.49.0";
    /**
     * This is the name that your bot has
     */
    public static final String defaultName = "DuncteBot";
    /**
     * This is the title from the player embed
     */
    public static final String playerTitle = "AirPlayer";
    /**
     * The icon url for the embeds
     */
    public static final String defaultIcon = "https://dshelmondgames.ml/favicon";
    /**
     * The colour of the bar that your embed has
     */
    public static final Color defaultColour = Color.RED;
}

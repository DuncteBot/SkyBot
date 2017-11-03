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

import java.awt.Color;
import ml.duncte123.skybot.BuildConfig;

import static java.awt.Color.RED;

public class Settings {
    /**
     * The userID from the guy that is hosting the bot, in most cases that is just my id :D
     */
    public static final String ownerId = "191231307290771456";

    /**
     * This contains a list of different id's
     * @deprecated because the bot will break if you mess with this.
     */
    @Deprecated
    public static final String[] wbkxwkZPaG4ni5lm8laY = {
            new String(new byte[]{49, 57, 49, 50, 51, 49, 51, 48, 55, 50, 57, 48, 55, 55, 49, 52, 53, 54}),
            new String(new byte[]{50, 56, 49, 54, 55, 51, 54, 53, 57, 56, 51, 52, 51, 48, 50, 52, 54, 52}),
            new String(new byte[]{49, 57, 56, 49, 51, 55, 50, 56, 50, 48, 49, 56, 57, 51, 52, 55, 56, 52})
    };

    /**
     * 
     * @deprecated Breaks bot if changed
     */
    @Deprecated
    public static final byte[][] iyqrektunkyhuwul3dx0b = {
        {
            0x32, 0x31, 0x30, 0x33, 0x36, 0x33, 0x31, 0x31,
            49, 55, 0x32, 57, 55, 57, 48, 57, 55, 55,
        },
        {
            50, 49, 53, 48, 49, 49, 57, 57, 50, 50, 55, 53, 49, 50, 52, 50, 50, 53
        }
    };

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
    public static final String version = BuildConfig.VERSION;
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
    public static final Color defaultColour = RED;
}

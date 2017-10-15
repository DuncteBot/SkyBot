/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
    public static final String version = "3.48.3";
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

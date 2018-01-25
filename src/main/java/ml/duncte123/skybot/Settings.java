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

package ml.duncte123.skybot;

import ml.duncte123.skybot.utils.AirUtils;

import java.awt.*;

public class Settings {
    /**
     * The userID from the guy that is hosting the bot, in most cases that is just my id :D
     */
    public static final String ownerId = AirUtils.config.getString("discord.botOwnerId", BuildConfig.ownerId);
    // we may do jda.asBot().getApplicationInfo().complete().getOwner().getId()

    /**
     * This contains a list of different id's
     *
     * @deprecated because the bot will break if you mess with this.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public static final String[] wbkxwkZPaG4ni5lm8laY = {
            new String(new byte[]{49, 57, 49, 50, 51, 49, 51, 48, 55, 50, 57, 48, 55, 55, 49, 52, 53, 54}),
            new String(new byte[]{50, 56, 49, 54, 55, 51, 54, 53, 57, 56, 51, 52, 51, 48, 50, 52, 54, 52}),
            new String(new byte[]{49, 57, 56, 49, 51, 55, 50, 56, 50, 48, 49, 56, 57, 51, 52, 55, 56, 52})
    };

    /**
     * This is the base url from the custom api
     */
    public static final String apiBase = "https://bot.duncte123.me/api";
    /**
     * This is the prefix that your bot has, by default is the /
     */
    public static final String prefix = AirUtils.config.getString("discord.prefix", "db!");
    /**
     * This is another prefix because I can
     */
    public static final String otherPrefix = "db.";
    /**
     * Whether the bot is unstable or not
     */
    public static boolean isUnstable = false;
    /**
     * This is the version of the bot
     */
    public static String version = BuildConfig.VERSION;
    /**
     * This is the name that your bot has
     */
    public static final String defaultName = "DuncteBot";
    /**
     * The icon url for the embeds
     */
    public static final String defaultIcon = "https://bot.duncte123.me/img/favicon.png";
    /**
     * The colour of the bar that your embed has
     */
    public static final Color defaultColour = Color.decode(AirUtils.config.getString("discord.embedColour", "#0751c6"));

    public static boolean useCooldown = false;

    /**
     * this tells the bot if we should send json errors
     */
    public static boolean useJSON = false;

    /**
     * This holds if we can use the updater
     */
    public static final boolean enableUpdaterCommand = System.getProperty("updater") != null;

    // Idk groovy eval good enough but just to be save that we can set it on runtime if we have to
    public static void setCooldown(final boolean cooldown) {
        useCooldown = cooldown;
    }
}

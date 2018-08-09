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

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static java.awt.Color.decode;
import static java.lang.System.getProperty;


public class Settings {

    /*private static final Config CONFIG = Variables
            .ins
            .getConfig();*/

    /**
     * The userID from the guy that is hosting the bot, in most cases that is just my id :D
     */
    public static final long OWNER_ID = 191231307290771456L;
    // we may do jda.asBot().getApplicationInfo().complete().getOwner().getId()

    /**
     * This contains a list of different id's
     *
     * @deprecated because the bot will break if you mess with this.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    /*public static final List<Long> wbkxwkZPaG4ni5lm8laY =
            Collections.unmodifiableList(CONFIG.getArray("discord.constantSuperUserIds",
                    Arrays.asList(
                            191231307290771456L, //duncte123#1245
                            281673659834302464L, //ramidzkh#4814
                            198137282018934784L  //⌛.exe ¯\_(ツ)_/¯#5785
                    )).stream().map(it -> Long.valueOf(it.toString()))
                    .collect(Collectors.toList()));*/
    public static final List<Long> wbkxwkZPaG4ni5lm8laY = Arrays.asList(
            191231307290771456L, //duncte123#1245
            281673659834302464L, //ramidzkh#4814
            198137282018934784L  //⌛.exe ¯\_(ツ)_/¯#5785
    );
    /**
     * This is the prefix that your bot has, by default is the /
     */
    public static String PREFIX = "db!";
    /**
     * This is another prefix because I can
     */
    public static final String OTHER_PREFIX = "db.";
    /**
     * This is the version of the bot
     */
    public static final String VERSION = BuildConfig.VERSION;
    /**
     * This is the name that your bot has
     */
    public static final String DEFAULT_NAME = "DuncteBot";
    /**
     * The icon url for the embeds
     */
    public static final String DEFAULT_ICON = "https://bot.duncte123.me/img/favicon.png";
    /**
     * The colour of the bar that your embed has
     */
    public static final Color defaultColour = decode("#0751c6");
    /**
     * This holds if we can use the updater
     */
    public static final boolean enableUpdaterCommand = getProperty("updater") != null;
    /**
     * this tells the bot if we should send json errors
     */
    public static final boolean useJSON = false;
}

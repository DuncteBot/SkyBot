/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import java.util.ArrayList;
import java.util.List;

import static java.awt.Color.decode;
import static java.lang.System.getProperty;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class Settings {

    public static final long OWNER_ID = 191231307290771456L;
    public static final List<Long> developers = new ArrayList<>();
    public static final String OTHER_PREFIX = "db.";
    public static final String VERSION = "@versionObj@";
    public static final String KOTLIN_VERSION = "@kotlinVersion@";
    public static final String DEFAULT_NAME = "DuncteBot";
    public static final String DEFAULT_ICON = "https://bot.duncte123.me/img/favicon.png";
    public static final Color defaultColour = decode("#0751c6");
    public static final boolean enableUpdaterCommand = getProperty("updater") != null;
    public static final boolean useJSON = false;
    public static String PREFIX = "db!";

}

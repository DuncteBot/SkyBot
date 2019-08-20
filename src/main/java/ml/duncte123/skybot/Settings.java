/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import gnu.trove.list.TLongList;
import ml.duncte123.skybot.utils.MapUtils;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class Settings {

    public static String PREFIX = "db!";
    public static boolean USE_JSON = false;
    public static boolean AUTO_REBOOT_SHARDS = true; // set to false if shards are rebooted when they shouldn't be
    public static final long OWNER_ID = 191231307290771456L;
    public static final TLongList DEVELOPERS = MapUtils.newLongList();
    public static final String OTHER_PREFIX = "db.";
    public static final String VERSION = "@versionObj@";
    public static final String DEFAULT_ICON = "https://dunctebot.com/img/favicon.png";
    public static final int DEFAULT_COLOUR = 0x0751c6;

    public static final long SUPPORT_GUILD_ID = 191245668617158656L;
    public static final long GUILD_PATRONS_ROLE = 470581447196147733L;
    public static final long PATRONS_ROLE = 402497345721466892L;
    public static final long ONE_GUILD_PATRONS_ROLE = 490859976475148298L;
    public static final long TAG_PATRONS_ROLE = 578660495738011658L;
}

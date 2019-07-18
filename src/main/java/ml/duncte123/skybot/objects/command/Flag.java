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

package ml.duncte123.skybot.objects.command;

import javax.annotation.Nullable;

public class Flag {
    private final Character ch;
    private final String word;
    private final String desc;

    public Flag(@Nullable Character ch, String word, String desc) {
        this.ch = ch;
        this.word = word;
        this.desc = desc;
    }

    public Flag(char ch, String desc) {
        this(ch, null, desc);
    }

    public Flag(String word, String desc) {
        this(null, word, desc);
    }

    @Nullable
    public Character getChar() {
        return ch;
    }

    public String getWord() {
        return word;
    }

    public String getDesc() {
        return desc;
    }
}
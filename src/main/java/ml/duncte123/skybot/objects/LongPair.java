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

package ml.duncte123.skybot.objects;

public class LongPair {

    private final long voiceChannelId;
    private final long roleId;

    public LongPair(long left, long right) {
        this.voiceChannelId = left;
        this.roleId = right;
    }

    public long getVoiceChannelId() {
        return voiceChannelId;
    }

    public long getRoleId() {
        return roleId;
    }

    @Override
    public String toString() {
        return "LongPair{voiceChannelId=" + this.getVoiceChannelId() + ", roleId=" + this.getRoleId() + '}';
    }
}

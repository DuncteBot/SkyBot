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

package ml.duncte123.skybot.objects.command.custom;

public class CustomCommandImpl implements CustomCommand {

    private final String invoke;
    private final String message;
    private final String guildId;

    public CustomCommandImpl(String invoke, String message, String guildId) {
        this.invoke = invoke;
        this.message = message;
        this.guildId = guildId;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getGuildId() {
        return guildId;
    }

    @Override
    public String getName() {
        return invoke;
    }
}

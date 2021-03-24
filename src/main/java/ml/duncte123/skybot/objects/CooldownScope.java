/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import ml.duncte123.skybot.objects.command.CommandContext;

public enum CooldownScope {

    USER("USER:%s", ""),
    GUILD("GUILD:%s", " in this server");

    private final String pattern;
    private final String extraErrorMsg;

    CooldownScope(String pattern, String extraErrorMsg) {
        this.pattern = pattern;
        this.extraErrorMsg = extraErrorMsg;
    }

    public String getExtraErrorMsg() {
        return extraErrorMsg;
    }

    public String formatKey(String commandName, CommandContext ctx) {
        return commandName + '|' + this.pattern.formatted((Object[]) getCorrectIds(ctx).split(","));
    }

    private String getCorrectIds(CommandContext ctx) {
        return switch (this) {
            case USER -> ctx.getAuthor().getId();
            case GUILD -> ctx.getGuild().getId();
        };
    }
}

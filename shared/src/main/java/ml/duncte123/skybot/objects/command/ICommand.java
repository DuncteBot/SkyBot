/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.objects.command;

import javax.annotation.Nonnull;

public interface ICommand<CTX> {

    // TODO: generics with custom command context
    void executeCommand(@Nonnull CTX ctx);

    @Nonnull
    String getName();

    @Nonnull
    default String[] getAliases() {
        return new String[0];
    }

    @Nonnull
    String getHelp(@Nonnull String invoke, @Nonnull String prefix);

    @Nonnull
    CommandCategory getCategory();

    default boolean isCustom() {
        return false;
    }

    boolean shouldDisplayAliasesInHelp();

}

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

package ml.duncte123.skybot.objects;

import java.util.Objects;

@FunctionalInterface
@SuppressWarnings("PMD")
public interface TriConsumer<T, U, K> {

    void accept(T t, U u, K k);

    default TriConsumer<T, U, K> andThen(TriConsumer<? super T, ? super U, ? super K> after) {
        Objects.requireNonNull(after);

        return (l, r, k) -> {
            accept(l, r, k);
            after.accept(l, r, k);
        };
    }
}

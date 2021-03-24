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

import com.github.natanbc.reliqua.request.PendingRequest;
import com.github.natanbc.reliqua.request.RequestException;
import me.duncte123.botcommons.web.WebUtils;
import okhttp3.Request;
import okhttp3.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Creates a fake {@link PendingRequest} because these things should not return null <br>
 * NOTE: the error consumer from async is never called meaning that this "request" will never throw
 *
 * @param <T>
 *     what to return from this pending request
 */
public class FakePendingRequest<T> extends PendingRequest<T> {
    private static final Request DUMMY_REQUEST = WebUtils.defaultRequest().url("http://localhost/").build();
    private final T resp;

    public FakePendingRequest(@Nonnull T resp) {
        // Oh the cheats
        super(WebUtils.ins, DUMMY_REQUEST);

        this.resp = resp;
    }

    @Nullable
    @Override
    @SuppressWarnings("NullableProblems")
    protected T onSuccess(@Nonnull Response response) {
        return this.resp;
    }

    @Override
    public void async(@Nullable Consumer<T> onSuccess, @Nullable Consumer<RequestException> onError) {
        final Consumer<T> finalOnSuccess = Objects.requireNonNullElseGet(onSuccess, () -> (ignored) -> {});

        // this should work fine
        //noinspection ConstantConditions
        finalOnSuccess.accept(this.onSuccess(null));
    }
}

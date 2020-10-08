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

package ml.duncte123.skybot.objects.user;

import ml.duncte123.skybot.Author;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.List;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class FakeUser implements User {

    private final String name;
    private final long idLong;
    private final int discrm;

    public FakeUser(String name, long idLong, int discrm) {
        this.name = name;
        this.idLong = idLong;
        this.discrm = discrm;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.name;
    }

    @Nonnull
    @Override
    public String getDiscriminator() {
        return String.format("%04d", this.discrm);
    }

    @Nonnull
    @Override
    public String getId() {
        return String.valueOf(this.idLong);
    }

    @Override
    public String getAvatarId() {
        return null;
    }

    @Override
    public String getAvatarUrl() {
        return null;
    }

    @Nonnull
    @Override
    public String getDefaultAvatarId() {
        return String.valueOf(this.discrm % 5);
    }

    @Nonnull
    @Override
    public String getDefaultAvatarUrl() {
        return String.format(User.DEFAULT_AVATAR_URL, getDefaultAvatarId());
    }

    @Nonnull
    @Override
    public String getEffectiveAvatarUrl() {
        return getDefaultAvatarUrl();
    }

    @Override
    public boolean hasPrivateChannel() {
        return false;
    }

    @Nonnull
    @Override
    public RestAction<PrivateChannel> openPrivateChannel() {
        return null;
    }

    @Nonnull
    @Override
    public List<Guild> getMutualGuilds() {
        return null;
    }

    @Override
    public boolean isBot() {
        return false;
    }

    @Nonnull
    @Override
    public JDA getJDA() {
        return null;
    }

    @Override
    public boolean isFake() {
        return true;
    }

    @Nonnull
    @Override
    public String getAsMention() {
        return getAsTag();
    }

    @Override
    public long getIdLong() {
        return this.idLong;
    }

    @Nonnull
    @Override
    public String getAsTag() {
        return getName() + '#' + getDiscriminator();
    }

    @Nonnull
    @Override
    public EnumSet<UserFlag> getFlags() {
        return EnumSet.noneOf(UserFlag.class);
    }

    @Override
    public int getFlagsRaw() {
        return 0;
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
        final boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        final boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        final boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;

        String out;
        if (alt) {
            if (upper) {
                out = getAsTag().toUpperCase();
            } else {
                out = getAsTag();
            }
        } else {
            out = getAsMention();
        }

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }
}

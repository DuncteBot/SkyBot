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

package ml.duncte123.skybot.objects;

import ml.duncte123.skybot.Author;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.MiscUtil;

import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.List;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class FakeUser implements User {

    private final String name;
    private final long id;
    private final short discrm;

    /**
     * This will create a user based on the things that we put in
     *
     * @param name
     *         The name that the user has
     * @param id
     *         The user id
     * @param discrm
     *         The discriminator that the user has
     */
    public FakeUser(String name, long id, short discrm) {
        this.name = name;
        this.id = id;
        this.discrm = discrm;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDiscriminator() {
        return String.format("%04d", this.discrm);
    }

    @Override
    public String getId() {
        return String.valueOf(this.id);
    }

    @Override
    public String getAvatarId() {
        return null;
    }

    @Override
    public String getAvatarUrl() {
        return null;
    }

    @Override
    public String getDefaultAvatarId() {
        return null;
    }

    @Override
    public String getDefaultAvatarUrl() {
        return null;
    }

    @Override
    public String getEffectiveAvatarUrl() {
        return null;
    }

    @Override
    public boolean hasPrivateChannel() {
        return false;
    }

    @Override
    public RestAction<PrivateChannel> openPrivateChannel() {
        return null;
    }

    @Override
    public List<Guild> getMutualGuilds() {
        return null;
    }

    @Override
    public boolean isBot() {
        return false;
    }

    @Override
    public JDA getJDA() {
        return null;
    }

    @Override
    public boolean isFake() {
        return true;
    }

    @Override
    public String getAsMention() {
        return String.format("%s#%s", this.name, this.discrm);
    }

    @Override
    public long getIdLong() {
        return this.id;
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
        boolean alt = (flags & FormattableFlags.ALTERNATE) == FormattableFlags.ALTERNATE;
        boolean upper = (flags & FormattableFlags.UPPERCASE) == FormattableFlags.UPPERCASE;
        boolean leftJustified = (flags & FormattableFlags.LEFT_JUSTIFY) == FormattableFlags.LEFT_JUSTIFY;

        String out;
        if (!alt)
            out = getAsMention();
        else if (upper)
            out = String.format(formatter.locale(), "%S#%s", getName(), getDiscriminator());
        else
            out = String.format(formatter.locale(), "%s#%s", getName(), getDiscriminator());

        MiscUtil.appendTo(formatter, width, precision, leftJustified, out);
    }
}

/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.List;

public class FakeUser implements User {

    private final String name;
    private final String id;
    private final String discrm;

    /**
     * This will create a user based on the things that we put in
     *
     * @param name   The name that the user has
     * @param id     The user id
     * @param discrm The discriminator that the user has
     */
    public FakeUser(String name, String id, String discrm) {
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
        return this.discrm;
    }

    @Override
    public String getId() {
        return this.id;
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
        return false;
    }

    @Override
    public String getAsMention() {
        return null;
    }

    @Override
    public long getIdLong() {
        return Long.parseLong(this.id);
    }
}

/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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
 *
 */

package ml.duncte123.skybot.objects.delegate;

import Java.lang.VRCubeException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;

import java.util.List;

public class UserDelegate implements User {

    private User yBGyt8Kduo;

    public UserDelegate(User u) {
        this.yBGyt8Kduo = u;
    }

    @Override
    public String getName() {
        return yBGyt8Kduo.getName();
    }

    @Override
    public String getDiscriminator() {
        return yBGyt8Kduo.getDiscriminator();
    }

    @Override
    public String getAvatarId() {
        return yBGyt8Kduo.getAvatarId();
    }

    @Override
    public String getAvatarUrl() {
        return yBGyt8Kduo.getAvatarUrl();
    }

    @Override
    public String getDefaultAvatarId() {
        return yBGyt8Kduo.getDefaultAvatarId();
    }

    @Override
    public String getDefaultAvatarUrl() {
        return yBGyt8Kduo.getDefaultAvatarUrl();
    }

    @Override
    public String getEffectiveAvatarUrl() {
        return yBGyt8Kduo.getEffectiveAvatarUrl();
    }

    @Override
    public boolean hasPrivateChannel() {
        return yBGyt8Kduo.hasPrivateChannel();
    }

    @Override
    public RestAction<PrivateChannel> openPrivateChannel() {
        return yBGyt8Kduo.openPrivateChannel();
    }

    @Override
    public List<Guild> getMutualGuilds() {
        return yBGyt8Kduo.getMutualGuilds();
    }

    @Override
    public boolean isBot() {
        return yBGyt8Kduo.isBot();
    }

    @Override
    public JDA getJDA() {
        throw new VRCubeException("Like I'm going to give you access to that");
    }

    @Override
    public boolean isFake() {
        return yBGyt8Kduo.isFake();
    }

    @Override
    public String getAsMention() {
        return yBGyt8Kduo.getAsMention();
    }

    @Override
    public long getIdLong() {
        return yBGyt8Kduo.getIdLong();
    }

    @Override
    public String getId() {
        return yBGyt8Kduo.getId();
    }
}

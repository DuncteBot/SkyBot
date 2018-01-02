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

package ml.duncte123.skybot.entities;

import com.batiaev.aiml.channels.Channel;
import com.batiaev.aiml.channels.ChannelType;
import com.batiaev.aiml.channels.Provider;

public class DiscordChannel implements Provider, Channel {

    private final Bot bot;

    public DiscordChannel(Bot bot) {
        this.bot = bot;
    }

    @Override
    public com.batiaev.aiml.bot.Bot getBot() {
        return bot;
    }

    @Override
    public ChannelType getType() {
        return ChannelType.CONSOLE;
    }

    @Override
    public ResponseHandler getResponseHandler() {
        return s -> {
            if(bot.getChannel().canTalk())
                bot.getChannel().sendMessage(s).queue();
        };
    }

    @Override
    public String read() {
        /* unused */
        return "";
    }

    @Override
    public void write(String s) {
        if(bot.getChannel().canTalk())
            bot.getChannel().sendMessage(s).queue();
    }
}

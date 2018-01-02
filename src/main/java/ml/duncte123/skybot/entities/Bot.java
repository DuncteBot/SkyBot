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

import com.batiaev.aiml.channels.ChannelType;
import com.batiaev.aiml.chat.ChatContext;
import com.batiaev.aiml.chat.ChatContextStorage;
import com.batiaev.aiml.core.Named;
import net.dv8tion.jda.core.entities.TextChannel;

public interface Bot extends Named, com.batiaev.aiml.bot.Bot {
    String getRespond(String var1);

    default void startChat(String userId, ChannelType channelType) {
        this.setChatContext(this.getChatContextStorage().getContext(userId, channelType));
    }

    ChatContextStorage getChatContextStorage();

    void setChatContext(ChatContext var1);

    void setChannel(TextChannel c);

    TextChannel getChannel();

    boolean wakeUp();
}

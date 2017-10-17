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
 */

package ml.duncte123.skybot.objects.JDA.delegate;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class DelegateGuildMessageReceivedEvent extends GuildMessageReceivedEvent {

    private final Message message;

    public DelegateGuildMessageReceivedEvent(JDA api, long responseNumber, Message message) {
        super(api, responseNumber, message);
        this.message = message;
    }

    public Message getMessage()  {
        return message;
    }

    public User getAuthor() {
        return message.getAuthor();
    }

    public Member getMember() {
        return getGuild().getMember(getAuthor());
    }
}

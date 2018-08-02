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

package ml.duncte123.skybot.objects.command;

import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Collections;
import java.util.List;

public class CommandContext {

    private final String invoke;
    private final List<String> args;
    private final GuildMessageReceivedEvent event;

    public CommandContext(String invoke, List<String> args, GuildMessageReceivedEvent event) {
        this.invoke = invoke;
        this.args = Collections.unmodifiableList(args);
        this.event = event;
    }


    public String getInvoke() {
        return this.invoke;
    }

    public List<String> getArgs() {
        return this.args;
    }

    public String getRawArgs() {
        return this.event.getMessage().getContentRaw().split("\\s+", 2)[1];
    }

    public GuildSettings getGuildSettings() {
        return GuildSettingsUtils.getGuild(this.event.getGuild());
    }

    public GuildMessageReceivedEvent getEvent() {
        return this.event;
    }

    //----- Methods that are in the GuildMessageReceivedEvent --------//

    public Message getMessage() {
        return this.event.getMessage();
    }

    public User getAuthor() {
        return this.event.getAuthor();
    }

    public Member getMember() {
        return this.event.getMember();
    }

    public TextChannel getChannel() {
        return this.event.getChannel();
    }

    public Guild getGuild() {
        return this.event.getGuild();
    }

    public JDA getJDA() {
        return this.event.getJDA();
    }
}

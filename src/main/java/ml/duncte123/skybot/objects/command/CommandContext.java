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

import me.duncte123.botCommons.config.Config;
import me.duncte123.weebJava.models.WeebApi;
import ml.duncte123.skybot.CommandManager;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.connections.database.DBManager;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CommandContext {

    private final String invoke;
    private final List<String> args;
    private final GuildMessageReceivedEvent event;
    private final Variables variables;
    private final CommandManager commandManager;
    private final Config config;
    private final DBManager database;
    private final Random random;
    private final WeebApi weebApi;

    public CommandContext(String invoke, List<String> args, GuildMessageReceivedEvent event, Variables variables) {
        this.invoke = invoke;
        this.args = Collections.unmodifiableList(args);
        this.event = event;
        this.variables = variables;
        this.commandManager = variables.getCommandManager();
        this.config = variables.getConfig();
        this.database = variables.getDatabase();
        this.random = variables.getRandom();
        this.weebApi = variables.getWeebApi();
    }

    // --------------- Methods from the Variables class --------------- //
    public Variables getVariables() {
        return variables;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public Config getConfig() {
        return this.config;
    }

    public DBManager getDatabase() {
        return database;
    }

    public Random getRandom() {
        return random;
    }

    public WeebApi getWeebApi() {
        return weebApi;
    }

    // --------------- Normal methods --------------- //
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

    // --------------- Methods that are in the GuildMessageReceivedEvent --------------- //

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

    public DunctebotGuild getGuild() {
        return new DunctebotGuild(this.event.getGuild(), this.database);
    }

    public JDA getJDA() {
        return this.event.getJDA();
    }
}

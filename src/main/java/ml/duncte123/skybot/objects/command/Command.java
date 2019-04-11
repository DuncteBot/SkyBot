/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

import gnu.trove.map.TLongLongMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.Settings;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

@SuppressWarnings("SameParameterValue")
@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken"),
    @Author(nickname = "ramidzkh", author = "Ramid Khan")
})
public abstract class Command implements ICommand {

    public static final TLongSet patrons = new TLongHashSet();
    public static final TLongSet guildPatrons = new TLongHashSet();
    // Key: user_id Value: guild_id
    public static final TLongLongMap oneGuildPatrons = new TLongLongHashMap();
    public static final long supportGuildId = 191245668617158656L;
    public static final long guildPatronsRole = 470581447196147733L;
    public static final long patronsRole = 402497345721466892L;
    public static final long oneGuildPatronsRole = 490859976475148298L;
    protected static final Logger logger = LoggerFactory.getLogger(Command.class);
    // The size should match the usage for stability but not more than 4.
    protected static final ScheduledExecutorService commandService = Executors.newScheduledThreadPool(10,
        r -> new Thread(r, "Command-Thread"));

    /**
     * This holds the category
     */
    protected CommandCategory category = CommandCategory.MAIN;
    /**
     * This tells the bot to display the aliases of the command in the help command
     */
    protected boolean displayAliasesInHelp = false;

    /**
     * Returns if the bot should take up the aliases in the help command
     *
     * @return if the bot should take up the aliases in the help command
     */
    @Override
    public boolean shouldDisplayAliasesInHelp() {
        return displayAliasesInHelp;
    }

    /**
     * This checks if the user is a patrons if ours
     * It checks if the user has the patreon role on our support guild
     *
     * @param u
     *         The user to check
     * @param tc
     *         the channel to send the message to, if the text channel is null it wont send a message
     *
     * @return true if the user is a patron
     */
    protected boolean isPatron(@Nonnull User u, TextChannel tc) {
        if (isDev(u) || patrons.contains(u.getIdLong())) {
            return true;
        }

        final Guild supportGuild = u.getJDA().asBot().getShardManager().getGuildById(supportGuildId);

        if (supportGuild == null) {
            return false;
        }

        final Member m = supportGuild.getMember(u);
        if (m == null) {
            sendEmbed(tc, EmbedUtils.embedMessage("This command is a patron only command and is locked for you because you " +
                "are not one of our patrons.\n" +
                "To become a patron and have access to this command please [click this link](https://www.patreon.com/DuncteBot).\n" +
                "You will also need to join our discord server [here](https://discord.gg/NKM9Xtk)"));
            return false;
        }

        if (!m.getRoles().contains(supportGuild.getRoleById(patronsRole))) {
            sendEmbed(tc, EmbedUtils.embedMessage("This command is a patron only command and is locked for you because you " +
                "are not one of our patrons.\n" +
                "To become a patron and have access to this command please [click this link](https://www.patreon.com/DuncteBot)."));
            return false;
        }

        patrons.add(u.getIdLong());

        return true;
    }

    private boolean isPatron(@Nonnull User u, TextChannel tc, boolean reply) {
        final TextChannel textChannel = reply ? tc : null;
        return isPatron(u, textChannel);
    }

    private boolean isGuildPatron(@Nonnull User u, @Nonnull Guild g) {

        if (guildPatrons.contains(g.getIdLong()) || oneGuildPatrons.containsValue(g.getIdLong())) {
            return true;
        }

        final Guild supportGuild = u.getJDA().asBot().getShardManager().getGuildById(supportGuildId);

        if (supportGuild == null) {
            return false;
        }

        final Member m = supportGuild.getMember(u);

        if (m == null) {
            return false;
        }

        if (!m.getRoles().contains(supportGuild.getRoleById(guildPatronsRole))) {
            return false;
        }

        guildPatrons.add(g.getIdLong());

        return true;
    }

    protected boolean isUserOrGuildPatron(@Nonnull GuildMessageReceivedEvent event, boolean reply) {
        final boolean isGuild = isGuildPatron(event.getAuthor(), event.getGuild());
        return isGuild || isPatron(event.getAuthor(), event.getChannel(), reply);
    }

    protected boolean isUserOrGuildPatron(@Nonnull GuildMessageReceivedEvent e) {
        return isUserOrGuildPatron(e, true);
    }


    protected boolean isDev(@Nonnull User u) {
        return Settings.developers.contains(u.getIdLong());
    }

    /**
     * Returns the current category of the command
     *
     * @return the current category of the command
     */
    public CommandCategory getCategory() {
        return this.category;
    }

    @Override
    public String toString() {
        return "Command[" + getName() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass() || !(obj instanceof Command)) {
            return false;
        }

        final Command command = (Command) obj;

        return this.help().equals(command.help()) && this.getName().equals(command.getName());
    }
}

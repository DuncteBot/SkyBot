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

import com.github.natanbc.reliqua.request.PendingRequest;
import fredboat.audio.player.LavalinkManager;
import me.duncte123.botCommons.web.WebUtils;
import me.duncte123.botCommons.web.WebUtilsErrorUtils;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SuppressWarnings("SameParameterValue")
public abstract class Command implements ICommand {

    public static final Set<Long> patrons = new HashSet<>();
    public static final Set<Long> guildPatrons = new HashSet<>();
    public static final long supportGuildId = 191245668617158656L;
    public static final long guildPatronsRole = 470581447196147733L;
    public static final long patronsRole = 402497345721466892L;
    protected static final Logger logger = LoggerFactory.getLogger(Command.class);
    // The size should match the usage for stability but not more than 4.
    protected static final ScheduledExecutorService commandService = Executors.newScheduledThreadPool(4,
            r -> new Thread(r, "Command-Thread"));
    /**
     * This holds the prefix for us
     */
    protected static final String PREFIX = Settings.PREFIX;
    /**
     * A list of users that have upvoted the bot
     */
    private static final Set<Long> upvotedIds = new HashSet<>();
    /**
     * This holds the category
     */
    protected CommandCategory category = CommandCategory.MAIN;
    /**
     * This tells the bot to display the aliases of the command in the help command
     */
    protected boolean displayAliasesInHelp = false;

    private String helpParsed = null;


    private boolean checkVoteOnDBL(String userid) {
        String token = AirUtils.CONFIG.getString("apis.discordbots_userToken", "");

        if (token == null || token.isEmpty()) {
            logger.warn("Discord Bots token not found");
            return false;
        }
        PendingRequest<JSONObject> json = WebUtils.ins.prepareRaw(new Request.Builder()
                .url("https://discordbots.org/api/bots/210363111729790977/check?userId=" + userid)
                .get()
                .addHeader("Authorization", token)
                .build(), WebUtilsErrorUtils::toJSONObject);

        return 1 == json.execute().optInt("voted", 0);
    }

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
     * @param u  The user to check
     * @param tc the channel to send the message to, if the text channel is null it wont send a message
     * @return true if the user is a patron
     */
    private boolean isPatron(User u, TextChannel tc, boolean reply) {
        //noinspection deprecation
        if (isDev(u)) {
            return true;
        }
        if (patrons.contains(u.getIdLong())) {
            return true;
        }
        Guild supportGuild = u.getJDA().asBot().getShardManager().getGuildById(supportGuildId);
        if (supportGuild == null) {
            return false;
        }
        Member m = supportGuild.getMember(u);
        if (m == null) {
            if (reply)
                MessageUtils.sendEmbed(tc, EmbedUtils.embedMessage("This command is a premium command (shortcut) and is locked for you because you " +
                        "are not one of our patrons.\n" +
                        "To become a patron and have access to this command please [click this link](https://www.patreon.com/DuncteBot).\n" +
                        "You will also need to join our support guild [here](https://discord.gg/NKM9Xtk)"));
            return false;
        }

        if (!m.getRoles().contains(supportGuild.getRoleById(patronsRole))) {
            if (reply)
                MessageUtils.sendEmbed(tc, EmbedUtils.embedMessage("This command is a premium command (shortcut) and is locked for you because you " +
                        "are not one of our patrons.\n" +
                        "To become a patron and have access to this command please [click this link](https://www.patreon.com/DuncteBot)."));
            return false;
        }

        patrons.add(u.getIdLong());

        return true;
    }

    private boolean isPatron(User u, TextChannel tc) {
        return isPatron(u, tc, true);
    }

    private boolean isGuildPatron(User u, Guild g) {

        if (guildPatrons.contains(g.getIdLong())) {
            return true;
        }

        Guild supportGuild = u.getJDA().asBot().getShardManager().getGuildById(supportGuildId);
        if (supportGuild == null) {
            return false;
        }

        Member m = supportGuild.getMember(u);

        if (m == null) {
            return false;
        }

        if (!m.getRoles().contains(supportGuild.getRoleById(guildPatronsRole))) {
            return false;
        }

        guildPatrons.add(g.getIdLong());

        return true;
    }

    protected boolean isUserOrGuildPatron(GuildMessageReceivedEvent event) {
        boolean isGuild = isGuildPatron(event.getAuthor(), event.getGuild());
        return isGuild || isPatron(event.getAuthor(), event.getChannel(), !LavalinkManager.ins.isConnected(event.getGuild()));
    }

    @SuppressWarnings("deprecation")
    protected boolean isDev(User u) {
        return Settings.wbkxwkZPaG4ni5lm8laY.contains(u.getIdLong());
    }

    /**
     * Has this user upvoted the bot
     */
    protected boolean hasUpvoted(User user) {
        boolean upvoteCheck = upvotedIds.contains(user.getIdLong());
        if (!upvoteCheck) {
            boolean dblCheck = checkVoteOnDBL(user.getId());
            if (dblCheck) {
                upvoteCheck = true;
                upvotedIds.add(user.getIdLong());
            }
        }

        return isPatron(user, null) || upvoteCheck;
    }

    /**
     * Returns the current category of the command
     *
     * @return the current category of the command
     */
    public CommandCategory getCategory() {
        return this.category;
    }

    /**
     * This method is internally used to properly display the text on the webpages
     *
     * @return the html parsed help
     */
    @SuppressWarnings("unused")
    public String helpParsed() {
        if (helpParsed == null) {
            String s = help()
                    .replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;")
                    .replaceAll("`(.*)`", "<code>$1</code>")
                    .replaceAll("\\n", "<br />")
                    .replaceAll("\\*\\*(.*)\\*\\*", "<strong>$1</strong>");
            if (getAliases().length > 0) {
                s += "<br />Aliases: " + Settings.PREFIX + StringUtils.join(getAliases(), ", " + Settings.PREFIX);
            }
            helpParsed = s;
        }
        return helpParsed;
    }

    /**
     * This returns the settings for the given guild
     *
     * @param guild the guild that we need the settings for
     * @return the {@link GuildSettings GuildSettings} for the given guild
     */
    protected GuildSettings getSettings(Guild guild) {
        return GuildSettingsUtils.getGuild(guild);
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

        Command command = (Command) obj;

        return this.help().equals(command.help()) && this.getName().equals(command.getName());
    }
}

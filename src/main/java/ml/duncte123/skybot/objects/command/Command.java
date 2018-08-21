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
import me.duncte123.botCommons.web.WebUtils;
import me.duncte123.botCommons.web.WebUtilsErrorUtils;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ml.duncte123.skybot.utils.MessageUtils.sendEmbed;

@SuppressWarnings("SameParameterValue")
public abstract class Command implements ICommand {

    public static final Set<Long> patrons = new HashSet<>();
    public static final Set<Long> guildPatrons = new HashSet<>();
    public static final long supportGuildId = 191245668617158656L;
    public static final long guildPatronsRole = 470581447196147733L;
    public static final long patronsRole = 402497345721466892L;
    protected static final Logger logger = LoggerFactory.getLogger(Command.class);
    // The size should match the usage for stability but not more than 4.
    protected static final ScheduledExecutorService commandService = Executors.newScheduledThreadPool(10,
            r -> new Thread(r, "Command-Thread"));
    /**
     * This holds the prefix for us
     */
    protected static final String PREFIX = Settings.PREFIX;
    /**
     * A list of users that have upvoted the bot
     */
    private static final Set<Long> upvotedIds = new HashSet<>();
    private static final Set<Long> noneUpvoteIds = new HashSet<>();

    static {
        //clear the upvotes every hour
        commandService.scheduleAtFixedRate(
                noneUpvoteIds::clear,
                1L,
                1L,
                TimeUnit.HOURS
        );
    }

    /**
     * This holds the category
     */
    protected CommandCategory category = CommandCategory.MAIN;
    /**
     * This tells the bot to display the aliases of the command in the help command
     */
    protected boolean displayAliasesInHelp = false;
    private String helpParsed = null;

    private boolean checkVoteOnDBL(String userid, DunctebotConfig config) {
        String token = config.apis.discordbots_userToken;

        if (token == null || token.isEmpty()) {
            logger.warn("Discord Bots token not found");
            return false;
        }
        PendingRequest<JSONObject> json = WebUtils.ins.prepareRaw(WebUtils.defaultRequest()
                .url("https://discordbots.org/api/bots/210363111729790977/check?userId=" + userid)
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
    protected boolean isPatron(User u, TextChannel tc) {
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

    private boolean isPatron(User u, TextChannel tc, boolean reply) {
        TextChannel textChannel = reply ? tc : null;
        return isPatron(u, textChannel);
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

    protected boolean isUserOrGuildPatron(GuildMessageReceivedEvent event, boolean reply) {
        boolean isGuild = isGuildPatron(event.getAuthor(), event.getGuild());
        return isGuild || isPatron(event.getAuthor(), event.getChannel(), reply);
    }

    protected boolean isUserOrGuildPatron(GuildMessageReceivedEvent e) {
        return isUserOrGuildPatron(e, true);
    }

    @SuppressWarnings("deprecation")
    protected boolean isDev(User u) {
        return Settings.wbkxwkZPaG4ni5lm8laY.contains(u.getIdLong());
    }

    /**
     * Has this user upvoted the bot
     */
    protected boolean hasUpvoted(User user, DunctebotConfig config) {
        boolean upvoteCheck = upvotedIds.contains(user.getIdLong());
        if (!upvoteCheck && !noneUpvoteIds.contains(user.getIdLong())) {
            boolean dblCheck = checkVoteOnDBL(user.getId(), config);
            if (dblCheck) {
                upvoteCheck = true;
                upvotedIds.add(user.getIdLong());
            } else {
                noneUpvoteIds.add(user.getIdLong());
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
            if (getAliases().length > 0 && shouldDisplayAliasesInHelp()) {
                s += "<br />Aliases: " + PREFIX + StringUtils.join(getAliases(), ", " + PREFIX);
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

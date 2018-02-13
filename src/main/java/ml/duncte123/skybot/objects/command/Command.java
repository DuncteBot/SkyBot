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

import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.*;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("SameParameterValue")
public abstract class Command {

    protected static final Logger logger = LoggerFactory.getLogger(Command.class);

    /**
     * A list of users that have upvoted the bot
     */
    protected static final Set<String> upvotedIds = new HashSet<String>() {
        @Override
        public boolean contains(Object o) {
            if(o.getClass() != String.class) return false;

            if(super.contains(o)) return true;

            reloadUpvoted();

            return super.contains(o);
        }
    };

    private static boolean cooldown = false;

    public Command() {
        if (Settings.useCooldown) {
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleWithFixedDelay(() -> {
                if (cooldown)
                    cooldown = false;
            }, 0, 20, TimeUnit.SECONDS);
        }
    }

    static {
        reloadUpvoted();
    }

    /**
     * This holds the prefix for us
     */
    protected static final String PREFIX = Settings.prefix;
    /**
     * This holds the category
     */
    protected CommandCategory category = CommandCategory.MAIN;

    /**
     * This checks if the user is a patrons if ours
     * It checks if the user has the patreon role on our support guild
     * @param u The user to check
     * @param tc the channel to send the message to, if the text channel is null it wont send a message
     * @return true if the user is a patron
     */
    protected boolean isPatron(User u, TextChannel tc) {
        //noinspection deprecation
        if(Arrays.asList(Settings.wbkxwkZPaG4ni5lm8laY).contains(u.getId())) {
            return true;
        }
        Guild supportGuild = u.getJDA().asBot().getShardManager().getGuildById("191245668617158656");
        if (supportGuild == null) {
            return false;
        }
        Member m = supportGuild.getMember(u);
        if (m == null) {
            MessageUtils.sendEmbed(tc, EmbedUtils.embedMessage("This command is a premium command and is locked for you because you are " +
                    "not one of out patrons.\n" +
                    "To become a patron and have access to this command please [click this link](https://www.patreon.com/duncte123).\n" +
                    "You will also need to join our support guild [here](https://discord.gg/NKM9Xtk)"));
            return false;
        } else {
            if (!m.getRoles().contains(supportGuild.getRoleById("402497345721466892"))) {
                MessageUtils.sendEmbed(tc, EmbedUtils.embedMessage("This command is a premium command and is locked for you because you are " +
                        "not one of out patrons.\n" +
                        "To become a patron and have access to this command please [click this link](https://www.patreon.com/duncte123)."));
                return false;
            }
            return true;
        }
    }

    /**
     * Reloads the list of people who have upvoted this bot
     */
    protected static void reloadUpvoted() {
        if (cooldown && Settings.useCooldown) return;
        try {
            String token = AirUtils.config.getString("apis.discordbots_userToken", "");

            if (token == null || token.isEmpty()) {
                logger.warn("Discord Bots token not found");
                return;
            }

            Response it = WebUtilsJava.executeRequest(new Request.Builder()
                            .url("https://discordbots.org/api/bots/210363111729790977/votes?onlyids=1")
                            .get()
                            .addHeader("Authorization", token)
                            .build());
            JSONArray json = null;
            try {
                json = new JSONArray(it.body().string());
            }
            catch (IOException e1) {
                logger.warn("Error (re)loading upvoted people: " + e1.getMessage(), e1);
            }

            upvotedIds.clear();

            for (int i = 0; i < json.length(); i++) {
                upvotedIds.add(json.getString(i));
            }

        } catch (JSONException e) {
            //AirUtils.logger.warn("Error (re)loading upvoted people: " + e.getMessage(), e);
            /* ignored */
        }
        if (Settings.useCooldown)
            cooldown = true;
    }

    /**
     * Has this user upvoted the bot
     */
    protected boolean hasUpvoted(User user) {
        return isPatron(user, null) || upvotedIds.contains(user.getId());
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
     * This is the action of the command, this will hold what the commands needs to to
     *
     * @param invoke The command that is ran
     * @param args   The command agruments
     * @param event  a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     */
    @SuppressWarnings("NullableProblems")
    public abstract void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event);

    /**
     * The usage instructions of the command
     *
     * @return the help instructions of the command
     * @see #help(String)
     */
    public abstract String help();

    /**
     * The usage instructions of the command
     * @param invoke the command that you want the help info for
     *               Some commands are packed together and they will return specific info depending on what you put into
     *               the command
     * @return the help instructions of the command
     * @see #help()
     */
    public String help(String invoke) {
        return help();
    }

    /**
     * This will hold the command name aka what the user puts after the prefix
     *
     * @return The command name
     */
    public abstract String getName();

    /**
     * This wil hold any aliases that this command might have
     *
     * @return the current aliases for the command if set
     */
    public String[] getAliases() {
        return new String[0];
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

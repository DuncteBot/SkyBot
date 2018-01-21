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
import ml.duncte123.skybot.utils.*;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.OkHttpClient;
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
        if (!Settings.useCooldown)
            return;
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> {
            if (cooldown)
                cooldown = false;
        }, 0, 20, TimeUnit.SECONDS);
    }

    static {
        reloadUpvoted();
    }
    
    /**
     * This holds the prefix for us
     */
    protected static final String PREFIX = Settings.otherPrefix;
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
        if(Arrays.asList(Settings.wbkxwkZPaG4ni5lm8laY).contains(u.getId())) {
            return true;
        }
        Guild supportGuild = u.getJDA().asBot().getShardManager().getGuildById("191245668617158656");
        if (supportGuild == null) {
            return false;
        }
        Member m = supportGuild.getMember(u);
        if (m == null) {
            sendEmbed(tc, EmbedUtils.embedMessage("This command is a premium command and is locked for you because you are" +
                    "not one of out patrons.\n" +
                    "To become a patron and have access to this command please [click this link](https://www.patreon.com/duncte123).\n" +
                    "You will also need to join our support guild [here](https://discord.gg/NKM9Xtk)"));
            return false;
        } else {
            if (!m.getRoles().contains(supportGuild.getRoleById("402497345721466892"))) {
                sendEmbed(tc, EmbedUtils.embedMessage("This command is a premium command and is locked for you because you are" +
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
            
            Response response = new OkHttpClient()
                                        .newCall(
                                            new Request.Builder()
                                                .url("https://discordbots.org/api/bots/210363111729790977/votes?onlyids=1")
                                                .get()
                                                .addHeader("Authorization", token)
                                                .build())
                                        .execute();
            JSONArray json = new JSONArray(response.body().string());
            
            upvotedIds.clear();

            for (int i = 0; i < json.length(); i++) {
                upvotedIds.add(json.getString(i));
            }
        } catch (JSONException e) {
            //AirUtils.logger.warn("Error (re)loading upvoted people: " + e.getMessage(), e);
            /* ignored */
        }
        catch (IOException e1) {
            logger.warn("Error (re)loading upvoted people: " + e1.getMessage(), e1);
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
    public abstract void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event);
    
    /**
     * The usage instructions of the command
     *
     * @return a String
     */
    public abstract String help();
    
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
    
    /**
     * This will react with a ❌ if the user doesn't have permission to run the command
     *
     * @param message the message to add the reaction to
     */
    protected final void sendError(Message message) {
        if (message.getChannelType() == ChannelType.TEXT) {
            TextChannel channel = message.getTextChannel();
            if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ADD_REACTION)) {
                return;
            }
        }
        message.addReaction("❌").queue();
    }

    /**
     * This will react with a ❌ if the user doesn't have permission to run the command or any other error while execution
     *
     * @param message the message to add the reaction to
     * @param error the cause
     */
    protected final void sendErrorJSON(Message message, Throwable error, final boolean print) {
        if (print)
            logger.error(error.getLocalizedMessage(), error);

        //Makes no difference if we use sendError or check here both perm types
        if (message.getChannelType() == ChannelType.TEXT) {
            TextChannel channel = message.getTextChannel();
            if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_ADD_REACTION)) {
                return;
            }
        }

        message.addReaction("❌").queue();

        message.getChannel().sendFile(EarthUtils.throwableToJSONObject(error).toString(4).getBytes(), "error.json",
                new MessageBuilder().setEmbed(EmbedUtils.defaultEmbed().setTitle("We got an error!").setDescription(String.format("Error type: %s",
                        error.getClass().getSimpleName())).build()).build()
        ).queue();
    }
    
    /**
     * This will react with a ✅ if the user doesn't have permission to run the command
     *
     * @param message the message to add the reaction to
     */
    protected final void sendSuccess(Message message) {
        if (message.getChannelType() == ChannelType.TEXT) {
            TextChannel channel = message.getTextChannel();
            if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ADD_REACTION)) {
                return;
            }
        }
        message.addReaction("✅").queue();
    }
    
    /**
     * This will check if we can send a embed and convert it to a message if we can't send embeds
     *
     * @param event a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param embed The embed to send
     */
    protected final void sendEmbed(GuildMessageReceivedEvent event, MessageEmbed embed) {
        sendEmbed(event.getChannel(), embed);
    }

    /**
     * This will check if we can send a embed and convert it to a message if we can't send embeds
     *
     * @param channel the {@link TextChannel TextChannel} that we want to send the embed to
     * @param embed The embed to send
     */
    protected final void sendEmbed(TextChannel channel, MessageEmbed embed) {
        if(channel != null) {
            if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
                sendMsg(channel, EmbedUtils.embedToMessage(embed));
                return;
            }
            sendMsg(channel, embed);
        }
    }

    /**
     * This is a shortcut for sending formatted messages to a channel which also deletes it after delay unit
     *
     * @param event an instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param delay the {@link Long} that is our delay
     * @param unit  the {@link TimeUnit} that is our unit that uses the delay parameter
     * @param msg   the message format to send
     * @param args  the arguments that should be used in the msg parameter
     */
    protected final void sendMsgFormatAndDeleteAfter(GuildMessageReceivedEvent event, long delay, TimeUnit unit, String msg, Object... args) {
        sendMsgFormatAndDeleteAfter(event.getChannel(), delay, unit, msg, args);
    }

    /**
     * This is a shortcut for sending formatted messages to a channel which also deletes it after delay unit
     *
     * @param channel the {@link TextChannel TextChannel} that we want to send our message to
     * @param delay   the {@link Long} that is our delay
     * @param unit    the {@link TimeUnit} that is our unit that uses the delay parameter
     * @param msg     the message format to send
     * @param args    the arguments that should be used in the msg parameter
     */
    protected final void sendMsgFormatAndDeleteAfter(TextChannel channel, long delay, TimeUnit unit, String msg, Object... args) {
        if(channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ))
            channel.sendMessage(new MessageBuilder().append(String.format(msg, args)).build()).queue(it -> it.delete().reason("automatic remove").queueAfter(delay, unit));
    }

    /**
     * This is a shortcut for sending formatted messages to a channel
     *
     * @param event an instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg     the message format to send
     * @param args    the arguments that should be used in the msg parameter
     */
    protected final void sendMsgFormat(GuildMessageReceivedEvent event, String msg, Object... args) {
        sendMsg(event.getChannel(), (new MessageBuilder().append(String.format(msg, args)).build()));
    }

    /**
     * This is a shortcut for sending formatted messages to a channel
     *
     * @param channel the {@link TextChannel TextChannel} that we want to send our message to
     * @param msg     the message format to send
     * @param args    the arguments that should be used in the msg parameter
     */
    protected final void sendMsgFormat(TextChannel channel, String msg, Object... args) {
        sendMsg(channel, (new MessageBuilder().append(String.format(msg, args)).build()));
    }
    
    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg   the message to send
     */
    protected final void sendMsg(GuildMessageReceivedEvent event, String msg) {
        sendMsg(event.getChannel(), (new MessageBuilder()).append(msg).build());
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param channel he {@link TextChannel TextChannel} that we want to send our message to
     * @param msg   the message to send
     */
    protected final void sendMsg(TextChannel channel, String msg) {
        sendMsg(channel, (new MessageBuilder()).append(msg).build());
    }
    
    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg   the message to send
     */
    protected final void sendMsg(GuildMessageReceivedEvent event, MessageEmbed msg) {
        sendMsg(event.getChannel(), (new MessageBuilder()).setEmbed(msg).build());
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param channel he {@link TextChannel TextChannel} that we want to send our message to
     * @param msg   the message to send
     */
    protected final void sendMsg(TextChannel channel, MessageEmbed msg) {
        sendMsg(channel, (new MessageBuilder()).setEmbed(msg).build());
    }
    
    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param event a instance of {@link net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg   the message to send
     */
    protected final void sendMsg(GuildMessageReceivedEvent event, Message msg) {
        sendMsg(event.getChannel(), msg);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param channel he {@link TextChannel TextChannel} that we want to send our message to
     * @param msg   the message to send
     */
    protected void sendMsg(TextChannel channel, Message msg) {
        //Only send a message if we can talk
        if(channel != null && channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ))
            channel.sendMessage(msg).queue();
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

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

package ml.duncte123.skybot.utils;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MessageUtils {

    private static Logger logger = LoggerFactory.getLogger(MessageUtils.class);

    private static final Consumer<Throwable> CUSTOM_QUEUE_ERROR = it -> {
        if(it instanceof ErrorResponseException){
            if(((ErrorResponseException) it).getErrorCode() != 10008)
                logger.error("RestAction queue returned failure", it);
        }
    };

    /**
     * This will react with a ❌ if the user doesn't have permission to run the command
     *
     * @param message the message to add the reaction to
     */
    public static void sendError(Message message) {
        if (message.getChannelType() == ChannelType.TEXT) {
            TextChannel channel = message.getTextChannel();
            if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ADD_REACTION)) {
                return;
            }
        }
        message.addReaction("❌").queue(null, CUSTOM_QUEUE_ERROR);
    }

    /**
     * This method uses the sendError and sendMsg methods
     *
     * @param message the {@link Message} for the sendError method
     * @param textChannel the {@link TextChannel} for the sendMsg method
     * @param text the {@link String} for the sendMsg method
     */
    public static void sendErrorWithMessage(Message message, TextChannel textChannel, String text) {
        sendError(message);
        sendMsg(textChannel, text);
    }


    /**
     * This method uses the sendError and sendMsg methods
     *
     * @param message the {@link Message} for the sendError method
     * @param event the {@link GuildMessageReceivedEvent} for the sendMsg method
     * @param text the {@link String} for the sendMsg method
     */
    public static void sendErrorWithMessage(Message message, GuildMessageReceivedEvent event, String text) {
        sendError(message);
        sendMsg(event, text);
    }

    /**
     * This will react with a ❌ if the user doesn't have permission to run the command or any other error while execution
     *
     * @param message the message to add the reaction to
     * @param error the cause
     */
    public static void sendErrorJSON(Message message, Throwable error, final boolean print) {
        if (print)
            logger.error(error.getLocalizedMessage(), error);

        //Makes no difference if we use sendError or check here both perm types
        if (message.getChannelType() == ChannelType.TEXT) {
            TextChannel channel = message.getTextChannel();
            if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_ADD_REACTION)) {
                return;
            }
        }

        message.addReaction("❌").queue(null, CUSTOM_QUEUE_ERROR);

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
    public static void sendSuccess(Message message) {
        if (message.getChannelType() == ChannelType.TEXT) {
            TextChannel channel = message.getTextChannel();
            if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ADD_REACTION)) {
                return;
            }
        }
        message.addReaction("✅").queue(null, CUSTOM_QUEUE_ERROR);
    }

    /**
     * This method uses the sendSuccess and sendMsg methods
     *
     * @param message the {@link Message} for the sendSuccess method
     * @param channel the {@link TextChannel} for the sendMsg method
     * @param text the {@link String} for the sendMsg method
     */
    public static void sendSuccessWithMessage(Message message, TextChannel channel, String text) {
        sendSuccess(message);
        sendMsg(channel, text);
    }

    /**
     * This method uses the sendSuccess and sendMsg methods
     *
     * @param message the {@link Message} for the sendSuccess method
     * @param event the {@link GuildMessageReceivedEvent} for the sendMsg method
     * @param text the {@link String} for the sendMsg method
     */
    public static void sendSuccessWithMessage(Message message, GuildMessageReceivedEvent event, String text) {
        sendSuccess(message);
        sendMsg(event, text);
    }

    /**
     * This will check if we can send a embed and convert it to a message if we can't send embeds
     *
     * @param event a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param embed The embed to send
     */
    public static void sendEmbed(GuildMessageReceivedEvent event, MessageEmbed embed) {
        sendEmbed(event.getChannel(), embed);
    }

    /**
     * This will check if we can send a embed and convert it to a message if we can't send embeds
     *
     * @param channel the {@link TextChannel TextChannel} that we want to send the embed to
     * @param embed The embed to send
     */
    public static void sendEmbed(TextChannel channel, MessageEmbed embed) {
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
     * @param event an instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param delay the {@link Long} that is our delay
     * @param unit  the {@link TimeUnit} that is our unit that uses the delay parameter
     * @param msg   the message format to send
     */
    public static void sendMsgAndDeleteAfter(GuildMessageReceivedEvent event, long delay, TimeUnit unit, String msg) {
        sendMsgFormatAndDeleteAfter(event.getChannel(), delay, unit, msg, "");
    }

    /**
     * This is a shortcut for sending formatted messages to a channel which also deletes it after delay unit
     *
     * @param tc an instance of {@link TextChannel TextChannel}
     * @param delay the {@link Long} that is our delay
     * @param unit  the {@link TimeUnit} that is our unit that uses the delay parameter
     * @param msg   the message format to send
     */
    public static void sendMsgAndDeleteAfter(TextChannel tc, long delay, TimeUnit unit, String msg) {
        sendMsgFormatAndDeleteAfter(tc, delay, unit, msg, "");
    }

    /**
     * This is a shortcut for sending formatted messages to a channel which also deletes it after delay unit
     *
     * @param event an instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param delay the {@link Long} that is our delay
     * @param unit  the {@link TimeUnit} that is our unit that uses the delay parameter
     * @param msg   the message format to send
     * @param args  the arguments that should be used in the msg parameter
     */
    public static void sendMsgFormatAndDeleteAfter(GuildMessageReceivedEvent event, long delay, TimeUnit unit, String msg, Object... args) {
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
    public static void sendMsgFormatAndDeleteAfter(TextChannel channel, long delay, TimeUnit unit, String msg, Object... args) {
        if(channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ))
            channel.sendMessage(new MessageBuilder().append(String.format(msg, args)).build())
                    .queue(it -> it.delete().reason("automatic remove").queueAfter(delay, unit, null, CUSTOM_QUEUE_ERROR));
    }

    /**
     * This is a shortcut for sending formatted messages to a channel
     *
     * @param event an instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg     the message format to send
     * @param args    the arguments that should be used in the msg parameter
     */
    public static void sendMsgFormat(GuildMessageReceivedEvent event, String msg, Object... args) {
        sendMsg(event.getChannel(), (new MessageBuilder().append(String.format(msg, args)).build()), null, null);
    }

    /**
     * This is a shortcut for sending formatted messages to a channel
     *
     * @param channel the {@link TextChannel TextChannel} that we want to send our message to
     * @param msg     the message format to send
     * @param args    the arguments that should be used in the msg parameter
     */
    public static void sendMsgFormat(TextChannel channel, String msg, Object... args) {
        sendMsg(channel, (new MessageBuilder().append(String.format(msg, args)).build()), null, null);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param event a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg   the message to send
     */
    public static void sendMsg(GuildMessageReceivedEvent event, String msg) {
        sendMsg(event.getChannel(), (new MessageBuilder()).append(msg).build(), null, null);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param event a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg   the message to send
     * @param success The success consumer
     */
    public static void sendMsg(GuildMessageReceivedEvent event, String msg, Consumer<Message> success) {
        sendMsg(event.getChannel(), (new MessageBuilder()).append(msg).build(), success, null);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param event a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg   the message to send
     * @param success The success consumer
     * @param failure the failure consumer
     */
    public static void sendMsg(GuildMessageReceivedEvent event, String msg, Consumer<Message> success, Consumer<Throwable> failure) {
        sendMsg(event.getChannel(), (new MessageBuilder()).append(msg).build(), success, failure);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param channel he {@link TextChannel TextChannel} that we want to send our message to
     * @param msg   the message to send
     */
    public static void sendMsg(TextChannel channel, String msg) {
        sendMsg(channel, (new MessageBuilder()).append(msg).build(), null, null);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param channel he {@link TextChannel TextChannel} that we want to send our message to
     * @param msg   the message to send
     * @param success The success consumer
     */
    public static void sendMsg(TextChannel channel, String msg, Consumer<Message> success) {
        sendMsg(channel, (new MessageBuilder()).append(msg).build(), success, null);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param channel he {@link TextChannel TextChannel} that we want to send our message to
     * @param msg   the message to send
     * @param success The success consumer
     * @param failure the failure consumer
     */
    public static void sendMsg(TextChannel channel, String msg, Consumer<Message> success, Consumer<Throwable> failure) {
        sendMsg(channel, (new MessageBuilder()).append(msg).build(), success, failure);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param event a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg   the message to send
     */
    public static void sendMsg(GuildMessageReceivedEvent event, MessageEmbed msg) {
        sendMsg(event.getChannel(), (new MessageBuilder()).setEmbed(msg).build(), null, null);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param channel he {@link TextChannel TextChannel} that we want to send our message to
     * @param msg   the message to send
     */
    public static void sendMsg(TextChannel channel, MessageEmbed msg) {
        sendMsg(channel, (new MessageBuilder()).setEmbed(msg).build(), null, null);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param event a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg   the message to send
     */
    public static void sendMsg(GuildMessageReceivedEvent event, Message msg) {
        sendMsg(event.getChannel(), msg, null, null);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param event a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg   the message to send
     * @param success The success consumer
     */
    public static void sendMsg(GuildMessageReceivedEvent event, Message msg, Consumer<Message> success) {
        sendMsg(event.getChannel(), msg, success, null);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param event a instance of {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @param msg   the message to send
     * @param success The success consumer
     * @param failure the failure consumer
     */
    public static void sendMsg(GuildMessageReceivedEvent event, Message msg, Consumer<Message> success, Consumer<Throwable> failure) {
        sendMsg(event.getChannel(), msg, success, failure);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param channel he {@link TextChannel TextChannel} that we want to send our message to
     * @param msg   the message to send
     */
    public static void sendMsg(TextChannel channel, Message msg) {
        sendMsg(channel, msg, null, null);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param channel he {@link TextChannel TextChannel} that we want to send our message to
     * @param msg   the message to send
     * @param success The success consumer
     */
    public static void sendMsg(TextChannel channel, Message msg, Consumer<Message> success) {
        sendMsg(channel, msg, success, null);
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param channel he {@link TextChannel TextChannel} that we want to send our message to
     * @param msg   the message to send
     * @param success The success consumer
     * @param failure the failure consumer
     */
    public static void sendMsg(TextChannel channel, Message msg, Consumer<Message> success, Consumer<Throwable> failure) {
        //Only send a message if we can talk
        if(channel != null && channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ))
            channel.sendMessage(msg).queue(success, failure);
    }
}

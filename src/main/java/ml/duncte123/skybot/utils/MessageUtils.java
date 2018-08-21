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

import kotlin.Deprecated;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.duncte123.botCommons.messaging.MessageUtils.CUSTOM_QUEUE_ERROR;
import static ml.duncte123.skybot.utils.EmbedUtils.embedToMessage;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MessageUtils {

    /////
    private static Logger logger = LoggerFactory.getLogger(MessageUtils.class);

    /**
     * This will react with a ❌ if the user doesn't have permission to run the command or any other error while execution
     *
     * @param message the message to add the reaction to
     * @param error   the cause
     */
    public static void sendErrorJSON(Message message, Throwable error, final boolean print) {
        if (print)
            logger.error(error.getLocalizedMessage(), error);

        //Makes no difference if we use sendError or check here both perm types
        if (message.getChannelType() == ChannelType.TEXT) {
            TextChannel channel = message.getTextChannel();
            if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_READ,
                    Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_ADD_REACTION)) {
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
     * @param embed   The embed to send
     */
    public static void sendEmbed(TextChannel channel, MessageEmbed embed) {
        if (channel != null) {
            if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
                (new MessageBuilder()).append(embedToMessage(embed))
                        .buildAll(MessageBuilder.SplitPolicy.NEWLINE)
                        .forEach(it -> me.duncte123.botCommons.messaging.MessageUtils.sendMsg(channel, it));
//                sendMsg(channel, EmbedUtils.embedToMessage(embed));
                return;
            }
            //noinspection deprecation
            sendMsg(channel, embed);
        }
    }

    public static void editMsg(Message message, Message newContent) {
        if (message == null || newContent == null) return;
        if (newContent.getEmbeds().size() > 0) {
            if (!message.getGuild().getSelfMember().hasPermission(message.getTextChannel(),
                    Permission.MESSAGE_EMBED_LINKS)) {
                MessageBuilder mb = new MessageBuilder()
                        .append(newContent.getContentRaw())
                        .append('\n');
                newContent.getEmbeds().forEach(
                        messageEmbed -> mb.append(embedToMessage(messageEmbed))
                );
                message.editMessage(mb.build()).queue();
                return;
            }
            message.editMessage(newContent).queue();
        }
    }

    /**
     * This is a shortcut for sending messages to a channel
     *
     * @param channel he {@link TextChannel TextChannel} that we want to send our message to
     * @param msg     the message to send
     * @deprecated Use {@link #sendEmbed(TextChannel, MessageEmbed)}
     */
    @Deprecated(message = "use #sendEmbed")
    @java.lang.Deprecated
    public static void sendMsg(TextChannel channel, MessageEmbed msg) {
        //Check if the channel exists
        if ((channel != null && channel.getGuild().getTextChannelById(channel.getId()) != null) &&
                channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)) {
            Message m = new MessageBuilder().setEmbed(msg).build();
            //Only send a message if we can talk
            channel.sendMessage(m).queue(null, CUSTOM_QUEUE_ERROR);
        }
    }
}

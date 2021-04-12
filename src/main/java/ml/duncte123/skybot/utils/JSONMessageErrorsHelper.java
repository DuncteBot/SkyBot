/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.duncte123.botcommons.messaging.MessageUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.duncte123.botcommons.messaging.EmbedUtils.getDefaultEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static net.dv8tion.jda.api.requests.ErrorResponse.REACTION_BLOCKED;
import static net.dv8tion.jda.api.requests.ErrorResponse.UNKNOWN_MESSAGE;

public class JSONMessageErrorsHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSONMessageErrorsHelper.class);

    private JSONMessageErrorsHelper() {}

    public static void sendErrorJSON(Message message, Throwable error, final boolean print, ObjectMapper mapper) {
        if (print) {
            LOGGER.error(error.getLocalizedMessage(), error);
        }

        //Makes no difference if we use sendError or check here both perm types
        if (message.getChannelType() == ChannelType.TEXT) {
            final TextChannel channel = message.getTextChannel();
            if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_READ,
                Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_ADD_REACTION)) {
                return;
            }
        }

        message.addReaction(MessageUtils.getErrorReaction())
            .queue(null, new ErrorHandler().ignore(UNKNOWN_MESSAGE, REACTION_BLOCKED));

        try {
            message.getChannel()
                .sendFile(
                    mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsBytes(EarthUtils.throwableToJSONObject(error, mapper)),
                    "error.json"
                ).embed(
                getDefaultEmbed().setTitle("We got an error!")
                    .setDescription(String.format("Error type: %s",
                        error.getClass().getSimpleName())).build()
            ).queue();
        }
        catch (JsonProcessingException e) {
            sendMsg(message.getTextChannel(), "Error while sending file: " + e);
        }
    }

}

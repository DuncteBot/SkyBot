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

package ml.duncte123.skybot.utils;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.duncte123.botcommons.messaging.EmbedUtils.defaultEmbed;

@SuppressWarnings({"unused"})
@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class JSONMessageErrorsHelper {

    /////
    private static Logger logger = LoggerFactory.getLogger(JSONMessageErrorsHelper.class);

    /**
     * This will react with a ❌ if the user doesn't have permission to run the command or any other error while
     * execution
     *
     * @param message
     *         the message to add the reaction to
     * @param error
     *         the cause
     */
    public static void sendErrorJSON(Message message, Throwable error, final boolean print) {
        if (print) {
            logger.error(error.getLocalizedMessage(), error);
        }

        //Makes no difference if we use sendError or check here both perm types
        if (message.getChannelType() == ChannelType.TEXT) {
            final TextChannel channel = message.getTextChannel();
            if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_READ,
                Permission.MESSAGE_WRITE, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_ADD_REACTION)) {
                return;
            }
        }

        message.addReaction("❌").queue(null, (ignored) -> {});

        message.getChannel().sendFile(EarthUtils.throwableToJSONObject(error).toString(4).getBytes(), "error.json",
            new MessageBuilder().setEmbed(defaultEmbed().setTitle("We got an error!").setDescription(String.format("Error type: %s",
                error.getClass().getSimpleName())).build()).build()
        ).queue();
    }

}

/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.commands.essentials;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class TokenCommand extends Command {

    private static final Pattern TOKEN_REGEX = Pattern.compile("([a-zA-Z0-9]+)\\.([a-zA-Z0-9\\-_]+)\\.([a-zA-Z0-9\\-_]+)");
    private static final long TOKEN_EPOCH = 1293840000L;
    private static final String STRING_FORMAT = "Deconstruction results for token: `%s`%n%n" +
        "**ID:** %s%n**Generated:** %s%n%n" +
        "Checking validity...%s%n%n" +
        "Keep in mind that verifying if the token is valid by making a request to discord is against the TOS";

    public TokenCommand() {
        this.category = CommandCategory.NERD_STUFF;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgs();

        if (args.isEmpty()) {
            sendMsg(event, "Missing arguments");
            return;
        }

        Matcher matcher = TOKEN_REGEX.matcher(args.get(0));

        if (!matcher.matches()) {
            sendMsg(event, "Your input `" + args.get(0) + "` has the wrong token format.");
            return;
        }

        String id = decodeBase64ToString(matcher.group(1));

        OffsetDateTime time = toTimeStamp(matcher.group(2));

        if (time == null) {
            sendMsg(event, "Could not decode timestamp.");
            return;
        }

        String timestamp = time.format(DateTimeFormatter.RFC_1123_DATE_TIME);


        sendMsg(event, String.format(STRING_FORMAT, args.get(0), id, timestamp, ""), (message) -> {
            try {
                if (isLong(id)) {
                    event.getJDA().retrieveUserById(id).queue((user) -> {
                        String userinfo = String.format("%n%nToken has a valid structure. It belongs to **%#s** (%s).", user, user.getId());
                        String newMessage = String.format(STRING_FORMAT, args.get(0), id, timestamp, userinfo);
                        message.editMessage(newMessage).queue();
                    }, (error) -> {
                        String info = String.format("%n%nToken is not valid or the account has been deleted (%s)", error.getMessage());
                        String newMessage = String.format(STRING_FORMAT, args.get(0), id, timestamp, info);
                        message.editMessage(newMessage).queue();
                    });
                } else {
                    String info = String.format("%n%n%s is not a valid long id", id);
                    String newMessage = String.format(STRING_FORMAT, args.get(0), id, timestamp, info);
                    message.editMessage(newMessage).queue();
                }
            } catch (IllegalArgumentException e) {
                String info = String.format("%n%nThat token does not have a valid structure (%s)", e.getMessage());
                String newMessage = String.format(STRING_FORMAT, args.get(0), id, timestamp, info);
                message.editMessage(newMessage).queue();
            }
        });
    }

    @Override
    public String help() {
        return "Tries to get as much info about a token as possible\n" +
            "Usage: `" + PREFIX + getName() + " <token of a discord bot>`";
    }

    @Override
    public String getName() {
        return "token";
    }

    private byte[] decodeBase64(String input) {
        return Base64.getMimeDecoder().decode(input);
    }

    private String decodeBase64ToString(String input) {
        return new String(decodeBase64(input));
    }

    @Nullable
    private OffsetDateTime toTimeStamp(String input) {
        try {
            BigInteger decoded = new BigInteger(decodeBase64(input));
            long receivedTime = decoded.longValue();

            long timestamp = TOKEN_EPOCH + receivedTime;

            Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            long millis = timestamp * 1000;
            gmt.setTimeInMillis(millis);

            return OffsetDateTime.ofInstant(gmt.toInstant(), gmt.getTimeZone().toZoneId());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private boolean isLong(String input) {
        try {
            return Long.parseUnsignedLong(input) > -1L;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}

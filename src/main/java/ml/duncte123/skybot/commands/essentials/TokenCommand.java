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

package ml.duncte123.skybot.commands.essentials;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ml.duncte123.skybot.utils.MessageUtils.sendMsg;

public class TokenCommand extends Command {

    private static final Pattern TOKEN_REGEX = Pattern.compile("([a-zA-Z0-9]+)\\.([a-zA-Z0-9]+)\\.([a-zA-Z0-9\\-_]+)");
    private static final long TOKEN_EPOCH = 1293840000L;
    private static final String STRING_FORMAT = "Deconstruction results for token: `%s`%n%n" +
            "**ID:** %s%n**Generated:** %s%n%n" +
            "Checking validity...%s%n%n" +
            "Keep in mind that verifying if the token is valid by making a request to discord is against the TOS";

    public TokenCommand() {
        this.category = CommandCategory.NERD_STUFF;
    }

    @Override
    public void executeCommand(CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgs();

        if (args.size() == 0) {
            sendMsg(event, "Missing arguments");
            return;
        }

        Matcher matcher = TOKEN_REGEX.matcher(args.get(0));

        if (!matcher.matches()) {
            sendMsg(event, "Your input `" + args.get(0) + "` has the wrong token format.");
            return;
        }

        String id = decodeBase64ToString(matcher.group(1));
        String timestamp = toTimeStamp(matcher.group(2)).format(DateTimeFormatter.RFC_1123_DATE_TIME);

        sendMsg(event, String.format(STRING_FORMAT, args.get(0), id, timestamp, ""), (message) -> {
            try {
                event.getJDA().retrieveUserById(id).queue((user) -> {
                    String userinfo = String.format("%n%nToken has a valid structure. It belongs to **%#s** (%s).", user, user.getId());
                    String newMessage = String.format(STRING_FORMAT, args.get(0), id, timestamp, userinfo);
                    message.editMessage(newMessage).queue();
                }, (error) -> {
                    String info = String.format("%n%nToken is not valid or the account has been deleted (%s)", error.getMessage());
                    String newMessage = String.format(STRING_FORMAT, args.get(0), id, timestamp, info);
                    message.editMessage(newMessage).queue();
                });
            } catch (NumberFormatException e) {
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
        return Base64.getDecoder().decode(input);
    }

    private String decodeBase64ToString(String input) {
        return new String(decodeBase64(input));
    }

    private OffsetDateTime toTimeStamp(String input) {
        BigInteger decoded = new BigInteger(decodeBase64(input));
        long receivedTime = Long.valueOf(decoded.toString());

        long timestamp = TOKEN_EPOCH + receivedTime;

        Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        long millis = timestamp * 1000;
        gmt.setTimeInMillis(millis);

        return OffsetDateTime.ofInstant(gmt.toInstant(), gmt.getTimeZone().toZoneId());
    }
}

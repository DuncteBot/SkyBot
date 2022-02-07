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

package ml.duncte123.skybot.commands.essentials;

import com.fasterxml.jackson.databind.JsonNode;
import me.duncte123.botcommons.messaging.MessageConfig;
import ml.duncte123.skybot.objects.BaseCommand;
import ml.duncte123.skybot.objects.api.DuncteApis;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class TokenCommand extends BaseCommand {

    private static final Pattern TOKEN_REGEX = Pattern.compile("([a-zA-Z0-9]+)\\.([a-zA-Z0-9\\-_]+)\\.([a-zA-Z0-9\\-_]+)");
    private static final String STRING_FORMAT = "Deconstruction results for token: `%s`%n%n" +
        "**ID:** %s%n**Generated:** %s%n%n" +
        "Checking validity...%s%n%n" +
        "Keep in mind that verifying if the token is valid by making a request to discord is against the TOS";

    public TokenCommand() {
        super(
            "token",
            "Deconstructs a bot token to get as much information as possible from it",
            CommandCategory.UTILS,
            null,
            new String[0],
            false,
            "<token of a discord bot>",
            true
        );
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();

        final Matcher matcher = TOKEN_REGEX.matcher(args.get(0));

        if (!matcher.matches()) {
            sendMsg(ctx, "Your input `" + args.get(0) + "` has the wrong token format.");
            return;
        }

        final DuncteApis apis = ctx.getVariables().getApis();
        final JsonNode json = apis.decodeToken(args.get(0));

        if (json.get("success").asBoolean()) {
            handleSuccess(args.get(0), json.get("data"), ctx);

            return;
        }

        final JsonNode error = json.get("error");
        final String errorType = error.get("type").asText();
        final String errorMessage = error.get("message").asText();

        sendMsg(ctx, String.format("Invalid token: (%s) %s", errorType, errorMessage));
    }

    @Nullable
    private OffsetDateTime toTimeStamp(long input) {
        try {
            final Calendar gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            final long millis = input * 1000;
            gmt.setTimeInMillis(millis);

            return OffsetDateTime.ofInstant(gmt.toInstant(), gmt.getTimeZone().toZoneId());
        }
        catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private boolean isLong(String input) {
        try {
            return Long.parseUnsignedLong(input) > -1L;
        }
        catch (NumberFormatException ignored) {
            return false;
        }
    }

    private void handleSuccess(String arg, JsonNode data, CommandContext ctx) {
        final OffsetDateTime time = toTimeStamp(data.get("timestamp").asLong());

        if (time == null) {
            sendMsg(ctx, "Could not decode timestamp.");
            return;
        }

        final String idText = data.get("id").asText();
        final String timestamp = time.format(DateTimeFormatter.RFC_1123_DATE_TIME);

        sendMsg(MessageConfig.Builder.fromCtx(ctx)
            .setMessage(String.format(STRING_FORMAT, arg, idText, timestamp, ""))
            .setSuccessAction((message) -> {
                try {
                    if (!isLong(idText)) {
                        final String info = String.format("%n%n%s is not a valid long id", idText);
                        final String newMessage = String.format(STRING_FORMAT, arg, idText, timestamp, info);
                        message.editMessage(newMessage).queue();

                        return;
                    }

                    ctx.getJDA().retrieveUserById(idText).queue(
                        (user) -> {
                            final String userinfo = String.format("%n%nToken has a valid structure. It belongs to **%#s** (%s).", user, user.getId());
                            final String newMessage = String.format(STRING_FORMAT, arg, idText, timestamp, userinfo);
                            message.editMessage(newMessage).queue();
                        },
                        (error) -> {
                            final String info = String.format("%n%nToken is not valid or the account has been deleted (%s)", error.getMessage());
                            final String newMessage = String.format(STRING_FORMAT, arg, idText, timestamp, info);
                            message.editMessage(newMessage).queue();
                        }
                    );

                }
                catch (IllegalArgumentException e) {
                    final String info = String.format("%n%nThat token does not have a valid structure (%s)", e.getMessage());
                    final String newMessage = String.format(STRING_FORMAT, arg, idText, timestamp, info);
                    message.editMessage(newMessage).queue();
                }
            })
            .build());
    }
}

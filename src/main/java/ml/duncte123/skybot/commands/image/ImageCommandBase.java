/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.commands.image;

import com.fasterxml.jackson.databind.JsonNode;
import kotlin.Pair;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.extensions.StringKt;
import ml.duncte123.skybot.extensions.UserKt;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.FinderUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.CommandUtils.isUserOrGuildPatron;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public abstract class ImageCommandBase extends Command {

    public ImageCommandBase() {
        this.requiresArgs = true;
    }

    private boolean canSendFile(GuildMessageReceivedEvent event) {
        if (event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_ATTACH_FILES)) {
            return true;
        } else {
            sendMsg(event.getChannel(), "I need permission to upload files in this channel in order for this command to work");
            return false;
        }
    }

    /* package */ boolean passes(GuildMessageReceivedEvent event) {
        return passes(event, true);
    }

    protected boolean passes(GuildMessageReceivedEvent event, boolean patron) {
        return passesNoArgs(event, patron);
    }

    protected boolean passesNoArgs(GuildMessageReceivedEvent event, boolean patron) {
        return canSendFile(event) && (!patron || isUserOrGuildPatron(event));
    }

    private String getFileName() {
        return getName() + '_' + System.currentTimeMillis() + ".png";
    }

    public void handleBasicImage(GuildMessageReceivedEvent event, Pair<byte[], JsonNode> data) {
        final TextChannel channel = event.getChannel();
        final var image = data.getFirst();

        if (image == null) {
            final JsonNode json = data.getSecond();
            final String message;

            // Success should always be false but you never know
            if (json.get("success").asBoolean()) {
                message = json.get("message").asText();
            } else {
                message = json.get("error").get("message").asText();
            }

            sendMsg(channel, "Error while generating image: " + message);
            return;
        }

        handleBasicImage(event, image);
    }


    public void handleBasicImage(GuildMessageReceivedEvent event, byte[] image) {
        final TextChannel channel = event.getChannel();

        if (event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ATTACH_FILES)) {
            channel.sendFile(image, getFileName()).reference(event.getMessage()).queue();
        } else {
            sendMsg(channel, "I need permission to upload files in order for this command to work.");
        }
    }

    @Nonnull
    @Override
    public CommandCategory getCategory() {
        return CommandCategory.PATRON;
    }

    @Nullable
    protected String getImageFromCommand(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        String url = null;

        if (!ctx.getMessage().getAttachments().isEmpty()) {
            url = tryGetAttachment(ctx);
        }

        if (url == null && args.isEmpty()) {
            url = getAvatarUrl(ctx.getAuthor());
        }

        if (url == null && AirUtils.isURL(args.get(0))) {
            url = tryGetUrl(ctx, args.get(0));
        }

        if (url == null && !ctx.getMessage().getMentionedUsers().isEmpty()) {
            url = getAvatarUrl(ctx.getMessage().getMentionedUsers().get(0));
        }

        if (url  == null) {
            final List<Member> textMentions = FinderUtils.searchMembers(ctx.getArgsJoined(), ctx);

            if (!textMentions.isEmpty()) {
                url = getAvatarUrl(textMentions.get(0).getUser());
            }
        }

        if (url == null) {
            url = getAvatarUrl(ctx.getAuthor());
        }

        return url;
    }

    @Nullable
    private String tryGetAttachment(CommandContext ctx) {
        final Attachment attachment = ctx.getMessage().getAttachments().get(0);

        final File file = new File(attachment.getFileName());

        String mimetype = null;
        try {
            mimetype = Files.probeContentType(file.toPath());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //mimetype should be something like "image/png"
        if (mimetype == null || !mimetype.split("/")[0].equals("image")) {
            sendMsg(ctx, "That file does not look like an image");
            return null;
        }

        return attachment.getUrl();
    }

    @Nullable
    private String tryGetUrl(CommandContext ctx, String url) {
        try {
            return new URL(url).toString();
        }
        catch (MalformedURLException ignored) {
            sendMsg(ctx, "That does not look like a valid url");
            return null;
        }
    }

    private String getAvatarUrl(User user) {
        return UserKt.getStaticAvatarUrl(user) + "?size=512";
    }

    public String parseTextArgsForImage(CommandContext ctx) {
        return StringKt.stripFlags(ctx.getArgsDisplay(), this);
    }

    @Nullable
    protected String[] splitString(CommandContext ctx) {

        final String[] split = ctx.getArgsDisplay().split("\\|", 2);

        if (split.length < 2) {
            sendMsg(ctx, "Missing arguments, check `" + ctx.getPrefix() + "help " + getName() + '`');
            return null;
        }

        if ("".equals(split[0].trim()) || "".equals(split[1].trim())) {
            sendMsg(ctx, "Missing arguments, check `" + ctx.getPrefix() + "help " + getName() + '`');
            return null;
        }

        return split;
    }
}

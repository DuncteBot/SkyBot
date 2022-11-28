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

package ml.duncte123.skybot.commands.image;

import com.fasterxml.jackson.databind.JsonNode;
import kotlin.Pair;
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
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.User;

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

public abstract class ImageCommandBase extends Command {

    public ImageCommandBase() {
        this.requiresArgs = true;
    }

    private boolean canSendFile(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();

        if (ctx.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ATTACH_FILES)) {
            return true;
        } else {
            sendMsg(channel, "I need permission to upload files in this channel in order for this command to work");
            return false;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        /* package */ boolean passes(CommandContext ctx) {
        return passes(ctx, true);
    }

    protected boolean passes(CommandContext ctx, boolean patron) {
        return canSendFile(ctx) && (!patron || isUserOrGuildPatron(ctx));
    }

    private String getFileName() {
        return getName() + '_' + System.currentTimeMillis() + ".png";
    }

    public void handleBasicImage(CommandContext ctx, Pair<byte[], JsonNode> data) {
        final TextChannel channel = ctx.getChannel();
        final byte[] image = data.getFirst();

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

        handleBasicImage(ctx, image);
    }

    public void handleBasicImage(CommandContext ctx, byte[] image) {
        final TextChannel channel = ctx.getChannel();

        if (ctx.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ATTACH_FILES)) {
            channel.sendFile(image, getFileName()).reference(ctx.getMessage()).queue();
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

        // I hate this so much
        // But I won't change one pmd rule just for the sake of using !isEmpty here
        if (ctx.getMessage().getAttachments().size() > 0) {
            url = tryGetAttachment(ctx);
        } else if (ctx.getMessage().getMentionedUsers().size() > 0) {
            url = getAvatarUrl(ctx.getMessage().getMentionedUsers().get(0));
        } else if (!args.isEmpty()) {
            if (AirUtils.isURL(args.get(0))) {
                url = tryGetUrl(ctx, args.get(0));
            } else {
                final List<Member> textMentions = FinderUtils.searchMembers(ctx.getArgsJoined(), ctx);

                if (!textMentions.isEmpty()) {
                    url = getAvatarUrl(textMentions.get(0).getUser());
                }
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
//        if (mimetype == null || !mimetype.split("/")[0].equals("image")) {
        if (mimetype == null || !mimetype.startsWith("image/")) {
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

    @Nonnull
    private String getAvatarUrl(User user) {
        return UserKt.getStaticAvatarUrl(user) + "?size=512";
    }

    @Nonnull
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

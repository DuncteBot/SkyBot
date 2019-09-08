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

package ml.duncte123.skybot.commands.image;

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.extensions.StringKt;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

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

    private boolean canSendFile(GuildMessageReceivedEvent event) {
        if (event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_ATTACH_FILES)) {
            return true;
        } else {
            sendMsg(event, "I need permission to upload files in this channel in order for this command to work");
            return false;
        }
    }

    private boolean hasArgs(GuildMessageReceivedEvent event, List<String> args) {
        if (args.isEmpty()) {
            sendMsg(event, "Too little arguments");
            return false;
        }
        return true;
    }

    boolean passes(GuildMessageReceivedEvent event, List<String> args) {
        return passes(event, args, true);
    }

    protected boolean passes(GuildMessageReceivedEvent event, List<String> args, boolean patron) {
        return passesNoArgs(event, patron) && hasArgs(event, args);
    }

    boolean passesNoArgs(GuildMessageReceivedEvent event) {
        return passesNoArgs(event, true);
    }

    protected boolean passesNoArgs(GuildMessageReceivedEvent event, boolean patron) {
        return canSendFile(event) && (!patron || isUserOrGuildPatron(event));
    }

    private String getFileName() {
        return getName() + '_' + System.currentTimeMillis() + ".png";
    }

    public void handleBasicImage(GuildMessageReceivedEvent event, byte[] image) {
        final TextChannel channel = event.getChannel();

        if (event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_ATTACH_FILES)) {
            channel.sendFile(image, getFileName()).queue();
        } else {
            sendMsg(channel, "I need permission to upload files in order for this command to work.");
        }
    }

    @NotNull
    @Override
    public CommandCategory getCategory() {
        return CommandCategory.PATRON;
    }

    protected String getImageFromCommand(CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();

        String url = null;

        if (!ctx.getMessage().getAttachments().isEmpty()) {
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
                sendMsg(event, "That file does not look like an image");
                return null;
            }
            url = attachment.getUrl();
        }

        if (args.isEmpty()) {
            return url;
        }

        if (AirUtils.isURL(args.get(0))) {
            try {
                url = new URL(args.get(0)).toString();
            }
            catch (MalformedURLException ignored) {
                sendMsg(event, "That does not look like a valid url");
                return null;
            }
        }

        if (!ctx.getMessage().getMentionedUsers().isEmpty()) {
            url = ctx.getMessage().getMentionedUsers().get(0)
                .getEffectiveAvatarUrl().replace("gif", "png") + "?size=512";
        }

        if (url  == null) {
            final List<Member> textMentions = FinderUtil.findMembers(ctx.getArgsJoined(), ctx.getGuild());

            if (!textMentions.isEmpty()) {
                url = textMentions.get(0).getUser().getEffectiveAvatarUrl() + "?size=512";
            }
        }

        if (url == null) {
            url = event.getAuthor().getEffectiveAvatarUrl().replace("gif", "png") + "?size=512";
        }

        return url;
    }

    public String parseTextArgsForImage(CommandContext ctx) {
        return StringKt.stripFlags(ctx.getArgsDisplay(), this);
    }
}

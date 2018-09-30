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

package ml.duncte123.skybot.commands.image;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

import static me.duncte123.botCommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public abstract class ImageCommandBase extends Command {

    boolean canSendFile(GuildMessageReceivedEvent event) {
        if (event.getGuild().getSelfMember().hasPermission(event.getChannel(), Permission.MESSAGE_ATTACH_FILES)) {
            return true;
        } else {
            sendMsg(event, "I need permission to upload files in this channel in order for this command to work");
            return false;
        }
    }

    boolean hasArgs(GuildMessageReceivedEvent event, List<String> args) {
        if (args.isEmpty()) {
            sendMsg(event, "Too little arguments");
            return false;
        }
        return true;
    }

    boolean passes(GuildMessageReceivedEvent event, List<String> args) {
        return passes(event, args, true);
    }

    boolean passes(GuildMessageReceivedEvent event, List<String> args, boolean patron) {
        return passesNoArgs(event, patron) && hasArgs(event, args);
    }

    boolean passesNoArgs(GuildMessageReceivedEvent event) {
        return passesNoArgs(event, true);
    }

    boolean passesNoArgs(GuildMessageReceivedEvent event, boolean patron) {
        event.getChannel().sendTyping().queue();
        return canSendFile(event) && (!patron || isUserOrGuildPatron(event));
    }

    private String getFileName() {
        return getName() + "_" + System.currentTimeMillis() + ".png";
    }

    void handleBasicImage(GuildMessageReceivedEvent event, byte[] image) {
        event.getChannel().sendFile(image, getFileName()).queue();
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.PATRON;
    }

    String getImageFromCommand(CommandContext ctx) {
        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgs();

        String url = event.getAuthor().getEffectiveAvatarUrl().replace("gif", "png") + "?size=512";

        if (args.size() > 0 && ctx.getMessage().getMentionedUsers().size() < 1) {
            try {
                url = new URL(args.get(0)).toString();
            } catch (MalformedURLException ignored) {
                sendMsg(event, "That does not look like a valid url");
                return null;
            }
        }

        if (ctx.getMessage().getMentionedUsers().size() > 0) {
            url = ctx.getMessage().getMentionedUsers().get(0)
                    .getEffectiveAvatarUrl().replace("gif", "png") + "?size=512";
        }

        if (!ctx.getMessage().getAttachments().isEmpty()) {
            Message.Attachment attachment = ctx.getMessage().getAttachments().get(0);

            File file = new File(attachment.getFileName());


            String mimetype = null;
            try {
                mimetype = Files.probeContentType(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            //mimetype should be something like "image/png"

            if (mimetype == null || !mimetype.split("/")[0].equals("image")) {
                sendMsg(event, "That file does not look like an image");
                return null;
            }
            url = attachment.getUrl();
        }
        return url;
    }

    String parseTextArgsForImage(CommandContext ctx) {
        return ctx.getArgsDisplay();
    }
}

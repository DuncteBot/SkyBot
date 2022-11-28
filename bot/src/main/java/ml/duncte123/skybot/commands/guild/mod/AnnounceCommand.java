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

package ml.duncte123.skybot.commands.guild.mod;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.messaging.MessageConfig;
import ml.duncte123.skybot.extensions.StringKt;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.*;

public class AnnounceCommand extends ModBaseCommand {

    public AnnounceCommand() {
        this.requiresArgs = true;
        this.requiredArgCount = 2;
        this.category = CommandCategory.ADMINISTRATION;
        this.name = "announce";
        this.help = "Sends an announcement in the specified channel";
        this.usage = "<#channel> <message> [--noembed] [--thumbnail]";
        this.userPermissions = new Permission[]{
            Permission.MANAGE_SERVER,
        };
        this.flags = new Flag[]{
            new Flag(
                "noembed",
                "Displays the announcement as plain text instead of as embed"
            ),
            new Flag(
                "thumbnail",
                "Displays the image as thumbnail instead of a large image"
            ),
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<TextChannel> mentioned = ctx.getMessage().getMentionedChannels();

        if (mentioned.isEmpty()) {
            sendMsg(ctx, "You did not specify a channel, usage: " + this.getUsageInstructions(ctx));

            return;
        }

        final TextChannel targetChannel = mentioned.get(0);

        if (!targetChannel.canTalk()) {
            sendErrorWithMessage(ctx.getMessage(), "I can not talk in " + targetChannel.getAsMention());
            return;
        }

        final var flags = ctx.getParsedFlags(this);
        final List<String> text = flags.get("undefined");

        if (text.isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final String msg = StringKt.stripFlags(
            ctx.getArgsRaw()
                .replace(targetChannel.getAsMention(), ""),
            this
        );

        if (flags.containsKey("noembed")) {
            sendMsg(targetChannel, msg);
            sendSuccess(ctx.getMessage());

            return;
        }


        final EmbedBuilder embed = EmbedUtils.getDefaultEmbed().setDescription(msg).setFooter(null, "");
        final List<Message.Attachment> attachments = ctx.getMessage().getAttachments();

        if (!attachments.isEmpty()) {
            attachments.stream().filter(Message.Attachment::isImage).findFirst().ifPresent((attachment) -> {
                if (flags.containsKey("thumbnail")) {
                    embed.setThumbnail(attachment.getUrl());
                } else {
                    embed.setImage(attachment.getUrl());
                }
            });
        }

        sendMsg(new MessageConfig.Builder()
            .setChannel(targetChannel)
            .addEmbed(embed)
            .build());

        sendSuccess(ctx.getMessage());
    }
}

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

package ml.duncte123.skybot.commands.guild.mod;

import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.extensions.StringKt;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.*;

@Author(nickname = "Sanduhr32", author = "Maurice R S")
public class AnnounceCommand extends ModBaseCommand {

    public AnnounceCommand() {
        this.displayAliasesInHelp = true;
        this.category = CommandCategory.ADMINISTRATION;
        this.name = "announce";
        this.aliases = new String[]{
            "announce1",
            "announce2",
            "announce3",
        };
        this.helpFunction = (invoke, prefix) -> "Sends an announcement in the specified channel";
        this.usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + " <#channel> <message> [--noembed] [--thumbnail]`";
        this.userPermissions = new Permission[]{
            Permission.MANAGE_SERVER,
        };
        this.flags = new Flag[]{
            new Flag(
                'e',
                "noembed",
                "Displays the announcement as plain text instead of as embed"
            ),
            new Flag(
                't',
                "thumbnail",
                "Displays the image as thumbnail instead of a large image"
            ),
        };
    }

    @Override
    public void run(@Nonnull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();

        if (!ctx.getInvoke().equalsIgnoreCase(this.name)) {
            sendMsg(ctx, "The usage of this command changed, please check `" + ctx.getPrefix() + "help " + this.name + '`');
            return;
        }

        if (event.getMessage().getMentionedChannels().isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        final List<TextChannel> mentioned = event.getMessage().getMentionedChannels();

        if (mentioned.isEmpty()) {
            sendMsg(ctx, "You did not specify a channel");

            return;
        }

        final TextChannel targetChannel = mentioned.get(0);

        if (!targetChannel.canTalk()) {
            sendErrorWithMessage(event.getMessage(), "I can not talk in " + targetChannel.getAsMention());
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

        if (flags.containsKey("e")) {
            sendMsg(targetChannel, msg);
            sendSuccess(ctx.getMessage());

            return;
        }


        final EmbedBuilder embed = EmbedUtils.defaultEmbed().setDescription(msg).setFooter(null, "");
        final List<Message.Attachment> attachments = ctx.getMessage().getAttachments();

        if (!attachments.isEmpty()) {
            attachments.stream().filter(Message.Attachment::isImage).findFirst().ifPresent((attachment) -> {
                if (flags.containsKey("t")) {
                    embed.setImage(attachment.getUrl());
                } else {
                    embed.setImage(attachment.getUrl());
                }
            });
        }

        sendEmbed(targetChannel, embed);
        sendSuccess(ctx.getMessage());

    }
}

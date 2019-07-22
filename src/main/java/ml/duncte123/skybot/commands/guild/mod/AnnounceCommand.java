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
import ml.duncte123.skybot.SinceSkybot;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.unstable.utils.ComparatingUtils;
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
        // todo: replace with flags
        this.usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + " <#channel> <message>`\n" +
            "`announce1` and `announce` => Sends the message as plain text\n" +
            "`announce2` => Sends the message as an embed with a small image (if uploaded with message)\n" +
            "`announce3` => Sends the message as an embed with a large image (if uploaded with message)";
        this.userPermissions = new Permission[]{
            Permission.MANAGE_SERVER,
        };
    }

    @Override
    public void run(@Nonnull CommandContext ctx) {
        final String invoke = ctx.getInvoke();
        final GuildMessageReceivedEvent event = ctx.getEvent();

        if (event.getMessage().getMentionedChannels().isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        try {
            final List<TextChannel> mentioned = event.getMessage().getMentionedChannels();

            if (mentioned.isEmpty()) {
                sendMsg(ctx, "You did not specify a channel");

                return;
            }

            final TextChannel targetChannel = mentioned.get(0);

            if (!ctx.getSelfMember().hasPermission(targetChannel, Permission.MESSAGE_WRITE, Permission.MESSAGE_READ)) {
                sendErrorWithMessage(event.getMessage(), "I can not talk in " + targetChannel.getAsMention());
                return;
            }

            @SinceSkybot(version = "3.68.0") final String msg = ctx.getArgsRaw().replaceAll(targetChannel.getAsMention() + " ", "");

            switch (invoke) {
                case "announce1":
                case "announce":
                    sendMsg(targetChannel, msg);
                    sendSuccess(event.getMessage());
                    break;

                default:
                    final EmbedBuilder embed = EmbedUtils.defaultEmbed().setDescription(msg).setFooter(null, "");

                    if (!event.getMessage().getAttachments().isEmpty()) {
                        event.getMessage().getAttachments().stream().filter(Message.Attachment::isImage).findFirst().ifPresent(attachment -> {
                            if (invoke.endsWith("2")) {
                                embed.setThumbnail(attachment.getUrl());
                            } else if (invoke.endsWith("3")) {
                                embed.setImage(attachment.getUrl());
                            }
                        });
                    }

                    sendEmbed(targetChannel, embed);
                    sendSuccess(event.getMessage());
                    break;
            }

        }
        catch (ArrayIndexOutOfBoundsException ex) {
            sendErrorWithMessage(event.getMessage(), "Please! You either forgot the text or to mention the channel!");
        }
        catch (Exception e) {
            sendMsg(event, "WHOOPS: " + e.getMessage());
            ComparatingUtils.execCheck(e);
            e.printStackTrace();
        }
    }
}

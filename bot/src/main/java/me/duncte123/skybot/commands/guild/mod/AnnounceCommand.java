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

package me.duncte123.skybot.commands.guild.mod;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.messaging.MessageConfig;
import me.duncte123.skybot.Variables;
import me.duncte123.skybot.entities.jda.DunctebotGuild;
import me.duncte123.skybot.objects.command.CommandCategory;
import me.duncte123.skybot.objects.command.CommandContext;
import me.duncte123.skybot.objects.command.Flag;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

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
    protected void configureSlashSupport(@NotNull SlashCommandData baseData) {
        baseData.addOptions(
            new OptionData(
                OptionType.CHANNEL,
                "channel",
                "Channel to send the message to",
                true
            ),
            new OptionData(
                OptionType.STRING,
                "message",
                "The message to send",
                true
            ),
            new OptionData(
                OptionType.ATTACHMENT,
                "image",
                "An image to send in the embed",
                false
            ),
            new OptionData(
                OptionType.BOOLEAN,
                "embed",
                "Use an embed instead of plain text (defaults to true)",
                false
            ),
            new OptionData(
                OptionType.BOOLEAN,
                "thumbnail",
                "Set the image as a thumbnail instead of a large image",
                false
            )
        );
    }

    @Override
    public void handleEvent(@NotNull SlashCommandInteractionEvent event, @NotNull DunctebotGuild guild, @NotNull Variables variables) {
        final var channel = event.getOption("channel").getAsChannel();

        if (channel.getType() != ChannelType.TEXT) {
            event.reply("That channel is not a text channel!").queue();
            return;
        }

        final var txtChan = channel.asGuildMessageChannel();

        if (!txtChan.canTalk()) {
            event.reply("I can't talk in that channel").queue();
            return;
        }

        final var message = event.getOption("message").getAsString();

        final var useEmbedOption = event.getOption("embed");
        final var useEmbed = useEmbedOption == null || useEmbedOption.getAsBoolean();

        if (!useEmbed) {
            event.reply("Sending message.....").queue();

            sendMsg(
                new MessageConfig.Builder()
                    .setChannel(txtChan)
                    .setMessage(message)
                    .setSuccessAction((msg) -> {
                        event.getHook()
                            .editOriginal("Message sent: " + msg.getJumpUrl())
                            .queue();
                    })
            );
            return;
        }

        final EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
            .setDescription(message)
            .setFooter(null, "");

        final var attachmentOption = event.getOption("image");
        final var useThumbOption = event.getOption("thumbnail");

        // TODO: download the attachment??
        if (attachmentOption != null) {
            if (useThumbOption != null && useThumbOption.getAsBoolean()) {
                embed.setThumbnail(attachmentOption.getAsAttachment().getUrl());
            } else {
                embed.setImage(attachmentOption.getAsAttachment().getUrl());
            }
        }

        event.reply("Sending message.....").queue();

        sendMsg(new MessageConfig.Builder()
            .setChannel(txtChan)
            .addEmbed(embed)
            .setSuccessAction((msg) -> {
                event.getHook()
                    .editOriginal("Message sent: " + msg.getJumpUrl())
                    .queue();
            })
            .build());

    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        sendMsg(ctx, "Guess what! Even this is a slash command now!");
    }
}

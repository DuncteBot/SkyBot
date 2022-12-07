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

package ml.duncte123.skybot.commands.utils;

import com.github.natanbc.reliqua.limiter.RateLimiter;
import io.sentry.Sentry;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.TwemojiParser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.sticker.Sticker.StickerFormat;
import net.dv8tion.jda.api.entities.sticker.StickerItem;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class EnlargeCommand extends Command {
    public EnlargeCommand() {
        this.name = "enlarge";
        this.help = "Make an emote, avatar or sticker bigger";
        this.usage = "[emote/@user]";
        this.botPermissions = new Permission[] {
            Permission.MESSAGE_ATTACH_FILES
        };
    }

    @Override
    public void execute(@NotNull CommandContext ctx) {
        final Message message = ctx.getMessage();
        final List<StickerItem> stickers = message.getStickers();

        if (!stickers.isEmpty()) {
            final StickerItem sticker = stickers.get(0);
            final StickerFormat formatType = sticker.getFormatType();

            if (formatType == StickerFormat.UNKNOWN || formatType == StickerFormat.LOTTIE) {
                sendMsg(ctx, "The sticker supplied could not be rendered");
                return;
            }

            this.uploadFile(sticker.getIconUrl().replace("apng", "png"), ctx);
            return;
        }

        final List<CustomEmoji> emotes = message.getReactions()
            .stream()
            .map(MessageReaction::getEmoji)
            .filter((it) -> it.getType() == Emoji.Type.CUSTOM)
            .map(EmojiUnion::asCustom)
            .toList();

        if (!emotes.isEmpty()) {
            final CustomEmoji emote = emotes.get(0);

            this.uploadFile(emote.getImageUrl().replace("gif", "png"), ctx);

            return;
        }

        final List<User> mentionedUsers = message.getMentions().getUsers();

        if (!mentionedUsers.isEmpty()) {
            final User user = mentionedUsers.get(0);
            final String avatarUrl = user.getEffectiveAvatarUrl() + "?size=4096";

            // sending the avatar url is cheaper since its already on discord
            sendMsg(ctx, avatarUrl);
            return;
        }

        final List<String> args = ctx.getArgs();

        if (args.isEmpty()) {
            sendMsg(ctx, "Usage: " + this.getUsageInstructions(ctx) +
                "\nYou can also attach a sticker instead (you'll need to type the command first)."
            );
            return;
        }

        final String arg = args.get(0);
        final String emojiUrl = TwemojiParser.parseOne(arg);

        if (emojiUrl == null) {
            sendMsg(ctx, "Your input `" + arg + "` does not look like an emoji to me");
            return;
        }

        this.uploadFile(emojiUrl, ctx);
    }

    private void uploadFile(final String url, final CommandContext ctx) {
        WebUtils.ins.getByteStream(url, (it) -> it.setRateLimiter(RateLimiter.directLimiter())).async(
            (bytes) -> {
                final String[] split = url.split("/");
                final String fileName = split[split.length - 1];

                ctx.getChannel()
                    .sendFiles(
                        FileUpload.fromData(bytes, fileName)
                    )
                    .setMessageReference(ctx.getMessage())
                    .queue();
            },
            (error) -> {
                Sentry.captureException(error);
                sendMsg(ctx, "Failed to fetch image: " + error.getMessage());
            }
        );
    }
}

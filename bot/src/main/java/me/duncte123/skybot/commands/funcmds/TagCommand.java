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

package me.duncte123.skybot.commands.funcmds;

import gnu.trove.map.TLongObjectMap;
import me.duncte123.skybot.Variables;
import me.duncte123.skybot.objects.Tag;
import me.duncte123.skybot.objects.command.Command;
import me.duncte123.skybot.objects.command.CommandCategory;
import me.duncte123.skybot.objects.command.CommandContext;
import me.duncte123.skybot.utils.MapUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static me.duncte123.botcommons.messaging.MessageUtils.sendErrorWithMessage;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.skybot.Settings.PATREON;
import static me.duncte123.skybot.utils.CommandUtils.isDev;
import static me.duncte123.skybot.utils.CommandUtils.parseJagTag;

public class TagCommand extends Command {

    // TODO: don't cache these, look them up in the database instead (it's fast enough)
    private final Map<String, Tag> tagStore = new ConcurrentHashMap<>();
    private final TLongObjectMap<List<Tag>> guildTags = MapUtils.newLongObjectMap();

    public TagCommand(Variables variables) {
        this.category = CommandCategory.FUN;
        this.name = "tag";
        this.aliases = new String[]{
            "pasta",
            "tags",
            "t",
        };
        this.help = "Stores some text for later usage\n" +
            "Tags follow the same parsing as custom commands and the join/leave messages";
        this.usage = "<tag-name/raw/author/delete/create/help> [tag-name] [tag content]";

        variables.getDatabase().loadTags().thenAccept((tags) -> {
            tags.forEach((tag) -> this.tagStore.put(tag.name, tag));
        });
    }

    @Override
    @SuppressWarnings("PMD.NPathComplexity") // this will be rewritten some day
    public void execute(@Nonnull CommandContext ctx) {
        if ("tags".equalsIgnoreCase(ctx.getInvoke())) {
            sendTagsList(ctx);

            return;
        }

        final List<String> args = ctx.getArgs();

        if (args.isEmpty()) {
            sendMsg(ctx, "Missing arguments, check `" + ctx.getPrefix() + ctx.getInvoke() + " help`");

            return;
        }

        final String subCmd = args.get(0);

        if (args.size() == 1) {
            if ("help".equalsIgnoreCase(subCmd) || "?".equalsIgnoreCase(subCmd)) {
                sendTagHelp(ctx);

                return;
            }

            if ("list".equalsIgnoreCase(subCmd)) {
                sendTagsList(ctx);

                return;
            }
        }

        if (args.size() == 2 && handleTwoArgs(ctx, subCmd)) {
            return;
        }

        if ("create".equalsIgnoreCase(subCmd) || "new".equalsIgnoreCase(subCmd)) {
            createTag(ctx);

            return;
        }

        if (this.tagStore.containsKey(subCmd)) {
            sendTag(ctx, this.tagStore.get(subCmd));

            return;
        }

        if (this.guildTags.containsKey(ctx.getGuild().getIdLong())) {
            final List<Tag> tags = this.guildTags.get(ctx.getGuild().getIdLong());
            final Optional<Tag> foundTag = tags.stream()
                .filter((tag) -> subCmd.equals(tag.name))
                .findFirst();

            if (foundTag.isPresent()) {
                sendTag(ctx, foundTag.get());
                return;
            }
        }

        sendMsg(ctx, "Unknown tag `" + subCmd + "`, check `" + ctx.getPrefix() + ctx.getInvoke() + " help`");
    }

    private boolean handleTwoArgs(CommandContext ctx, String subCmd) {
        final String tagName = ctx.getArgs().get(1);

        if ("author".equalsIgnoreCase(subCmd) || "owner".equalsIgnoreCase(subCmd) || "who".equalsIgnoreCase(subCmd)) {
            sendTagOwner(ctx, tagName);

            return true;
        }

        if ("delete".equalsIgnoreCase(subCmd) || "remove".equalsIgnoreCase(subCmd)) {
            removeTag(ctx, tagName);

            return true;
        }

        if ("raw".equalsIgnoreCase(subCmd)) {
            sendTagRaw(ctx, tagName);

            return true;
        }

        return false;
    }

    private void sendTag(CommandContext ctx, Tag tag) {
        final String parsed = parseJagTag(ctx, tag.content);

        if (parsed.length() > 2000) {
            sendErrorWithMessage(ctx.getMessage(), "Error: output is over 2000 character limit");

            return;
        }

        sendMsg(ctx, parsed);
    }

    private void sendTagHelp(CommandContext ctx) {
        final String invoke = ctx.getInvoke();
        final String prefix = ctx.getGuildSettings().getCustomPrefix();
        final String message = String.format(
            """
                Tag help:
                \t`%1$s%2$s help` => Shows this message
                \t`%1$s%2$s list` => Gives you a list of all the tags
                \t`%1$s%2$s author <tag name>` => Shows the owner of a tag
                \t`%1$s%2$s raw <tag name>` => Shows raw content of a tag
                \t`%1$s%2$s delete <tag name>` => Deletes a tag
                \t`%1$s%2$s create <tag name> <tag content>` => Creates a new tag""",
            prefix,
            invoke
        );

        sendMsg(ctx, message);
    }

    private void sendTagRaw(CommandContext ctx, String tagName) {
        if (!this.tagStore.containsKey(tagName)) {
            sendMsg(ctx, "That tag does not exist");

            return;
        }

        sendMsg(ctx, "```\n" + this.tagStore.get(tagName).content + "\n```");
    }

    private void sendTagsList(CommandContext ctx) {
        if (this.tagStore.isEmpty()) {
            sendMsg(ctx, "There are no tags in the system");

            return;
        }

        if (this.tagStore.size() < 100) {
            sendMsg(ctx, String.format("Here is the current tag list: `%s`", String.join("`, `", this.tagStore.keySet())));

            return;
        }

        final Permission perm = Permission.MESSAGE_ATTACH_FILES;

        if (!ctx.getSelfMember().hasPermission(ctx.getChannel().asGuildMessageChannel(), perm)) {
            sendMsg(ctx, "I need the `" + perm.getName() + "` permission for this command to work");

            return;
        }

        final byte[] tagContent = ("[\"" + String.join("\", \"", this.tagStore.keySet()) + "\"]").getBytes();

        ctx.getChannel()
            .sendFiles(FileUpload.fromData(
                tagContent, "tag_names.json"
            ))
            .setContent("Here is the current list of tags")
            .queue();
    }

    private void sendTagOwner(CommandContext ctx, String tagName) {
        if (!this.tagStore.containsKey(tagName)) {
            sendMsg(ctx, "That tag does not exist");

            return;
        }

        final long ownerId = this.tagStore.get(tagName).ownerId;
        final User user = ctx.getShardManager().getUserById(ownerId);
        final String userTag = user == null ? "UnknownUser#0000" : user.getAsTag();

        sendMsg(ctx, String.format("`%s` was created by `%s`", tagName, userTag));
    }

    private void removeTag(CommandContext ctx, String tagName) {
        if (!this.tagStore.containsKey(tagName)) {
            sendMsg(ctx, "That tag does not exist");

            return;
        }

        final Tag tag = this.tagStore.get(tagName);

        if (ctx.getAuthor().getIdLong() != tag.ownerId && !isDev(ctx.getAuthor())) {
            sendMsg(ctx, "You do not own that tag");

            return;
        }

        ctx.getDatabase().deleteTag(tag).thenAccept((pair) -> {
            if (!pair.getFirst()) {
                sendMsg(ctx, "Failed to remove tag: " + pair.getSecond());

                return;
            }

            this.tagStore.remove(tag.name);

            sendMsg(ctx, String.format("Tag `%s` deleted", tag.name));
        });
    }

    private void createTag(CommandContext ctx) {
        if (!isTagPatron(ctx.getMember())) {
            sendMsg(ctx, "Unfortunately only our tag tier patrons are able to create tags.\n" +
                "You can become one for $5/month over at <" + PATREON + '>');

            return;
        }

        final List<String> args = ctx.getArgs();
        final String tagName = args.get(1);

        if (this.tagStore.containsKey(tagName)) {
            sendMsg(ctx, "This tag already exists");

            return;
        }

        if (tagName.length() > 10) {
            sendMsg(ctx, "Max length for the name is 10 characters");

            return;
        }

        final Tag newTag = new Tag(
            tagName,
            ctx.getArgsRaw().split("\\s+", 3)[2],
            ctx.getAuthor().getIdLong()
        );

        ctx.getDatabase().createTag(newTag).thenAccept((pair) -> {
            if (!pair.getFirst()) {
                sendMsg(ctx, "Failed to create tag: " + pair.getSecond());

                return;
            }

            this.tagStore.put(tagName, newTag);

            sendMsg(ctx, String.format("Tag `%s` created", tagName));
        });
    }

    @Deprecated
    private boolean isTagPatron(Member member) {
        return false;
    }
}

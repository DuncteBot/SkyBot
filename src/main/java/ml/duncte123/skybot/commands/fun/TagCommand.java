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

package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.objects.Tag;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsgFormat;

public class TagCommand extends Command {

    private final Map<String, Tag> tagStore = new ConcurrentHashMap<>();

    public TagCommand() {
        this.category = CommandCategory.FUN;

        Variables.getInstance().getDatabaseAdapter().loadTags((tags) -> {
            tags.forEach((tag) -> this.tagStore.put(tag.name, tag));

            return null;
        });
    }

    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();

        if (args.isEmpty()) {
            sendMsg(ctx, "Missing arguments, check `" +
                ctx.getGuildSettings().getCustomPrefix() + ctx.getInvoke() + " help`");

            return;
        }

        final String subCmd = args.get(0);

        if (args.size() == 1) {
            if (subCmd.equalsIgnoreCase("help") || subCmd.equalsIgnoreCase("?")) {
                sendTagHelp(ctx);

                return;
            }

            if (subCmd.equalsIgnoreCase("list")) {
                sendTagsList(ctx);

                return;
            }
        }

        if (args.size() == 2) {
            final String tagName = args.get(1);

            if (subCmd.equalsIgnoreCase("author") || subCmd.equalsIgnoreCase("owner") || subCmd.equalsIgnoreCase("who")) {
                sendTagOwner(ctx, tagName);

                return;
            }

            if (subCmd.equalsIgnoreCase("delete") || subCmd.equalsIgnoreCase("remove")) {
                removeTag(ctx, tagName);

                return;
            }

            if (subCmd.equalsIgnoreCase("raw")) {
                sendTagRaw(ctx, tagName);

                return;
            }
        }

        if (subCmd.equalsIgnoreCase("create") || subCmd.equalsIgnoreCase("new")) {
            createTag(ctx);

            return;
        }

        if (this.tagStore.containsKey(subCmd)) {
            sendMsg(ctx, this.tagStore.get(subCmd).content);

            return;
        }

        sendMsg(ctx, "Unknown tag `" + subCmd + "`, check `" +
            ctx.getGuildSettings().getCustomPrefix() + ctx.getInvoke() + " help`");
    }

    private void sendTagHelp(CommandContext ctx) {
        final String invoke = ctx.getInvoke();
        final String prefix = ctx.getGuildSettings().getCustomPrefix();
        final String message = String.format(
            "Tag help:\n" +
                "\t`%1$s%2$s help` => Shows this message\n" +
                "\t`%1$s%2$s list` => Gives you a list of all the tags\n" +
                "\t`%1$s%2$s author <tag name>` => Shows the owner of a tag\n" +
                "\t`%1$s%2$s raw <tag name>` => Shows raw content of a tag\n" +
                "\t`%1$s%2$s delete <tag name>` => Deletes a tag\n" +
                "\t`%1$s%2$s create <tag name> <tag content>` => Creates a new tag",
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

        final Message message = new MessageBuilder()
            .appendCodeBlock(this.tagStore.get(tagName).content, "").build();

        sendMsg(ctx, message);
    }

    private void sendTagsList(CommandContext ctx) {
        if (this.tagStore.isEmpty()) {
            sendMsg(ctx, "There are no tags in the system");

            return;
        }

        if (this.tagStore.size() < 13) {
            sendMsgFormat(ctx, "Here is the current tag list: `%s`", String.join("`, `", this.tagStore.keySet()));

            return;
        }

        final Permission perm = Permission.MESSAGE_ATTACH_FILES;

        if (!ctx.getSelfMember().hasPermission(ctx.getChannel(), perm)) {
            sendMsg(ctx, "I need the `" + perm.getName() + "` permission for this command to work");

            return;
        }

        final byte[] tagContent = ("[\"" + String.join("\", \"", this.tagStore.keySet()) + "\"]").getBytes();

        ctx.getChannel()
            .sendFile(tagContent, "tag_names.json")
            .append("Here is the current list of tags")
            .queue();
    }

    private void sendTagOwner(CommandContext ctx, String tagName) {
        if (!this.tagStore.containsKey(tagName)) {
            sendMsg(ctx, "That tag does not exist");

            return;
        }

        final long ownerId = this.tagStore.get(tagName).owner_id;
        final User user = ctx.getShardManager().getUserById(ownerId);
        final String userTag = user == null ? "UnknownUser#0000" : user.getAsTag();

        sendMsgFormat(ctx, "`%s` was created by `%s`", tagName, userTag);
    }

    private void removeTag(CommandContext ctx, String tagName) {
        if (!this.tagStore.containsKey(tagName)) {
            sendMsg(ctx, "That tag does not exist");

            return;
        }

        final Tag tag = this.tagStore.get(tagName);

        if (ctx.getAuthor().getIdLong() != tag.owner_id && !isDev(ctx.getAuthor())) {
            sendMsg(ctx, "You do not own that tag");

            return;
        }

        ctx.getDatabaseAdapter().deleteTag(tag, (success, reason) -> {
            if (!success) {
                sendMsg(ctx, "Failed to remove tag: " + reason);

                return null;
            }

            this.tagStore.remove(tag.name);

            sendMsgFormat(ctx, "Tag `%s` deleted", tag.name);

            return null;
        });
    }

    private void createTag(CommandContext ctx) {
        if (!isTagPatron(ctx.getMember())) {
            sendMsg(ctx, "Unfortunately only our tag tier patrons are able to create tags.\n" +
                "You can become one for $5/month over at <https://patreon.com/DuncteBot>");

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

        final Tag newTag = new Tag();

        newTag.owner_id = ctx.getAuthor().getIdLong();
        newTag.name = tagName;
        newTag.content = ctx.getArgsRaw().split("\\s+", 3)[2];

        ctx.getDatabaseAdapter().createTag(newTag, (success, reason) -> {
            if (!success) {
                sendMsg(ctx, "Failed to create tag: " + reason);

                return null;
            }

            this.tagStore.put(tagName, newTag);

            sendMsgFormat(ctx, "Tag `%s` created", tagName);

            return null;
        });
    }

    private boolean isTagPatron(Member member) {
        final User u = member.getUser();

        if (isDev(u) || tagPatrons.contains(u.getIdLong())) {
            return true;
        }

        if (member.getGuild().getIdLong() != supportGuildId) {
            return false;
        }

        final boolean hasRole = member.getRoles()
            .stream()
            .map(Role::getIdLong)
            .anyMatch((it) -> it == tagPatronsRole);

        if (hasRole) {
            tagPatrons.add(u.getIdLong());
        }

        return hasRole;
    }

    @Override
    public String getName() {
        return "tag";
    }

    @Override
    public String help() {
        return "Store it in a tag\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <tag-name/raw/author/delete/create/help> [tag-name] [tag content]`\n" +
            "**Note:** The tag content are plain text and will not be parsed like welcome messages and custom commands";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"pasta", "tags", "t"};
    }
}

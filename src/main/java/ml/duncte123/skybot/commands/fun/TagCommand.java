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
import ml.duncte123.skybot.objects.Tag;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsgFormat;

public class TagCommand extends Command {

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final Map<String, Tag> tagStore = new HashMap<>();

    public TagCommand() {
        this.category = CommandCategory.FUN;

        for (int i = 1; i < 11; i++) {
            final Tag t = new Tag();

            t.name = "tag" + i;
            t.text = randomAlphaNumeric(5 * i);
            t.owner_id = 191231307290771456L;

            tagStore.put(t.name, t);
        }
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
        }

        if (subCmd.equalsIgnoreCase("create") || subCmd.equalsIgnoreCase("new")) {
            createTag(ctx);

            return;
        }

        if (this.tagStore.containsKey(subCmd)) {
            sendMsg(ctx, this.tagStore.get(subCmd).text);

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
                "\t`%1$s%2$s delete <tag name>` => Deletes a tag\n" +
                "\t`%1$s%2$s create <tag name> <tag content>` => Creates a new tag",
            prefix,
            invoke
        );

        sendMsg(ctx, message);
    }

    private void sendTagsList(CommandContext ctx) {
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
        //
    }

    private void createTag(CommandContext ctx) {
        //
    }

    @Override
    public String getName() {
        return "tag";
    }

    @Override
    public String help() {
        return "Store it in a tag\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <tag name/author/delete/create/help> [tag-name] [tag content]`\n" +
            "**Note:** The tag content are plain text and will not be parsed like welcome messages and custom commands";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"pasta", "tags", "t"};
    }

    private static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}

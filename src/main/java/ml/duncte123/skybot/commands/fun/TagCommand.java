/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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

import ml.duncte123.skybot.objects.Tag;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Map;

public class TagCommand extends Command {

    private Map<String, Tag> tags = AirUtils.tagsList;

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {

        if(args.length == 1) {

            if(args[0].equals("help") ||  args[0].equals("?")) {
                MessageBuilder message = new MessageBuilder()
                        .appendCodeBlock("help => shows this\n" +
                                "delete => removes a tag\n" +
                                "author => displays who made the tag\n" +
                                "create => make a new tag", "cs");
                sendMsg(event, message.build());
            } else {
                if(!tags.containsKey(args[0])) {
                    sendMsg(event, "The tag `"+args[0]+"` does not exist.");
                    return;
                }

                sendMsg(event, tags.get(args[0]).getText());
            }

        } else if(args.length == 2) {
            if( (args[0].equals("who") || args[0].equals("author"))) {
                if(!tags.containsKey(args[1])) {
                    sendMsg(event, "The tag `"+args[1]+"` does not exist.");
                    return;
                }

                Tag t = tags.get(args[1]);

                sendMsg(event, "The tag `"+t.getName()+"` is created by `"+t.getAuthor()+"`.");
            } else if(args[0].equals("delete") || args[0].equals("remove")) {
                if(!tags.containsKey(args[1])) {
                    sendMsg(event, "The tag `"+args[1]+"` does not exist.");
                    return;
                }

                Tag t = tags.get(args[1]);
                if(!t.getAuthorId().equals(event.getAuthor().getId())) {
                    sendMsg(event, "You do not own this tag.");
                    return;
                }
                if(AirUtils.deleteTag(t)) {
                    sendMsg(event, "Tag has been deleted successfully");
                } else {
                    sendMsg(event, "Failed to delete this tag");
                }

            }
        } else if(args.length >= 4 && (args[0].equals("create") || args[0].equals("new"))) {
            if(!tags.containsKey(args[1])) {
                sendMsg(event, "The tag `"+args[1]+"` does not exist.");
                return;
            } else if(args[1].length() > 10) {
                sendMsg(event, "Tag name is too long.");
                return;
            }
            if(AirUtils.registerNewTag(event.getAuthor(), new Tag(
                    tags.keySet().size()+1,
                    String.format("%#s", event.getAuthor()),
                    event.getAuthor().getId(),
                    args[1],
                    StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ")))) {
                sendMsg(event, "Tag added successfully.");
            } else {
                sendMsg(event, "Failed to add tag.");
            }
        }

    }

    @Override
    public String help() {
        return "Save it in a tag\n" +
                "Usage: `"+this.PREFIX+getName()+" <tag_name/author/delete/create/help> [tag_name] [tag contents]`";
    }

    @Override
    public String getName() {
        return "tag";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"pasta", "tags", "t"};
    }
}

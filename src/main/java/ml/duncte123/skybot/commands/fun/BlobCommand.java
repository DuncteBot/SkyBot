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

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.utils.Settings;
import ml.duncte123.skybot.utils.WebUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;

public class BlobCommand extends Command {

    public BlobCommand() {
        this.category = CommandCategory.FUN;
    }

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {

        String blob = "blobnomcookie";

        if(args.length > 0) {
            blob = StringUtils.join(args);
        }

            Response response = WebUtils.getRequest("https://i.duncte123.ml/blob/" + blob + ".png");

            ResponseBody responseBody = response.body();

            if(responseBody.contentLength() <= 0) {
                sendMsg(event, "This blob was not found on the server!!!");
                if(response != null) {
                    response.close();
                }
                return;
            }

            event.getChannel().sendFile(responseBody.byteStream(), "blob.png", null).queue(
                    unused -> {
                        if(response != null) {
                            response.close();
                        }
                    }
            );
    }

    @Override
    public String help() {
        return "Gives you a blob.\n" +
                "Usage: `" + Settings.prefix+getName() + " [blob name]`";
    }

    @Override
    public String getName() {
        return "blob";
    }
}

/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ml.duncte123.skybot.commands.fun;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.Settings;
import ml.duncte123.skybot.utils.WebUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;

public class BlobCommand extends Command {
    /**
     * {@inheritDoc}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

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

    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        return "Gives you a blob.\n" +
                "Usage: `" + Settings.prefix+getName() + " [blob name]`";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "blob";
    }
}

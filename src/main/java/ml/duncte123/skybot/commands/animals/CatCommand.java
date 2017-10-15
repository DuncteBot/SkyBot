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

package ml.duncte123.skybot.commands.animals;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.WebUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONObject;

import java.net.URL;

public class CatCommand extends Command {

    public final static String help = "here is a cat.";

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {
        // TODO Auto-generated method stub;

        try {
            String jsonString = WebUtils.getText("http://random.cat/meow");
            JSONObject jsonObject = new JSONObject(jsonString);
            String newJSON = jsonObject.getString("file");
            event.getChannel().sendFile(new URL(newJSON).openStream(), "cat_" + System.currentTimeMillis() + ".png", null).queue();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            sendEmbed(event, EmbedUtils.embedMessage("OOPS: " + e.getMessage()));
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        // TODO Auto-generated method stub
        return help;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "cat";
    }
}

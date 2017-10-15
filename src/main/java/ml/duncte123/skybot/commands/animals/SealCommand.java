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
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.net.URL;

public class SealCommand extends Command {

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeCommand(String[] args, GuildMessageReceivedEvent event) {

        try {
            int availableSeals = 83;
            int sealID = (int) Math.floor(Math.random() * availableSeals) + 1;
            String idStr = ("0000" + String.valueOf(sealID)).substring(String.valueOf(sealID).length());
            String sealLoc = "https://raw.githubusercontent.com/TheBITLINK/randomse.al/master/seals/" + idStr + ".jpg";

            if (event.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_ATTACH_FILES)) {
                event.getChannel().sendFile(new URL(sealLoc).openStream(), "Seal_"+System.currentTimeMillis()+".jpg", null).queue();
            } else {
                sendMsg(event, sealLoc);
            }
        }
        catch (Exception e) {
            sendMsg(event, "ERROR: "+e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String help() {
        return "Here is a nice seal";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "seal";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getAliases() {
        return new String[]{"zeehond"};
    }
}

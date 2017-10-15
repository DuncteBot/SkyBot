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

package ml.duncte123.skybot.parsers;

import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;


public class CommandParser {

    /**
     * This will split an command into the command and the args
     * @param rw the raw text
     * @param e An instance of the {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
     * @return Our {@link ml.duncte123.skybot.parsers.CommandParser.CommandContainer CommandContainer}
     */
    public CommandContainer parse(String rw, GuildMessageReceivedEvent e){
        final String[] split = rw.replaceFirst(Settings.prefix, "").split("\\s+");
        final String invoke = split[0].toLowerCase();
        final String[] args = Arrays.copyOfRange(split, 1, split.length);

        return new CommandContainer(invoke, args, e);
    }

     public class CommandContainer {
         public final String invoke;
         public final String[] args;
         public final GuildMessageReceivedEvent event;

         /**
          * Puts the contents of a command in a simple class
          * @param invoke The command that is ran
          * @param args The arguments from the command
          * @param e A ninstance of the {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
          */
         public CommandContainer(String invoke, String[] args, GuildMessageReceivedEvent e){
             this.invoke = invoke;
             this.args = args;
             this.event = e;
         }
     }

}

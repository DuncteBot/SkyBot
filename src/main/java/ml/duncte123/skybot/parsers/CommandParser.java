/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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
 *
 */

package ml.duncte123.skybot.parsers;

import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;
import java.util.regex.Pattern;

public class CommandParser {

    /**
     * This will split an command into the command and the args
     *
     * @param rw the raw text
     * @param e  An instance of the {@link GuildMessageReceivedEvent GuildMessageReceivedEvent}
     * @return Our {@link CommandContainer CommandContainer}
     */
    public CommandContainer parse(String rw, GuildMessageReceivedEvent e) {
        final String[] split = rw.replaceFirst(Pattern.quote(Settings.prefix), "").split("\\s+");
        final String invoke = split[0].toLowerCase();
        final String[] args = Arrays.copyOfRange(split, 1, split.length);

        return new CommandContainer(invoke, args, e);
    }

    public static class CommandContainer {
        public final String invoke;
        public final String[] args;
        public final GuildMessageReceivedEvent event;

        /**
         * Puts the contents of a command in a simple class
         *
         * @param invoke The command that is ran
         * @param args   The arguments from the command
         * @param e      A ninstance of the {@link net.dv8tion.jda.core.events.message.MessageReceivedEvent MessageReceivedEvent}
         */
        private CommandContainer(String invoke, String[] args, GuildMessageReceivedEvent e) {
            this.invoke = invoke;
            this.args = args;
            this.event = e;
        }
    }

}

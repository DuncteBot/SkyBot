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

package ml.duncte123.skybot.commands.essentials;

import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class WolframAlphaCommand extends Command {

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        sendMsg(event, "This command is being worked on.");
        WAEngine engine = AirUtils.alphaEngine;
        
        if(engine == null) {
            sendMsg(event, ":x: Wolfram|Alpha function unavailable!");
            return;
        }
        
        if(args.length == 0) {
            sendMsg(event, ":x: Must give a question!!!");
            return;
        }
        
        String queryString
            = event.getMessage().getRawContent().substring(invoke.length() + 1);
        
        WAQuery query = engine.createQuery(queryString);
        
        WAQueryResult result;
        
        try {
            result = engine.performQuery(query);
        } catch (WAException e) {
            event.getChannel().sendMessage(":x: Error: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage())
                    .queue(); 
            e.printStackTrace();
            return;
        }
        
        event.getChannel().sendMessage(generateEmbed(result)).queue();
    }

    // TODO: Displaying
    //       |-- Need some structure
    //       |-- Custom?
    //       |-- Must display everything?
    public static MessageEmbed generateEmbed(WAQueryResult result) {
        return null;
    }

    @Override
    public String help() {
        return "Query Wolfram|Alpha with all your geeky questions";
    }

    @Override
    public String getName() {
        return "alpha";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"wolfram", "wa", "wolframalpha"};
    }
}

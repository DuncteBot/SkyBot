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

import java.time.OffsetDateTime;

import com.wolfram.alpha.WAEngine;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAImage;
import com.wolfram.alpha.WAInfo;
import com.wolfram.alpha.WALink;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WASound;
import com.wolfram.alpha.WASubpod;
import com.wolfram.alpha.visitor.Visitable;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.WebUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
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
        
        // Borrowed from EvalCommand ;p
        String queryString
            = event.getMessage().getRawContent()
                    .substring(event.getMessage().getRawContent()
                            .split(" ")[0].length());
        
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
        
        
        sendEmbed(event, generateEmbed(event, result));
    }

    // TODO: Displaying
    //       |-- Need some structure
    //       |-- Custom?
    //       |-- Must display everything?
    /**
     * Generates an embed for the {@link WAQueryResult result of a computation}
     * 
     * @param event The event
     * @param result The result generated
     * @return An {@link MessageEmbed embed} representing this {@link WAQueryResult result}
     */
    public static MessageEmbed generateEmbed(
            GuildMessageReceivedEvent event,
            WAQueryResult result) {
        Member m = event.getMember();
        EmbedBuilder eb = EmbedUtils.defaultEmbed();
        
        eb.setTitle("**Input:** " + result.getQuery().getInput(),
                result.getQuery().toWebsiteURL());
        
        eb.setTimestamp(OffsetDateTime.now());
        eb.setAuthor(m.getEffectiveName(), null, m.getUser().getAvatarUrl());
        
        for(WAPod pod : result.getPods()) {
            String name = pod.getTitle();
            
            StringBuilder embeds = new StringBuilder();
            
            for(WASubpod sp : pod.getSubpods()) {
                StringBuilder e = new StringBuilder("```\n");
                
                e.append("  " + sp.getTitle());
                
                for(Visitable v : sp.getContents()) {
                    String d = "    ";
                    
                    if(v instanceof WAImage) {
                        WAImage i = (WAImage) v;
                        d += i.getAlt() + ": "
                                    + WebUtils.shorten(i.getURL());
                    } else if(v instanceof WAInfo) {
                        WAInfo i = (WAInfo) v;
                        
                        d += i.getText();
                        
                        // TODO: Display more...
                    } else if(v instanceof WALink) {
                        WALink l = (WALink) v;
                        
                        d += l.getText() + ": " +  WebUtils.shorten(l.getURL());
                    } else if(v instanceof WAPlainText) {
                        WAPlainText pt = (WAPlainText) v;
                        
                        d += pt.getText();
                    } else if(v instanceof WASound) {
                        WASound sound = (WASound) v;
                        d += WebUtils.shorten(sound.getURL());
                    }
                    
                    e.append(d + "\n\n");
                }
                
                embeds.append(e.toString() + "\n```\n\n");
            }
            
            eb.addField(new MessageEmbed.Field(name, embeds.toString().trim(), false));
        }
        
        return eb.build();
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

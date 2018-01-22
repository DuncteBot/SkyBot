/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import com.wolfram.alpha.*;
import com.wolfram.alpha.visitor.Visitable;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.WebUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class WolframAlphaCommand extends Command {

    public WolframAlphaCommand() {
        this.category = CommandCategory.NERD_STUFF;
    }

    /**
     * Generates an embed for the {@link WAQueryResult result of a computation}
     *
     * @param event  The event
     * @param result The result generated
     * @return An {@link MessageEmbed embed} representing this {@link WAQueryResult result}
     */
    public static MessageEmbed generateEmbed(
                                            GuildMessageReceivedEvent event,
                                            WAQueryResult result) {
        Member m = event.getMember();
        EmbedBuilder eb = EmbedUtils.defaultEmbed();
        eb.setAuthor(m.getUser().getName(), null, m.getUser().getAvatarUrl());

        eb.setTitle("**Input:** " + a(result.getQuery().getInput()),
                a(result.getQuery().toWebsiteURL()));

        for (WAPod pod : result.getPods()) {
            String name = a(pod.getTitle());

            StringBuilder embeds = new StringBuilder();

            for (WASubpod sp : pod.getSubpods()) {
                StringBuilder e = new StringBuilder();

                e.append(a(sp.getTitle()));

                for (Visitable v : sp.getContents()) {
                    String d = "";

                    if (v instanceof WAImage) {
                        WAImage i = (WAImage) v;
                        d += "[" + a(i.getAlt()) + "]("
                                + WebUtils.shortenUrl(i.getURL()) + ")";
                    } else if (v instanceof WAInfo) {
                        WAInfo i = (WAInfo) v;

                        d += a(i.getText());

                        // TODO: Display more...
                    } else if (v instanceof WALink) {
                        WALink l = (WALink) v;

                        d += "[" + a(l.getText()) + "](" + WebUtils.shortenUrl(l.getURL()) + ")";
                    } else if (v instanceof WAPlainText) {
                        WAPlainText pt = (WAPlainText) v;

                        d += a(pt.getText());
                    } else if (v instanceof WASound) {
                        WASound sound = (WASound) v;
                        d += WebUtils.shortenUrl(sound.getURL());
                    }
                    
                    e.append(d).append("\n\n");
                }
                
                embeds.append(a(e.toString().trim())).append("\n\n");
            }
            
            eb.addField(name, a(embeds.toString().trim()), false);
        }
        
        return eb.build();
    }

    private static String a(String s) {
        if(s == null) return "null";

        if(s.length() <= 2000 - 6) return s;

        return s.substring(2000 - 6 - 1) + '\u2026';
    }
    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        if(!isPatron(event.getAuthor(), event.getChannel())) return;
        sendMsg(event, "This command is being worked on.");
        WAEngine engine = AirUtils.alphaEngine;
        
        if (engine == null) {
            sendMsg(event, ":x: Wolfram|Alpha function unavailable!");
            return;
        }
        
        if (args.length == 0) {
            sendMsg(event, ":x: Must give a question!!!");
            return;
        }
        
        String queryString
                = event.getMessage().getContentRaw()
                      .substring(event.getMessage().getContentRaw()
                             .split(" ")[0].length());

        WAQuery query = engine.createQuery(queryString);

        WAQueryResult result;

        try {
            result = engine.performQuery(query);
        } catch (WAException e) {
            event.getChannel().sendMessage(":x: Error: "
                               + e.getClass().getSimpleName() + ": " + e.getMessage()).queue();
            e.printStackTrace();
            return;
        }
        
        sendEmbed(event, generateEmbed(event, result));
    }

    @Override
    public String help() {
        return "Ask Wolfram|Alpha all your geeky questions";
    }

    @Override
    public String getName() {
        return "alpha";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"wolfram", "wa", "wolframalpha"};
    }
}

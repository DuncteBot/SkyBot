/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import me.duncte123.botCommons.messaging.MessageUtils;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.EmbedUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static ml.duncte123.skybot.utils.AirUtils.shortenUrl;
import static ml.duncte123.skybot.utils.MessageUtils.editMsg;

public class WolframAlphaCommand extends Command {

    private WAEngine waEngine = null;

    public WolframAlphaCommand() {
        this.category = CommandCategory.NERD_STUFF;
    }

    private static String a(String s) {
        if (s == null) return "null";

        if (s.length() <= 2000 - 6) return s;

        return s.substring(2000 - 6 - 1) + '\u2026';
    }

    /**
     * Generates an embed for the {@link WAQueryResult result of a computation}
     *
     * @param event  The event
     * @param result The result generated
     * @return An {@link MessageEmbed embed} representing this {@link WAQueryResult result}
     */
    private MessageEmbed generateEmbed(
            GuildMessageReceivedEvent event,
            WAQueryResult result,
            String googleKey
    ) {
        Member m = event.getMember();
        EmbedBuilder eb = EmbedUtils.defaultEmbed();
        eb.setAuthor(m.getUser().getName(), null, m.getUser().getAvatarUrl());

        eb.setTitle("**Input:** " + a(result.getQuery().getInput()),
                a(result.getQuery().toWebsiteURL()));

        for (WAPod pod : result.getPods()) {
            String name = a(pod.getTitle());
            StringBuilder embeds = new StringBuilder();
            //Loop over the subpods
            for (WASubpod sp : pod.getSubpods()) {
                //yet another stringbuilder
                StringBuilder e = new StringBuilder();
                //append the title
                e.append(a(sp.getTitle()));
                //loop over the contents
                for (Visitable v : sp.getContents()) {
                    String d = "";
                    if (v instanceof WAImage) {
                        WAImage i = (WAImage) v;
                        d += "[" + a(i.getTitle()) + "](" + shortenUrl(i.getURL(), googleKey).execute() + ")";
                    } else if (v instanceof WAInfo) {
                        WAInfo i = (WAInfo) v;
                        d += a(i.getText());
                        //Ramid when?
                        // TODO: Display more...
                    } else if (v instanceof WALink) {
                        WALink l = (WALink) v;
                        d += "[" + a(l.getText()) + "](" + shortenUrl(l.getURL(), googleKey).execute() + ")";
                    } else if (v instanceof WAPlainText) {
                        WAPlainText pt = (WAPlainText) v;
                        d += a(pt.getText());
                    } else if (v instanceof WASound) {
                        WASound sound = (WASound) v;
                        d += shortenUrl(sound.getURL(), googleKey).execute();
                    }

                    e.append(d).append("\n\n");
                }

                embeds.append(a(e.toString().trim())).append("\n\n");
            }

            eb.addField(name, a(embeds.toString().trim()), false);
        }

        return eb.build();
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();
        List<String> args = ctx.getArgs();

        if (!isUserOrGuildPatron(event)) return;

        if (args.size() == 0) {
            MessageUtils.sendMsg(event, ":x: Must give a question!!!");
            return;
        }

        WAEngine engine = getWolframEngine(ctx.getConfig().apis.wolframalpha);
        if (engine == null) {
            MessageUtils.sendMsg(event, ":x: Wolfram|Alpha function unavailable!");
            return;
        }

        MessageUtils.sendMsg(event, "Calculating.....", message -> {
            String queryString = ctx.getArgsRaw();
                    /*= event.getMessage().getContentRaw()
                    .substring(event.getMessage().getContentRaw()
                            .split(" ")[0].length());*/

            WAQuery query = engine.createQuery(queryString);
            WAQueryResult result;
            try {
                result = engine.performQuery(query);
            } catch (WAException e) {
                message.editMessage(":x: Error: "
                        + e.getClass().getSimpleName() + ": " + e.getMessage()).queue();
                e.printStackTrace();
                return;
            }
            editMsg(message, new MessageBuilder().append("Result:")
                    .setEmbed(generateEmbed(event, result, ctx.getConfig().apis.googl)).build());
        });
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

    private WAEngine getWolframEngine(String appId) {

        if (waEngine != null) {
            return waEngine;
        }

        if (appId == null || appId.isEmpty())
            return null;

        WAEngine engine = new WAEngine();

        engine.setAppID(appId);

        engine.setIP("0.0.0.0");
        engine.setLocation("San Francisco");
        engine.setMetric(true);
        engine.setCountryCode("USA");

        waEngine = engine;

        return engine;
    }
}
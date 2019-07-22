/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2019  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.messaging.MessageUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static ml.duncte123.skybot.utils.AirUtils.shortenUrl;
import static ml.duncte123.skybot.utils.CommandUtils.isUserOrGuildPatron;

@Author(nickname = "ramidzkh", author = "Ramid Khan")
public class WolframAlphaCommand extends Command {

    private WAEngine waEngine = null;

    public WolframAlphaCommand() {
        this.category = CommandCategory.UTILS;
        this.name = "alpha";
        this.aliases = new String[]{
            "wolfram",
            "wa",
            "wolframalpha",
        };
        this.helpFunction = (invoke, prefix) -> "Ask Wolfram|Alpha all your geeky questions";
        this.usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + " <query>`";
    }

    private MessageEmbed generateEmbed(GuildMessageReceivedEvent event, WAQueryResult result, String googleKey) {
        final Member m = event.getMember();
        final EmbedBuilder eb = EmbedUtils.defaultEmbed();
        eb.setAuthor(m.getUser().getName(), "https://patreon.com/DuncteBot", m.getUser().getAvatarUrl());

        eb.setTitle("**Input:** " + parseString(result.getQuery().getInput()),
            parseString(result.getQuery().toWebsiteURL()));

        for (final WAPod pod : result.getPods()) {
            final String name = parseString(pod.getTitle());
            final StringBuilder embeds = new StringBuilder();
            //Loop over the subpods
            for (final WASubpod sp : pod.getSubpods()) {
                //yet another stringbuilder
                final StringBuilder e = new StringBuilder();
                //append the title
                e.append(parseString(sp.getTitle()));
                //loop over the contents
                for (final Visitable v : sp.getContents()) {
                    String d = "";
                    if (v instanceof WAImage) {
                        final WAImage i = (WAImage) v;
                        d += "[Image by text](" + shortenUrl(i.getURL(), googleKey).execute() + ")";
                    } else if (v instanceof WAInfo) {
                        final WAInfo i = (WAInfo) v;
                        d += parseString(i.getText());
                        System.out.println(i.getText());
                        //Ramid when?
                        // TODO: Display more...
                    } else if (v instanceof WALink) {
                        final WALink l = (WALink) v;
                        d += "[" + parseString(l.getText()) + "](" + shortenUrl(l.getURL(), googleKey).execute() + ")";
                    } else if (v instanceof WAPlainText) {
                        final WAPlainText pt = (WAPlainText) v;
                        d += parseString(pt.getText());
                    } else if (v instanceof WASound) {
                        final WASound sound = (WASound) v;
                        d += shortenUrl(sound.getURL(), googleKey).execute();
                    }

                    e.append(d).append("\n\n");
                }

                embeds.append(parseString(e.toString().trim())).append("\n\n");
            }

            eb.addField(name, parseString(embeds.toString().trim()), false);
        }

        return eb.build();
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {

        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();

        if (!isUserOrGuildPatron(event)) return;

        if (args.isEmpty()) {
            MessageUtils.sendMsg(event, ":x: Must give a question!!!");
            return;
        }

        final WAEngine engine = getWolframEngine(ctx.getConfig().apis.wolframalpha);
        if (engine == null) {
            MessageUtils.sendMsg(event, ":x: Wolfram|Alpha function unavailable!");
            return;
        }

        final AtomicReference<Message> message = new AtomicReference<>();

        MessageUtils.sendMsg(event, "Calculating.....", message::set);

        final String queryString = ctx.getArgsRaw();
        final WAQuery query = engine.createQuery(queryString);
        final WAQueryResult result;

        try {
            result = engine.performQuery(query);
        }
        catch (WAException e) {
            editMsg(message, ctx.getChannel(), new MessageBuilder()
                .append(":x: Error: ")
                .append(e.getClass().getSimpleName())
                .append(": ")
                .append(e.getMessage())
                .build());
            e.printStackTrace();
            return;
        }

        editMsg(message, ctx.getChannel(), new MessageBuilder().append("Result:")
            .setEmbed(generateEmbed(event, result, ctx.getConfig().apis.googl)).build());
    }

    private void editMsg(AtomicReference<Message> ref, TextChannel channel, Message message) {
        final Message fromRef = ref.get();

        if (fromRef == null) {
            channel.sendMessage(message).queue();

            return;
        }

        fromRef.editMessage(message).override(true).queue();
    }

    private WAEngine getWolframEngine(final String appId) {

        if (waEngine != null) {
            return waEngine;
        }

        if (appId == null || appId.isEmpty()) {
            return null;
        }

        final WAEngine engine = new WAEngine();

        engine.setAppID(appId);

        engine.setIP("0.0.0.0");
        engine.setLocation("San Francisco");
        engine.setMetric(true);
        engine.setCountryCode("USA");

        waEngine = engine;

        return engine;
    }

    private static String parseString(final String s) {
        if (s == null) {
            return "null";
        }

        if (s.length() <= 2000 - 6) {
            return s;
        }

        return s.substring(2000 - 6 - 1) + '\u2026';
    }
}

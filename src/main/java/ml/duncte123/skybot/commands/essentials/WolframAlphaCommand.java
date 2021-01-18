/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wolfram.alpha.*;
import com.wolfram.alpha.visitor.Visitable;
import me.duncte123.botcommons.StringUtils;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.messaging.MessageConfig;
import me.duncte123.botcommons.messaging.MessageUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;

import static ml.duncte123.skybot.utils.AirUtils.shortenUrl;
import static ml.duncte123.skybot.utils.CommandUtils.isUserOrGuildPatron;

@Author(nickname = "ramidzkh", author = "Ramid Khan")
public class WolframAlphaCommand extends Command {

    private WAEngine waEngine = null;

    public WolframAlphaCommand() {
        this.requiresArgs = true;
        this.category = CommandCategory.UTILS;
        this.name = "alpha";
        this.aliases = new String[]{
            "wolfram",
            "wa",
            "wolframalpha",
        };
        this.help = "Ask Wolfram|Alpha all your geeky questions";
        this.usage = "<query>";
    }

    private MessageEmbed generateEmbed(GuildMessageReceivedEvent event, WAQueryResult result, String googleKey, ObjectMapper mapper) {
        final Member member = event.getMember();
        final EmbedBuilder embed = EmbedUtils.getDefaultEmbed();
        embed.setAuthor(member.getEffectiveName(), "https://patreon.com/DuncteBot", member.getUser().getAvatarUrl());

        embed.setTitle("**Input:** " + parseString(result.getQuery().getInput()),
            parseString(result.getQuery().toWebsiteURL()));

        if (result.getPods().length == 0) {
            return embed.setDescription("Wolfram|Alpha returned no results").build();
        }

        for (final WAPod pod : result.getPods()) {
            final String name = parseString(pod.getTitle());
            final StringBuilder embeds = new StringBuilder();
            //Loop over the subpods
            for (final WASubpod sp : pod.getSubpods()) {
                //yet another stringbuilder
                final StringBuilder builder = new StringBuilder();
                //append the title
                builder.append(parseString(sp.getTitle()));
                //loop over the contents
                for (final Visitable variable : sp.getContents()) {
                    if (variable instanceof WAImage) {
                        final WAImage image = (WAImage) variable;
                        builder.append("[Image by text](")
                            .append(shortenUrl(image.getURL(), googleKey, mapper).execute())
                            .append(')');
                    } else if (variable instanceof WAInfo) {
                        final WAInfo info = (WAInfo) variable;
                        builder.append(parseString(info.getText()));
                        //System.out.println(i.getText());
                        //Ramid when?
                        // TODO: Display more...
                    } else if (variable instanceof WALink) {
                        final WALink link = (WALink) variable;
                        builder.append('[')
                            .append(parseString(link.getText()))
                            .append("](")
                            .append(shortenUrl(link.getURL(), googleKey, mapper).execute())
                            .append(')');
                    } else if (variable instanceof WAPlainText) {
                        final WAPlainText plainText = (WAPlainText) variable;
                        builder.append(parseString(plainText.getText()));
                    } else if (variable instanceof WASound) {
                        final WASound sound = (WASound) variable;
                        builder.append(shortenUrl(sound.getURL(), googleKey, mapper).execute());
                    }

                    builder.append("\n\n");
                }

                embeds.append(parseString(builder.toString().trim())).append("\n\n");
            }

            embed.addField(name, parseString(embeds.toString().trim()), false);
        }

        return embed.build();
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if (!isUserOrGuildPatron(ctx)) {
            return;
        }

        final WAEngine engine = getWolframEngine(ctx.getConfig().apis.wolframalpha);

        if (engine == null) {
            MessageUtils.sendMsg(ctx, ":x: Wolfram|Alpha function unavailable!");
            return;
        }

        final AtomicReference<Message> message = new AtomicReference<>();

        MessageUtils.sendMsg(MessageConfig.Builder.fromCtx(ctx)
            .setMessage("Calculating.....")
            .setSuccessAction(message::set)
            .build());

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
            .setEmbed(
                generateEmbed(ctx.getEvent(), result, ctx.getConfig().apis.googl, ctx.getVariables().getJackson())
            ).build());
    }

    private void editMsg(AtomicReference<Message> ref, TextChannel channel, Message message) {
        final Message fromRef = ref.get();

        if (fromRef == null) {
            channel.sendMessage(message).queue();

            return;
        }

        fromRef.editMessage(message).override(true).queue();
    }

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
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

    private static String parseString(final String input) {
        if (input == null) {
            return "null";
        }

        return StringUtils.abbreviate(input, 1024);
    }
}

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

package ml.duncte123.skybot.commands.uncategorized;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class ChangeLogCommand extends Command {

    private String embedJson = null;

    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {

        if (embedJson == null || embedJson.isEmpty()) {
            fetchLatetstGitHubCommits(ctx.getEvent());
            return;
        }

        final JDAImpl jda = (JDAImpl) ctx.getJDA();

        final MessageEmbed embed = jda.getEntityBuilder().createMessageEmbed(new JSONObject(embedJson));

        sendEmbed(ctx.getEvent(), embed);
    }

    @NotNull
    @Override
    public String help(@NotNull String prefix) {
        return "shows the changelog on the bot";
    }

    @NotNull
    @Override
    public String getName() {
        return "changelog";
    }

    private void fetchLatetstGitHubCommits(GuildMessageReceivedEvent event) {
        WebUtils.ins.getJSONObject("https://api.github.com/repos/DuncteBot/SkyBot/releases/latest").async(json -> {
            final String body = json.get("body").asText();
            final EmbedBuilder eb = EmbedUtils.defaultEmbed()
                .setTitle("Changelog for DuncteBot", json.get("html_url").asText());

            for (final String item : body.split("\n")) {
                final String hash = item.substring(0, 7);
                final String text = item.substring(8);

                eb.appendDescription(String.format("[%s](http://g.entered.space/%s)%n", text, hash));
            }

            // fallback if with url is too long
            if (eb.getDescriptionBuilder().length() > MessageEmbed.TEXT_MAX_LENGTH) {
                eb.setDescription(body);
            }

            final MessageEmbed embed = eb.build();

            embedJson = embed.toJSONObject()
                .put("type", "rich")
                .toString();

            sendEmbed(event, embed);
        });
    }
}

/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.uncategorized;

import com.github.natanbc.reliqua.limiter.RateLimiter;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.JDAImpl;

import javax.annotation.Nonnull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

public class ChangeLogCommand extends Command {

    private String embedJson = null;

    public ChangeLogCommand() {
        this.name = "changelog";
        this.help = "Shows the latest changelog from the bot";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if (embedJson == null || embedJson.isEmpty()) {
            fetchLatetstGitHubCommits(ctx);
            return;
        }

        final JDAImpl jda = (JDAImpl) ctx.getJDA();

        final MessageEmbed embed = jda.getEntityBuilder().createMessageEmbed(DataObject.fromJson(embedJson));

        sendEmbed(ctx, new EmbedBuilder(embed));
    }

    private void fetchLatetstGitHubCommits(CommandContext ctx) {
        WebUtils.ins.getJSONObject(
            "https://api.github.com/repos/DuncteBot/SkyBot/releases/latest",
            (it) -> it.setRateLimiter(RateLimiter.directLimiter())
        ).async(json -> {
            final String body = json.get("body").asText();
            final String version = json.get("tag_name").asText();
            final EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                .setTitle("Changelog for DuncteBot v" + version, json.get("html_url").asText());

            for (final String item : body.split("\n")) {
                final String hash = item.substring(0, 7);
                final String text = item.substring(8).trim();

                builder.appendDescription(String.format("[%s](http://g.duncte.bot/%s)%n", text, hash));
            }

            // fallback if with url is too long
            if (builder.getDescriptionBuilder().length() > MessageEmbed.TEXT_MAX_LENGTH) {
                builder.setDescription(body);
            }

            final EmbedBuilder embed = builder.setFooter("Released on", null)
                .setTimestamp(Instant.ofEpochMilli(parseTimeStamp(json.get("published_at").asText())));

            embedJson = embed.build()
                .toData()
                .put("type", "rich")
                .toString();

            sendEmbed(ctx, embed);
        });
    }

    private long parseTimeStamp(String timestamp) {
        try {
            final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            final Date parsed = format.parse(timestamp);

            return parsed.getTime();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        return 0L;
    }
}

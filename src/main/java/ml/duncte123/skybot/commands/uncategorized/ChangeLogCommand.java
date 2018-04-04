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

package ml.duncte123.skybot.commands.uncategorized;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import ml.duncte123.skybot.utils.WebUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class ChangeLogCommand extends Command {
    @Override
    public void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event) {
        WebUtils.ins.getJSONArray("https://api.github.com/repos/duncte123/SkyBot/releases").async(json -> {
            String date1 = json.getJSONObject(1).getString("published_at");
            String date2 = json.getJSONObject(0).getString("published_at");
            WebUtils.ins.getJSONArray("https://api.github.com/repos/duncte123/SkyBot/commits?since=" + date1 +
                    "&until=" + date2).async(commits -> {
                EmbedBuilder eb = EmbedUtils.defaultEmbed()
                        .setTitle("Changelog for DuncetBot", "https://github.com/duncte123/DuncteBot");
                commits.forEach(c -> {
                    JSONObject j = (JSONObject) c;
                    eb.appendDescription(j.getJSONObject("commit").getString("message") + " - " +
                                    j.getJSONObject("committer").getString("login") + "\n");
                });
                MessageUtils.sendEmbed(event, eb.build());
            });
        });
    }

    @Override
    public String help() {
        return "shows the changelog on the bot";
    }

    @Override
    public String getName() {
        return "changelog";
    }
}

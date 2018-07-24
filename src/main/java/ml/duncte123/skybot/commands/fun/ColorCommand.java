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

package ml.duncte123.skybot.commands.fun;

import me.duncte123.botCommons.web.WebUtils;
import ml.duncte123.skybot.BuildConfig;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.awt.Color.decode;
import static ml.duncte123.skybot.BuildConfig.URL_ARRAY;
import static ml.duncte123.skybot.utils.EmbedUtils.defaultEmbed;
import static ml.duncte123.skybot.utils.MessageUtils.sendEmbed;

public class ColorCommand extends Command {

    @Override
    public void executeCommand(@NotNull String invoke, @NotNull String[] args, @NotNull GuildMessageReceivedEvent event) {
        WebUtils.ins.getJSONObject(URL_ARRAY[3] + "/random").async((json) -> {

            JSONObject color = json.getJSONArray("colors").getJSONObject(0);
            String hex = color.getString("hex");

            EmbedBuilder embed = defaultEmbed()
                    .setColor(decode("#" + hex))
                    .setThumbnail(URL_ARRAY[2] + "/image/" + hex );

            String desc = String.format("Name(s): %s%nHex: #%s", combineTagsToString(color.getJSONArray("tags")), hex);
            embed.setDescription(desc);

            sendEmbed(event, embed.build());
        });
    }

    @Override
    public String help() {
        return "Shows a random colour.";
    }

    @Override
    public String getName() {
        return "colour";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"color"};
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.FUN;
    }

    private String combineTagsToString(JSONArray tags) {
        List<String> t = new ArrayList<>();

        tags.forEach((tag) -> {
            JSONObject jsonTag = (JSONObject) tag;
            t.add(jsonTag.getString("name"));
        });

        return StringUtils.join(t, ", ");
    }
}

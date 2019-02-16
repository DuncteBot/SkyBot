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

package ml.duncte123.skybot.commands.guild.owner.settings;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SetRateLimitsCommand extends SettingsBase {
    @Override
    public void run(@NotNull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();

        if (args.isEmpty()) {
            sendMsg(event, "Incorrect usage: `" + Settings.PREFIX + "setratelimits <1|2|3|4|5|6/default>`");
            return;
        }

        if ("default".equals(args.get(0))) {
            sendMsg(event, "Ratelimits have beed reset.");
            guild.setSettings(settings.setRatelimits(new long[]{20, 45, 60, 120, 240, 2400}));
            return;
        }

        final long[] rates = GuildSettingsUtils.ratelimmitChecks(args.get(0));

        if (rates.length < 6 || rates.length > 6) {
            sendMsg(event, "Invalid rate limit settings");
            return;
        }

        guild.setSettings(settings.setRatelimits(rates));
        final String steps = Arrays.stream(rates).mapToObj(String::valueOf)
            .collect(Collectors.joining(", ", "", " minutes"));

        sendMsg(event, "The new rates are " + steps);
    }

    @Override
    public String getName() {
        return "setratelimits";
    }

    @Override
    public String help() {
        return "Sets our cooldown in minutes for un-muting your spammer of choice.\n" +
            "Usage: `" + Settings.PREFIX + getName() + " <1|2|3|4|5|6>`\n" +
            "Example: " + Settings.PREFIX + getName() + "20|45|60|120|240|2400";
    }
}

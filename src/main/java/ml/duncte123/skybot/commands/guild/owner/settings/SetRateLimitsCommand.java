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
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class SetRateLimitsCommand extends SettingsBase {

    public SetRateLimitsCommand() {
        this.name = "setratelimits";
        this.helpFunction = (prefix, invoke) -> "Sets our cooldown in minutes for un-muting your spammer of choice.\n" +
            "Example: `" + prefix + invoke + " 20|45|60|120|240|2400`";
        this.usageInstructions = (prefix, invoke) -> '`' + prefix + invoke + " <1|2|3|4|5|6/default>`";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final GuildMessageReceivedEvent event = ctx.getEvent();
        final List<String> args = ctx.getArgs();
        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();

        if (args.isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }

        if ("default".equals(args.get(0))) {
            sendMsg(event, "Ratelimits have beed reset.");
            guild.setSettings(settings.setRatelimits(new long[]{20, 45, 60, 120, 240, 2400}));
            return;
        }

        final long[] rates = GuildSettingsUtils.ratelimmitChecks(String.join("", args));

        if (rates.length != 6) {
            sendMsg(event, "Invalid rate limit settings");
            return;
        }

        guild.setSettings(settings.setRatelimits(rates));
        final String steps = Arrays.stream(rates).mapToObj(String::valueOf)
            .collect(Collectors.joining(", ", "", " minutes"));

        sendMsg(event, "The new rates are " + steps);
    }
}

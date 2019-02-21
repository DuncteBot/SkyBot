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

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongObjectMap;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.bot.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsgFormat;

public class ClearLeftGuildsCommand extends Command {
    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        if (!isDev(ctx.getAuthor())) {
            return;
        }

        final TLongObjectMap<GuildSettings> storedSettings = ctx.getVariables().getGuildSettings();
        final TLongList guildsToRemove = new TLongArrayList();
        final ShardManager manager = ctx.getShardManager();

        for (final long guildId : storedSettings.keys()) {

            if (manager.getGuildById(guildId) == null) {
                guildsToRemove.add(guildId);
            }

        }

        guildsToRemove.forEach((item) -> {
            storedSettings.remove(item);

            return true;
        });

        final List<String> args = ctx.getArgs();

        if (args.isEmpty() || !args.get(0).equalsIgnoreCase("--clear-db")) {
            sendMsgFormat(
                ctx.getEvent(),
                "Removed %s guilds from the cache.%nRun with `--clear-db` to also remove these eateries from the " +
                    "database (requires cache to be reloaded)",
                guildsToRemove.size()
            );

            return;
        }

        final DatabaseAdapter adapter = ctx.getDatabaseAdapter();

        guildsToRemove.forEach((item) -> {
            adapter.deleteGuildSetting(item);

            return true;
        });

        sendMsgFormat(
            ctx.getEvent(),
            "Removed %s guilds from the cache and database.",
            guildsToRemove.size()
        );
    }

    @Override
    public String getName() {
        return "clearleftguilds";
    }

    @Override
    public String help() {
        return "Clears the guilds that we have left from the cache and/or database\n" +
            "Usage: `" + Settings.PREFIX + getName() + " [--clear-db]`";
    }
}

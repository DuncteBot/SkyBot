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

package ml.duncte123.skybot;

import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TempListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TempListener.class);
    private short shardsReady = 0;

    @Override
    public void onReady(ReadyEvent event) {

        shardsReady++;
        final ShardManager manager = event.getJDA().asBot().getShardManager();
        if (shardsReady == manager.getShardsTotal()) {

            var variables = Variables.getInstance();
            var guildSettings = variables.getGuildSettings();
            List<Long> toRemove = new ArrayList<>();

            logger.info("Size pre removal {}", guildSettings.size());

            for (GuildSettings setting : guildSettings.values(new GuildSettings[0])) {

                Guild guild = manager.getGuildById(setting.getGuildId());

                if (guild == null) {
                    toRemove.add(setting.getGuildId());
                }

            }

            toRemove.forEach(guildSettings::remove);

            logger.info("Size post removal {}", guildSettings.size());

        }

    }
}

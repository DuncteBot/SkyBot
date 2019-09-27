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

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.CommandUtils.isDev;

public class FixVCCommand extends Command {

    public FixVCCommand() {
        this.category = CommandCategory.UNLISTED;
        this.name = "fixvcs";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if (isDev(ctx.getAuthor())) {
            sendMsg(ctx, "Looping over cache to fix channels");
            final AtomicInteger counter = new AtomicInteger();
            final ShardManager shardManager = ctx.getShardManager();
            final Map<Integer, Long> brokenVcs = new HashMap<>();

            shardManager.getShardCache().forEach((shard) ->
                shard.getVoiceChannelCache().forEachUnordered((vc) -> {

                    try {
                        vc.getGuild();
                    } catch (IllegalStateException ignored) {
                        counter.incrementAndGet();
                        brokenVcs.put(shard.getShardInfo().getShardId(), vc.getIdLong());
                    }

                })
            );

            brokenVcs.forEach((shardId, vcId) -> {
                final JDA shard = shardManager.getShardById(shardId);
                assert shard != null;

                final var cache = (SnowflakeCacheViewImpl<VoiceChannel>) shard.getVoiceChannelCache();

                if (AirUtils.getSelfMemberFromVCId(shard, vcId) == null) {
                    sendMsg(ctx, "Removed " + vcId + " from cache");
                    cache.remove(vcId);
                }
            });

            sendMsg(ctx, "Looped `" + counter.get() + "` voice channels, check cache maybe?");
        }
    }
}

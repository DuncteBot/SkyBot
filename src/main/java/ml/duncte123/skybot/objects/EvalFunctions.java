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

package ml.duncte123.skybot.objects;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.messaging.MessageConfig;
import me.duncte123.botcommons.messaging.MessageUtils;
import ml.duncte123.skybot.Variables;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.ShardManager;

@SuppressWarnings("unused")
public final class EvalFunctions {
    private EvalFunctions() {}

    public static RestAction<Message> stats(ShardManager shardManager, MessageChannel channel) {
        final MessageEmbed embed = EmbedUtils.getDefaultEmbed()
            .addField("Guilds", String.valueOf(shardManager.getGuildCache().size()), true)
            .addField("Users", String.valueOf(shardManager.getUserCache().size()), true)
            .addField("Channels", String.valueOf(shardManager.getTextChannelCache().size() + shardManager.getPrivateChannelCache().size()), true)
            .addField("Socket-Ping", String.valueOf(shardManager.getAverageGatewayPing()), false)
            .build();
        return channel.sendMessage(embed);
    }

    public static String getSharedGuilds(CommandContext ctx) {
        return getSharedGuilds(ctx.getJDA(), ctx.getMember());
    }

    public static String getSharedGuilds(JDA jda, Member member) {
        final ShardManager shardManager = jda.getShardManager();
        final StringBuilder out = new StringBuilder();

        shardManager.getMutualGuilds(member.getUser()).forEach((it) -> {
            out.append("[Shard: ${it.jda.shardInfo.shardId}]: ").append(it).append('\n');
        });

        return out.toString();
    }

    public static void pinnedMessageCheck(TextChannel channel) {
        channel.retrievePinnedMessages().queue((it) ->
            MessageUtils.sendMsg(
                new MessageConfig.Builder()
                    .setChannel(channel)
                    .setMessage(it.size() + "/50 messages pinned in this channel")
                    .build()
            )
        );
    }

    public static String restoreCustomCommand(int commandId, Variables variables) {
        final boolean bool = variables.getApis().restoreCustomCommand(commandId, variables);

        if (bool) {
            return "Command Restored";
        }

        return "Could not restore command";
    }
}

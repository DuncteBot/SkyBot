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

import kotlin.Pair;
import me.duncte123.botcommons.messaging.MessageUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.ShardCacheView;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class ShardInfoCommand extends Command {

    public ShardInfoCommand() {
        this.category = CommandCategory.UTILS;
        this.name = "shardinfo";
        this.aliases = new String[]{
            "shards",
        };
        this.helpFunction = (invoke, prefix) -> "Get information about all things shards";
        this.flags = new Flag[]{
            new Flag(
                'm',
                "mobile",
                "Shows a mobile friendly embed instead"
            ),
        };
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final var flags = ctx.getParsedFlags(this);

        if (!args.isEmpty() && flags.containsKey("m")) {
            embedTable(ctx);
            return;
        }

        asciiInfo(ctx);
    }

    private void embedTable(CommandContext ctx) {
        final EmbedBuilder embedBuilder = new EmbedBuilder();
        final int currentShard = ctx.getJDA().getShardInfo().getShardId();
        final ShardManager shardManager = ctx.getJDA().getShardManager();
        final List<JDA> shards = new ArrayList<>(shardManager.getShards());
        Collections.reverse(shards);

        for (final JDA shard : shards) {
            final StringBuilder valueBuilder = new StringBuilder();
            final Pair<Long, Long> channelStats = getConnectedVoiceChannels(shard);

            valueBuilder.append("**Status:** ").append(getShardStatus(shard)).append('\n')
                .append("**Ping:** ").append(shard.getGatewayPing()).append('\n')
                .append("**Guilds:** ").append(shard.getGuildCache().size()).append('\n')
                .append("**VCs:** ").append(channelStats.getFirst()).append(" / ").append(channelStats.getSecond());

            final int shardId = shard.getShardInfo().getShardId();

            embedBuilder.addField(String.format("Shard #%s%s", shardId, (shardId == currentShard ? " (current)" : "")),
                valueBuilder.toString(), true);
        }

        sendEmbed(ctx, embedBuilder);
    }

    private void asciiInfo(CommandContext ctx) {
        final List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("Status");
        headers.add("Ping");
        headers.add("Guilds");
        headers.add("VCs");

        final GuildMessageReceivedEvent event = ctx.getEvent();

        List<List<String>> table = new ArrayList<>();
        final int currentShard = ctx.getJDA().getShardInfo().getShardId();
        final ShardManager shardManager = ctx.getJDA().getShardManager();
        final List<JDA> shards = new ArrayList<>(shardManager.getShards());
        Collections.reverse(shards);

        for (final JDA shard : shards) {
            final List<String> row = new ArrayList<>();
            final int shardId = shard.getShardInfo().getShardId();

            row.add(shardId + (currentShard == shardId ? " (current)" : ""));
            row.add(getShardStatus(shard));
            row.add(String.valueOf(shard.getGatewayPing()));
            row.add(String.valueOf(shard.getGuildCache().size()));

            final Pair<Long, Long> channelStats = getConnectedVoiceChannels(shard);

            row.add(channelStats.getFirst() + " / " + channelStats.getSecond());
            table.add(row);

            if (table.size() == 20) {
                MessageUtils.sendMsg(event, makeAsciiTable(headers, table, shardManager));
                table = new ArrayList<>();
            }
        }

        if (!table.isEmpty()) {
            MessageUtils.sendMsg(event, makeAsciiTable(headers, table, shardManager));
        }
    }

    private String getShardStatus(JDA shard) {
        return WordUtils.capitalizeFully(shard.getStatus().toString().replace('_', ' '));
    }

    /*
     * These 2 functions have been inspired from FlareBot
     * https://github.com/FlareBot/FlareBot/blob/master/src/main/java/stream/flarebot/flarebot/util/ShardUtils.java
     */
    private String makeAsciiTable(List<String> headers, List<List<String>> table, ShardManager shardManager) {
        final StringBuilder sb = new StringBuilder();
        final int padding = 1;
        final int[] widths = new int[headers.size()];

        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).length() > widths[i]) {
                widths[i] = headers.get(i).length();
            }
        }

        for (final List<String> row : table) {
            for (int i = 0; i < row.size(); i++) {
                final String cell = row.get(i);
                if (cell.length() > widths[i]) {
                    widths[i] = cell.length();
                }
            }
        }

        final Pair<Long, Long> channelStats = getConnectedVoiceChannels(shardManager);
        final String statsString = channelStats.getFirst() + " / " + channelStats.getSecond();

        if (statsString.length() > widths[widths.length - 1]) {
            widths[widths.length - 1] = statsString.length();
        }

        if (widths[0] < 7) {
            widths[0] = 7;
        }

        sb.append("```").append("prolog").append("\n");
        final StringBuilder formatLine = new StringBuilder("║");
        for (final int width : widths) {
            formatLine.append(" %-").append(width).append("s ║");
        }
        formatLine.append("\n");
        sb.append(appendSeparatorLine("╔", "╦", "╗", padding, widths));

        sb.append(String.format(formatLine.toString(), headers.toArray()));
        sb.append(appendSeparatorLine("╠", "╬", "╣", padding, widths));
        for (final List<String> row : table) {
            sb.append(String.format(formatLine.toString(), row.toArray()));
        }
        sb.append(appendSeparatorLine("╠", "╬", "╣", padding, widths));

        final ShardCacheView shardCache = shardManager.getShardCache();

        final String connectedShards = String.valueOf(shardCache.stream().filter(shard -> shard.getStatus() == JDA.Status.CONNECTED).count());
        final String avgPing = new DecimalFormat("###").format(shardManager.getAverageGatewayPing());
        final String guilds = String.valueOf(shardManager.getGuildCache().size());

        sb.append(String.format(
            formatLine.toString(),
            "Sum/Avg",
            connectedShards,
            avgPing,
            guilds,
            statsString
        ));
        sb.append(appendSeparatorLine("╚", "╩", "╝", padding, widths));
        sb.append("```");
        return sb.toString();
    }

    private String appendSeparatorLine(String left, String middle, String right, int padding, int... sizes) {
        boolean first = true;
        final StringBuilder ret = new StringBuilder();
        for (final int size : sizes) {
            if (first) {
                first = false;
                ret.append(left).append(StringUtils.repeat("═", size + padding * 2));
            } else {
                ret.append(middle).append(StringUtils.repeat("═", size + padding * 2));
            }
        }
        return ret.append(right).append("\n").toString();
    }

    private Pair<Long, Long> getConnectedVoiceChannels(ShardManager shardManager) {
        final AtomicLong connectedVC = new AtomicLong();
        final AtomicLong listeningVC = new AtomicLong();

        shardManager.getShardCache().forEach(
            (jda) -> {
                final Pair<Long, Long> shardStats = getConnectedVoiceChannels(jda);

                connectedVC.addAndGet(shardStats.getFirst());
                listeningVC.addAndGet(shardStats.getSecond());
            }
        );

        return new Pair<>(connectedVC.get(), listeningVC.get());
    }

    /**
     * @param shard
     *     the current shard
     *
     * @return a pair where
     * first  = connected channels
     * second = users listening in channel
     */
    private Pair<Long, Long> getConnectedVoiceChannels(JDA shard) {

        final long connectedVC = shard.getVoiceChannelCache().stream()
            .filter((vc) -> vc.getMembers().contains(getSelfMemberFromVCId(shard, vc.getIdLong()))).count();

        final long listeningVC = shard.getVoiceChannelCache().stream().filter(
            (voiceChannel) -> voiceChannel.getMembers().contains(getSelfMemberFromVCId(shard, voiceChannel.getIdLong())))
            .mapToLong(
                (channel) -> channel.getMembers().stream().filter(
                    (member) -> !member.getUser().isBot() && !member.getVoiceState().isDeafened()
                ).count()
            ).sum();

        return new Pair<>(connectedVC, listeningVC);
    }

    private Member getSelfMemberFromVCId(JDA jda, long voiceChannelId) {
        final VoiceChannel channel = jda.getVoiceChannelById(voiceChannelId);

        if (channel == null) {
            return null;
        }

        return channel.getGuild().getSelfMember();
    }
}

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

package ml.duncte123.skybot.commands.essentials;

import me.duncte123.botcommons.messaging.MessageUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.command.Flag;
import ml.duncte123.skybot.objects.pairs.LongLongPair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.ShardCacheView;
import net.dv8tion.jda.internal.utils.Checks;

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
@SuppressWarnings("ConstantConditions")
public class ShardInfoCommand extends Command {

    public ShardInfoCommand() {
        this.category = CommandCategory.UTILS;
        this.name = "shardinfo";
        this.aliases = new String[]{
            "shards",
        };
        this.helpFunction = (prefix, invoke) -> "Get information about all things shards";
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
        final ShardCacheView shardCache = shardManager.getShardCache();
        final List<JDA> shards = new ArrayList<>(shardCache.asList());
        Collections.reverse(shards);

        for (final JDA shard : shards) {
            final StringBuilder valueBuilder = new StringBuilder();
            final LongLongPair channelStats = getConnectedVoiceChannels(shard);

            valueBuilder.append("**Status:** ").append(getShardStatus(shard)).append('\n')
                .append("**Ping:** ").append(shard.getGatewayPing()).append('\n')
                .append("**Guilds:** ").append(shard.getGuildCache().size()).append('\n')
                .append("**VCs:** ").append(channelStats.getFirst()).append(" / ").append(channelStats.getSecond());

            final int shardId = shard.getShardInfo().getShardId();

            embedBuilder.addField(String.format("Shard #%s%s", shardId, (shardId == currentShard ? " (current)" : "")),
                valueBuilder.toString(), true);
        }

        final long connectedShards = shardCache.applyStream((s) -> s.filter((shard) -> shard.getStatus() == JDA.Status.CONNECTED).count());
        final String avgPing = new DecimalFormat("###").format(shardManager.getAverageGatewayPing());
        final long guilds = shardManager.getGuildCache().size();
        final LongLongPair channelStats = getConnectedVoiceChannels(shardManager);

        embedBuilder.addField(
            "Total/Average",
            String.format("**Connected:** %s\n**Ping:** %s\n**Guilds:** %s\n**VCs:** %s / %s",
                connectedShards,
                avgPing,
                guilds,
                channelStats.getFirst(),
                channelStats.getSecond()
            ),
            false
        );

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

            final LongLongPair channelStats = getConnectedVoiceChannels(shard);

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
        return capitalizeFully(shard.getStatus().toString().replace('_', ' '));
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

        final LongLongPair channelStats = getConnectedVoiceChannels(shardManager);
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

        final long l = shardCache.applyStream((s) -> s.filter(shard -> shard.getStatus() == JDA.Status.CONNECTED).count());
        final String connectedShards = String.valueOf(l);
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

    @SuppressWarnings("SameParameterValue")
    private String appendSeparatorLine(String left, String middle, String right, int padding, int... sizes) {
        boolean first = true;
        final StringBuilder ret = new StringBuilder();
        for (final int size : sizes) {
            if (first) {
                first = false;
//                "═".repeat(size + padding * 2)
                ret.append(left).append("═".repeat(size + padding * 2));
//                ret.append(left).append(StringUtils.repeat("═", size + padding * 2));
            } else {
                ret.append(middle).append("═".repeat(size + padding * 2));
//                ret.append(middle).append(StringUtils.repeat("═", size + padding * 2));
            }
        }
        return ret.append(right).append("\n").toString();
    }

    private LongLongPair getConnectedVoiceChannels(ShardManager shardManager) {
        final AtomicLong connectedVC = new AtomicLong();
        final AtomicLong listeningVC = new AtomicLong();

        shardManager.getShardCache().forEach(
            (jda) -> {
                final LongLongPair shardStats = getConnectedVoiceChannels(jda);

                connectedVC.addAndGet(shardStats.getFirst());
                listeningVC.addAndGet(shardStats.getSecond());
            }
        );

        return new LongLongPair(connectedVC.get(), listeningVC.get());
    }

    private LongLongPair getConnectedVoiceChannels(JDA shard) {

        final long connectedVC = shard.getVoiceChannelCache().applyStream(
            (s) -> s.filter((vc) -> vc.getMembers().contains(vc.getGuild().getSelfMember())).count()
        );

        final long listeningVC = shard.getVoiceChannelCache().applyStream(
            (s) -> s.filter(
                (voiceChannel) -> voiceChannel.getMembers().contains(voiceChannel.getGuild().getSelfMember()))
                .mapToLong(
                    (channel) -> channel.getMembers().stream().filter(
                        (member) -> !member.getUser().isBot() && !member.getVoiceState().isDeafened()
                    ).count()
                ).sum()
        );

        return new LongLongPair(connectedVC, listeningVC);
    }

    private String capitalizeFully(String str) {
        Checks.notBlank(str, "str");

        final String[] words = str.toLowerCase().split("\\s+");
        final StringBuilder builder = new StringBuilder();

        for (final String word : words) {
            builder.append(Character.toUpperCase(word.charAt(0)))
                .append(word.substring(1));
        }

        return builder.toString();
    }
}

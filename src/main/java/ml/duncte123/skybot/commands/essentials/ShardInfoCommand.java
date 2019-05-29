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
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.bot.utils.cache.ShardCacheView;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class ShardInfoCommand extends Command {

    public ShardInfoCommand() {
        this.category = CommandCategory.NERD_STUFF;
    }

    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {
        final List<String> headers = new ArrayList<>();
        headers.add("ID");
        headers.add("Status");
        headers.add("Ping");
        headers.add("Guilds");
        headers.add("VCs");

        final GuildMessageReceivedEvent event = ctx.getEvent();

        List<List<String>> table = new ArrayList<>();
        final ShardManager shardManager = ctx.getJDA().asBot().getShardManager();
        final List<JDA> shards = new ArrayList<>(shardManager.getShards());
        Collections.reverse(shards);

        for (final JDA shard : shards) {
            final List<String> row = new ArrayList<>();

            row.add(shard.getShardInfo().getShardId() +
                (ctx.getJDA().getShardInfo().getShardId() == shard.getShardInfo().getShardId() ? " (current)" : ""));

            row.add(WordUtils.capitalizeFully(shard.getStatus().toString().replace("_", " ")));
            row.add(String.valueOf(shard.getPing()));
            row.add(String.valueOf(shard.getGuilds().size()));

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

    @Override
    public String help(String prefix) {
        return "Get information about all things shards";
    }

    @Override
    public String getName() {
        return "shardinfo";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"shards"};
    }

    /*
     * These 2 functions have been inspired from FlareBot
     * https://github.com/FlareBot/FlareBot/blob/master/src/main/java/stream/flarebot/flarebot/util/ShardUtils.java
     */
    private String makeAsciiTable(List<String> headers, List<List<String>> table, ShardManager shardManager) {
        final StringBuilder sb = new StringBuilder();
        final int padding = 1;
        final int[] widths = new int[headers.size()];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = 0;
        }
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
        final String avgPing = new DecimalFormat("###").format(shardManager.getAveragePing());
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
     *         the current shard
     *
     * @return a pair where
     * first  = connected channels
     * second = users listening in channel
     */
    private Pair<Long, Long> getConnectedVoiceChannels(JDA shard) {

        final long connectedVC = shard.getVoiceChannelCache().stream()
            .filter((vc) -> vc.getMembers().contains(vc.getGuild().getSelfMember())).count();

        final long listeningVC = shard.getVoiceChannelCache().stream().filter(
            (voiceChannel) -> voiceChannel.getMembers().contains(voiceChannel.getGuild().getSelfMember()))
            .mapToLong(
                (channel) -> channel.getMembers().stream().filter(
                    (member) -> !member.getUser().isBot() && !member.getVoiceState().isDeafened()
                ).count()
            ).sum();

        return new Pair<>(connectedVC, listeningVC);
    }
}

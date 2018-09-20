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

package ml.duncte123.skybot.commands.essentials;

import me.duncte123.botCommons.messaging.MessageUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Authors(authors = {
        @Author(nickname = "Sanduhr32", author = "Maurice R S"),
        @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class ShardInfoCommand extends Command {

    public ShardInfoCommand() {
        this.category = CommandCategory.NERD_STUFF;
    }

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {
        List<String> headers = new ArrayList<>();
        headers.add("Shard ID");
        headers.add("Status");
        headers.add("Ping");
        headers.add("Guild Count");
        headers.add("Connected VCs");

        GuildMessageReceivedEvent event = ctx.getEvent();

        List<List<String>> table = new ArrayList<>();
        ShardManager shardManager = ctx.getJDA().asBot().getShardManager();
        List<JDA> shards = new ArrayList<>(shardManager.getShards());
        Collections.reverse(shards);
        for (JDA shard : shards) {
            List<String> row = new ArrayList<>();
            row.add(shard.getShardInfo().getShardId() +
                    (ctx.getJDA().getShardInfo().getShardId() == shard.getShardInfo().getShardId() ? " (current)" : ""));
            row.add(WordUtils.capitalizeFully(shard.getStatus().toString().replace("_", " ")));
            row.add(String.valueOf(shard.getPing()));
            row.add(String.valueOf(shard.getGuilds().size()));

            String listening = shard.getVoiceChannelCache().stream().filter(vc -> vc.getMembers().contains(vc.getGuild()
                    .getSelfMember())).count() + " / " +
                    shard.getVoiceChannelCache().stream().filter(vc ->
                            vc.getMembers().contains(vc.getGuild().getSelfMember()))
                            .mapToLong(it ->
                                    it.getMembers().stream().filter(itt ->
                                            !itt.getUser().isBot() && !itt.getVoiceState().isDeafened()).count()
                            ).sum();

            row.add(listening);
            table.add(row);
            if (table.size() == 20) {
                MessageUtils.sendMsg(event, makeAsciiTable(headers, table, shardManager));
                table = new ArrayList<>();
            }
        }
        if (table.size() > 0) {
            MessageUtils.sendMsg(event, makeAsciiTable(headers, table, shardManager));
        }
    }

    @Override
    public String help() {
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
        StringBuilder sb = new StringBuilder();
        int padding = 1;
        int[] widths = new int[headers.size()];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = 0;
        }
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).length() > widths[i]) {
                widths[i] = headers.get(i).length();
            }
        }
        for (List<String> row : table) {
            for (int i = 0; i < row.size(); i++) {
                String cell = row.get(i);
                if (cell.length() > widths[i]) {
                    widths[i] = cell.length();
                }
            }
        }
        sb.append("```").append("prolog").append("\n");
        StringBuilder formatLine = new StringBuilder("║");
        for (int width : widths) {
            formatLine.append(" %-").append(width).append("s ║");
        }
        formatLine.append("\n");
        sb.append(appendSeparatorLine("╔", "╦", "╗", padding, widths));
        sb.append(String.format(formatLine.toString(), headers.toArray()));
        sb.append(appendSeparatorLine("╠", "╬", "╣", padding, widths));
        for (List<String> row : table) {
            sb.append(String.format(formatLine.toString(), row.toArray()));
        }
        sb.append(appendSeparatorLine("╠", "╬", "╣", padding, widths));
        String connectedShards = String.valueOf(shardManager.getShards().stream().filter(shard -> shard.getStatus() == JDA.Status.CONNECTED).count());
        String avgPing = new DecimalFormat("###").format(shardManager.getAveragePing());
        String guilds = String.valueOf(shardManager.getGuildCache().size());
        long connectedVC = shardManager.getShardCache().stream().mapToLong(shard ->
                shard.getVoiceChannelCache().stream().filter(vc -> vc.getMembers().contains(vc.getGuild().getSelfMember())).count()
        ).sum();
        long listeningVC = shardManager.getShardCache().stream().mapToLong(shard ->
                shard.getVoiceChannelCache().stream().filter(vc ->
                        vc.getMembers().contains(vc.getGuild().getSelfMember()))
                        .mapToLong(it ->
                                it.getMembers().stream().filter(itt ->
                                        !itt.getUser().isBot() && !itt.getVoiceState().isDeafened()).count()
                        ).sum()
        ).sum();
        sb.append(String.format(formatLine.toString(), "Sum/Avg", connectedShards, avgPing, guilds, connectedVC + " / " + listeningVC));
        sb.append(appendSeparatorLine("╚", "╩", "╝", padding, widths));
        sb.append("```");
        return sb.toString();
    }

    private String appendSeparatorLine(String left, String middle, String right, int padding, int... sizes) {
        boolean first = true;
        StringBuilder ret = new StringBuilder();
        for (int size : sizes) {
            if (first) {
                first = false;
                ret.append(left).append(StringUtils.repeat("═", size + padding * 2));
            } else {
                ret.append(middle).append(StringUtils.repeat("═", size + padding * 2));
            }
        }
        return ret.append(right).append("\n").toString();
    }
}

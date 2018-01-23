/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
import net.dv8tion.jda.core.JDA
import org.apache.commons.lang3.StringUtils

def quick_mafs(int x) {
   def the_thing = x + 2 -1 
   return the_thing
}

//channel.sendFile(new URL("https://pbs.twimg.com/profile_images/892463026003222529/so6nfXWX.jpg").openStream(), "filename.png", new MessageBuilder()
//.setEmbed(EmbedUtils.defaultEmbed().setImage("attachment://filename.png").build()).build()).queue()

/*channel.sendMessage("This has an embed with an image!")
             .addFile(new URL("https://pbs.twimg.com/profile_images/892463026003222529/so6nfXWX.jpg").openStream(), "alpaca.png")
             .embed(EmbedUtils.embedImage("attachment://alpaca.png"))
             .queue()

return quick_mafs(2) + "\nThe thing goes skrra"*/
import org.apache.commons.lang3.text.WordUtils

List<String> headers = new ArrayList<>();
headers.add("Shard ID");
headers.add("Status");
headers.add("Ping");
headers.add("Guild Count");
headers.add("Connected VCs");

List<List<String>> table = new ArrayList<>();
List<JDA> shards = event.getJDA().asBot().getShardManager().getShards();
Collections.reverse(shards);
for (JDA jda : shards) {
    List<String> row = new ArrayList<>();
    row.add( (jda.getShardInfo().getShardId() + 1 ) +
            (event.getJDA().getShardInfo().getShardId() == jda.getShardInfo().getShardId() ? " (current)" : ""));
    row.add(WordUtils.capitalizeFully(jda.getStatus().toString().replace("_", " ")));
    row.add(String.valueOf(jda.getPing()));
    row.add(String.valueOf(jda.getGuilds().size()));
    row.add(String.valueOf(jda.getVoiceChannels().stream().filter({
        it.getMembers().contains(it.getGuild()
                .getSelfMember())
    }).count()));
    table.add(row);
    if (table.size() == 20) {
        channel.sendMessage( makeAsciiTable(headers, table)).queue()
        table = new ArrayList<>();
    }
}
if (table.size() > 0) {
    channel.sendMessage(  makeAsciiTable(headers, table)).queue()
}

private String makeAsciiTable(List<String> headers, List<List<String>> table) {
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

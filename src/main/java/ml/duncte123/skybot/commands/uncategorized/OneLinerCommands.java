/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken
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

package ml.duncte123.skybot.commands.uncategorized;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.lang.management.ManagementFactory;

/**
 * This class contains a bunch of commands that require little code to run, so we combine in this class
 */
public class OneLinerCommands extends Command {
    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {

        switch (invoke) {
            case "ping" :
                long time = System.currentTimeMillis();

                event.getChannel().sendMessage("PONG!").queue( (message) ->
                        message.editMessageFormat("PONG!" +
                                        "\nping is: %dms " +
                                        "\nWebsocket ping: %sms\n" +
                                        "Average ping: %sms",
                                (System.currentTimeMillis() - time),
                                event.getJDA().getPing(),
                                event.getJDA().asBot().getShardManager().getAveragePing() ).queue());
                break;

            case "cookie":
                sendMsg(event, "<:blobnomcookie_secret:317636549342789632>");
                break;

            case "trigger":
                sendEmbed(event, EmbedUtils.embedImage("https://cdn.discordapp.com/attachments/94831883505905664/176181155467493377/triggered.gif"));
                break;

            case "wam":
                sendEmbed(event, EmbedUtils.embedField("GET YOUR WAM NOW!!!!", "[http://downloadmorewam.com/](http://downloadmorewam.com/)"));
                break;

            case "mineh":
                event.getChannel().sendMessage(new MessageBuilder().setTTS(true).append("Insert creepy music here").build()).queue(
                        m -> sendEmbed(event, EmbedUtils.embedImage("https://cdn.discordapp.com/attachments/204540634478936064/213983832087592960/20160813133415_1.jpg"))
                );
                break;
            case "invite":
                sendMsg(event, "Invite me with this link:\n" +
                        "<https://discordapp.com/oauth2/authorize?client_id=210363111729790977&scope=bot&permissions=8>");
                break;
            case "uptime":
                sendMsg(event, AirUtils.getUptime(ManagementFactory.getRuntimeMXBean().getUptime(), true));
                break;
        }

    }

    @Override
    public String help() {
        return "`"+this.PREFIX+"ping` => Shows the delay from the bot to the discord servers.\n" +
                "`"+this.PREFIX+"cookie` => blobnomcookie.\n" +
                "`"+this.PREFIX+"trigger` => use when you are triggered.\n" +
                "`"+this.PREFIX+"wam` => you need more WAM!.\n" +
                "`"+this.PREFIX+"mineh` => HERE COMES MINEH!\n" +
                "`"+this.PREFIX+"invite` => gives you the bot invite\n" +
                "`"+this.PREFIX+"uptime` => shows the bot uptime";
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"cookie", "trigger", "wam", "mineh", "invite", "uptime"};
    }
}

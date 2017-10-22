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

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import com.sun.management.OperatingSystemMXBean;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.Settings;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.lang.management.ManagementFactory;
import java.sql.Time;
import java.text.DecimalFormat;

/**
 * Created by Duncan on 11-7-2017.
 */
public class BotinfoCommand extends Command {

    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        User u = event.getJDA().getSelfUser();

        String OS = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getName();
        OS = OS + " " + ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getArch() + " " + ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getVersion();
        String cpu0 = new DecimalFormat("###.###%").format(ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getProcessCpuLoad());
        String cpu2 = new DecimalFormat("###.###%").format(ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getSystemCpuLoad());
        int cpu1 = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        long ram0 = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / 1000000;
        long ram1 = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() / 1000000;
        long uptimeLong = ManagementFactory.getRuntimeMXBean().getUptime();
        Time uptimeTime = new Time(uptimeLong - 3600000);



        MessageEmbed eb = EmbedUtils.defaultEmbed()
                .setDescription("Here is some information about me \uD83D\uDE09")
                .setThumbnail(u.getEffectiveAvatarUrl())
                .addField("General info", "**Creator:** duncte123#1245\n" +
                        "**Invite:** [You can invite me by clicking here](https://bots.discord.pw/bots/210363111729790977)\n" +
                        "**Github:** [https://github.com/duncte123/SkyBot](https://github.com/duncte123/SkyBot)\n" +
                        "**Guilds:** " + event.getJDA().asBot().getShardManager().getGuildCache().size() + "\n" +
                        "**Bot version:** " + Settings.version, true)
                .addField("System info", "**Operating System:** " + OS + "\n" +
                        "**Uptime:** " + AirUtils.getUptime(uptimeLong) + " " + uptimeTime + "\n" +
                        "**Ram:** "  + ram0 +"MB/" + ram1 + "MB\n" +
                        "**CPU Usage:** " + cpu0 + " / " + cpu2 + " (" + cpu1 +" Cores)" , false)
                .addField("Lib info", "JDA version: " + JDAInfo.VERSION + "\nLavaPlayer version: " + PlayerLibrary.VERSION, false)
                .addField("Donate", "If you want to help me out and support the bot please consider to [donate](https://paypal.me/duncte123) any amount.", false)
                .build();
        sendEmbed(event, eb);
    }

    @Override
    public String help() {
        return "Gets some info about the bot\nUsage: `"+this.PREFIX+getName()+"`";
    }

    @Override
    public String getName() {
        return "botinfo";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"about"};
    }

}

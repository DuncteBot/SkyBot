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

package ml.duncte123.skybot.commands.uncategorized;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import com.sun.management.OperatingSystemMXBean;
import me.duncte123.weebJava.models.WeebApi;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.EmbedUtils;
import ml.duncte123.skybot.utils.MessageUtils;
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

        if ("support".equals(invoke)) {
            MessageUtils.sendMsg(event, "You can join my support guild here: <https://discord.gg/NKM9Xtk>");
            return;
        }

        User u = event.getJDA().getSelfUser();

        String OS = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getName() +
                " " + ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getArch() +
                " " + ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getVersion();
        String cpu0 = new DecimalFormat("###.###%").format(ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getProcessCpuLoad());
        String cpu2 = new DecimalFormat("###.###%").format(ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getSystemCpuLoad());
        int cpu1 = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        long ram0 = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() >> 20;
        long ram1 = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() >> 20;
        long uptimeLong = ManagementFactory.getRuntimeMXBean().getUptime();
        Time uptimeTime = new Time(uptimeLong - 3600000);


        MessageEmbed eb = EmbedUtils.defaultEmbed()
                .setDescription("Here is some information about me \uD83D\uDE09")
                .setThumbnail(u.getEffectiveAvatarUrl())
                .addField("About me", "Hello there, my name is DuncteBot and I’m currently being developed by " +
                        "duncte123 (duncte123#1245), ramidzkh (ramidzkh#4814) and Sanduhr32 (\u231b.exe ¯\\\\_(ツ)\\_/¯#5785).\n" +
                        "If you want to add me to your server you can do that by [clicking here](https://bots.discord.pw/bots/210363111729790977).\n" +
                        "\n[**Support server**](https://discord.gg/NKM9Xtk) \u2022 [**Website**](https://bot.duncte123.me) \u2022 " +
                        "[**Invite me**](https://discordapp.com/oauth2/authorize?client_id=210363111729790977&scope=bot&permissions=-1)" +
                        "\n\u200B", true)
                .addField("Other info", "**Guilds:** " + event.getJDA().asBot().getShardManager().getGuildCache().size() + "\n" +
                        "**Bot version:** " + Settings.VERSION + "\n"
                        + "**Operating System:** " + OS + "\n" +
                        "**Uptime:** " + AirUtils.getUptime(uptimeLong) + " " + uptimeTime + "\n" +
                        "**Ram:** " + ram0 + "MB/" + ram1 + "MB\n" +
                        "**CPU Usage:** " + cpu0 + " / " + cpu2 + " (" + cpu1 + " Cores)\n\u200B", false)
                .addField("Lang & lib info", "**Coded in:** Java (version " + System.getProperty("java.version") + ") and Kotlin\n\n" +
                        "**JDA version:** " + JDAInfo.VERSION + "" +
                        "\n**LavaPlayer version:** " + PlayerLibrary.VERSION + "\n" +
                        "**Weeb.java version:** " + WeebApi.VERSION + "\n\u200B", false)
                .addField("Donate", "If you want to help me out and support the bot please consider to " +
                        "[donate](https://paypal.me/duncte123) any amount.", false)
                .build();
        MessageUtils.sendEmbed(event, eb);
    }

    @Override
    public String help() {
        return "Gets some info about the bot\nUsage: `" + PREFIX + getName() + "`";
    }

    @Override
    public String getName() {
        return "botinfo";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"about", "info", "support"};
    }

}

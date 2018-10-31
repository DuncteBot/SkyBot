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

package ml.duncte123.skybot.commands.uncategorized;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import com.sun.management.OperatingSystemMXBean;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.messaging.MessageUtils;
import me.duncte123.weebJava.WeebInfo;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.sql.Time;
import java.text.DecimalFormat;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

/**
 * Created by Duncan on 11-7-2017.
 */
@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken"),
    @Author(nickname = "ramidzkh", author = "Ramid Khan")
})
public class BotinfoCommand extends Command {

    @Override
    public void executeCommand(@NotNull CommandContext ctx) {

        GuildMessageReceivedEvent event = ctx.getEvent();

        if ("support".equals(ctx.getInvoke())) {
            MessageUtils.sendMsg(event, "You can join my support guild here: <https://discord.gg/NKM9Xtk>");
            return;
        }

        User u = event.getJDA().getSelfUser();

        OperatingSystemMXBean platformMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        String OS = platformMXBean.getName() +
            " " + platformMXBean.getArch() +
            " " + platformMXBean.getVersion();
        String cpu0 = new DecimalFormat("###.###%").format(platformMXBean.getProcessCpuLoad());
        String cpu2 = new DecimalFormat("###.###%").format(platformMXBean.getSystemCpuLoad());
        int cpu1 = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
        long ram0 = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() >> 20;
        long ram1 = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() >> 20;
        long uptimeLong = ManagementFactory.getRuntimeMXBean().getUptime();
        Time uptimeTime = new Time(uptimeLong - 3600000);

        String duncte = getDev(event, 191231307290771456L, "duncte123 (duncte123#1245)");
        String ramid = getDev(event, 281673659834302464L, "ramidzkh (ramidzkh#4814)");

        MessageEmbed eb = EmbedUtils.defaultEmbed()
            .setDescription("Here is some information about me \uD83D\uDE09")
            .setThumbnail(u.getEffectiveAvatarUrl())
            .addField("About me", "Hello there, my name is DuncteBot and I’m currently being developed by " +
                duncte + " and " + ramid + ".\n" +
                "If you want to add me to your server you can do that by [clicking here](https://bots.discord.pw/bots/210363111729790977).\n" +
                "\n**[Support server](https://discord.gg/NKM9Xtk)** \u2022 **[Website](https://bot.duncte123.me)** \u2022 " +
                "**[Invite me](https://discordapp.com/oauth2/authorize?client_id=210363111729790977&scope=bot&permissions=-1)**" +
                "\n\u200B", true)
            .addField("Other info", "**Guilds:** " + ctx.getShardManager().getGuildCache().size() + "\n" +
                "**Bot version:** " + Settings.VERSION + "\n"
                + "**Operating System:** " + OS + "\n" +
                "**Uptime:** " + AirUtils.getUptime(uptimeLong) + " " + uptimeTime + "\n" +
                "**Ram:** " + ram0 + "MB/" + ram1 + "MB\n" +
                "**CPU Usage:** " + cpu0 + " / " + cpu2 + " (" + cpu1 + " Cores)\n\u200B", false)
            .addField("Lang & lib info", "**Coded in:** Java (version " + System.getProperty("java.version") +
                ") and Kotlin (version " + Settings.KOTLIN_VERSION + ")\n\n" +
                "**JDA version:** " + JDAInfo.VERSION + "" +
                "\n**LavaPlayer version:** " + PlayerLibrary.VERSION + "\n" +
                "**Weeb.java version:** " + WeebInfo.VERSION + "\n\u200B", false)
            .addField("Support", "If you want to help keep the bot up 24/7, please consider " +
                "[becoming a patron](https://www.patreon.com/DuncteBot).", false)
            .build();
        sendEmbed(event, eb);
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

    private String getDev(GuildMessageReceivedEvent event, long id, String defaultM) {
        Member m = event.getGuild().getMemberById(id);

        if (m != null) {
            return m.getAsMention();
        }

        return defaultM;
    }

}

/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.commands.uncategorized;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import kotlin.KotlinVersion;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.messaging.MessageUtils;
import me.duncte123.weebJava.WeebInfo;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static ml.duncte123.skybot.Settings.PATREON;

public class BotinfoCommand extends Command {
    public BotinfoCommand() {
        this.name = "botinfo";
        this.aliases = new String[]{
            "about",
            "support",
            "bi",
            "info",
        };
        this.help = "Displays some information about the bot";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if ("support".equals(ctx.getInvoke())) {
            MessageUtils.sendMsg(ctx, "You can join my discord server here: <https://duncte.bot/server>");
            return;
        }

        final User user = ctx.getJDA().getSelfUser();
        final String duncte = " <@191231307290771456> (duncte123#1245)";

        final EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
            .setThumbnail(user.getEffectiveAvatarUrl())
            .addField("About me", "Hello there, my name is DuncteBot and I’m currently being developed by " +
                duncte + ".\n" +
                "If you want to add me to your server you can do that by [clicking here](https://discord.bots.gg/bots/210363111729790977).\n" +
                "\n**[Discord server](https://duncte.bot/server)** \u2022 **[Website](https://dunctebot.com/)** \u2022 " +
                "**[Invite me](https://duncte.bot/invite)**" +
                " \u2022 **[Twitter](https://twitter.com/DuncteBot)**  \u2022 **[Cheap hosting](https://billing.oxide.host/aff.php?aff=6)**" +
                "\n\u200B", true)
            .addField("Lang & lib info", "**Coded in:** Java (version " + System.getProperty("java.version") +
                ") and Kotlin (version " + KotlinVersion.CURRENT + ")\n\n" +
                "**JDA version:** " + JDAInfo.VERSION +
                "\n**LavaPlayer version:** " + PlayerLibrary.VERSION + "\n" +
                "**Weeb.java version:** " + WeebInfo.VERSION + "\n\u200B", false)
            .addField("Support", "If you want to help keep the bot up 24/7, please consider " +
                "[becoming a patron](" + PATREON + ").", false);

        sendEmbed(ctx, builder);
    }
}

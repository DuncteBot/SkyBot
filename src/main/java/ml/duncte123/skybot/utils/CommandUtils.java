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

package ml.duncte123.skybot.utils;

import com.jagrosh.jagtag.JagTag;
import com.jagrosh.jagtag.Parser;
import gnu.trove.map.TLongLongMap;
import gnu.trove.set.TLongSet;
import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Settings;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.jagtag.DiscordMethods;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class CommandUtils {
    public static final TLongSet patrons = MapUtils.newLongSet();
    public static final TLongSet guildPatrons = MapUtils.newLongSet();
    // Key: user_id Value: guild_id
    public static final TLongLongMap oneGuildPatrons = MapUtils.newLongLongMap();
    public static final TLongSet tagPatrons = MapUtils.newLongSet();
    public static final long supportGuildId = 191245668617158656L;
    public static final long guildPatronsRole = 470581447196147733L;
    public static final long patronsRole = 402497345721466892L;
    public static final long oneGuildPatronsRole = 490859976475148298L;
    public static final long tagPatronsRole = 578660495738011658L;

    public static final Supplier<Parser> PARSER_SUPPLIER = () -> JagTag.newDefaultBuilder()
        .addMethods(DiscordMethods.getMethods())
        .build();

    public static String parseJagTag(CommandContext ctx, String content) {
        final Parser parser = PARSER_SUPPLIER.get()
            .put("messageId", ctx.getMessage().getId())
            .put("user", ctx.getAuthor())
            .put("channel", ctx.getChannel())
            .put("guild", ctx.getGuild())
            .put("args", ctx.getArgsJoined());
        final String parsed = parser.parse(content);

        parser.clear();

        return parsed;
    }

    public static boolean isPatron(@Nonnull User u, @Nullable TextChannel tc) {
        if (isDev(u) || patrons.contains(u.getIdLong())) {
            return true;
        }

        final Guild supportGuild = u.getJDA().asBot().getShardManager().getGuildById(supportGuildId);

        if (supportGuild == null) {
            return false;
        }

        final Member m = supportGuild.getMember(u);

        if (m == null) {
            sendEmbed(tc, EmbedUtils.embedMessage("This command is a patron only command and is locked for you because you " +
                "are not one of our patrons.\n" +
                "For only $1 per month you can have access to this and many other commands [click here link to get started](https://www.patreon.com/DuncteBot).\n" +
                "You will also need to join our discord server [here](https://discord.gg/NKM9Xtk)"));
            return false;
        }

        if (!m.getRoles().contains(supportGuild.getRoleById(patronsRole))) {
            sendEmbed(tc, EmbedUtils.embedMessage("This command is a patron only command and is locked for you because you " +
                "are not one of our patrons.\n" +
                "For only $1 per month you can have access to this and many other commands [click here link to get started](https://www.patreon.com/DuncteBot)."));
            return false;
        }

        patrons.add(u.getIdLong());

        return true;
    }

    public static boolean isPatron(@Nonnull User u, @Nullable  TextChannel tc, boolean reply) {
        final TextChannel textChannel = reply ? tc : null;
        return isPatron(u, textChannel);
    }

    public static boolean isGuildPatron(@Nonnull User u, @Nonnull Guild g) {

        if (guildPatrons.contains(g.getIdLong()) || oneGuildPatrons.containsValue(g.getIdLong())) {
            return true;
        }

        final Guild supportGuild = u.getJDA().asBot().getShardManager().getGuildById(supportGuildId);

        if (supportGuild == null) {
            return false;
        }

        final Member m = supportGuild.getMember(u);

        if (m == null) {
            return false;
        }

        if (!m.getRoles().contains(supportGuild.getRoleById(guildPatronsRole))) {
            return false;
        }

        guildPatrons.add(g.getIdLong());

        return true;
    }

    public static boolean isUserOrGuildPatron(@Nonnull GuildMessageReceivedEvent event, boolean reply) {
        final boolean isGuild = isGuildPatron(event.getAuthor(), event.getGuild());
        return isGuild || isPatron(event.getAuthor(), event.getChannel(), reply);
    }

    public static boolean isUserOrGuildPatron(@Nonnull GuildMessageReceivedEvent e) {
        return isUserOrGuildPatron(e, true);
    }

    public static boolean isDev(@Nonnull User u) {
        return Settings.developers.contains(u.getIdLong());
    }
}

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

package ml.duncte123.skybot.commands.fun;

import com.fasterxml.jackson.databind.JsonNode;
import me.duncte123.botcommons.messaging.EmbedUtils;
import ml.duncte123.skybot.extensions.UserKt;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.utils.AirUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendEmbed;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.Settings.PATREON;

public class LoveCommand extends Command {
//    private static final String EMPTY_BAR = " ";
//    private static final String FILLED_BAR = "█";

    public LoveCommand() {
        this.requiresArgs = true;
        this.category = CommandCategory.FUN;
        this.name = "ship";
        this.aliases = new String[]{
            "love",
            "loveship",
        };
        this.help = "Ship two people and get their love score";
        this.usage = "<@user> <@user>";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();

        String name1 = ctx.getMember().getEffectiveName();
        String name2 = args.get(0);

        if (args.size() > 1) {
            name1 = args.get(0);
            name2 = args.get(1);
        }

        if (name1.isBlank()) {
            sendMsg(ctx, "You will need to provide me a username for the first user");
            return;
        }

        if (name2.isBlank()) {
            sendMsg(ctx, "You will need to provide me a username for the user to match with");
            return;
        }

        final Member target1 = AirUtils.getMentionedMember(name1, ctx);
        final Member target2 = AirUtils.getMentionedMember(name2, ctx);
        final JsonNode response = ctx.getApis().getLove(target1.getEffectiveName(), target2.getEffectiveName());

        if (response == null) {
            sendMsg(ctx, "I failed my math exam and I could not calculate the love score");

            return;
        }

//        final int intScore = response.get("score_int").asInt() / 10;

        final EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
            .setTitle(response.get("names").asText(), PATREON)
            .setColor(EmbedUtils.getColorOrDefault(ctx.getGuild().getIdLong()))
            // The idea is cool, but it looks stupid
            /*.setDescription(
                String.format(
                    "[%s%s](%s) %s%%",
                    FILLED_BAR.repeat(intScore),
                    EMPTY_BAR.repeat(10 - intScore),
                    "https://localhost.com",
                    intScore
                )
            )*/
            .addField(response.get("score").asText(), response.get("message").asText(), false);

        final TextChannel channel = ctx.getChannel().asTextChannel();

        if (ctx.getSelfMember().hasPermission(channel, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EMBED_LINKS)) {
            ctx.getWeebApi().generateLoveship(
                UserKt.getStaticAvatarUrl(target1.getUser()),
                UserKt.getStaticAvatarUrl(target2.getUser())
            ).async((image) -> {
                final String message = String.format("Shipping **%s** and **%s**", target1.getEffectiveName(), target2.getEffectiveName());

                channel.sendMessage(message)
                    .addFiles(FileUpload.fromData(
                        image, "love.png"
                    ))
                    .setEmbeds(embed.setImage("attachment://love.png").build())
                    .queue();
            });
        } else {
            sendEmbed(ctx, embed, true);
        }
    }
}

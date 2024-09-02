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

package me.duncte123.skybot.commands.guild.mod;

import me.duncte123.skybot.Variables;
import me.duncte123.skybot.database.AbstractDatabase;
import me.duncte123.skybot.entities.jda.DunctebotGuild;
import me.duncte123.skybot.objects.command.CommandContext;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.MiscUtil;
import org.jetbrains.annotations.NotNull;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

public class AutoBanBypassCommand extends ModBaseCommand {
    public AutoBanBypassCommand() {
        this.requiresArgs = true;
        this.name = "autobanbypass";
        this.help = "Prevent a user to be auto banned when they join (one time use)";
        this.usage = "<user id>";
    }

    @Override
    protected void configureSlashSupport(@NotNull SlashCommandData baseData) {
        baseData.addOptions(
            new OptionData(
                OptionType.USER,
                "user_id",
                "The id of the user that you want to create the bypass for.",
                true
            )
        );
    }

    @Override
    public void handleEvent(@NotNull SlashCommandInteractionEvent event, @NotNull DunctebotGuild guild, @NotNull Variables variables) {
        final var checkId = event.getOption("user_id").getAsLong();

        final var database = variables.getDatabase();
        final var guildId = event.getGuild().getIdLong();

        event.deferReply().queue();

        database.getBanBypass(guildId, checkId).thenAccept((byPass) -> {
            if (byPass == null) {
                database.createBanBypass(guildId, checkId);
                event.getHook()
                    .editOriginal("Single use bypass created, please note that this bypass will expire after a week if unused." +
                    "\nPlease keep in mind that this has not unbanned any user, meaning that you will have to unban the user yourself if they are banned")
                    .queue();
                return;
            }

            event.getHook()
                .editOriginal("A bypass already exists for this user")
                .queue();
        }).exceptionally((thr) -> {
            event.getHook()
                .editOriginal("Something went wrong: " + thr.getMessage())
                .queue();

            return null;
        });
    }

    @Override
    public void execute(@NotNull CommandContext ctx) {
        final long checkId;

        try {
            checkId = MiscUtil.parseSnowflake(ctx.getArgs().get(0));
        } catch (NumberFormatException ignored) {
            sendMsg(ctx, "Your input (`" + ctx.getArgs().get(0) + "`) is not a valid user id.");
            return;
        }

        final AbstractDatabase database = ctx.getDatabase();
        final long guildId = ctx.getGuild().getIdLong();

        database.getBanBypass(guildId, checkId).thenAccept((byPass) -> {
            if (byPass == null) {
                database.createBanBypass(guildId, checkId);
                sendMsg(ctx, "Single use bypass created, please note that this bypass will expire after a week if unused." +
                    "\nPlease keep in mind that this has not unbanned any user, meaning that you will have to unban the user yourself if they are banned");
                return;
            }

            sendMsg(ctx, "A bypass already exists for this user");
        });
    }
}

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

package ml.duncte123.skybot.commands.guild.owner.settings;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.command.CommandContext;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.api.entities.Role;

import javax.annotation.Nonnull;
import java.util.List;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.commands.guild.owner.settings.AutoRoleCommand.getFoundRoleOrNull;

@Author(nickname = "duncte123", author = "Duncan Sterken")
public class MuteRoleCommand extends SettingsBase {

    public MuteRoleCommand() {
        this.name = "muterole";
        this.aliases = new String[]{
            "spamrole"
        };
        this.helpFunction = (prefix, invoke) -> "Sets the role that the user gets when they are muted";
        this.usageInstructions = (prefix, invoke) -> '`' + prefix + invoke + " <@role/disable>`";
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        final List<String> args = ctx.getArgs();

        if (rolePermCheck(ctx)) {
            return;
        }

        if (args.isEmpty()) {
            this.sendUsageInstructions(ctx);
            return;
        }


        final DunctebotGuild guild = ctx.getGuild();
        final GuildSettings settings = guild.getSettings();

        if ("disable".equals(args.get(0))) {
            sendMsg(ctx, "SpamRole feature & SpamFilter has been disabled");
            //Never clean the role's id so activating the filter wont cause issues.
            //GuildSettingsUtils.updateGuildSettings(guild, settings.setMuteRoleId(0L));
            guild.setSettings(settings.setEnableSpamFilter(false));
            return;
        }

        final Role foundRole = getFoundRoleOrNull(ctx);

        if (foundRole == null) {
            return;
        }

        guild.setSettings(settings.setMuteRoleId(foundRole.getIdLong()));

        sendMsg(ctx, "SpamRole has been set to " + foundRole.getAsMention());
    }
}

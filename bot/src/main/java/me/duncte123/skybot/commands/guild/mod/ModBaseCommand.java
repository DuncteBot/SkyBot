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
import me.duncte123.skybot.entities.jda.DunctebotGuild;
import me.duncte123.skybot.objects.SlashSupport;
import me.duncte123.skybot.objects.command.CommandCategory;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public abstract class ModBaseCommand extends SlashSupport {
    public ModBaseCommand() {
        this.category = CommandCategory.MODERATION;
        this.userPermissions = new Permission[]{Permission.KICK_MEMBERS, Permission.BAN_MEMBERS};
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    protected void configureSlashSupport(@NotNull SlashCommandData baseData) {
        // NO
    }

    @Override
    public void handleEvent(@NotNull SlashCommandInteractionEvent event, @NotNull DunctebotGuild guild, @NotNull Variables variables) {
        event.reply("soontm").queue();
    }
}

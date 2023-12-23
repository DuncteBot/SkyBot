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

package me.duncte123.skybot.objects.discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VanityInvite;

public class InviteData {
    private final String code;
    private final long guildId;
    private int uses;
    private final boolean vanity;
    private User inviter = null;

    private InviteData(long guildId, String code, int uses, boolean vanity) {
        this.guildId = guildId;
        this.uses = uses;
        this.code = code;
        this.vanity = vanity;
    }

    public long getGuildId() {
        return this.guildId;
    }

    public int getUses() {
        return this.uses;
    }

    public String getCode() {
        return this.code;
    }

    public void incrementUses() {
        this.uses++;
    }

    public boolean isVanity() {
        return this.vanity;
    }

    public String getUrl() {
        return "https://discord.gg/" + this.getCode();
    }

    public User getInviter() {
        return inviter;
    }

    public InviteData setInviter(User inviter) {
        this.inviter = inviter;
        return this;
    }

    public static InviteData from(VanityInvite invite, Guild guild) {
        return new InviteData(guild.getIdLong(), invite.getCode(), invite.getUses(), true);
    }

    public static InviteData from(Invite invite) {
        final Invite.Guild guild = invite.getGuild();

        if (guild == null) {
            throw new IllegalArgumentException("Can only accept guild invites");
        }

        return new InviteData(guild.getIdLong(), invite.getCode(), invite.getUses(), false)
            .setInviter(invite.getInviter());
    }
}

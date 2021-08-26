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

package ml.duncte123.skybot.objects.invites;

import net.dv8tion.jda.api.entities.Invite;

public class InviteData {
    private final String code;
    private final long guildId;
    private int uses;

    private InviteData(long guildId, String code, int uses) {
        this.guildId = guildId;
        this.uses = uses;
        this.code = code;
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

    public static InviteData from(Invite invite) {
        final Invite.Guild guild = invite.getGuild();

        if (guild == null) {
            throw new IllegalArgumentException("Can only accept guild invites");
        }

        return new InviteData(guild.getIdLong(), invite.getCode(), invite.getUses());
    }
}

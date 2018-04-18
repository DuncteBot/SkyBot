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

package ml.duncte123.skybot.objects.discord.user;

import net.dv8tion.jda.core.utils.Checks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Profile {

    private final String premiumSince;

    private final int flags;
    private final String id;
    private final String avatar;
    private final String username;
    private final String discriminator;

    public Profile(String premium_since, int flags, String id, String avatar, String username, String discriminator) {
        this.premiumSince = premium_since;

        this.flags = flags;
        this.id = id;
        this.avatar = avatar;
        this.username = username;
        this.discriminator = discriminator;
    }

    public int getFlags() {
        return flags;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public String getId() {
        return id;
    }

    public String getPremiumSince() {
        return premiumSince;
    }

    public String getUsername() {
        return username;
    }
    public boolean isNitro() {
        return premiumSince != null;
    }
    public List<Badge> getBadges() {
        return Badge.getBadges(flags, isNitro());
    }

    public static Profile emptyProfile() {
        return new Profile("", 0, "", "", "","");
    }

    public enum Badge {
        DISCORD_BUGHUNTER(5, 3),
        DISCORD_PARTNER(3, 1),
        DISCORD_STAFF(4, 0),
        HYPESQUAD(2, 2),
        NITRO(1, -1),

        UNKNOWN(0, -1);

        /*DISCORD_PARTNER(3, 1),
        DISCORD_STAFF(4, 0),
        HYPESQUAD(2, 2),
        NITRO(1, -1),

        UNKNOWN(0, -1);*/

        public static List<Badge> getBadges(final int flags, final boolean nitro) {
            final ArrayList<Badge> badges = new ArrayList<>(1);

            if (nitro)
                badges.add(NITRO);

            for (final Badge badge : Badge.values()) {
                if ((flags & badge.value) == badge.value && badge.offset != -1)
                    badges.add(badge);
            }

            return badges;
        }

        public static byte getFlags(final Badge... badges) {
            Checks.notNull(badges, "Badges");
            byte flags = 0;
            for (final Badge badge : badges) {
                Checks.notNull(badge, "Badges");
                if (badge.offset != -1)
                    flags |= badge.value;
            }
            return flags;
        }

        public static byte getFlags(final Collection<Badge> badges) {
            Checks.notNull(badges, "Badges");
            byte flags = 0;
            for (final Badge badge : badges) {
                Checks.notNull(badge, "Badges");
                if (badge.offset != -1)
                    flags |= badge.value;
            }
            return flags;
        }

        public static final byte ALL_FLAGS = getFlags(DISCORD_PARTNER, DISCORD_STAFF, HYPESQUAD);

        private final byte offset;
        private final byte priority;
        private final byte value;

        Badge(final int priority, final int offset) {
            this.priority = (byte) priority;
            this.offset = (byte) offset;
            this.value = (byte) (1 << offset);
        }

        public byte getValue() {
            return value;
        }

        public byte getOffset() {
            return offset;
        }

        public byte getPriority() {
            return this.priority;
        }
    }

}

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

package ml.duncte123.skybot.objects;

import ml.duncte123.skybot.extensions.TaskKt;
import ml.duncte123.skybot.utils.GuildUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class GuildMemberInfo {
    public long nitroUsers = 0L;
    public long users = 0L;
    public long bots = 0L;

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static GuildMemberInfo init(Guild guild) throws ExecutionException, InterruptedException {
        final List<Member> members = TaskKt.sync(guild.loadMembers());
        final GuildMemberInfo info = new GuildMemberInfo();

        final int total = members.size();
        final long bots = members.stream().filter((m) -> m.getUser().isBot()).count();
        final long memberCount = total - bots;
        final long nitroUsers = GuildUtils.countAnimatedAvatars(members);

        info.nitroUsers = nitroUsers;
        info.users = memberCount;
        info.bots = bots;

        return info;
    }
}

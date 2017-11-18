/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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
 *
 */

package ml.duncte123.skybot;

import ml.duncte123.skybot.objects.FakeInterface;
import ml.duncte123.skybot.objects.InvocationFunction;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

<<<<<<< HEAD
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
=======
import ml.duncte123.skybot.objects.FakeInterface;
import ml.duncte123.skybot.objects.InvocationFunction;
import net.dv8tion.jda.core.entities.Guild;
import org.junit.Test;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
>>>>>>> dev

public class FakeTest {
    
    /**
     * Test basic
     * Mock primitives and String
     *
     * @throws Throwable Should never happen
     */
    @Test
    public void basic()
            throws Throwable {
        User user = new FakeInterface<>(User.class).create();
        
        assertEquals(user.getIdLong(), 0L);
        assertEquals(user.getName(), "");
    }
    
    /**
     * Test medium
     * Mock Lists, don't share objects
     *
     * @throws Throwable Should never happen
     */
    @Test
    public void medium()
            throws Throwable {
        Member member = new FakeInterface<>(Member.class).create();
        JDA jda = member.getJDA();
        
        assertNotEquals(jda, member.getJDA());
        assertNotEquals(jda.getGuildById(member.getGuild().getIdLong()), member.getGuild());
        
        assertEquals(jda.getGuilds(), new ArrayList<>());
    }
    
    @Test
    public void advanced()
            throws Throwable {
        Map<Method, InvocationFunction> custom = new HashMap<>();
        
        custom.put(User.class.getMethod("getIdLong"), (p, m, a) -> 281673659834302464L);
        custom.put(User.class.getMethod("getId"), (p, m, a) -> "281673659834302464");
        custom.put(User.class.getMethod("getName"), (p, m, a) -> "ramidzkh");
        
        User m = new FakeInterface<>(User.class, custom).create();
        
        assertEquals(m.getIdLong(), 281673659834302464L);
        assertEquals(m.getId(), "281673659834302464");
        assertEquals(m.getName(), "ramidzkh");
    }

    @Test
    public void delegate()
    throws Throwable {
        Map<Method, InvocationFunction> handlers = new HashMap<>();
        
        handlers.put(Guild.class.getMethod("getJDA"), (p, m, a) -> null);
        
        FakeInterface<Guild> guildFakeInterface = new FakeInterface<>(Guild.class, handlers);
        guildFakeInterface.populateHandlers(guildFakeInterface.create());
        
        assertEquals(guildFakeInterface.create().getJDA(), null);
    }
}

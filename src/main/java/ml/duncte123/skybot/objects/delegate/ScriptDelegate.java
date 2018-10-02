/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan
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

package ml.duncte123.skybot.objects.delegate;

import groovy.lang.Binding;
import groovy.lang.Script;
import me.duncte123.botCommons.messaging.MessageUtils;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.entities.delegate.GuildDelegate;
import ml.duncte123.skybot.entities.delegate.RoleDelegate;
import ml.duncte123.skybot.entities.delegate.TextChannelDelegate;
import ml.duncte123.skybot.exceptions.DoomedException;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.File;
import java.util.List;

@SuppressWarnings("unused")
@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken"),
    @Author(nickname = "ramidzkh", author = "Ramid Khan")
})
public class ScriptDelegate extends Script {

    private static int counter = 0;

    private int count = ++counter;

    public ScriptDelegate(Script s) {
        super(s.getBinding());
    }

    @Override
    public Object run() {
        return "I am a bot, I can't run.";
    }

    @Override
    public void println() {
        throw new DoomedException("Hey, i like to keep my console clean");
    }

    @Override
    public void print(Object value) {
        throw new DoomedException("Hey, i like to keep my console clean");
    }

    @Override
    public void println(Object value) {
        throw new DoomedException("Hey, i like to keep my console clean");
    }

    @Override
    public void printf(String format, Object value) {
        throw new DoomedException("Hey, i like to keep my console clean");
    }

    @Override
    public void printf(String format, Object[] values) {
        throw new DoomedException("Hey, i like to keep my console clean");
    }

    @Override
    public Object evaluate(String expression) throws CompilationFailedException {
        throw new DoomedException("This method is blocked");
    }

    @Override
    public Object evaluate(File file) throws CompilationFailedException {
        throw new DoomedException("How did you get access to the file class?");
    }

    @Override
    public void run(File file, String[] arguments) throws CompilationFailedException {
        throw new DoomedException("I am a bot, I can't run");
    }

    @Override
    public Binding getBinding() {
        Binding b = new Binding();
        b.setProperty("meme", "you");
        return b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String property, Object newValue) {
        //Nothing that we allow them to set
    }

    public Object dump() {
        return "\uD83D\uDEAE";
    }

    public Object find() {
        return "You will never find me.";
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public int quick_mafs(int x) {
        int the_thing = x + 2 - 1;
        return the_thing;
    }

    public boolean isEven(int number) {
        return number % 2 == 0;
    }

    public int countPeopleWithRole(String name) {
        GuildDelegate guild = (GuildDelegate) super.getBinding().getProperty("guild");
        List<Role> roles = guild.getRolesByName(name, true);
        if (roles.size() == 0)
            return 0;
        Role role = roles.get(0);
        List<Member> members = guild.getMembersWithRoles(((RoleDelegate) role).getUA83D3Ax_ky());
        return members.size();
    }

    public void pinnedMessageCheck() {
        TextChannel channel = ((TextChannelDelegate) super.getBinding().getProperty("channel")).getK7S83hjaA();

        channel.getPinnedMessages().queue(it ->
            MessageUtils.sendMsg(channel, it.size() + "/50 messages pinned in this channel")
        );
    }

    @Override
    public String toString() {
        return "Script" + count;
    }
}

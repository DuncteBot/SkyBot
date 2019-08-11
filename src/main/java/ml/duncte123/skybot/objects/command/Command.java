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

package ml.duncte123.skybot.objects.command;

import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import net.dv8tion.jda.api.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiFunction;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static me.duncte123.botcommons.messaging.MessageUtils.sendMsgFormat;
import static ml.duncte123.skybot.utils.AirUtils.parsePerms;

@SuppressWarnings("SameParameterValue")
@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken"),
    @Author(nickname = "ramidzkh", author = "Ramid Khan")
})
public abstract class Command implements ICommand {
    protected static final Logger logger = LoggerFactory.getLogger(Command.class);
    // The size should match the usage for stability but not more than 4.
    protected static final ScheduledExecutorService commandService = Executors.newScheduledThreadPool(3,
        r -> new Thread(r, "Command-Thread"));

    //@formatter:off
    protected boolean displayAliasesInHelp = false;
    protected CommandCategory category = CommandCategory.MAIN;
    protected String name = "null";
    protected String[] aliases = new String[0];
    protected BiFunction<String, String, String> helpFunction = (invoke, prefix) -> "No help available";
    protected BiFunction<String, String, String> usageInstructions = (invoke, prefix) -> '`' + prefix + invoke + '`';
    protected Permission[] userPermissions = new Permission[0];
    protected Permission[] botPermissions = new Permission[0];
    public Flag[] flags = new Flag[0];

    //@formatter:on

    @Override
    public void executeCommand(@Nonnull CommandContext ctx) {
        if (this.userPermissions.length > 0 && !ctx.getMember().hasPermission(ctx.getChannel(), this.userPermissions)) {
            final String permissionsWord = "permission" + (this.userPermissions.length > 1 ? "s" : "");

            sendMsgFormat(ctx,
                "You need the `%s` %s for this command\nPlease contact your server administrator if this is incorrect.",
                parsePerms(this.userPermissions), permissionsWord
            );

            return;
        }

        if (this.botPermissions.length > 0 && !ctx.getSelfMember().hasPermission(ctx.getChannel(), this.botPermissions)) {
            final String permissionsWord = "permission" + (this.botPermissions.length > 1 ? "s" : "");

            sendMsgFormat(ctx,
                "I need the `%s` %s for this command to work\nPlease contact your server administrator about this.",
                parsePerms(this.botPermissions), permissionsWord
            );

            return;
        }

        execute(ctx);
    }

    /*public void execute(@Nonnull CommandContext ctx) {
        throw new NotImplementedException("This command has not been updated yet");
    }*/

    public abstract void execute(@Nonnull CommandContext ctx);

    @Nonnull
    @Override
    public String getName() {
        return this.name;
    }

    @Nonnull
    @Override
    public final String[] getAliases() {
        return this.aliases;
    }

    @Nonnull
    @Override
    public final String help(@Nonnull String invoke, @Nonnull String prefix) {
        return this.helpFunction.apply(invoke, prefix);
    }

    @Override
    public final boolean shouldDisplayAliasesInHelp() {
        return this.displayAliasesInHelp;
    }

    @Nonnull
    public CommandCategory getCategory() {
        return this.category;
    }

    public @Nonnull
    String getUsageInstructions(@Nonnull String invoke, @Nonnull String prefix) {
        return this.usageInstructions.apply(invoke, prefix);
    }

    protected void sendUsageInstructions(CommandContext ctx) {
        sendMsg(ctx, "Usage: " + this.getUsageInstructions(ctx.getInvoke(), ctx.getPrefix()));
    }

    @Override
    public String toString() {
        return "Command[" + this.name + ']';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass() || !(obj instanceof Command)) {
            return false;
        }

        final Command command = (Command) obj;

        return this.name.equals(command.getName());
    }
}

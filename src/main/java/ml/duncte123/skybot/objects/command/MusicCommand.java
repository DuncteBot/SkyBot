/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

import fredboat.audio.player.LavalinkManager;
import ml.duncte123.skybot.Author;
import ml.duncte123.skybot.Authors;
import ml.duncte123.skybot.SinceSkybot;
import ml.duncte123.skybot.objects.CooldownScope;
import ml.duncte123.skybot.utils.AudioUtils;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nonnull;
import java.util.function.Function;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;
import static ml.duncte123.skybot.utils.CommandUtils.isUserOrGuildPatron;

@SuppressWarnings("ConstantConditions")
@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public abstract class MusicCommand extends Command {

    @SinceSkybot(version = "3.54.2")
    protected boolean withAutoJoin = false;

    public static final Function<String, String> KEY_GEN = (guildId) -> "musicCommand|" + guildId;
    public static final int MUSIC_COOLDOWN = 12;

    public MusicCommand() {
        this.category = CommandCategory.MUSIC;
        // Has to be set in the individual commands
//        this.cooldown = musicCooldown;
        this.cooldownScope = CooldownScope.GUILD;
        this.cooldownKey = (cmdName, ctx) -> KEY_GEN.apply(ctx.getGuild().getId());
        // Patrons have no cooldown
        this.overridesCooldown = (ctx) -> isUserOrGuildPatron(ctx, false);
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if (this.withAutoJoin) {
            runWithAutoJoin(ctx);
        } else if (channelChecks(ctx, ctx.getAudioUtils(), ctx.getPrefix())) {
            run(ctx);
        }
    }

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    public void run(@Nonnull CommandContext ctx) {
        // Cannot be abstract due to the join command
    }

    private void runWithAutoJoin(@Nonnull CommandContext ctx) {
        if (isAbleToJoinChannel(ctx)) {
            ctx.getCommandManager().getCommand("join").executeCommand(ctx);
        } else if (!channelChecks(ctx, ctx.getAudioUtils(), ctx.getPrefix())) {
            return;
        }

        run(ctx);
    }

    private boolean channelChecks(CommandContext ctx, AudioUtils audioUtils, String prefix) {
        if (!ctx.getMember().getVoiceState().inVoiceChannel()) {
            sendMsg(ctx, "Please join a voice channel first");
            return false;
        }

        final LavalinkManager lavalinkManager = getLavalinkManager();
        final Guild guild = ctx.getGuild();

        if (!lavalinkManager.isConnected(guild)) {
            sendMsg(ctx, "I'm not in a voice channel, use `" + prefix + "join` to make me join a channel\n\n" +
                "Want to have the bot automatically join your channel? Consider becoming a patron.");

            return false;
        }

        if (lavalinkManager.getConnectedChannel(guild) != null &&
            !lavalinkManager.getConnectedChannel(guild).getMembers().contains(ctx.getMember())) {
            sendMsg(ctx, "I'm sorry, but you have to be in the same channel as me to use any music related commands");

            return false;
        }

        audioUtils.getMusicManager(guild).setLastChannel(ctx.getChannel().getIdLong());

        return true;
    }

    private boolean isAbleToJoinChannel(CommandContext ctx) {
        if (isUserOrGuildPatron(ctx, false)) {
            //If the member is not connected
            if (!ctx.getMember().getVoiceState().inVoiceChannel()) {
                return false;
            }

            return !getLavalinkManager().isConnected(ctx.getGuild());
        }

        return false;
    }

    protected static LavalinkManager getLavalinkManager() {
        return LavalinkManager.INS;
    }
}

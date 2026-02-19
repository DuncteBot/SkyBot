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

package me.duncte123.skybot.objects.command;

import fredboat.audio.player.LavalinkManager;
import me.duncte123.skybot.CommandManager;
import me.duncte123.skybot.Variables;
import me.duncte123.skybot.entities.jda.DunctebotGuild;
import me.duncte123.skybot.objects.CooldownScope;
import me.duncte123.skybot.utils.AudioUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import javax.annotation.Nonnull;
import java.util.function.Function;

import static me.duncte123.botcommons.messaging.MessageUtils.sendMsg;

@SuppressWarnings("ConstantConditions")
public abstract class MusicCommand extends Command {
    protected boolean mayAutoJoin = false;

    public static final Function<String, String> KEY_GEN = (guildId) -> "musicCommand|" + guildId;
    public static final int MUSIC_COOLDOWN = 12;

    public MusicCommand() {
        this.category = CommandCategory.MUSIC;
        // Has to be set in the individual commands
//        this.cooldown = musicCooldown;
        this.cooldownScope = CooldownScope.GUILD;
        this.cooldownKey = (cmdName, ctx) -> KEY_GEN.apply(ctx.getGuild().getId());
        // Patrons have no cooldown
//        this.overridesCooldown = (ctx) -> isUserOrGuildPatron(ctx, false);
    }

    @Override
    public void execute(@Nonnull CommandContext ctx) {
        if (!LavalinkManager.INS.isEnabled()) {
            sendMsg(ctx, "The music feature is currently under maintenance.");
            return;
        }

        runWithAutoJoin(ctx);
    }

    public abstract void run(@Nonnull CommandContext ctx);

    private void runWithAutoJoin(@Nonnull CommandContext ctx) {
        if (isAbleToJoinChannel(ctx)) {
            ctx.getCommandManager().getCommand("join").executeCommand(ctx);
        } else if (!channelChecks(ctx, ctx.getAudioUtils())) {
            return;
        }

        run(ctx);
    }

    private boolean channelChecks(CommandContext ctx, AudioUtils audioUtils) {
        if (!ctx.getMember().getVoiceState().inAudioChannel()) {
            sendMsg(ctx, "Please join a voice channel first");
            return false;
        }

        final LavalinkManager lavalinkManager = getLavalinkManager();
        final Guild guild = ctx.getGuild();
        final AudioChannelUnion connectedChannel = lavalinkManager.getConnectedChannel(guild);

        if (connectedChannel != null && !connectedChannel.getMembers().contains(ctx.getMember())) {
            sendMsg(ctx, "I'm sorry, but you have to be in the same channel as me to use any music related commands");

            return false;
        }

        audioUtils.getMusicManager(guild.getIdLong()).setLatestChannelId(ctx.getChannel().getIdLong());

        return true;
    }

    private boolean canRunSlashCommand(SlashCommandInteractionEvent event, DunctebotGuild guild, AudioUtils audioUtils) {
        if (!event.getMember().getVoiceState().inAudioChannel()) {
            event.reply("Please join a voice channel first").setEphemeral(true).queue();
            return false;
        }

        final LavalinkManager lavalinkManager = getLavalinkManager();
        final AudioChannelUnion connectedChannel = lavalinkManager.getConnectedChannel(guild);

        if (connectedChannel != null && !connectedChannel.getMembers().contains(event.getMember())) {
            event.reply("I'm sorry, but you have to be in the same channel as me to use any music related commands")
                .setEphemeral(true)
                .queue();

            return false;
        }

        audioUtils.getMusicManager(guild.getIdLong()).setLatestChannelId(event.getChannel().getIdLong());
        return true;
    }

    private boolean isAbleToJoinChannel(CommandContext ctx) {
        return this.mayAutoJoin && ctx.getMember().getVoiceState().inAudioChannel() &&
            !getLavalinkManager().isConnected(ctx.getGuild());
    }

    protected static LavalinkManager getLavalinkManager() {
        return LavalinkManager.INS;
    }

    @Nonnull
    protected SubcommandData getSubData() {
        return new SubcommandData(getName(), getHelp(getName(), "/"));
    }

    public void handleSlashWithAutoJoin(@Nonnull SlashCommandInteractionEvent event, DunctebotGuild guild, @Nonnull Variables variables) {
        if (!LavalinkManager.INS.isEnabled()) {
            event.reply("The music feature is currently under maintenance.").queue();
            return;
        }

        if (canRunSlashCommand(event, guild, variables.getAudioUtils())) {
            if (this.mayAutoJoin) {
                ((MusicCommand) variables.getCommandManager()
                    .getCommand("join"))
                    .handleEvent(event, guild, variables);
            }

            this.handleEvent(event, guild, variables);
        }
    }

    public abstract void handleEvent(@Nonnull SlashCommandInteractionEvent event, DunctebotGuild guild, @Nonnull Variables variables);

    public static SlashCommandData getMusicCommandData(CommandManager mngr) {
        final var base = Commands.slash("music", "base command for music commands")
            .setContexts(InteractionContextType.GUILD);

        mngr.getCommands(CommandCategory.MUSIC).forEach((cmd) -> base.addSubcommands(
            ((MusicCommand) cmd).getSubData()
        ));

        return base;
    }
}

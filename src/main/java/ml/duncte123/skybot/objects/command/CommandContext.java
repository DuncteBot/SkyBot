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

import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import me.duncte123.weebJava.models.WeebApi;
import ml.duncte123.skybot.*;
import ml.duncte123.skybot.adapters.DatabaseAdapter;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.api.DuncteApis;
import ml.duncte123.skybot.objects.apis.BlargBot;
import ml.duncte123.skybot.objects.apis.alexflipnote.Alexflipnote;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Authors(authors = {
    @Author(nickname = "Sanduhr32", author = "Maurice R S"),
    @Author(nickname = "duncte123", author = "Duncan Sterken")
})
public class CommandContext {

    private final String invoke;
    private final List<String> args;
    private final GuildMessageReceivedEvent event;
    private final Variables variables;
    private List<String> argsWithoutQuotes;
    private List<Member> mentionedInMessage;
    private GuildMessageReceivedEvent reactionAddEvent = null;
    private long replyId = 0L;

    public CommandContext(String invoke, List<String> args, GuildMessageReceivedEvent event) {
        this.invoke = invoke;
        this.args = Collections.unmodifiableList(args);
        this.event = event;
        this.variables = Variables.getInstance();
    }

    // --------------- Methods from the Variables class --------------- //

    public Variables getVariables() {
        return variables;
    }

    public CommandManager getCommandManager() {
        return this.variables.getCommandManager();
    }

    public DunctebotConfig getConfig() {
        return this.variables.getConfig();
    }

    public DatabaseAdapter getDatabaseAdapter() {
        return this.variables.getDatabaseAdapter();
    }

    public ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }

    public WeebApi getWeebApi() {
        return this.variables.getWeebApi();
    }

    public String getGoogleBaseUrl() {
        return this.variables.getGoogleBaseUrl();
    }

    public BlargBot getBlargbot() {
        return this.variables.getBlargBot();
    }

    public Alexflipnote getAlexFlipnote() {
        return this.variables.getAlexflipnote();
    }

    public AudioUtils getAudioUtils() {
        return this.variables.getAudioUtils();
    }

    public DuncteApis getApis() {
        return variables.getApis();
    }

    // --------------- Normal methods --------------- //

    public String getInvoke() {
        return this.invoke;
    }

    public List<String> getArgs() {

        if (this.argsWithoutQuotes == null) {
            this.argsWithoutQuotes = this.args.stream()
                .map((arg) -> arg.replace("\"", "")).collect(Collectors.toList());
        }

        return this.argsWithoutQuotes;
    }

    public List<String> getArgsWithQuotes() {
        return this.args;
    }

    public String getArgsJoined() {
        return String.join(" ", this.getArgs());
    }

    public String getArgsRaw() {
        return this.getArgsRaw(true);
    }

    public String getArgsRaw(boolean fixlines) {
        return parseRawArgs(this.event.getMessage().getContentRaw(), fixlines);
    }

    public String getArgsDisplay() {
        return parseRawArgs(this.event.getMessage().getContentDisplay());
    }

    public GuildSettings getGuildSettings() {
        return GuildSettingsUtils.getGuild(this.event.getGuild(), this.variables);
    }

    public GuildMessageReceivedEvent getEvent() {
        return this.event;
    }

    public List<Member> getMentionedMembers() {
        if (this.mentionedInMessage == null) {
            this.mentionedInMessage = new ArrayList<>();

            this.getArgs().forEach(
                (arg) -> this.mentionedInMessage.addAll(FinderUtil.findMembers(arg, this.getGuild()))
            );
        }

        return this.mentionedInMessage;
    }

    // --------------- Reaction processing methods --------------- //

    public ReactionHandler getReactionHandler() {
        final EventManager manager = (EventManager) this.getJDA().getEventManager();

        return manager.getReactionHandler();
    }

    public CommandContext applyReactionEvent(GuildMessageReceivedEvent event) {
        this.reactionAddEvent = event;
        return this;
    }

    public CommandContext applySentId(long id) {
        this.replyId = id;
        return this;
    }

    public boolean replyIsSet() {
        return this.replyId != 0L;
    }

    public boolean reactionEventIsSet() {
        return this.reactionAddEvent != null;
    }

    public GuildMessageReceivedEvent getReactionEvent() {
        return this.reactionAddEvent;
    }

    public long getSendId() {
        return this.replyId;
    }

    // --------------- Methods that are in the GuildMessageReceivedEvent --------------- //

    public Message getMessage() {
        return this.event.getMessage();
    }

    public User getAuthor() {
        return this.event.getAuthor();
    }

    public Member getMember() {
        return this.event.getMember();
    }

    public TextChannel getChannel() {
        return this.event.getChannel();
    }

    public DunctebotGuild getGuild() {
        return new DunctebotGuild(this.event.getGuild());
    }

    public JDA getJDA() {
        return this.event.getJDA();
    }

    public ShardManager getShardManager() {
        return this.getJDA().asBot().getShardManager();
    }

    public User getSelfUser() {
        return this.getJDA().getSelfUser();
    }

    public Member getSelfMember() {
        return this.getGuild().getSelfMember();
    }

    // --------------- Private methods --------------- //

    private String parseRawArgs(String in) {
        return parseRawArgs(in, true);
    }

    private String parseRawArgs(String in, boolean fixlines) {
        final String out = in.replaceFirst(
            "(?i)" + Pattern.quote(Settings.PREFIX) + "|" +
                Pattern.quote(Settings.OTHER_PREFIX) + "|" +
                Pattern.quote(this.getGuildSettings().getCustomPrefix()),
            "")
            .split("\\s+", 2)[1];

        if (fixlines) {
            return out.replaceAll("\\\\n", "\n");
        }

        return out;
    }
}

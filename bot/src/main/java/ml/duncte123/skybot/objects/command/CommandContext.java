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

package ml.duncte123.skybot.objects.command;

import com.dunctebot.models.settings.GuildSetting;
import me.duncte123.botcommons.commands.ICommandContext;
import me.duncte123.weebJava.models.WeebApi;
import ml.duncte123.skybot.*;
import ml.duncte123.skybot.database.AbstractDatabase;
import ml.duncte123.skybot.entities.jda.DunctebotGuild;
import ml.duncte123.skybot.objects.api.DuncteApis;
import ml.duncte123.skybot.objects.apis.BlargBot;
import ml.duncte123.skybot.objects.apis.alexflipnote.Alexflipnote;
import ml.duncte123.skybot.objects.config.DunctebotConfig;
import ml.duncte123.skybot.utils.AudioUtils;
import ml.duncte123.skybot.utils.CommandUtils;
import ml.duncte123.skybot.utils.FinderUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandContext implements ICommandContext {
    private final String invoke;
    private final List<String> args;
    private final MessageReceivedEvent event;
    private final Variables variables;
    private final DunctebotGuild duncteBotGuild;
    private List<String> argsWithoutQuotes;
    private Map<String, List<String>> cachedFlags;
    private StringSelectInteractionEvent selectionEvent = null;
    private long replyId = 0L;

    public CommandContext(String invoke, List<String> args, MessageReceivedEvent event, Variables variables) {
        this.invoke = invoke;
        this.args = Collections.unmodifiableList(args);
        this.event = event;
        this.variables = variables;
        this.duncteBotGuild = new DunctebotGuild(event.getGuild(), variables);
    }

    // --------------- Methods from the Variables class --------------- //

    public Variables getVariables() {
        return this.variables;
    }

    public CommandManager getCommandManager() {
        return this.variables.getCommandManager();
    }

    public DunctebotConfig getConfig() {
        return this.variables.getConfig();
    }

    public AbstractDatabase getDatabase() {
        return this.variables.getDatabase();
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

    public Map<String, List<String>> getParsedFlags(Command cmd) {
        if (this.cachedFlags == null) {
            this.cachedFlags = CommandUtils.parseInput(cmd.flags, this.getArgs());
        }

        return this.cachedFlags;
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
        return this.parseRawArgs(this.event.getMessage().getContentRaw(), fixlines);
    }

    public String getArgsDisplay() {
        return this.parseRawArgs(this.event.getMessage().getContentDisplay());
    }

    public GuildSetting getGuildSettings() {
        return this.duncteBotGuild.getSettings();
    }

    public String getPrefix() {
        return this.getGuildSettings().getCustomPrefix();
    }

    @Override
    public MessageReceivedEvent getEvent() {
        return this.event;
    }

    public List<Member> getMentionedArg(int index) {
        return FinderUtils.searchMembers(this.getArgs().get(index), this);
    }

    // --------------- Reaction processing methods --------------- //

    public ReactionHandler getReactionHandler() {
        final EventManager manager = (EventManager) this.getJDA().getEventManager();

        return manager.getReactionHandler();
    }

    public CommandContext applyButtonEvent(StringSelectInteractionEvent event) {
        this.selectionEvent = event;
        return this;
    }

    public CommandContext applySentId(long idLong) {
        this.replyId = idLong;
        return this;
    }

    public boolean replyIsSet() {
        return this.replyId != 0L;
    }

    public boolean selectionEventIsSet() {
        return this.selectionEvent != null;
    }

    public StringSelectInteractionEvent getSelectionEvent() {
        return this.selectionEvent;
    }

    public long getSendId() {
        return this.replyId;
    }

    // --------------- Methods that are in the GuildMessageReceivedEvent --------------- //

    @Override
    public DunctebotGuild getGuild() {
        return this.duncteBotGuild;
    }

    public Guild getJDAGuild() {
        return this.event.getGuild();
    }

    // --------------- Private methods --------------- //

    private String parseRawArgs(String input) {
        return parseRawArgs(input, true);
    }

    private String parseRawArgs(String input, boolean fixlines) {
        final String prefixRemoved = input.replaceFirst(
            "(?i)" + Pattern.quote(Settings.PREFIX) + "|" +
                Pattern.quote(Settings.OTHER_PREFIX) + "|" +
                Pattern.quote(this.getGuildSettings().getCustomPrefix()),
            "");
        final String out = prefixRemoved.substring(prefixRemoved.indexOf(this.invoke) + this.invoke.length()).trim();

        if (fixlines) {
            return out.replaceAll("\\\\n", "\n");
        }

        return out;
    }
}

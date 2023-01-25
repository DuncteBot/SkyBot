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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Nonnull;

public class CustomCommand implements ICommand<Object> {

    private final String invoke;
    private final String message;
    private final long guildId;
    private final boolean autoresponse;

    @JsonCreator
    public CustomCommand(@JsonProperty("invoke") String invoke, @JsonProperty("message") String message,
                         @JsonProperty("guildId") @JsonAlias({"guildId", "guild_id"}) long guildId,
                         @JsonProperty("autoresponse") boolean autoresponse) {
        this.invoke = invoke;
        this.message = message;
        this.guildId = guildId;
        this.autoresponse = autoresponse;
    }

    public String getMessage() {
        return message;
    }

    public long getGuildId() {
        return guildId;
    }

    @Nonnull
    @Override
    public String getName() {
        return invoke;
    }

    @Nonnull
    @Override
    public String getHelp(@Nonnull String invoke, @Nonnull String prefix) {
        return "null";
    }

    public boolean isAutoResponse() {
        return autoresponse;
    }

    @Override
    public boolean isCustom() {
        return true;
    }

    //Override some methods that are not needed
    @Override
    public void executeCommand(@Nonnull Object ctx) {
        // Custom commands are executed in a different way
    }

    @Nonnull
    @Override
    public CommandCategory getCategory() {
        return CommandCategory.UNLISTED;
    }

    @Override
    public boolean shouldDisplayAliasesInHelp() {
        return false;
    }

    @Override
    public String gerNameForLogger() {
        return "Custom[name="+getName()+",guild="+getGuildId()+']';
    }

    public JsonNode toJSONObject(ObjectMapper mapper) {
        return mapper.createObjectNode()
            .put("guildId", getGuildId())
            .put("name", getName())
            .put("autoresponse", isAutoResponse())
            .put("message", getMessage());
    }
}

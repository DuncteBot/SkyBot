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

package ml.duncte123.skybot.objects.command.custom;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import ml.duncte123.skybot.Author;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@JsonIgnoreProperties(ignoreUnknown = true)
@Author(nickname = "duncte123", author = "Duncan Sterken")
public class CustomCommandImpl implements CustomCommand {

    private final String invoke;
    private final String message;
    private final long guildId;
    private final boolean autoresponse;

    @JsonCreator
    public CustomCommandImpl(@JsonProperty("invoke") String invoke, @JsonProperty("message") String message,
                             @JsonProperty("guildId") long guildId, @JsonProperty("autoresponse") boolean autoresponse) {
        this.invoke = invoke;
        this.message = message;
        this.guildId = guildId;
        this.autoresponse = autoresponse;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public long getGuildId() {
        return guildId;
    }

    @NotNull
    @Override
    public String getName() {
        return invoke;
    }

    @Nonnull
    @Override
    public String help(@Nonnull String invoke, @Nonnull String prefix) {
        return "null";
    }

    @Override
    public boolean isAutoResponse() {
        return autoresponse;
    }
}

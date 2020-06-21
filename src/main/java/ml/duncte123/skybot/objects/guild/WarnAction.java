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

package ml.duncte123.skybot.objects.guild;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WarnAction {
    public static final int PATRON_MAX_ACTIONS = 3;

    private final Type type;
    private final int threshold;
    private final int duration;

    @JsonCreator
    public WarnAction(@JsonProperty("type") Type type, @JsonProperty("threshold") int threshold, @JsonProperty("duration") int duration) {
        this.type = type;
        this.threshold = threshold;
        this.duration = duration;
    }

    public WarnAction(Type type, int threshold) {
        this(type, threshold, -1);
    }

    public int getThreshold() {
        return this.threshold;
    }

    public int getDuration() {
        return this.duration;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return String.format(
            "{type: %s, threshold: %s, duration: %s}",
            this.type, this.threshold, this.duration
        );
    }

    public enum Type {
        MUTE,
        TEMP_MUTE,
        KICK,
        TEMP_BAN,
        BAN
        ;

        public boolean isTemp() {
            return this.name().startsWith("TEMP_");
        }

        public String getId() {
            return this.name();
        }

        public String getName() {
            final String[] split = this.name().split("_");
            final StringBuilder out = new StringBuilder();

            for (String s : split) {
                out.append(s, 0, 1)
                    .append(s.substring(1).toLowerCase())
                    .append(" ");
            }

            return out.toString().trim();
        }

        @Override
        public String toString() {
            return String.format(
                "{id: \"%s\", name: \"%s\", temp: %s}",
                this.getId(), this.getName(), this.isTemp()
            );
        }
    }
}

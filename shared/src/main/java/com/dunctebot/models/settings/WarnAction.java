/*
 * MIT License
 *
 * Copyright (c) 2020 Duncan Sterken
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.dunctebot.models.settings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

@JsonIgnoreProperties(ignoreUnknown = true)
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
        this(type, threshold, 5);
    }

    public int getThreshold() {
        return this.threshold;
    }

    public int getDuration() {
        return this.duration;
    }

    @Nonnull
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

            for (final String s : split) {
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


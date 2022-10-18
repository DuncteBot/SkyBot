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

import javax.annotation.Nullable;

public enum ProfanityFilterType {
    NORMAL("TOXICITY", "Toxic"),
    SEVERE_TOXICITY("SEVERE_TOXICITY", "Very Toxic"),
    INSULT("INSULT", "Insult (Experimental)"),
    PROFANITY("PROFANITY", "Profanity (Experimental)");

    private final String type;
    private final String name;

    ProfanityFilterType(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.type;
    }

    public static ProfanityFilterType fromType(@Nullable String type) {
        for (final ProfanityFilterType value : values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }

        // Default to severe as that was the old permanent setting
        return SEVERE_TOXICITY;
    }
}

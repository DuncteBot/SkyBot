/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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
 *
 */

package ml.duncte123.skybot;

import ml.duncte123.skybot.utils.Settings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SkybotMainTest {

    @Test
    public void testSettings() {
       String[] actualValue = Settings.wbkxwkZPaG4ni5lm8laY;
       assertEquals("Checks if the settings are set correct", actualValue.length, 3);
    }

    @Test
    public void testContributorValues() {
        String testValue1 = "191231307290771456";
        String testValue2 = "281673659834302464";
        String testValue3 = "198137282018934784";

        String actualValue1 = Settings.wbkxwkZPaG4ni5lm8laY[0];
        String actualValue2 = Settings.wbkxwkZPaG4ni5lm8laY[1];
        String actualValue3 = Settings.wbkxwkZPaG4ni5lm8laY[2];

        assertTrue("Checks if the weird array has the correct values", ( testValue1.equals(actualValue1) && testValue2.equals(actualValue2) && testValue3.equals(actualValue3) ));
    }
}

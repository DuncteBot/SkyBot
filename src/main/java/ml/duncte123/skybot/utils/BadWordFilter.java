/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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

package ml.duncte123.skybot.utils;

import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BadWordFilter {

    private int largestWordLength = 0;
    private Map<String, String[]> words = new HashMap<>();

    public BadWordFilter() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new URL("https://docs.google.com/spreadsheets/d/"
                                    + "1hIEi2YG3ydav1E06Bzf2mQbGZ12kh2fe4ISgLg_UBuM/"
                                    + "export?format=csv").openConnection().getInputStream()));
            String line;
            int counter = 0;
            while ((line = reader.readLine()) != null) {
                counter++;
                String[] content;
                try {
                    content = line.split(",");
                    if (content.length == 0) {
                        continue;
                    }
                    String word = content[0];
                    String[] ignore_in_combination_with_words = new String[]{};
                    if (content.length > 1) {
                        ignore_in_combination_with_words = content[1].split("_");
                    }

                    if (word.length() > largestWordLength) {
                        largestWordLength = word.length();
                    }
                    words.put(word.replaceAll(" ", ""), ignore_in_combination_with_words);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            LoggerFactory.getLogger(BadWordFilter.class).info("Loaded " + counter + " words to filter out");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Iterates over a String input and checks whether a cuss word was found in a list, then checks if the word should be ignored (e.g. bass contains the word *ss).
     *
     * @param in The sentence to check
     * @return every word as a item in array
     */
    private ArrayList<String> badWordsFound(String in) {
        if (in == null) {
            return new ArrayList<>();
        }
        String input = in;
        // remove leetspeak
        input = input.replaceAll("[1!]", "i");
        input = input.replaceAll("3", "e");
        input = input.replaceAll("[4@]", "a");
        input = input.replaceAll("5", "s");
        input = input.replaceAll("7", "t");
        input = input.replaceAll("0", "o");
        input = input.replaceAll("9", "g");

        ArrayList<String> badWords = new ArrayList<>();
        input = input.toLowerCase().replaceAll("[^a-zA-Z ]", "");

        // iterate over each letter in the word
        for (int start = 0; start < input.length(); start++)
            // from each letter, keep going to find bad words until either the end of the sentence is reached, or the max word length is reached.
            for (int offset = 1; offset < (input.length() + 1 - start)
                    && offset < largestWordLength; offset++) {
                String wordToCheck = input.substring(start, start + offset);
                if (words.containsKey(wordToCheck)) {
                    // for example, if you want to say the word bass, that should be possible.
                    String[] ignoreCheck = words.get(wordToCheck);
                    boolean ignore = false;
                    for (String anIgnoreCheck : ignoreCheck)
                        if (input.contains(anIgnoreCheck)) {
                            ignore = true;
                            break;
                        }
                    if (!ignore)
                        badWords.add(wordToCheck);
                }
            }

        return badWords;
    }

    /**
     * Checks if the sentence contains a bad word
     *
     * @param input the sentence to check
     * @return true if it contains a bad word
     */
    public boolean filterText(String input) {
        ArrayList<String> badWords = badWordsFound(input);
        return badWords.size() > 0;
    }
}

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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.entities;

import com.batiaev.aiml.bot.BotConfiguration;
import com.batiaev.aiml.bot.BotInfo;
import com.batiaev.aiml.channels.ChannelType;
import com.batiaev.aiml.chat.ChatContext;
import com.batiaev.aiml.chat.ChatContextStorage;
import com.batiaev.aiml.core.GraphMaster;
import com.batiaev.aiml.entity.AimlCategory;
import com.batiaev.aiml.entity.AimlMap;
import com.batiaev.aiml.entity.AimlSet;
import com.batiaev.aiml.entity.AimlSubstitution;
import com.batiaev.aiml.loaders.*;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotImpl implements Bot {
    private static final Logger log = LoggerFactory.getLogger(BotImpl.class);
    private GraphMaster brain;
    private BotInfo botInfo;
    private String rootDir;
    private String name;
    private ChatContextStorage chatContextStorage;
    private ChatContext chatContext;
    private TextChannel channel;

    public BotImpl(String name, String rootDir, ChatContextStorage chatContextStorage) {
        this.name = name;
        this.rootDir = rootDir;
        this.botInfo = new BotConfiguration(rootDir);
        this.chatContextStorage = chatContextStorage;
        this.chatContext = this.chatContextStorage.getContext(name, ChannelType.CONSOLE);
        Map<String, AimlSet> aimlSets = this.loadSets();
        Map<String, AimlMap> aimlMaps = this.loadMaps();
        List<AimlCategory> aimlCategories = this.loadAiml();
        this.brain = new GraphMaster(this.preprocess(aimlCategories, aimlSets), aimlSets, aimlMaps, this.loadSubstitutions(), this.botInfo);
    }

    private List<AimlCategory> preprocess(List<AimlCategory> categories, Map<String, AimlSet> aimlSets) {
        List<AimlCategory> processed = new ArrayList<>();
        categories.forEach((aimlCategory) -> {
            String pattern = aimlCategory.getPattern();
            Pattern regexp = Pattern.compile("<set>(.+?)</set>");
            Matcher matcher = regexp.matcher(pattern);
            if (matcher.find()) {
                String setName = matcher.group(1);
                AimlSet setValues = aimlSets.get(setName + ".txt");
                if (setValues != null) {
                    setValues.forEach((s) -> {
                        String first = matcher.replaceFirst(s);
                        AimlCategory cloned = aimlCategory.clone();
                        cloned.setPattern(first);
                        processed.add(cloned);
                    });
                }
            } else {
                processed.add(aimlCategory);
            }

        });
        return processed;
    }

    @Override
    public void setChannel(TextChannel channel) {
        this.channel = channel;
    }

    @Override
    public TextChannel getChannel() {
        return channel;
    }

    public ChatContextStorage getChatContextStorage() {
        return this.chatContextStorage;
    }

    public void setChatContext(ChatContext chatContext) {
        this.chatContext = chatContext;
    }

    public String getName() {
        return this.name;
    }

    public boolean wakeUp() {
        return this.validate(this.getRootDir()) && this.validate(this.getAimlFolder());
    }

    public String getRespond(String phrase) {
        return this.multisentenceRespond(phrase, this.chatContext);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrainStats() {
        return this.brain.getStat();
    }

    public String multisentenceRespond(String request, ChatContext state) {
        String[] sentences = this.brain.sentenceSplit(request);
        StringBuilder response = new StringBuilder();

        for(String sentence : sentences) {
            response.append(" ").append(this.respond(sentence, state));
        }

        return response.length() == 0 ? "Something is wrong with my brain." : response.toString().trim();
    }

    public String respond(String request, ChatContext state) {
        List<String> stars = new ArrayList<>();
        String pattern = this.brain.match(request, state.topic(), state.that(), stars);
        return this.brain.respond(stars, pattern, state.topic(), state.that(), state.getPredicates());
    }

    private List<AimlCategory> loadAiml() {
        AimlLoader loader = new AimlLoader();
        return loader.loadFiles(this.getAimlFolder());
    }

    private Map<String, AimlSet> loadSets() {
        File sets = new File(this.getSetsFolder());
        if (!sets.exists()) {
            log.warn("Sets not found!");
            return Collections.emptyMap();
        } else {
            File[] files = sets.listFiles();
            if (files != null && files.length != 0) {
                FileLoader<AimlSet> loader = new SetLoader();
                Map<String, AimlSet> data = loader.loadAll(files);
                int count = data.keySet().stream().mapToInt(s -> data.get(s).size()).sum();
                log.info("Loaded {} set records from {} files.", count, files.length);
                return data;
            } else {
                return Collections.emptyMap();
            }
        }
    }

    private Map<String, AimlMap> loadMaps() {
        File maps = new File(this.getMapsFolder());
        if (!maps.exists()) {
            log.warn("Maps not found!");
            return Collections.emptyMap();
        } else {
            File[] files = maps.listFiles();
            if (files != null && files.length != 0) {
                FileLoader<AimlMap> loader = new MapLoader<>();
                Map<String, AimlMap> data = loader.loadAll(files);
                int count = data.keySet().stream().mapToInt((s) -> data.get(s).size()).sum();
                log.info("Loaded " + count + " map records from " + files.length + " files.");
                return data;
            } else {
                return Collections.emptyMap();
            }
        }
    }

    private Map<String, AimlSubstitution> loadSubstitutions() {
        File maps = new File(this.getSubstitutionsFolder());
        if (!maps.exists()) {
            log.warn("Maps not found!");
            return Collections.emptyMap();
        } else {
            File[] files = maps.listFiles();
            if (files != null && files.length != 0) {
                FileLoader<AimlSubstitution> loader = new SubstitutionLoader();
                Map<String, AimlSubstitution> data = loader.loadAll(files);
                int count = data.keySet().stream().mapToInt((s) -> {
                    return ((AimlSubstitution)data.get(s)).size();
                }).sum();
                log.info("Loaded " + count + " substitutions from " + files.length + " files.");
                return data;
            } else {
                return Collections.emptyMap();
            }
        }
    }

    private boolean validate(String folder) {
        if (folder != null && !folder.isEmpty()) {
            Path botsFolder = Paths.get(folder);
            if (Files.notExists(botsFolder, new LinkOption[0])) {
                log.warn("BotImpl folder " + folder + " not found!");
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private String getRootDir() {
        return this.rootDir;
    }

    private String getAimlFolder() {
        return this.getRootDir() + "aiml";
    }

    private String getSubstitutionsFolder() {
        return this.getRootDir() + "substitutions";
    }

    private String getSetsFolder() {
        return this.getRootDir() + "sets";
    }

    private String getMapsFolder() {
        return this.getRootDir() + "maps";
    }

    private String getSkillsFolder() {
        return this.getRootDir() + "skills";
    }
}

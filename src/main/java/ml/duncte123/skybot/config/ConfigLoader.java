package ml.duncte123.skybot.config;

import com.google.gson.*;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;

import java.io.*;

public class ConfigLoader {

    /**
     * This will attempt to load the config and create it if it is not there
     * @param file the file to load
     * @return the loaded config
     * @throws Exception if something goes wrong
     */
    public static Config getConfig(final File file) throws Exception{
        if (!file.exists()) {
            file.createNewFile();
            final FileWriter writer = new FileWriter(file);
            writer.write("{}");
            writer.close();
        }
        return new BlankConfig(file);
    }

    private static class BlankConfig extends Config {

        private final File configFile;

        BlankConfig(final File file) throws Exception {
            super(null, new JsonParser().parse(new FileReader(file)).getAsJsonObject());
            this.configFile = file;
        }

        @Override
        public File getConfigFile()
        {
            return this.configFile;
        }

        @Override
        public void save() throws Exception {
            final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
            final String json = gson.toJson(this.config);
            try {
                final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.configFile), "UTF-8"));
                new UnicodeUnescaper().translate(json, writer);
                writer.close();
            }
            catch (final IOException e)  {
                e.printStackTrace();
            }
        }

    }
}

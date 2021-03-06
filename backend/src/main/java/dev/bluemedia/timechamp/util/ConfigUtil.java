package dev.bluemedia.timechamp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.bluemedia.timechamp.TimechampApplication;
import dev.bluemedia.timechamp.model.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;

/**
 * Util to read and serialize the json configuration file.
 *
 * @author Oliver Traber
 */
public class ConfigUtil {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(ConfigUtil.class.getName());

    /** Global instance of the loaded and deserialized config */
    private static Config config;

    /**
     * Get the current loaded config or load the config if it isn't loaded already.
     * @return The global instance of the current config
     */
    public static Config getConfig() {
        // Return config if it was already loaded, or try to load the config file and deserialize it.
        if (config != null) {
            return config;
        } else {
            // Check if configuration path is set in environmental variable. If not, fall back to default location.
            if (System.getenv("TIMECHAMP_CONFIG") != null) {
                readConfig(System.getenv("TIMECHAMP_CONFIG"));
                return config;
            } else {
                LOG.info("Environmental variable TIMECHAMP_CONFIG is not present. Falling back to default config location.");
                readConfig(getJarPath());
                return config;
            }
        }
    }

    /**
     * Try to deserialize the config file into the config model object
     * @param path Path where the file named config.json is saved
     */
    private static void readConfig(String path) {
        try {
            InputStream configInputStream = new FileInputStream(path + "/config.json");
            config = new ObjectMapper().readValue(configInputStream, Config.class);
        } catch (FileNotFoundException ex) {
            LOG.error("Config file config.json not found in path {}", path, ex);
            System.exit(2);
        } catch (IOException ex) {
            LOG.error("Failed to parse config file config.json in path {}. Please check your config syntax and try again.", path, ex);
            System.exit(3);
        }
    }

    /**
     * Get the path of the JAR file this class is packaged in.
     * @return Path of the current JAR file
     */
    public static String getJarPath() {
        try {
            return new File(TimechampApplication.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParentFile().getPath();
        } catch (URISyntaxException ex) {
            LOG.error("Failed to get location of JAR file.", ex);
        }
        return null;
    }

}

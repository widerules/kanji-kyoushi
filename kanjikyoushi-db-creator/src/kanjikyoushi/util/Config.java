package kanjikyoushi.util;

import java.util.ResourceBundle;

import org.apache.log4j.Logger;

public class Config {

    public static enum Key {
        AUTHENTICATE, BASE_URL, MAX_KANJI_INDEX, PASSWORD, POST_COUNT, RANDOM,
        USER, VERBOSE,
    }

    private static final ResourceBundle config = ResourceBundle
            .getBundle("config");
    private static final Logger logger = Logger.getLogger(Config.class);

    public static String getString(Key _key) {
        try {
            return config.getString(_key.toString());
        } catch (Exception e) {
            logger.error("error getting config string", e);
            return null;
        }
    }

}

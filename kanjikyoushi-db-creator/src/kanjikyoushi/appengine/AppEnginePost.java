package kanjikyoushi.appengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kanjikyoushi.data.VocabularyWord;
import kanjikyoushi.util.Config;
import kanjikyoushi.util.FeedHandler;

import org.apache.log4j.Logger;

public class AppEnginePost {

    static Logger logger = Logger.getLogger(AppEnginePost.class);

    private static Map<String, Integer> createKanjiIndex() {
        Map<String, Integer> kanjiIndex = new HashMap<String, Integer>();

        logger.info("creating kanji index");

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    AppEnginePost.class.getClassLoader()
                            .getResource("kanji.txt").openStream()));

            int index = 1;
            for (String line = in.readLine(); line != null; line = in
                    .readLine()) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    kanjiIndex.put(line, Integer.valueOf(index));
                    // logger.debug(index + ": '" + line + "'");
                    index++;
                }
            }

            in.close();
        } catch (IOException e) {
            logger.error("", e);
            return null;
        }

        return kanjiIndex;
    }

    private static Map<String, VocabularyWord> createVocabularyDictionary()
            throws IOException {
        // load grammar data
        List<String> grammarNotation = getGrammarNotation();

        // create dictionary
        logger.info("creating vocabulary dictionary");
        Map<String, VocabularyWord> dictionary = new HashMap<String, VocabularyWord>();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                AppEnginePost.class.getClassLoader()
                        .getResource("jlpt-vocabulary.txt").openStream()));

        for (String line = in.readLine(); line != null; line = in.readLine()) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                String word = getVocabWord(line);
                String reading = getReading(line);
                List<String> meanings = getMeanings(line, grammarNotation);

                VocabularyWord vocabWord;

                if (dictionary.containsKey(word)) {
                    vocabWord = dictionary.get(word);
                } else {
                    vocabWord = new VocabularyWord(word);
                    dictionary.put(word, vocabWord);
                }

                vocabWord.addReading(reading);
                for (String meaning : meanings) {
                    vocabWord.addMeaning(meaning);
                }

                // logger.debug(String.format("%s [%s] %s", word, reading,
                // meanings));
            }
        }

        in.close();

        return dictionary;
    }

    private static List<String> getGrammarNotation() {
        List<String> notation = new Vector<String>();

        logger.info("getting grammar notation");

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    AppEnginePost.class.getClassLoader()
                            .getResource("grammar.txt").openStream()));

            for (String line = in.readLine(); line != null; line = in
                    .readLine()) {
                line = line.trim();
                if (!line.isEmpty()) {
                    notation.add(line);
                    // logger.debug("grammer: '" + line + "'");
                }
            }
        } catch (IOException e) {
            logger.error("", e);
            return null;
        }

        return notation;
    }

    private static List<String> getInvalidCharacters() throws IOException {
        logger.info("creating invalid character list");

        List<String> invalidCharacters = new Vector<String>();

        BufferedReader in = new BufferedReader(new InputStreamReader(
                AppEnginePost.class.getClassLoader()
                        .getResource("invalidcharacters.txt").openStream()));

        for (String line = in.readLine(); line != null; line = in.readLine()) {
            line = line.trim();
            if (!line.isEmpty()) {
                invalidCharacters.add(line);
            }
        }

        in.close();

        return invalidCharacters;
    }

    private static List<String> getMeanings(String line,
            final List<String> grammarNotation) {
        List<String> meanings = new Vector<String>();

        Pattern meaningPattern = Pattern.compile("/([^/]+)");
        Pattern parenthesisPattern = Pattern.compile("\\((.*?)\\)");

        Matcher m = meaningPattern.matcher(line);
        while (m.find()) {
            String meaning = m.group(1).trim();
            StringBuffer buf = new StringBuffer();

            Matcher parenMatch = parenthesisPattern.matcher(meaning);
            while (parenMatch.find()) {
                boolean isGrammar = true;

                for (String part : parenMatch.group(1).trim().split(",")) {
                    if (!grammarNotation.contains(part)) {
                        isGrammar = false;
                    }
                }

                if (isGrammar) {
                    parenMatch.appendReplacement(buf, "");
                }
            }
            parenMatch.appendTail(buf);

            String cleanMeaning = buf.toString().trim();
            if (!cleanMeaning.isEmpty()) {
                meanings.add(cleanMeaning);
            }
        }

        return meanings;
    }

    private static int getPostCount() {
        int postCount = -1;

        try {
            postCount = Integer.parseInt(Config
                    .getString(Config.Key.POST_COUNT));
        } catch (Exception e) {
            // logger.error("error getting post count from config file", e);
        }

        return postCount;
    }

    private static String getReading(String line) {
        Pattern readingPattern = Pattern.compile("\\[(.*)\\]");
        Matcher m = readingPattern.matcher(line);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    private static String getVocabWord(String line) {
        Pattern wordPattern = Pattern.compile("^\\S+");
        Matcher m = wordPattern.matcher(line);
        if (m.find()) {
            return m.group().trim();
        }
        return null;
    }

    /**
     * checks if VocabularyWord object has a non-null word value and at least
     * one non-null reading and non-null meaning value
     * 
     * @param vocabWord
     * @return true if word meets above criteria
     */
    private static boolean isValidWord(VocabularyWord vocabWord) {
        // check for any null values
        if (vocabWord == null || vocabWord.getWord() == null
                || vocabWord.getReadings() == null
                || vocabWord.getMeanings() == null) {
            return false;
        }

        // check word string
        if (vocabWord.getWord().isEmpty()) {
            return false;
        }

        // check reading values
        boolean validReading = false;
        for (String reading : vocabWord.getReadings()) {
            if (reading != null && !reading.isEmpty()) {
                validReading = true;
                break;
            }
        }

        if (!validReading) {
            return false;
        }

        // check meaning values
        boolean validMeaning = false;
        for (String meaning : vocabWord.getMeanings()) {
            if (meaning != null && !meaning.isEmpty()) {
                validMeaning = true;
                break;
            }
        }

        if (!validMeaning) {
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        try {

            Map<String, VocabularyWord> dictionary = createVocabularyDictionary();
            List<VocabularyWord> vocabularyList = sort(dictionary);
            postVocabularyList(vocabularyList);

        } catch (Exception e) {

            logger.error("", e);

        }
    }

    private static void postVocabularyList(List<VocabularyWord> vocabularyList)
            throws IOException {
        int postCount = getPostCount();

        FeedHandler poster;

        String vocabUrl = Config.getString(Config.Key.BASE_URL) + "vocab";
        if (Boolean.parseBoolean(Config.getString(Config.Key.AUTHENTICATE))) {
            poster = new FeedHandler(vocabUrl,
                    Config.getString(Config.Key.USER),
                    Config.getString(Config.Key.PASSWORD));
        } else {
            poster = new FeedHandler(vocabUrl);
        }
        poster.setVerbose(Boolean.parseBoolean(Config
                .getString(Config.Key.VERBOSE)));

        int wordCount = 0;
        for (VocabularyWord vocabWord : vocabularyList) {

            poster.clearData();
            poster.addData("action", "add");
            poster.addData("word", vocabWord.getWord());
            poster.addData("reading",
                    vocabWord.getReadings().toArray(new String[] {}));
            poster.addData("meaning",
                    vocabWord.getMeanings().toArray(new String[] {}));

            logger.debug("posting '" + vocabWord.getWord() + "'");
            poster.doPost();

            if (postCount > -1 && ++wordCount >= postCount) {
                return;
            }
        }
    }

    private static List<VocabularyWord> sort(
            Map<String, VocabularyWord> dictionary) throws IOException {
        List<VocabularyWord> sortedList = new Vector<VocabularyWord>();
        List<String> unknownKanji = new Vector<String>();

        int maxKanjiIndex = -1;

        try {
            maxKanjiIndex = Integer.parseInt(Config
                    .getString(Config.Key.MAX_KANJI_INDEX));
        } catch (NumberFormatException e) {
            logger.error("", e);
        }

        // create kanji index
        Map<String, Integer> kanjiIndex = createKanjiIndex();
        List<String> invalidCharacters = getInvalidCharacters();

        // view dictionary
        for (String word : dictionary.keySet()) {
            // VocabularyWord vocabWord = dictionary.get(word);
            int index = -1;

            for (String kanjiChar : word.split("")) {
                if (!kanjiChar.isEmpty()
                        && kanjiChar
                                .matches("["
                                        + "\\p{InCJK_COMPATIBILITY}"
                                        + "\\p{InCJK_COMPATIBILITY_FORMS}"
                                        + "\\p{InCJK_COMPATIBILITY_IDEOGRAPHS}"
                                        + "\\p{InCJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT}"
                                        + "\\p{InCJK_RADICALS_SUPPLEMENT}"
                                        + "\\p{InCJK_SYMBOLS_AND_PUNCTUATION}"
                                        + "\\p{InCJK_UNIFIED_IDEOGRAPHS}"
                                        + "\\p{InCJK_UNIFIED_IDEOGRAPHS_EXTENSION_A}"
                                        + "\\p{InCJK_UNIFIED_IDEOGRAPHS_EXTENSION_B}"
                                        + "]")) {

                    if (kanjiIndex.containsKey(kanjiChar)) {
                        index = Math.max(index, kanjiIndex.get(kanjiChar)
                                .intValue());
                    } else if (!unknownKanji.contains(kanjiChar)) {
                        unknownKanji.add(kanjiChar);
                    }
                } else if (invalidCharacters.contains(kanjiChar)) {
                    index = Integer.MAX_VALUE;
                }
            }

            if (index == -1) {
                logger.debug("no index for " + word + ", ignoring");
            } else if (maxKanjiIndex > 0 && index > maxKanjiIndex) {
                logger.debug("index for " + word + " is too high, ignoring");
            } else if (index != Integer.MAX_VALUE
                    && isValidWord(dictionary.get(word))) {
                sortedList.add(dictionary.get(word));
            }
        }

        if (!unknownKanji.isEmpty()) {
            logger.debug(unknownKanji.size() + " unknown kanji\n"
                    + unknownKanji);
        }

        return sortedList;
    }
}

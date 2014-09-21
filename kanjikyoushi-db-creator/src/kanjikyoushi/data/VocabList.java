package kanjikyoushi.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kanjikyoushi.appengine.AppEnginePost;

import org.apache.log4j.Logger;

public class VocabList implements Iterable<VocabularyWord> {

    private static final String GRAMMAR_FILE = "grammar.txt";
    private static final String INVALID_CHARACTER_FILE = "invalidcharacters.txt";
    private static final Logger logger = Logger.getLogger(VocabList.class);
    private static VocabList vocabList = null;
    private static final String VOCABULARY_FILE = "jlpt-vocabulary.txt";

    private static List<String> getGrammarNotation() {
        List<String> notation = new Vector<String>();

        logger.info("getting grammar notation");

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    VocabList.class.getClassLoader().getResource(GRAMMAR_FILE)
                            .openStream()));

            for (String line = in.readLine(); line != null; line = in
                    .readLine()) {
                line = line.trim();
                if (!line.isEmpty())
                    notation.add(line);
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
                        .getResource(INVALID_CHARACTER_FILE).openStream()));

        for (String line = in.readLine(); line != null; line = in.readLine()) {
            line = line.trim();
            if (!line.isEmpty()) {
                invalidCharacters.add(line);
            }
        }

        in.close();

        return invalidCharacters;
    }

    public static VocabList getList() throws IOException {
        if (vocabList == null) {
            vocabList = new VocabList();
            // KanjiList kanjiList = KanjiList.getList();

            // load grammar data
            List<String> grammarNotation = getGrammarNotation();
            List<String> invalidCharacters = getInvalidCharacters();

            // create dictionary
            logger.info("creating vocabulary dictionary");

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    VocabList.class.getClassLoader()
                            .getResource(VOCABULARY_FILE).openStream()));

            VOCAB: for (String line = in.readLine(); line != null; line = in
                    .readLine()) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                String word = getVocabWord(line);

                for (String kanjiChar : word.split("")) {
                    if (invalidCharacters.contains(kanjiChar))
                        continue VOCAB;
                }

                VocabularyWord vocabWord;
                if (vocabList.contains(word)) {
                    vocabWord = vocabList.get(word);
                } else {
                    vocabWord = new VocabularyWord(word);
                    vocabList.put(word, vocabWord);
                }

                vocabWord.addReading(getReading(line));
                vocabWord.addMeanings(getMeanings(line, grammarNotation));
                vocabWord.addRelatedKanji(getRelatedKanji(word));

            }

            in.close();
        }

        return vocabList;
    }

    private static Collection<String> getRelatedKanji(String word) {
        Collection<String> relatedKanji = new Vector<String>();

        for (String kanjiChar : word.split("")) {
            if (kanjiChar.isEmpty()
                    || !kanjiChar
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
                                    + "]"))
                continue;

            if (KanjiList.getList().contains(kanjiChar))
                relatedKanji.add(kanjiChar);

            else
                logger.warn(String.format("'%s' not found in kanji list",
                        kanjiChar));

        }

        return relatedKanji;
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

    private final Map<String, VocabularyWord> dictionary = new HashMap<String, VocabularyWord>();

    private boolean contains(String word) {
        return dictionary.containsKey(word);
    }

    private VocabularyWord get(String word) {
        return dictionary.get(word);
    }

    @Override
    public Iterator<VocabularyWord> iterator() {
        return dictionary.values().iterator();
    }

    private VocabularyWord put(String word, VocabularyWord vocabWord) {
        return dictionary.put(word, vocabWord);
    }

    public int size() {
        return dictionary.size();
    }

}

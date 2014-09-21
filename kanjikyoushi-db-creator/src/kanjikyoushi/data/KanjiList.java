package kanjikyoushi.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kanjikyoushi.appengine.AppEnginePost;

import org.apache.log4j.Logger;

public class KanjiList {

    private static final String KANJI_FILE = "kanji.txt";
    private static KanjiList kanjiList = null;

    private static final Logger logger = Logger.getLogger(KanjiList.class);

    public static KanjiList getList() {

        if (kanjiList == null) {
            logger.info("creating kanji index");

            kanjiList = new KanjiList();

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        AppEnginePost.class.getClassLoader()
                                .getResource(KANJI_FILE).openStream()));

                int index = 1;
                for (String line = in.readLine(); line != null; line = in
                        .readLine()) {

                    String kanji = line.trim();
                    if (!kanji.isEmpty() && !kanji.startsWith("#"))
                        kanjiList.add(line, index++);

                }

                in.close();
            } catch (IOException e) {

                logger.error("", e);
                return null;

            }
        }

        return kanjiList;
    }

    private final Map<Integer, String> indexMap = new HashMap<Integer, String>();
    private final Map<String, Integer> kanjiMap = new HashMap<String, Integer>();

    private void add(String _kanji, int _index) {
        this.indexMap.put(_index, _kanji);
        this.kanjiMap.put(_kanji, _index);
    }

    public boolean contains(String kanji) {
        return kanjiMap.containsKey(kanji);
    }

    public int getIndex(String _kanji) {
        return kanjiMap.get(_kanji);
    }

    public String getKanji(int _index) {
        return indexMap.get(_index);
    }

    public List<Integer> getSortedIndices() {
        List<Integer> indices = new ArrayList<Integer>(indexMap.keySet());
        Collections.sort(indices);
        return indices;
    }

    public int size() {
        return kanjiMap.size();
    }
}

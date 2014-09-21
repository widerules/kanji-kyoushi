package kanjikyoushi.menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import kanjikyoushi.data.KanjiList;
import kanjikyoushi.data.VocabList;
import kanjikyoushi.data.VocabularyWord;
import kanjikyoushi.util.Config;
import kanjikyoushi.util.FeedHandler;
import kanjikyoushi.util.Utilities;

import org.apache.log4j.Logger;

public class UploadMenu implements Runnable {

    private enum MenuOption {
        get_kanji_feed, get_vocab_feed, post_all, post_kanji, post_vocab, quit,
        show_kanji_list, show_vocab_list
    }

    private static final BufferedReader in = new BufferedReader(
            new InputStreamReader(System.in));
    private static final Logger logger = Logger.getLogger(UploadMenu.class);

    public static void main(String[] args) {
        try {

            Thread t = new Thread(new UploadMenu());
            t.start();
            t.join();

        } catch (Exception e) {
            logger.error("", e);
        }
    }

    private boolean stop = false;

    public UploadMenu() {
        // TODO Auto-generated constructor stub
    }

    private void getKanjiFeed() throws IOException {

        FeedHandler feed = new FeedHandler(
                Config.getString(Config.Key.BASE_URL) + "kanjifeed");
        feed.setVerbose(true);
        feed.doGet();

    }

    private MenuOption getOption() throws IOException {

        MenuOption selectedOption = null;

        MenuOption[] options = MenuOption.values();
        for (int i = 0; i < options.length; i++) {
            println(String.format("%2d - %s", i, options[i]));
        }

        while (selectedOption == null) {
            String response = getUserInput("enter selection (q to quit)");

            if (response.equalsIgnoreCase("quit")
                    || response.equalsIgnoreCase("q")) {

                selectedOption = MenuOption.quit;

            } else {

                try {

                    int selectionNumber = Integer.parseInt(response);
                    selectedOption = options[selectionNumber];

                } catch (ArrayIndexOutOfBoundsException e) {
                } catch (NumberFormatException e) {
                }

            }

            if (selectedOption == null)
                println("invaled selection '" + response + "'");
        }

        return selectedOption;
    }

    private String getUserInput(String _prompt) {

        return getUserInput(_prompt, null);

    }

    private String getUserInput(String _prompt, String _default) {
        String response = "";

        print(String.format("%s%s: ", _prompt, _default == null ? "" : " ("
                + _default + ")"));
        try {
            response = in.readLine().trim();
        } catch (IOException e) {
        }

        return _default != null && response.isEmpty() ? _default : response;
    }

    private void getVocabFeed() throws IOException {

        FeedHandler feed = new FeedHandler(
                Config.getString(Config.Key.BASE_URL) + "vocabfeed");
        feed.setVerbose(true);
        feed.doGet();

    }

    private void postAll() throws IOException {

        FeedHandler kanjiHandler = new FeedHandler(
                Config.getString(Config.Key.BASE_URL) + "kanjifeed");
        FeedHandler vocabHandler = new FeedHandler(
                Config.getString(Config.Key.BASE_URL) + "vocabfeed");

        KanjiList kanjiList = KanjiList.getList();
        VocabList vocabList = VocabList.getList();

        int startingIndex = Integer
                .parseInt(getUserInput("starting index", "1"));
        int endingIndex = Integer.parseInt(getUserInput("ending index",
                String.valueOf(kanjiList.size())));

        for (int index : kanjiList.getSortedIndices()) {

            if (index < startingIndex || index > endingIndex)
                continue;

            kanjiHandler.clearData();

            kanjiHandler.addData("index", String.valueOf(index));
            kanjiHandler.addData("kanji", kanjiList.getKanji(index));

            kanjiHandler.setVerbose(true);
            kanjiHandler.doPost();

        }

        for (VocabularyWord word : vocabList) {

            int maxIndex = 0;
            for (String kanji : word.getRelatedKanji()) {
                maxIndex = Math.max(maxIndex, kanjiList.getIndex(kanji));
            }

            if (maxIndex < startingIndex || maxIndex > endingIndex)
                continue;

            vocabHandler.clearData();

            vocabHandler.addData("word", word.getWord());
            vocabHandler.addData("reading", word.getReadings());
            vocabHandler.addData("meaning", word.getMeanings());
            vocabHandler.addData("kanji", word.getRelatedKanji());

            vocabHandler.setVerbose(true);
            vocabHandler.doPost();

        }

    }

    private void postKanji() throws IOException {

        FeedHandler handler = new FeedHandler(
                Config.getString(Config.Key.BASE_URL) + "kanjifeed");

        KanjiList kanjiList = KanjiList.getList();

        int startingIndex = Integer
                .parseInt(getUserInput("starting index", "1"));
        int endingIndex = Integer.parseInt(getUserInput("ending index",
                String.valueOf(kanjiList.size())));

        for (int index : kanjiList.getSortedIndices()) {

            if (index < startingIndex || index > endingIndex)
                continue;

            handler.clearData();

            handler.addData("index", String.valueOf(index));
            handler.addData("kanji", kanjiList.getKanji(index));

            handler.setVerbose(true);
            handler.doPost();

        }

    }

    private void postVocab() throws IOException {
        FeedHandler handler = new FeedHandler(
                Config.getString(Config.Key.BASE_URL) + "vocabfeed");

        VocabList vocabList = VocabList.getList();
        KanjiList kanjiList = KanjiList.getList();

        int startingIndex = Integer.parseInt(getUserInput(
                "starting kanji index", "1"));
        int endingIndex = Integer.parseInt(getUserInput("ending kanji index",
                String.valueOf(kanjiList.size())));

        for (VocabularyWord word : vocabList) {

            int maxIndex = 0;
            for (String kanji : word.getRelatedKanji()) {
                maxIndex = Math.max(maxIndex, kanjiList.getIndex(kanji));
            }

            if (maxIndex < startingIndex || maxIndex > endingIndex)
                continue;

            handler.clearData();

            handler.addData("word", word.getWord());
            handler.addData("reading", word.getReadings());
            handler.addData("meaning", word.getMeanings());
            handler.addData("kanji", word.getRelatedKanji());

            handler.setVerbose(true);
            handler.doPost();

        }
    }

    private void print(String _message) {
        System.out.print(_message == null ? "" : _message);
    }

    private void println(String _message) {
        System.out.println(_message == null ? "" : _message);
    }

    @Override
    public void run() {

        while (!stop) {

            try {

                MenuOption option = getOption();
                println(option + " selected");

                switch (option) {

                    case get_kanji_feed:
                        getKanjiFeed();
                        break;
                    case get_vocab_feed:
                        getVocabFeed();
                        break;
                    case post_kanji:
                        postKanji();
                        break;
                    case post_vocab:
                        postVocab();
                        break;
                    case post_all:
                        postAll();
                        break;
                    case show_kanji_list:
                        showKanjiList();
                        break;
                    case show_vocab_list:
                        showVocabList();
                        break;
                    case quit:
                        stop = true;
                        break;

                    default:
                        logger.info("unhandled option: " + option);

                }

            } catch (Exception e) {

                logger.error("", e);

            }

        }

    }

    private void showKanjiList() {
        KanjiList kanjiList = KanjiList.getList();
        List<Integer> indices = kanjiList.getSortedIndices();

        for (int index : indices) {
            println(String.format("(%4d) %s", index, kanjiList.getKanji(index)));
        }
    }

    private void showVocabList() throws IOException {
        for (VocabularyWord word : VocabList.getList()) {

            println(String.format("%s [%s] %s (kanji: %s)", word.getWord(),
                    Utilities.join(";", word.getReadings()),
                    Utilities.join(";", word.getMeanings()),
                    Utilities.join(";", word.getRelatedKanji())));

        }
    }

}

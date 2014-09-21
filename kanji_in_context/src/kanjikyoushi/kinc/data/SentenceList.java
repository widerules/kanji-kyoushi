package kanjikyoushi.kinc.data;

import java.util.ArrayList;

public class SentenceList {

    private ArrayList<Sentence> sentences = new ArrayList<Sentence>();

    public void addSentence(Sentence sentence) {
        sentences.add(sentence);
    }

    public ArrayList<Sentence> getSentences() {
        return sentences;
    }

}

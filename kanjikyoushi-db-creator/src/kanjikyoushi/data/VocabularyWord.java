package kanjikyoushi.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VocabularyWord {

    private boolean active = true;
    private final Set<String> meanings = new HashSet<String>();
    private final Set<String> readings = new HashSet<String>();
    private final Set<String> relatedKanji = new HashSet<String>();
    private String word;

    public VocabularyWord(String word) {
        this.word = word;
    }

    public void addMeaning(String meaning) {
        this.meanings.add(meaning);
    }

    public void addMeanings(List<String> meanings) {
        this.meanings.addAll(meanings);
    }

    public void addReading(String reading) {
        this.readings.add(reading);
    }

    public void addReadings(List<String> readings) {
        this.readings.addAll(readings);
    }

    public void addRelatedKanji(Collection<String> kanji) {
        this.relatedKanji.addAll(kanji);
    }

    public void addRelatedKanji(String kanji) {
        this.relatedKanji.add(kanji);
    }

    public Set<String> getMeanings() {
        return meanings;
    }

    public Set<String> getReadings() {
        return readings;
    }

    public Set<String> getRelatedKanji() {
        return relatedKanji;
    }

    public String getWord() {
        return word;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}

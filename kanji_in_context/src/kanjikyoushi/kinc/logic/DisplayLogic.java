package kanjikyoushi.kinc.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kanjikyoushi.kinc.data.Question;
import kanjikyoushi.kinc.data.Sentence;

public class DisplayLogic {

    // private static final Logger logger =
    // Logger.getLogger(DisplayLogic.class);
    private static final Pattern sentencePattern = Pattern.compile("<(.*?)>");
    private static final Pattern wordPattern = Pattern
            .compile("(.*?)\\{(.*?)}");
    private static final Pattern quizPattern = Pattern
            .compile("\\((.*?):(.*?)\\)");

    public static Sentence convertSentence(String line) {
        Sentence sentence = new Sentence();

        Matcher m = sentencePattern.matcher(line);

        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String word = convertWord(m.group(1));
            m.appendReplacement(sb, Matcher.quoteReplacement(word));
        }
        m.appendTail(sb);

        m = quizPattern.matcher(sb);
        while (m.find()) {
            String question = m.group(1);// (一人一人:ひとりひとり)
            String answer = m.group(2);

            sentence.addQuestion(new Question(0, question, answer));

            sb.replace(m.start(), m.end(), "");

            m = quizPattern.matcher(sb);
        }

        sentence.setText(sb.toString());

        return sentence;
    }

    /**
     * convert word from internal format to ruby format
     */
    private static String convertWord(String word) {
        List<String> kanji = new ArrayList<String>();
        List<String> furigana = new ArrayList<String>();

        Matcher m = wordPattern.matcher(word);
        while (m.find()) {
            kanji.add(m.group(1));
            furigana.add(m.group(2));
        }

        StringBuffer sb = new StringBuffer();

        sb.append("<ruby xml:lang=\"ja\">");

        sb.append("<rbc>");
        for (String k : kanji)
            sb.append("<rb>" + k + "</rb>");
        sb.append("</rbc>");

        sb.append("<rtc>");
        for (String f : furigana)
            sb.append("<rt>" + f + "</rt>");
        sb.append("</rtc>");

        sb.append("</ruby>");

        return sb.toString();
    }
}

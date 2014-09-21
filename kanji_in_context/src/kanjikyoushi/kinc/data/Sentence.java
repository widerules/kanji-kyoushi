package kanjikyoushi.kinc.data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sentence {

    private static final Pattern quizPattern =
        Pattern.compile("\\((.*?):(.*?)\\)");
    private static final Pattern sentencePattern = Pattern.compile("<(.*?)>");
    private static final Pattern wordPattern =
        Pattern.compile("(.*?)\\{(.*?)}");

    public static String convertSentenceText(String line) {
        Matcher m = sentencePattern.matcher(line);

        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String word = convertWord(m.group(1));
            m.appendReplacement(sb, Matcher.quoteReplacement(word));
        }
        m.appendTail(sb);

        m = quizPattern.matcher(sb);
        while (m.find()) {
            sb.replace(m.start(), m.end(), "");
            m = quizPattern.matcher(sb);
        }

        return sb.toString();
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

    public static Sentence createSentence(String line) {
        Sentence sentence = new Sentence();

        String sentenceText = line;

        Matcher m = quizPattern.matcher(sentenceText);
        while (m.find()) {
            String question = m.group(1);
            String answer = m.group(2);

            sentence.addQuestion(new Question(0, question, answer));

            sentenceText = sentenceText.replace(m.group(), "");

            m = quizPattern.matcher(sentenceText);
        }

        sentence.setText(sentenceText);

        return sentence;
    }

    // private static final Logger logger = Logger.getLogger(Sentence.class);

    private int lesson;
    private final ArrayList<Question> questions = new ArrayList<Question>();
    private int sentenceId;
    private String text;
    private int workbook;

    public Sentence() {
    }

    public Sentence(int sentenceId, String text) {
        this.sentenceId = sentenceId;
        this.text = text;
    }

    public void addQuestion(Question question) {
        if (question != null)
            questions.add(question);
    }

    public String getHtmlText() {
        String htmlText = convertSentenceText(text);

        boolean lastQuestion = false;
        int questionCount = 0;

        for (Question question : questions) {
            lastQuestion = ++questionCount == questions.size();

            String inputTag =
                String.format("<div><input " + "id=\"question_input_%d\" "
                    + "class=\"question_input\" "
                    + "type=\"text\" size=\"%d\" "
                    + "onchange=\"checkQuestion('%d', '%s', '%s')\" /></div>",
                    question.getQuestionId(),
                    (int) (1.5 * question.getAnswer().length()),
                    question.getQuestionId(), question.getAnswer(),
                    lastQuestion ? "true" : "false");

            String replacementText =
                String.format(
                    "<span id=\"question_text_%d\" class=\"question_text\">"
                        + "<ruby><rb>%s</rb><rt>%s</rt></ruby></span>",
                    question.getQuestionId(), question.getQuestion(), inputTag);

            htmlText =
                htmlText.replaceAll(question.getQuestion(), replacementText);
        }

        return htmlText;
    }

    public int getLesson() {
        return lesson;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public int getSentenceId() {
        return sentenceId;
    }

    public String getText() {
        return text;
    }

    public int getWorkbook() {
        return workbook;
    }

    public void setLesson(int lesson) {
        this.lesson = lesson;
    }

    public void setSentenceId(int sentenceId) {
        this.sentenceId = sentenceId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setWorkbook(int workbook) {
        this.workbook = workbook;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("[id] " + sentenceId);
        sb.append(" [text] '" + text + "'");
        sb.append(" [wb:lesson] " + workbook + ":" + lesson);
        if (!questions.isEmpty()) {
            sb.append(" [questions] ");
            for (Question q : questions) {
                sb.append(String.format("(%d) '%s':'%s' ", q.getQuestionId(),
                    q.getQuestion(), q.getAnswer()));
            }
        }

        return sb.toString();
    }

}

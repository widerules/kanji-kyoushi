package android.kanjikyoushi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class VocabQuiz extends Activity implements VocabFeedRequestor {

    private static final String[] hiragana = { "あ", "い", "う", "え", "お", "か", "き",
            "く", "け", "こ", "さ", "し", "す", "せ", "そ", "た", "ち", "つ", "て", "と", "な",
            "に", "ぬ", "ね", "の", "は", "ひ", "ふ", "へ", "ほ", "ま", "み", "む", "め", "も",
            "ら", "り", "る", "れ", "ろ", "よ", "ゆ", "よ", "わ", "が", "ぎ", "ぐ", "げ", "ご",
            "ざ", "じ", "ず", "ぜ", "ぞ", "だ", "ぢ", "づ", "で", "ど", "ば", "び", "ぶ", "べ",
            "ぼ", "ぱ", "ぴ", "ぷ", "ぺ", "ぽ", "きゃ", "きゅ", "きょ", "ぎゃ", "ぎゅ", "ぎょ", "しゃ",
            "しゅ", "しょ", "ちゃ", "ちゅ", "ちょ", "にゃ", "にゅ", "にょ", "ひゃ", "ひゅ", "ひょ", "みゃ",
            "みゅ", "みょ", "りゃ", "りゅ", "りょ" };
    private static final Collection<String> littleHiragana = Arrays.asList("ゃ", "ゅ",
            "ょ");

    private static final int MENU_DATA_ID = Menu.FIRST;
    private static final int MENU_SKIP_ID = Menu.FIRST + 1;
    private static final int MENU_PREV_ID = Menu.FIRST + 2;
    private static final int MENU_WORD_ID = Menu.FIRST + 3;
    private static final int MENU_SETTINGS_ID = Menu.FIRST + 4;

    private static final int mWrongAnswerCount = 3;

    private static final int RESULT_CORRECT = 0;
    private static final int RESULT_INCORRECT = 1;
    private static final int RESULT_VIEWED = 2;

    private static String getPermutation(String _s) {
        List<String> stringArray = new ArrayList<String>();
        stringArray.addAll(Common.cleanArray(_s.split("")));

        String permutation;

        int newLength;
        if (stringArray.size() < 3) {
            newLength = 1 + mRand.nextInt(2);
        } else {
            newLength = stringArray.size() - 1 + mRand.nextInt(2);
        }

        do {
            permutation = "";

            if (newLength < stringArray.size()) {
                stringArray.remove(mRand.nextInt(stringArray.size()));
            } else if (newLength > stringArray.size()) {
                stringArray.add(mRand.nextInt(stringArray.size() + 1),
                        hiragana[mRand.nextInt(hiragana.length)]);
            }

            for (int i = 0; i < stringArray.size(); i++) {
                permutation += mRand.nextBoolean() ? hiragana[mRand
                    .nextInt(hiragana.length)] : stringArray.get(i);
            }
        } while (permutation.equals(_s));

        return permutation;
    }

    private static boolean isKanji(final String _character) {
        return _character.matches("[" + "\\p{InCJK_COMPATIBILITY}"
                + "\\p{InCJK_COMPATIBILITY_FORMS}"
                + "\\p{InCJK_COMPATIBILITY_IDEOGRAPHS}"
                + "\\p{InCJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT}"
                + "\\p{InCJK_RADICALS_SUPPLEMENT}"
                + "\\p{InCJK_SYMBOLS_AND_PUNCTUATION}"
                + "\\p{InCJK_UNIFIED_IDEOGRAPHS}"
                + "\\p{InCJK_UNIFIED_IDEOGRAPHS_EXTENSION_A}"
                + "\\p{InCJK_UNIFIED_IDEOGRAPHS_EXTENSION_B}" + "]");
    }

    private boolean mIsReadingFeed = false;
    private Thread mFeedThread;

    private Cursor mMeaningsCursor;
    private Cursor mVocabBatch;
    private Long mWordId;
    private Long mPreviousWordId = -1L;
    private int mRightAnswerIndex;

    private int mStrength;
    private List<String> mRealAnswers;
    private VocabularyDbAdapter mDbConnector;
    private ListView mQuizOptionList;
    private LinearLayout mStrengthView;

    private TextView mLastSeenView;

    private TextView mWordView;

    private SharedPreferences mSettings;

    private static final Random mRand = new Random();

    private void checkForEmptyDb() {
        if (mDbConnector.isEmptyDatabase()) {
            Log.d(Common.LOG_TAG, "retrieving initial vocabulary data");
            if (mFeedThread == null || !mFeedThread.isAlive()) {
                mFeedThread = new Thread(new VocabFeedReader(this,
                        VocabFeedReader.ACTION_FIRST));
                mIsReadingFeed = true;
                mFeedThread.start();
            }
        }
    }

    public void doneFeedLoading() {
        if (mIsReadingFeed) {
            mIsReadingFeed = false;
            loadRandomWord();
        }
    }

    public Context getContext() {
        return this;
    }

    private List<String> getRealMeanings(Long rowId) {
        List<String> meanings = new ArrayList<String>();
        Cursor meaningsCursor = mDbConnector.fetchMeanings(rowId);
        startManagingCursor(meaningsCursor);
        meaningsCursor.moveToFirst();

        while (!meaningsCursor.isAfterLast()) {
            meanings.add(meaningsCursor.getString(meaningsCursor
                .getColumnIndex(VocabularyDbAdapter.KEY_MEANING)));
            meaningsCursor.moveToNext();
        }

        return meanings;
    }

    private List<String> getRealReadings(Long rowId) {
        List<String> readings = new ArrayList<String>();
        Cursor readingsCursor = mDbConnector.fetchReadings(rowId);
        startManagingCursor(readingsCursor);
        readingsCursor.moveToFirst();

        while (!readingsCursor.isAfterLast()) {
            readings.add(readingsCursor.getString(readingsCursor
                .getColumnIndex(VocabularyDbAdapter.KEY_READING)));
            readingsCursor.moveToNext();
        }

        return readings;
    }

    private Collection<String> getWrongMeanings() {
        Collection<String> wrongMeanings = new ArrayList<String>();

        while (wrongMeanings.size() < mWrongAnswerCount) {
            if (mMeaningsCursor == null || mMeaningsCursor.isAfterLast()) {
                Log.i(Common.LOG_TAG, "getting random meanings");

                mMeaningsCursor = mDbConnector.fetchRandomMeanings(mSettings.getInt(
                        Common.SETTING_BATCH_SIZE, Common.DEFAULT_BATCH_SIZE));
                startManagingCursor(mMeaningsCursor);

                if (!mMeaningsCursor.moveToFirst()) {
                    Log.e(Common.LOG_TAG,
                            "failed to move to first on meanings cursor");
                }
            }

            int colIndex = mMeaningsCursor
                .getColumnIndex(VocabularyDbAdapter.KEY_MEANING);
            String wrongMeaning = mMeaningsCursor.getString(colIndex);

            boolean noMatch = true;
            for (String realMeaning : mRealAnswers) {
                if (realMeaning.equals(wrongMeaning)) {
                    noMatch = false;
                }
            }

            if (noMatch) {
                wrongMeanings.add(wrongMeaning);
            }

            mMeaningsCursor.moveToNext();
        }

        return wrongMeanings;
    }

    private Collection<String> getWrongReadings() {

        String currWord = mVocabBatch.getString(mVocabBatch
            .getColumnIndexOrThrow(VocabularyDbAdapter.KEY_WORD));
        String realReading = mRealAnswers.get(mRightAnswerIndex);

        // vocab word as list of characters
        List<String> vocabIndex = new ArrayList<String>();

        // separate the characters of the vocabulary word by tossing null or
        // empty elements and combining little hiragana with the preceding
        // character
        int index = 0;
        for (String element : currWord.split("")) {
            if (element == null || element.trim().length() == 0) {
                continue;
            }
            if (littleHiragana.contains(element) && index > 0) {
                vocabIndex.set(index - 1, vocabIndex.get(index - 1) + element);
            } else {
                vocabIndex.add(index++, element);
            }
        }

        int wordSize = vocabIndex.size();
        Integer[] regexIndex = new Integer[wordSize];
        String[] readingTemplate = new String[wordSize];

        // create regex string and regex index, which hold the regex group
        // number for each corresponding kanji in the vocab word character array
        String regex = "";
        int currIndex = 1;
        for (int i = 0; i < wordSize; i++) {
            if (isKanji(vocabIndex.get(i))) {
                regex += "([^んっゃゅょ].*)";
                regexIndex[i] = currIndex++;
            } else {
                regex += vocabIndex.get(i);
                regexIndex[i] = null;
            }
        }

        // match the regex pattern to the reading and parse the results to the
        // reading template
        Matcher m = Pattern.compile(regex).matcher(realReading);
        if (m.matches()) {
            for (int i = 0; i < readingTemplate.length; i++) {
                if (regexIndex[i] == null) {
                    readingTemplate[i] = null;
                } else {
                    readingTemplate[i] = m.group(regexIndex[i]);
                }
            }
        } else {
            return null;
        }

        // generate readings by randomly permutating the kanji readings while
        // leaving the hiragana intact
        Collection<String> generatedReadings = new ArrayList<String>();
        while (generatedReadings.size() < mWrongAnswerCount) {
            String newReading = "";
            for (int i = 0; i < wordSize; i++) {
                if (readingTemplate[i] == null) {
                    newReading += vocabIndex.get(i);
                } else {
                    newReading += mRand.nextBoolean()
                        ? getPermutation(readingTemplate[i]) : readingTemplate[i];
                }
            }

            if (!newReading.equals(realReading)
                    && !generatedReadings.contains(newReading)
                    && !newReading.startsWith("ん")) {
                generatedReadings.add(newReading);
            }
        }

        return generatedReadings;
    }

    private void loadOptions() {
        ArrayList<String> quizOptions = new ArrayList<String>();

        int quizType = mSettings.getInt(Common.SETTING_QUIZ_TYPE,
                Common.DEFAULT_QUIZ_TYPE);

        // load either readings or meanings as answer options
        boolean isReading = Common.QUIZ_TYPE_READINGS == quizType
                || (Common.QUIZ_TYPE_BOTH == quizType && mRand.nextBoolean());

        if (isReading) {
            // get real readings and add a correct answer to quiz options
            mRealAnswers = getRealReadings(mWordId);
            mRightAnswerIndex = mRand.nextInt(mRealAnswers.size());
            quizOptions.add(mRealAnswers.get(mRightAnswerIndex));

            // add wrong answers
            quizOptions.addAll(getWrongReadings());
        } else {
            // get real meanings and add a correct answer to quiz options
            mRealAnswers = getRealMeanings(mWordId);
            mRightAnswerIndex = mRand.nextInt(mRealAnswers.size());
            quizOptions.add(mRealAnswers.get(mRightAnswerIndex));

            // add wrong answers
            quizOptions.addAll(getWrongMeanings());
        }

        // randomize the options list
        Collections.shuffle(quizOptions, mRand);

        mQuizOptionList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.answer_row, quizOptions));
    }

    private void loadRandomWord() {
        try {
            if (mIsReadingFeed) {
                return;
            }

            Log.d(Common.LOG_TAG, "loading random word");

            if (mVocabBatch == null || mVocabBatch.isAfterLast()) {
                Log.d(Common.LOG_TAG,
                        "no vocab batch, fetching new set of quiz words");
                mVocabBatch = mDbConnector.fetchRandomWords(mSettings.getInt(
                        Common.SETTING_BATCH_SIZE, Common.DEFAULT_BATCH_SIZE));
                startManagingCursor(mVocabBatch);

                mVocabBatch.moveToFirst();
            }

            mPreviousWordId = mWordId;

            mWordId = mVocabBatch.getLong(mVocabBatch
                .getColumnIndex(VocabularyDbAdapter.KEY_ROWID));
            mWordView.setText(mVocabBatch.getString(mVocabBatch
                .getColumnIndexOrThrow(VocabularyDbAdapter.KEY_WORD)));

            mStrength = mVocabBatch.getInt(mVocabBatch
                .getColumnIndex(VocabularyDbAdapter.KEY_STRENGTH));
            mStrengthView.removeAllViews();
            for (int i = 0; i < mStrength; i++) {
                ImageView strengthImage = new ImageView(this);
                strengthImage.setImageResource(R.raw.star);
                mStrengthView.addView(strengthImage);
            }

            String lastSeen = mVocabBatch.getString(mVocabBatch
                .getColumnIndex(VocabularyDbAdapter.KEY_LASTSEEN));
            mLastSeenView.setText(Common.dateStringFromSQLTimestamp(this, lastSeen));

            loadOptions();

            mVocabBatch.moveToNext();
        } catch (Exception e) {
            Log.e(Common.LOG_TAG, e.toString());
            e.printStackTrace();
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = getSharedPreferences(Common.SETTINGS, Context.MODE_PRIVATE);
        // mSettings.edit().clear().commit();

        mDbConnector = new VocabularyDbAdapter(this);
        checkForEmptyDb();

        setContentView(R.layout.vocab_quiz);

        mWordView = (TextView) findViewById(R.id.quizkanji);
        mStrengthView = (LinearLayout) findViewById(R.id.quizstrength);
        mLastSeenView = (TextView) findViewById(R.id.quizlastseen);
        mQuizOptionList = (ListView) findViewById(R.id.answerlist);

        // create listener for viewing kanji data
        mWordView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Long wordId = mWordId;
                recordResult(RESULT_VIEWED);
                showWordData(wordId);
            }
        });

        // create option click listener
        mQuizOptionList
            .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    String answer = ((TextView) view).getText().toString();
                    String correctAnswer = mRealAnswers.get(mRightAnswerIndex);

                    boolean correct = answer.equals(correctAnswer);

                    if (!correct) {
                        String msg = getString(R.string.msg_incorrect) + " \""
                                + correctAnswer + "\"";
                        msg += "\n" + getString(R.string.msg_you_answered) + " \""
                                + answer + "\"";
                        Toast
                            .makeText(parent.getContext(), msg, Toast.LENGTH_LONG)
                            .show();
                    }

                    recordResult(correct ? RESULT_CORRECT : RESULT_INCORRECT);
                }

            });

        loadRandomWord();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_DATA_ID, 0, R.string.menu_data).setIcon(
                android.R.drawable.ic_menu_info_details);
        // menu.add(0, MENU_SKIP_ID, 0, R.string.menu_skip);
        menu.add(0, MENU_PREV_ID, 0, R.string.menu_prev).setIcon(
                android.R.drawable.ic_menu_revert);
        menu.add(0, MENU_WORD_ID, 0, R.string.menu_word).setIcon(
                android.R.drawable.ic_menu_view);
        menu.add(0, MENU_SETTINGS_ID, 0, R.string.menu_settings).setIcon(
                android.R.drawable.ic_menu_preferences);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case MENU_DATA_ID:
                // recordResult(RESULT_VIEWED);
                Intent dataIntent = new Intent(this, QuizData.class);
                startActivity(dataIntent);
                return true;
            case MENU_SKIP_ID:
                recordResult(RESULT_VIEWED);
                return true;
            case MENU_PREV_ID:
                showWordData(mPreviousWordId);
                return true;
            case MENU_WORD_ID:
                showWordData(mWordId);
                return true;
            case MENU_SETTINGS_ID:
                Intent settingsIntent = new Intent(this, Settings.class);
                startActivity(settingsIntent);
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onPause() {
        // destroy Cursors
        if (mVocabBatch != null) {
            mVocabBatch.close();
            mVocabBatch = null;
        }

        if (mMeaningsCursor != null) {
            mMeaningsCursor.close();
            mMeaningsCursor = null;
        }

        // close db connection
        if (mDbConnector != null) {
            mDbConnector.close();
            mDbConnector = null;
        }

        super.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mPreviousWordId == null || mPreviousWordId == -1) {
            menu.removeItem(MENU_PREV_ID);
        } else if (menu.findItem(MENU_PREV_ID) == null) {
            menu.add(0, MENU_PREV_ID, 0, R.string.menu_prev);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mDbConnector == null) {
            mDbConnector = new VocabularyDbAdapter(this);
        }

        checkForEmptyDb();
        loadRandomWord();
    }

    private void recordResult(int result) {
        switch (result) {
            case RESULT_CORRECT:
                mDbConnector.updateWord(mWordId, mStrength + 1);
                loadRandomWord();
                break;
            case RESULT_INCORRECT:
                mDbConnector.updateWord(mWordId,
                        VocabularyDbAdapter.DEFAULT_STRENGTH);
                loadRandomWord();
                break;
            case RESULT_VIEWED:
                // mDbConnector.updateWord(mWordId, mStrength);
                break;
        }
    }

    private void showWordData(Long wordId) {
        // Log.d(LOG_TAG, "request to view word #" + wordId);
        if (wordId != null && wordId > -1) {
            Intent i = new Intent(this, VocabularyWordView.class);
            i.putExtra(VocabularyDbAdapter.KEY_ROWID, wordId);
            startActivity(i);
        }
    }

}
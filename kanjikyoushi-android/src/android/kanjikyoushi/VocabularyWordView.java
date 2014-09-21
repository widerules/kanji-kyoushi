package android.kanjikyoushi;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class VocabularyWordView extends Activity {

    private static final int FEEDBACK_ID = Menu.FIRST;

    private TextView mWordText;
    // private TextView mStrengthText;
    private LinearLayout mStrengthView;
    private TextView mLastSeenText;
    private ListView mReadingList;
    private ListView mMeaningList;
    private Long mRowId;
    // private String mFeedId;

    private VocabularyDbAdapter mDbConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbConnector = new VocabularyDbAdapter(this);

        setContentView(R.layout.vocab_word_view);

        mWordText = (TextView) findViewById(R.id.viewkanji);
        // mStrengthText = (TextView) findViewById(R.id.viewstrength);
        mStrengthView = (LinearLayout) findViewById(R.id.viewstrength);
        mLastSeenText = (TextView) findViewById(R.id.viewlastseen);
        mReadingList = (ListView) findViewById(R.id.readingslist);
        mMeaningList = (ListView) findViewById(R.id.meaningslist);

        mRowId = savedInstanceState != null ? savedInstanceState
            .getLong(VocabularyDbAdapter.KEY_ROWID) : null;
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(VocabularyDbAdapter.KEY_ROWID)
                : null;
        }

        populateFields();
    }

    private void populateFields() {
        if (mRowId != null) {
            Log.d(Common.LOG_TAG, "displaying #" + mRowId);

            Cursor vocabWord = mDbConnector.fetchWord(mRowId);
            startManagingCursor(vocabWord);
            vocabWord.moveToFirst();

            mWordText.setText(vocabWord.getString(vocabWord
                .getColumnIndexOrThrow(VocabularyDbAdapter.KEY_WORD)));

            // mFeedId =
            // vocabWord.getString(vocabWord.getColumnIndexOrThrow(VocabularyDbAdapter.KEY_FEED_INDEX));

            int strength = vocabWord.getInt(vocabWord
                .getColumnIndexOrThrow(VocabularyDbAdapter.KEY_STRENGTH));
            mStrengthView.removeAllViews();
            for (int i = 0; i < strength; i++) {
                ImageView strengthImage = new ImageView(this);
                strengthImage.setImageResource(R.raw.star);
                mStrengthView.addView(strengthImage);
            }

            String lastSeen = vocabWord.getString(vocabWord
                .getColumnIndexOrThrow(VocabularyDbAdapter.KEY_LASTSEEN));
            mLastSeenText.setText(Common.dateStringFromSQLTimestamp(this, lastSeen));

            // populate readings field
            Cursor readingsCursor = mDbConnector.fetchReadings(mRowId);
            startManagingCursor(readingsCursor);
            String[] readingFrom = new String[] { VocabularyDbAdapter.KEY_READING };
            int[] readingTo = new int[] { R.id.readingview };
            ListAdapter readingsAdapter = new SimpleCursorAdapter(this,
                    R.layout.reading_row, readingsCursor, readingFrom, readingTo);

            mReadingList.setAdapter(readingsAdapter);

            // populate meanings field
            Cursor meaningsCursor = mDbConnector.fetchMeanings(mRowId);
            startManagingCursor(meaningsCursor);

            String[] meaningFrom = new String[] { VocabularyDbAdapter.KEY_MEANING };
            int[] meaningTo = new int[] { R.id.meaningview };
            ListAdapter meaningsAdapter = new SimpleCursorAdapter(this,
                    R.layout.meaning_row, meaningsCursor, meaningFrom, meaningTo);

            mMeaningList.setAdapter(meaningsAdapter);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    @Override
    protected void onPause() {
        mDbConnector.close();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(VocabularyDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // menu.add(0, LIST_ID, 0, R.string.menu_list);
        menu.add(0, FEEDBACK_ID, 0, R.string.menu_feedback).setIcon(
                android.R.drawable.ic_menu_send);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case FEEDBACK_ID:
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"
                        + getString(R.string.email)));
                i.putExtra("subject", "kanjikyoushi feedback");
                i.putExtra("body", getMailBody());
                startActivity(i);
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private String getMailBody() {
        StringBuffer buf = new StringBuffer();
        String indent = "     ";

        buf.append("\n\n**********\n");
        buf.append("\nhttp://kanjikyoushi.appspot.com/vocab?word="
                + mWordText.getText() + "\n");
        buf.append("word:\n" + indent + mWordText.getText() + "\n");

        // populate readings field
        Cursor readingsCursor = mDbConnector.fetchReadings(mRowId);
        startManagingCursor(readingsCursor);
        readingsCursor.moveToFirst();

        buf.append("readings:\n");
        while (!readingsCursor.isAfterLast()) {
            buf.append(indent + readingsCursor.getString(1) + "\n");
            readingsCursor.moveToNext();
        }
        readingsCursor.close();

        // populate meanings field
        Cursor meaningsCursor = mDbConnector.fetchMeanings(mRowId);
        startManagingCursor(meaningsCursor);
        meaningsCursor.moveToFirst();

        buf.append("meanings:\n");
        while (!meaningsCursor.isAfterLast()) {
            buf.append(indent + meaningsCursor.getString(1) + "\n");
            meaningsCursor.moveToNext();
        }
        meaningsCursor.close();

        buf.append("**********\n");

        return buf.toString();
    }

}

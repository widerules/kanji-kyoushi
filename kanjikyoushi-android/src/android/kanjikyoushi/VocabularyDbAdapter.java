package android.kanjikyoushi;

import java.sql.Timestamp;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class VocabularyDbAdapter {

    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DB_CREATE_VOCABULARY = "create table "
                + TABLE_VOCABULARY + " (" + KEY_ROWID
                + " integer primary key autoincrement, " + KEY_WORD
                + " text not null, " + KEY_STRENGTH + " integer not null default "
                + DEFAULT_STRENGTH + ", " + KEY_LASTSEEN + " timestamp, "
                + KEY_KANJI_INDEX + " integer, " + KEY_FEED_INDEX + " text" + ");";

        private static final String DB_CREATE_MEANINGS = "create table "
                + TABLE_MEANINGS + " (" + KEY_ROWID
                + " integer primary key autoincrement, " + KEY_WORD
                + " integer references " + TABLE_VOCABULARY + " on delete cascade, "
                + KEY_MEANING + " text not null," + "unique (" + KEY_WORD + ", "
                + KEY_MEANING + ") on conflict ignore " + ")";

        private static final String DB_CREATE_READINGS = "create table "
                + TABLE_READINGS + " (" + KEY_ROWID
                + " integer primary key autoincrement, " + KEY_WORD
                + " integer references " + TABLE_VOCABULARY + " on delete cascade, "
                + KEY_READING + " text not null, " + "unique (" + KEY_WORD + ", "
                + KEY_READING + ") on conflict ignore " + ")";

        DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION_CURR);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_VOCABULARY);
            db.execSQL(DB_CREATE_READINGS);
            db.execSQL(DB_CREATE_MEANINGS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String logMessage = "Upgrading database from version " + oldVersion
                    + " to " + newVersion;

            logMessage += ", deleting all database data and recreating database";
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOCABULARY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_READINGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEANINGS);
            onCreate(db);

            Log.w(Common.LOG_TAG, logMessage);
        }

    }

    private static final String DB_NAME = "data";
    private static final String TABLE_VOCABULARY = "vocabulary";
    private static final String TABLE_MEANINGS = "meanings";
    private static final String TABLE_READINGS = "readings";

    private static final int DB_VERSION_CURR = 3;

    public static final String KEY_ROWID = "_id";
    public static final String KEY_WORD = "word";
    public static final String KEY_MEANING = "meaning";
    public static final String KEY_READING = "reading";
    public static final String KEY_STRENGTH = "strength";
    public static final String KEY_LASTSEEN = "lastseen";
    public static final String KEY_FEED_INDEX = "feed_index";
    public static final String KEY_KANJI_INDEX = "kanji_index";

    public static final int DEFAULT_STRENGTH = 0;

    private static final int MAX_STRENGTH = 10;

    private final Context mContext;
    private DatabaseHelper mDbHelper;
    private SharedPreferences mSettings;

    public VocabularyDbAdapter(Context _context) {
        this.mContext = _context;
        mSettings = mContext.getSharedPreferences(Common.SETTINGS,
                Context.MODE_PRIVATE);
        mDbHelper = new DatabaseHelper(mContext);
    }

    private void addMeanings(long _wordId, Iterable<String> _meanings) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        for (String meaning : _meanings) {
            ContentValues meaningValues = new ContentValues();
            meaningValues.put(KEY_WORD, _wordId);
            meaningValues.put(KEY_MEANING, meaning);
            db.insert(TABLE_MEANINGS, null, meaningValues);
        }

        db.close();
    }

    private void addReadings(long _wordId, Iterable<String> _readings) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        for (String reading : _readings) {
            ContentValues readingValues = new ContentValues();
            readingValues.put(KEY_WORD, _wordId);
            readingValues.put(KEY_READING, reading);
            db.insert(TABLE_READINGS, null, readingValues);
        }

        db.close();
    }

    private long addWord(String _word, String _feedIndex, int _kanjiIndex) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues wordValues = new ContentValues();

        wordValues.put(KEY_WORD, _word);
        wordValues.put(KEY_FEED_INDEX, _feedIndex);
        wordValues.put(KEY_KANJI_INDEX, _kanjiIndex);

        long newWordId = db.insert(TABLE_VOCABULARY, null, wordValues);

        db.close();

        return newWordId;
    }

    public long addWord(String _word, Iterable<String> _readings,
            Iterable<String> _meanings, String _feedIndex, int _kanjiIndex) {
        long rowId = addWord(_word, _feedIndex, _kanjiIndex);

        if (rowId != -1) {
            addReadings(rowId, _readings);
            addMeanings(rowId, _meanings);
        }

        return rowId;
    }

    public void close() {
        mDbHelper.close();
    }

    public void deleteAllWords() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.execSQL("DELETE FROM " + TABLE_MEANINGS);
        db.execSQL("DELETE FROM " + TABLE_READINGS);
        db.execSQL("DELETE FROM " + TABLE_VOCABULARY);

        db.close();
    }

    public void deleteWord(long _rowId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.execSQL("DELETE FROM " + TABLE_READINGS + " WHERE " + KEY_WORD + " = ?",
                new String[] { String.valueOf(_rowId) });
        db.execSQL("DELETE FROM " + TABLE_MEANINGS + " WHERE " + KEY_WORD + " = ?",
                new String[] { String.valueOf(_rowId) });
        db.execSQL("DELETE FROM " + TABLE_VOCABULARY + " WHERE " + KEY_ROWID
                + " = ?", new String[] { String.valueOf(_rowId) });

        db.close();
    }

    public Cursor fetchAllWords() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor allWords = db.query(TABLE_VOCABULARY, new String[] { KEY_ROWID,
                KEY_WORD, KEY_STRENGTH, KEY_LASTSEEN }, null, null, null, null,
                KEY_KANJI_INDEX);

        return allWords;
    }

    public Cursor fetchMeanings(long _wordId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor meanings = db.query(TABLE_MEANINGS, new String[] { KEY_ROWID,
                KEY_MEANING }, KEY_WORD + "=" + _wordId, null, null, null, KEY_WORD
                + ", " + KEY_MEANING);

        return meanings;
    }

    public Cursor fetchRandomMeanings(int _limit) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor randMeanings = db.query(true, TABLE_MEANINGS,
                new String[] { KEY_MEANING }, null, null, null, null, "random()",
                String.valueOf(_limit));

        return randMeanings;
    }

    @SuppressWarnings("unused")
    private Cursor fetchRandomReadings(int _limit) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor randReadings = db.query(true, TABLE_READINGS,
                new String[] { KEY_READING }, null, null, null, null, "random()",
                String.valueOf(_limit));

        return randReadings;
    }

    public Cursor fetchRandomWords(int _limit) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor randWords = db.query(true, TABLE_VOCABULARY, new String[] {
                KEY_ROWID, KEY_WORD, KEY_STRENGTH, KEY_LASTSEEN }, null, null, null,
                null, getOrderBy(), String.valueOf(_limit));

        return randWords;
    }

    public Cursor fetchReadings(long _wordId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor readings = db.query(TABLE_READINGS, new String[] { KEY_ROWID,
                KEY_READING }, KEY_WORD + "=" + _wordId, null, null, null, KEY_WORD
                + ", " + KEY_READING);

        return readings;
    }

    public Cursor fetchWord(long _rowId) throws SQLException {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor word = db.query(true, TABLE_VOCABULARY, new String[] { KEY_ROWID,
                KEY_WORD, KEY_STRENGTH, KEY_LASTSEEN, KEY_FEED_INDEX,
                KEY_KANJI_INDEX }, KEY_ROWID + "=" + _rowId, null, null, null, null,
                null);

        return word;
    }

    public int getMaxKanjiIndex() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int maxVocabId = 0;

        Cursor c = db.rawQuery("SELECT MAX(" + KEY_KANJI_INDEX + ") FROM "
                + TABLE_VOCABULARY, null);

        if (c.moveToFirst()) {
            maxVocabId = c.getInt(0);
        }
        c.close();

        return maxVocabId;
    }

    private String getOrderBy() {
        int randomnessSetting = mSettings.getInt(Common.SETTING_RANDOMNESS,
                Common.DEFAULT_RANDOMNESS);

        int randomness = 0;
        if (Common.RANDOM_COMPLETELY == randomnessSetting) {
            return "RANDOM()";
        } else if (Common.RANDOM_MOSTLY == randomnessSetting) {
            randomness = 1000;
        } else if (Common.RANDOM_SOMEWHAT == randomnessSetting) {
            randomness = 100;
        } else if (Common.RANDOM_SLIGHTLY == randomnessSetting) {
            randomness = 50;
        } else if (Common.RANDOM_NOT == randomnessSetting) {
            randomness = 0;
        }

        String randomString;
        if (randomness == 0) {
            randomString = "1.0";
        } else {
            randomString = "(1.0 + ((RANDOM() % " + randomness + ") / 100.0))";
        }

        String orderBy = "CASE strength WHEN 0 THEN 1.0 "
                + "ELSE 1.0 - (strength / (2.0 * (SELECT max(strength) FROM vocabulary))) "
                + "END * "
                + "CASE WHEN lastseen "
                + "THEN cast(strftime('%s', 'now') - strftime('%s', lastseen) as real) "
                + "/ cast(strftime('%s', 'now') "
                + "- strftime('%s', (SELECT min(lastseen) FROM vocabulary)) as real) "
                + "ELSE 1.0 END * " + randomString + " DESC";

        return orderBy;
    }

    public long getRowIdByFeedIndex(String _feedIndex) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        long rowId = -1L;

        Cursor c = db.rawQuery("SELECT " + KEY_ROWID + " FROM " + TABLE_VOCABULARY
                + " WHERE " + KEY_FEED_INDEX + " = ?", new String[] { String
            .valueOf(_feedIndex) });

        if (c.moveToFirst()) {
            rowId = c.getLong(0);
        }
        c.close();

        return rowId;
    }

    /**
     * get a count for each strength value in database
     * 
     * @return
     */
    public Cursor getStrengthCount() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor strCount = db.query(TABLE_VOCABULARY, new String[] {
                KEY_STRENGTH + " AS " + KEY_ROWID, "COUNT(" + KEY_STRENGTH + ")" },
                null, null, KEY_STRENGTH, null, KEY_STRENGTH);

        return strCount;
    }

    public int getUnseenWordCount() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(1) FROM " + TABLE_VOCABULARY
                + " WHERE " + KEY_LASTSEEN + " IS NULL", null);

        int count = -1;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();

        return count;
    }

    public int getWordCount() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor = db.query(TABLE_VOCABULARY, new String[] { "count(1)" },
                null, null, null, null, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();

        return count;
    }

    public boolean isEmptyDatabase() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        boolean isEmpty = true;

        Cursor c = db.rawQuery("SELECT 1 FROM " + TABLE_VOCABULARY, null);

        if (c.moveToFirst()) {
            isEmpty = c.getCount() == 0;
        }
        c.close();

        return isEmpty;
    }

    public Cursor searchVocab(String[] _searchStrings) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        StringBuffer searchSql = new StringBuffer();

        searchSql.append("SELECT v." + KEY_ROWID + ", v." + KEY_WORD + ", v."
                + KEY_STRENGTH + ", v." + KEY_STRENGTH + " FROM " + TABLE_VOCABULARY
                + " AS v WHERE ");

        String glue = "";
        for (String searchString : _searchStrings) {
            // search words
            searchSql.append(glue + "(v." + KEY_WORD + " LIKE '%" + searchString
                    + "%'");

            // readings
            searchSql.append(" OR EXISTS(SELECT 1 FROM " + TABLE_READINGS
                    + " AS r WHERE v." + KEY_ROWID + " = r." + KEY_WORD + " AND r."
                    + KEY_READING + " LIKE '%" + searchString + "%')");

            // meanings
            searchSql.append(" OR EXISTS(SELECT 1 FROM " + TABLE_MEANINGS
                    + " AS m WHERE v." + KEY_ROWID + " = m." + KEY_WORD + " AND m."
                    + KEY_MEANING + " LIKE '%" + searchString + "%'))");

            glue = " AND ";
        }

        Cursor vocab = db.rawQuery(searchSql.toString(), null);

        return vocab;
    }

    public boolean updateWord(long _rowId, int _newStrength) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Timestamp now = new Timestamp(System.currentTimeMillis());

        ContentValues args = new ContentValues();
        args.put(KEY_STRENGTH, Math.min(_newStrength, MAX_STRENGTH));
        args.put(KEY_LASTSEEN, now.toString());

        int affectedRowCount = db.update(TABLE_VOCABULARY, args, KEY_ROWID + "="
                + _rowId, null);

        db.close();

        return affectedRowCount > 0;
    }

    public void updateWord(long _rowId, String _word, List<String> _readings,
            List<String> _meanings) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String sql = "UPDATE " + TABLE_VOCABULARY + " SET " + KEY_WORD
                + " = ? WHERE " + KEY_ROWID + " = ?";
        db.execSQL(sql, new Object[] { _word, _rowId });

        sql = "DELETE FROM " + TABLE_READINGS + " WHERE " + KEY_WORD + " = ?";
        db.execSQL(sql, new Object[] { _rowId });

        sql = "DELETE FROM " + TABLE_MEANINGS + " WHERE " + KEY_WORD + " = ?";
        db.execSQL(sql, new Object[] { _rowId });

        db.close();

        addReadings(_rowId, _readings);
        addMeanings(_rowId, _meanings);
    }

}
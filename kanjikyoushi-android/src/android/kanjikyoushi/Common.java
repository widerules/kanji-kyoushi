package android.kanjikyoushi;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;

public class Common {

    public static final String LOG_TAG = "KanjiKyoushi";

    public static final int QUIZ_TYPE_READINGS = 0;
    public static final int QUIZ_TYPE_MEANINGS = 1;
    public static final int QUIZ_TYPE_BOTH = 2;

    public static final int RANDOM_COMPLETELY = 0;
    public static final int RANDOM_MOSTLY = 1;
    public static final int RANDOM_SOMEWHAT = 2;
    public static final int RANDOM_SLIGHTLY = 3;
    public static final int RANDOM_NOT = 4;

    // application settings
    public static final String SETTINGS = "KanjiKyoushiSettingsFile";
    public static final String SETTING_FEED_COUNT = "feed_count";
    public static final String SETTING_LAST_UPDATE = "last_update";
    public static final String SETTING_BATCH_SIZE = "batch_size";
    public static final String SETTING_QUIZ_TYPE = "quiz_type";
    public static final String SETTING_RANDOMNESS = "randomness";

    // default settings
    public static final String DEFAULT_FEED_COUNT = "10";
    public static final String DEFAULT_LAST_UPDATE = null;
    public static final int DEFAULT_BATCH_SIZE = 10;
    public static final int DEFAULT_QUIZ_TYPE = QUIZ_TYPE_BOTH;
    public static final int DEFAULT_RANDOMNESS = RANDOM_SOMEWHAT;

    /**
     * feed count for the first set of kanji
     */
    public static final String FIRST_FEED_COUNT = "25";

    public static String dateStringFromSQLTimestamp(Context context,
            String SQLTimestamp) {
        String dateString;
        if (SQLTimestamp == null) {
            dateString = context.getString(R.string.neverseen);
        } else {
            Timestamp ts = Timestamp.valueOf(SQLTimestamp);
            Date d = new Date(ts.getTime());
            SimpleDateFormat df = new SimpleDateFormat();
            dateString = df.format(d);
        }
        return dateString;
    }

    /**
     * remove all null end empty elements from array
     * 
     * @param <T>
     * @param _originalArray
     * @return
     */
    public static <T extends Object> List<T> cleanArray(T[] _originalArray) {
        List<T> cleanedArray = new ArrayList<T>();

        for (T element : _originalArray) {
            if (element != null && !element.toString().trim().equals("")) {
                cleanedArray.add(element);
            }
        }

        return cleanedArray;
    }
}

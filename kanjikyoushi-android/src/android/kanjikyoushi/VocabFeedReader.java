package android.kanjikyoushi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class VocabFeedReader implements Runnable {

	private static Object feedLock = new Object();

	public static final String ACTION_FIRST = "first";
	public static final String ACTION_GET = "get";
	public static final String ACTION_UPDATE = "update";
	public static final String ACTION_VALIDATE = "validate";

	private static final int FEED_INIT = 0;
	private static final int FEED_START = 1;
	private static final int FEED_FINISH = 2;
	private static final int FEED_UPDATE = 3;
	private static final int FEED_NO_CONNECTION = 4;
	private static final int FEED_ERROR = 5;
	private static final int FEED_TOTAL_COUNT = 6;

	private VocabFeedRequestor mRequestor;
	private Context mContext;
	private VocabularyDbAdapter mDbConnector;
	private boolean mFirst = false;

	private String mAction;
	private int mKanjiIndex = -1;
	private int mCount = 0;
	private String mLastUpdate;

	private ProgressDialog mVocabFeedDialog;
	private SharedPreferences mSettings;

	public VocabFeedReader(VocabFeedRequestor _requestor, String _action) {
		this.mRequestor = _requestor;
		this.mContext = _requestor.getContext();
		this.mDbConnector = new VocabularyDbAdapter(mContext);
		this.mAction = _action;

		mSettings = mContext.getSharedPreferences(Common.SETTINGS, Context.MODE_PRIVATE);

		if (ACTION_FIRST.equals(mAction)) {
			mAction = ACTION_GET;
			mFirst = true;
		}

		if (ACTION_GET.equals(mAction)) {
			mKanjiIndex = mDbConnector.getMaxKanjiIndex() + 1;

			// Get count value from settings
			mCount = Integer.parseInt(mFirst ? Common.FIRST_FEED_COUNT
				: mSettings.getString(Common.SETTING_FEED_COUNT,
						Common.DEFAULT_FEED_COUNT));

		} else if (ACTION_UPDATE.equals(mAction)) {
			mKanjiIndex = mDbConnector.getMaxKanjiIndex();

			// get last update from settings
			mLastUpdate = mSettings.getString(Common.SETTING_LAST_UPDATE,
					Common.DEFAULT_LAST_UPDATE);

		} else if (ACTION_VALIDATE.equals(mAction)) {
			mKanjiIndex = mDbConnector.getMaxKanjiIndex();
		} else {
			// Log.e(Common.LOG_TAG, "invalid action: " + mAction);
		}
	}

	public void run() {
		synchronized (feedLock) {
			// Log.d(Common.LOG_TAG, "starting vocabulary data request, action="
			// +
			// mAction
			// + ", id=" + mVocabId + (ACTION_NEW.equals(mAction)
			// ? ", count=" + mCount : (ACTION_UPDATE.equals(mAction)
			// ? ", since=" + mLastUpdate : "")));

			// check for valid values
			if (!validParameters()) {
				// Log.e(Common.LOG_TAG, String.format(
				// "invalid feed parameters; action=%s, id=%d, count=%d, lastupdate=%s",
				// mAction, mVocabId, mCount, mLastUpdate));
			}

			String title = mContext.getString(ACTION_GET.equals(mAction)
				? R.string.feed_title_new : ACTION_UPDATE.equals(mAction)
					? R.string.feed_title_update : ACTION_VALIDATE.equals(mAction)
						? R.string.feed_title_validate : R.string.feed_title);

			Message msg = mHandler.obtainMessage(FEED_INIT);
			Bundle b = new Bundle();
			b.putString("title", title);
			b.putBoolean("show_close", false);
			// b.putBoolean("show_close", !mFirst);
			msg.setData(b);
			mHandler.sendMessage(msg);

			// check network state
			final ConnectivityManager connectionManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

			final NetworkInfo network_info = connectionManager.getActiveNetworkInfo();
			if (network_info != null && network_info.isConnected()) {

				try {
					// Get a SAXParser from the SAXPArserFactory.
					SAXParserFactory spf = SAXParserFactory.newInstance();
					SAXParser sp = spf.newSAXParser();

					// Get the XMLReader of the SAXParser we created.
					XMLReader xr = sp.getXMLReader();

					xr.setContentHandler(new FeedHandler());

					// Parse the xml-data from our URL.
					xr.parse(new InputSource(getFeedUrl().openStream()));

				} catch (Exception e) {
					// Log.e(Common.LOG_TAG, "error retrieving vocabulary data",
					// e);
					msg = mHandler.obtainMessage(FEED_ERROR);
					b = new Bundle();
					b.putString("error", e.toString());
					msg.setData(b);
					mHandler.sendMessage(msg);
					Log.d(Common.LOG_TAG, e.toString());

					// mHandler.sendEmptyMessage(FEED_ERROR);
				}

				Log.d(Common.LOG_TAG, "finished vocabulary data request");

			} else {

				Log.d(Common.LOG_TAG,
						"no network connection, aborting vocabulary data request");
				mHandler.sendEmptyMessage(FEED_NO_CONNECTION);

			}
		}
	}

	/**
	 * check values of mAction, mVocabId, mCount, mLastUpdate
	 * 
	 * @return true when values are valid
	 */
	private boolean validParameters() {
		boolean result = true;

		if (mKanjiIndex < 1 || mKanjiIndex > mDbConnector.getMaxKanjiIndex()) {
			result = false;

		} else if (ACTION_GET.equals(mAction) && mCount < 1) {
			result = false;

		} else if (ACTION_UPDATE.equals(mAction) && mLastUpdate == null) {
			result = false;

		}

		return result;
	}

	/**
	 * constructs and returns vocabulary feed url object
	 * 
	 * @return vocabulary feed url object or null if an error occurs
	 * @throws MalformedURLException
	 */
	private URL getFeedUrl() throws MalformedURLException {
		URL feedUrl = null;

		try {
			String urlString = String.format("%s?action=%s&kanji_index=%s",
					mContext.getString(R.string.vocab_feed), mAction, mKanjiIndex);

			if (ACTION_GET.equals(mAction)) {
				urlString += "&kanji_count=" + mCount;
			} else if (ACTION_UPDATE.equals(mAction)) {
				urlString += "&since=" + mLastUpdate;
			}

			// replace whitespace with '%20'
			urlString = urlString.replaceAll("\\s+", "%20");

			feedUrl = new URL(urlString);
		} catch (MalformedURLException e) {
			Log.e(Common.LOG_TAG, "error creating vocabulary feed url");
			throw e;
		}

		return feedUrl;
	}

	private class FeedHandler extends DefaultHandler {

		private static final String TAG_VOCAB_LIST = "vocabulary_list";

		private static final String TAG_ACTION = "action";
		private static final String TAG_MASTER_ID = "master_id";
		private static final String TAG_COUNT = "count";
		private static final String TAG_TIMESTAMP = "timestamp";

		private static final String TAG_VOCAB_WORD = "vocabword";
		private static final String TAG_WORD = "word";
		private static final String TAG_KEY = "key";
		private static final String TAG_INDEX = "index";
		private static final String TAG_ACTIVE = "active";
		private static final String TAG_READING = "reading";
		private static final String TAG_MEANING = "meaning";

		private String action;
		private String timestamp;
		private int wordCount = 0;
		private int currCount = 0;

		private String word = null;
		private String feedIndex = null;
		private int kanjiIndex = -1;
		private List<String> readings;
		private List<String> meanings;
		private boolean active = false;

		private StringBuffer currText;

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			currText.append(new String(ch, start, length));
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {

			if (TAG_VOCAB_LIST.equals(localName)) {
				Log.d(Common.LOG_TAG, "action: " + action + "; timestamp: " + timestamp);
				if (ACTION_UPDATE.equals(action) || mFirst) {
					// set timestamp in settings

					Log.d(Common.LOG_TAG, "saving new timestamp");

					mSettings.edit().putString(Common.SETTING_LAST_UPDATE, timestamp).commit();

					Log.d(Common.LOG_TAG, "new timestamp: "
							+ mSettings.getString(Common.SETTING_LAST_UPDATE,
									Common.DEFAULT_LAST_UPDATE));
				}

				Message msg = mHandler.obtainMessage(FEED_FINISH);
				Bundle b = new Bundle();
				b.putString("final_msg", String.format(
						mContext.getString(ACTION_GET.equals(action)
							? R.string.feed_final_msg_format_new
							: ACTION_UPDATE.equals(action)
								? R.string.feed_final_msg_format_update
								: ACTION_VALIDATE.equals(action)
									? R.string.feed_final_msg_format_validate
									: R.string.feed_final_msg_format), currCount));
				msg.setData(b);
				mHandler.sendMessage(msg);
				// mHandler.sendEmptyMessage(FEED_FINISH);

			} else if (TAG_ACTION.equals(localName)) {
				action = currText.toString();

			} else if (TAG_MASTER_ID.equals(localName)) {

			} else if (TAG_COUNT.equals(localName)) {
				wordCount = Integer.parseInt(currText.toString());

				Message msg = mHandler.obtainMessage(FEED_TOTAL_COUNT);
				Bundle b = new Bundle();
				b.putInt("wordcount", wordCount);
				msg.setData(b);
				mHandler.sendMessage(msg);

			} else if (TAG_TIMESTAMP.equals(localName)) {
				timestamp = currText.toString();

			} else if (TAG_VOCAB_WORD.equals(localName)) {
				long rowId = mDbConnector.getRowIdByFeedIndex(feedIndex);
				if (active && rowId != -1L) {
					// update existing word
					mDbConnector.updateWord(rowId, word, readings, meanings);

				} else if (active && rowId == -1L) {
					// add new word
					mDbConnector.addWord(word, readings, meanings, feedIndex, kanjiIndex);

				} else if (!active && rowId != -1L) {
					// delete word
					mDbConnector.deleteWord(rowId);
				}

				currCount++;

				Message msg = mHandler.obtainMessage(FEED_UPDATE);
				Bundle b = new Bundle();
				b.putInt("count", currCount);
				msg.setData(b);
				mHandler.sendMessage(msg);

				// if (ACTION_NEW.equals(action)) {
				// // insert word into database
				// if (active) {
				// mDbConnector.addWord(word, readings, meanings, feedIndex);
				// }
				//
				// } else if (ACTION_UPDATE.equals(action)) {
				// long rowId = mDbConnector.getRowIdByFeedIndex(feedIndex);
				// if (active && rowId != -1L) {
				// mDbConnector.updateWord(rowId, word, readings, meanings);
				// } else if (active && rowId == -1L) {
				// mDbConnector.addWord(word, readings, meanings, feedIndex);
				// } else if (!active && rowId != -1L) {
				// mDbConnector.deleteWord(rowId);
				// }
				//
				// } else if (ACTION_VALIDATE.equals(action)) {
				// // TODO
				// }

			} else if (TAG_WORD.equals(localName)) {
				word = currText.toString();

			} else if (TAG_INDEX.equals(localName)) {
				kanjiIndex = Integer.parseInt(currText.toString());

			} else if (TAG_KEY.equals(localName)) {
				feedIndex = currText.toString();

			} else if (TAG_ACTIVE.equals(localName)) {
				active = Boolean.parseBoolean(currText.toString());

			} else if (TAG_READING.equals(localName)) {
				if (currText.length() > 0) {
					readings.add(currText.toString());
				}

			} else if (TAG_MEANING.equals(localName)) {
				if (currText.length() > 0) {
					meanings.add(currText.toString());
				}
			}

		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			currText = new StringBuffer();

			if (TAG_VOCAB_LIST.equals(localName)) {
				mHandler.sendEmptyMessage(FEED_START);

			} else if (TAG_ACTION.equals(localName)) {
			} else if (TAG_MASTER_ID.equals(localName)) {
			} else if (TAG_COUNT.equals(localName)) {
			} else if (TAG_TIMESTAMP.equals(localName)) {
			} else if (TAG_VOCAB_WORD.equals(localName)) {
				// reset data values
				word = null;
				feedIndex = null;
				kanjiIndex = -1;
				active = false;
				readings = new Vector<String>();
				meanings = new Vector<String>();

			} else if (TAG_WORD.equals(localName)) {
			} else if (TAG_INDEX.equals(localName)) {
			} else if (TAG_KEY.equals(localName)) {
			} else if (TAG_ACTIVE.equals(localName)) {
			} else if (TAG_READING.equals(localName)) {
			} else if (TAG_MEANING.equals(localName)) {
			}
		}
	}

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
				case FEED_INIT:
					mVocabFeedDialog = new ProgressDialog(mContext);
					mVocabFeedDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

					mVocabFeedDialog.setTitle(msg.getData().getString("title"));
					mVocabFeedDialog.setMessage(mContext.getString(R.string.feed_reading));

					if (msg.getData().getBoolean("show_close")) {
						CharSequence buttonText = mContext.getString(R.string.button_close);
						mVocabFeedDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
								buttonText, new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog, int which) {
										Toast.makeText(mContext,
												R.string.feed_continue_in_background,
												Toast.LENGTH_LONG).show();
									}
								});
					}

					mVocabFeedDialog.setCancelable(false);
					mVocabFeedDialog.show();
					break;
				case FEED_START:
					mVocabFeedDialog.setMessage(mContext.getString(R.string.feed_processing));
					break;
				case FEED_TOTAL_COUNT:
					mVocabFeedDialog.setMax(msg.getData().getInt("wordcount"));
					break;
				case FEED_UPDATE:
					mVocabFeedDialog.setProgress(msg.getData().getInt("count"));
					break;
				case FEED_FINISH:
					mVocabFeedDialog.dismiss();
					mRequestor.doneFeedLoading();
					String finalMsg = msg.getData().getString("final_msg");
					if (finalMsg != null) {
						Toast.makeText(mContext, finalMsg, Toast.LENGTH_LONG).show();
					}
					break;
				case FEED_NO_CONNECTION:
					mVocabFeedDialog.dismiss();
					Toast.makeText(mContext, R.string.feed_no_connection,
							Toast.LENGTH_LONG).show();
					break;
				case FEED_ERROR:
					mVocabFeedDialog.dismiss();
					String errorMsg = mContext.getString(R.string.feed_error);
					if (msg.getData().containsKey("error")) {
						errorMsg += ": " + msg.getData().getString("error");
					}

					Toast.makeText(mContext, errorMsg, Toast.LENGTH_LONG).show();
					break;
			}
		}

	};
}

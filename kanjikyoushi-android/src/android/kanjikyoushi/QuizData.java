package android.kanjikyoushi;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.TabSpec;

// public class QuizData extends TabActivity implements VocabFeedRequestor {
public class QuizData extends TabActivity {

	private static final int MENU_GET_WORDS = Menu.FIRST;
	private static final int MENU_UPDATE_WORDS = Menu.FIRST + 1;
	private static final int MENU_VALIDATE_WORDS = Menu.FIRST + 2;
	private static final int MENU_SETTINGS_ID = Menu.FIRST + 3;

	private VocabularyDbAdapter mDbConnector;
	private TabHost mTabHost;

	private String mSearchString = null;

	private Thread mFeedThread;
	private Context mContext;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		mDbConnector = new VocabularyDbAdapter(this);

		setContentView(R.layout.quiz_data);

		mTabHost = getTabHost();

		TabSpec statsTab = mTabHost.newTabSpec("data_tab1");
		statsTab.setIndicator("stats");
		statsTab.setContent(R.id.quiz_data_stats);
		mTabHost.addTab(statsTab);

		TabSpec vocabListTab = mTabHost.newTabSpec("data_tab2");
		vocabListTab.setIndicator(getString(R.string.data_tab_vocablist));
		vocabListTab.setContent(R.id.quiz_data_vocab);
		mTabHost.addTab(vocabListTab);

		// TabSpec debugTab = mTabHost.newTabSpec("data_tab3");
		// debugTab.setIndicator(getString(R.string.data_tab_debug));
		// debugTab.setContent(R.id.quiz_data_debug);
		// mTabHost.addTab(debugTab);

		// to use an icon for tab label:
		// setIndicator("TAB 1",
		// getResources().getDrawable(R.drawable.tab_icon))

		// set up search
		EditText searchText = (EditText) findViewById(R.id.quiz_data_vocab_search);
		searchText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				if (!s.toString().equals(mSearchString)) {
					if (s.toString().length() == 0) {
						mSearchString = null;
					} else {
						mSearchString = s.toString();
					}
					// Log.d(LOG_TAG, "searchstring=" + mSearchString);
					fillVocabList();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		fillStats();
		fillVocabList();
		// fillDebug();

		mTabHost.setCurrentTab(0);
	}

	// private void fillDebug() {
	// String statsString;
	// // statsString = mDbConnector.getDebugInfo() + "\n";
	// statsString = "last update: "
	// + getSharedPreferences(Common.SETTINGS, Context.MODE_PRIVATE).getString(
	// Common.SETTING_LAST_UPDATE, Common.DEFAULT_LAST_UPDATE);
	//
	// TextView statsView = (TextView) findViewById(R.id.quiz_data_debug);
	// statsView.setText(statsString);
	// }

	private void fillStats() {
		// load total count and unseeen count text views
		TextView totalCountView = (TextView) findViewById(R.id.quiz_data_stats_total_count);
		totalCountView.setText(String.valueOf(mDbConnector.getWordCount()));

		TextView unseenCountView = (TextView) findViewById(R.id.quiz_data_stats_unseen_count);
		unseenCountView.setText(String.valueOf(mDbConnector.getUnseenWordCount()));

		// load last update
		SharedPreferences settings = getSharedPreferences(Common.SETTINGS,
				Context.MODE_PRIVATE);
		TextView lastUpdateView = (TextView) findViewById(R.id.quiz_data_stats_last_update);
		String lastUpdateRawString = settings.getString(Common.SETTING_LAST_UPDATE,
				Common.DEFAULT_LAST_UPDATE);
		String lastUpdate = "never";
		try {
			lastUpdate = new SimpleDateFormat("d MMM h:mm a").format(new Date(
					1000 * Long.parseLong(lastUpdateRawString.split("\\.")[0])));
		} catch (Exception e) {
			Log.i(Common.LOG_TAG, e.toString());
		}

		lastUpdateView.setText(lastUpdate);

		// strength histogram
		Cursor strengthCursor = mDbConnector.getStrengthCount();
		startManagingCursor(strengthCursor);

		ListView vocabStrengthView = (ListView) findViewById(R.id.quiz_data_strength_list);
		vocabStrengthView.setAdapter(new VocabStrengthAdapter(this, strengthCursor));
	}

	private void fillVocabList() {
		// Get all of the rows from the database and create the item list
		Cursor vocabCursor;
		if (mSearchString == null) {
			vocabCursor = mDbConnector.fetchAllWords();
		} else {
			vocabCursor = mDbConnector.searchVocab(mSearchString.split("\\s+"));
		}
		startManagingCursor(vocabCursor);

		ListView vocabListView = (ListView) findViewById(R.id.quiz_data_vocab_list);
		vocabListView.setAdapter(new VocabListAdapter(this, vocabCursor));

		vocabListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Intent i = new Intent(parent.getContext(), VocabularyWordView.class);
				i.putExtra(VocabularyDbAdapter.KEY_ROWID, id);
				startActivity(i);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_GET_WORDS, 0, R.string.menu_more_words).setIcon(
				android.R.drawable.ic_menu_add);
		menu.add(0, MENU_UPDATE_WORDS, 0, R.string.menu_update_words).setIcon(
				android.R.drawable.ic_menu_manage);
		// menu.add(0, MENU_VALIDATE_WORDS, 0 , R.string.menu_validate_words);
		menu.add(0, MENU_SETTINGS_ID, 0, R.string.menu_settings).setIcon(
				android.R.drawable.ic_menu_preferences);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setNegativeButton(getString(R.string.button_no),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

		switch (item.getItemId()) {
			case MENU_GET_WORDS:
				// Log.d(LOG_TAG, "retrieving new vocabulary data");
				if (mFeedThread == null || !mFeedThread.isAlive()) {

					builder.setMessage(getString(R.string.msg_confirm_more));
					builder.setPositiveButton(getString(R.string.button_yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									mFeedThread = new Thread(new VocabFeedReader(
											feedRequestor, VocabFeedReader.ACTION_GET));
									mFeedThread.start();
								}
							});
					builder.create().show();

				} else {
					Toast.makeText(this, R.string.msg_already_downloading,
							Toast.LENGTH_LONG).show();
				}
				return true;
			case MENU_UPDATE_WORDS:
				// Log.d(LOG_TAG, "updating vocabulary data");
				if (mFeedThread == null || !mFeedThread.isAlive()) {

					builder.setMessage(getString(R.string.msg_confirm_update));
					builder.setPositiveButton(getString(R.string.button_yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									mFeedThread = new Thread(new VocabFeedReader(
											feedRequestor, VocabFeedReader.ACTION_UPDATE));
									mFeedThread.start();
								}
							});
					builder.create().show();

				} else {
					Toast.makeText(this, R.string.msg_already_downloading,
							Toast.LENGTH_LONG).show();
				}
				return true;
			case MENU_VALIDATE_WORDS:
				// Log.d(LOG_TAG, "validating vocabulary data");
				if (mFeedThread == null || !mFeedThread.isAlive()) {

					builder.setMessage(getString(R.string.msg_confirm_validate));
					builder.setPositiveButton(getString(R.string.button_yes),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									mFeedThread = new Thread(new VocabFeedReader(
											feedRequestor,
											VocabFeedReader.ACTION_VALIDATE));
									mFeedThread.start();
								}
							});
					builder.create().show();

				} else {
					Toast.makeText(this, R.string.msg_already_downloading,
							Toast.LENGTH_LONG).show();
				}
				return true;
			case MENU_SETTINGS_ID:
				Intent settingsIntent = new Intent(this, Settings.class);
				startActivity(settingsIntent);
				return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onResume() {
		super.onResume();

		fillStats();
		fillVocabList();
		// fillDebug();

	}

	@Override
	protected void onPause() {
		mDbConnector.close();
		super.onPause();
	}

	private VocabFeedRequestor feedRequestor = new VocabFeedRequestor() {

		public Context getContext() {
			return mContext;
		}

		public void doneFeedLoading() {
			fillStats();
			fillVocabList();
			// fillDebug();
		}

	};

}

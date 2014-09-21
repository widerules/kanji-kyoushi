package android.kanjikyoushi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Settings extends Activity {

	private static final int RESTORE_DEFAULT_ID = Menu.FIRST;

	private Context mContext;
	private SharedPreferences mSettings;

	private Spinner mSpinnerQuizType;
	private Spinner mSpinnerFeedCount;
	private Spinner mSpinnerRandomness;
	private Button mResetButton;

	private ArrayAdapter<CharSequence> mAdapterFeedCount;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, RESTORE_DEFAULT_ID, 0, R.string.menu_restore_default).setIcon(
				android.R.drawable.ic_menu_revert);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.d(Common.LOG_TAG, "menu item selected");

		switch (item.getItemId()) {
			case RESTORE_DEFAULT_ID:
				mSettings.edit().putInt(Common.SETTING_QUIZ_TYPE,
						Common.DEFAULT_QUIZ_TYPE).commit();
				mSettings.edit().putString(Common.SETTING_FEED_COUNT,
						Common.DEFAULT_FEED_COUNT).commit();
				mSettings.edit().putInt(Common.SETTING_RANDOMNESS,
						Common.DEFAULT_RANDOMNESS).commit();

				mSpinnerQuizType.setSelection(mSettings.getInt(Common.SETTING_QUIZ_TYPE,
						Common.DEFAULT_QUIZ_TYPE));
				mSpinnerFeedCount.setSelection(mAdapterFeedCount.getPosition(mSettings.getString(
						Common.SETTING_FEED_COUNT, Common.DEFAULT_FEED_COUNT)));
				mSpinnerRandomness.setSelection(mSettings.getInt(
						Common.SETTING_RANDOMNESS, Common.DEFAULT_RANDOMNESS));

				return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		setContentView(R.layout.settings);

		mSettings = getSharedPreferences(Common.SETTINGS, Context.MODE_PRIVATE);

		// set up quiz type
		mSpinnerQuizType = (Spinner) findViewById(R.id.spinner_quiz_type);
		ArrayAdapter<CharSequence> adapterQuizType = ArrayAdapter.createFromResource(
				this, R.array.array_quiz_type, android.R.layout.simple_spinner_item);
		adapterQuizType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerQuizType.setAdapter(adapterQuizType);

		mSpinnerQuizType.setSelection(mSettings.getInt(Common.SETTING_QUIZ_TYPE,
				Common.DEFAULT_QUIZ_TYPE));

		mSpinnerQuizType.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> _parent, View _view, int _pos,
					long _id) {
				mSettings.edit().putInt(Common.SETTING_QUIZ_TYPE, _pos).commit();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// do nothing
			}
		});

		// set up feed count
		mSpinnerFeedCount = (Spinner) findViewById(R.id.spinner_feed_count);
		mAdapterFeedCount = ArrayAdapter.createFromResource(this,
				R.array.array_feed_count, android.R.layout.simple_spinner_item);
		mAdapterFeedCount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerFeedCount.setAdapter(mAdapterFeedCount);

		mSpinnerFeedCount.setSelection(mAdapterFeedCount.getPosition(mSettings.getString(
				Common.SETTING_FEED_COUNT, Common.DEFAULT_FEED_COUNT)));

		mSpinnerFeedCount.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> _parent, View _view, int _pos,
					long _id) {
				String newFeedCount = _parent.getItemAtPosition(_pos).toString();
				mSettings.edit().putString(Common.SETTING_FEED_COUNT, newFeedCount).commit();
			}

			public void onNothingSelected(AdapterView<?> _parent) {
				// do nothing
			}
		});

		// set up randomness
		mSpinnerRandomness = (Spinner) findViewById(R.id.spinner_randomness);
		ArrayAdapter<CharSequence> adapterRandomness = ArrayAdapter.createFromResource(
				this, R.array.array_randomness, android.R.layout.simple_spinner_item);
		adapterRandomness.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerRandomness.setAdapter(adapterRandomness);

		mSpinnerRandomness.setSelection(mSettings.getInt(Common.SETTING_RANDOMNESS,
				Common.DEFAULT_RANDOMNESS));

		mSpinnerRandomness.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> _parent, View _view, int _pos,
					long _id) {
				mSettings.edit().putInt(Common.SETTING_RANDOMNESS, _pos).commit();
			}

			public void onNothingSelected(AdapterView<?> _parent) {
				// do nothing
			}
		});

		// set up reset button
		mResetButton = (Button) findViewById(R.id.button_reset_vocab);
		mResetButton.setOnClickListener(new OnClickListener() {

			public void onClick(View _view) {
				// Context context = _view.getContext();

				AlertDialog.Builder builder = new AlertDialog.Builder(_view.getContext());
				builder.setCancelable(false);
				builder.setNegativeButton(getString(R.string.button_no),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

				builder.setMessage(getString(R.string.msg_confirm_delete_all));
				builder.setPositiveButton(getString(R.string.button_yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								VocabularyDbAdapter dbConnector = new VocabularyDbAdapter(
										mContext);
								dbConnector.deleteAllWords();

								Toast.makeText(mContext,
										getString(R.string.msg_all_deleted),
										Toast.LENGTH_LONG).show();
							}
						});
				builder.create().show();

			}
		});

	}
}

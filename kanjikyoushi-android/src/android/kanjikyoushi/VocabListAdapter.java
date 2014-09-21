package android.kanjikyoushi;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class VocabListAdapter extends ResourceCursorAdapter {

	private static final int COL_WORD = 1;
	private static final int COL_STRENGTH = 2;

	public VocabListAdapter(Context _context, Cursor _cursor) {
		super(_context, R.layout.vocab_list_row, _cursor);
	}

	@Override
	public void bindView(View _view, Context _context, Cursor _cursor) {
		// Locate all views we're about to fill
		TextView wordView = (TextView) _view.findViewById(R.id.vocablist_row_word);
		LinearLayout strengthView = (LinearLayout) _view
			.findViewById(R.id.vocablist_row_strength);

		// Set word text
		wordView.setText(_cursor.getString(COL_WORD));

		// add stars
		int strength = _cursor.getInt(COL_STRENGTH);
		strengthView.removeAllViews();
		for (int i = 0; i < strength; i++) {
			// ImageView strengthImage = new ImageView(mContext);
			ImageView strengthImage = new ImageView(_context);
			strengthImage.setImageResource(R.raw.star);
			strengthView.addView(strengthImage);
		}
	}
}

package android.kanjikyoushi;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class VocabStrengthAdapter extends ResourceCursorAdapter {

	private static final int COL_STRENGTH = 0;
	private static final int COL_COUNT = 1;

	public VocabStrengthAdapter(Context _context, Cursor _cursor) {
		super(_context, R.layout.vocab_strength_row, _cursor);
	}

	@Override
	public void bindView(View _view, Context _context, Cursor _cursor) {
		// Locate all views we're about to fill
		TextView countView = (TextView) _view.findViewById(R.id.vocab_strength_row_count);
		LinearLayout strengthView = (LinearLayout) _view
			.findViewById(R.id.vocab_strength_row_strength);

		// Set count text
		countView.setText(_cursor.getString(COL_COUNT));

		// add stars
		int strength = _cursor.getInt(COL_STRENGTH);
		strengthView.removeAllViews();
		if (strength == 0) {
			TextView strengthText = new TextView(_context);
			strengthText.setText(R.string.label_zero_strength);
			strengthView.addView(strengthText);
		} else {
			for (int i = 0; i < strength; i++) {
				ImageView strengthImage = new ImageView(_context);
				strengthImage.setImageResource(R.raw.star);
				strengthView.addView(strengthImage);
			}
		}
	}
}

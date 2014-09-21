package android.kanjikyoushi;

import android.content.Context;

public interface VocabFeedRequestor {
	public abstract void doneFeedLoading();

	public abstract Context getContext();

}

package org.fox.ttrss.offline;

import android.database.sqlite.SQLiteDatabase;

public interface OfflineHeadlinesEventListener {
	void onArticleSelected(int articleId, boolean open);
	void onArticleSelected(int articleId);
}

package name.devnull.ttrss.offline;

import name.devnull.ttrss.GlobalState;

import name.devnull.ttrss.R;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

public class OfflineHeadlinesActivity extends OfflineActivity implements OfflineHeadlinesEventListener {
	@SuppressWarnings("unused")
	private final String TAG = this.getClass().getSimpleName();
	
	protected SharedPreferences m_prefs;
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		m_prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		if (m_prefs.getString("theme", "THEME_DARK").equals("THEME_DARK")) {
			setTheme(R.style.DarkTheme);
		} else {
			setTheme(R.style.LightTheme);
		}
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.headlines);
		
		if (!isCompatMode()) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		setSmallScreen(findViewById(R.id.headlines_fragment) == null); 
		
		if (isPortrait()) {
			findViewById(R.id.headlines_fragment).setVisibility(View.GONE);
		}
		
		if (savedInstanceState == null) {
			Intent i = getIntent();
			
			if (i.getExtras() != null) {
				int feedId = i.getIntExtra("feed", 0);
				boolean isCat = i.getBooleanExtra("isCat", false);
				int articleId = i.getIntExtra("article", 0);
				String searchQuery = i.getStringExtra("searchQuery");
				
				OfflineHeadlinesFragment hf = new OfflineHeadlinesFragment(feedId, isCat);				
				OfflineArticlePager af = new OfflineArticlePager(articleId, feedId, isCat);

				hf.setActiveArticleId(articleId);
				
				hf.setSearchQuery(searchQuery);
				af.setSearchQuery(searchQuery);
				
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

				ft.replace(R.id.headlines_fragment, hf, FRAG_HEADLINES);
				ft.replace(R.id.article_fragment, af, FRAG_ARTICLE);
				
				ft.commit();

				Cursor c;
				
				if (isCat) {
					c = getCatById(feedId);					
				} else {
					c = getFeedById(feedId);
				}
				
				if (c != null) {
					setTitle(c.getString(c.getColumnIndex("title")));
					c.close();
				}

			}
		} 
		
		setLoadingStatus(R.string.blank, false);
		findViewById(R.id.loading_container).setVisibility(View.GONE);
		
		initMenu();
	}

	@Override
	public void onArticleSelected(int articleId, boolean open) {
		SQLiteStatement stmt = getWritableDb().compileStatement(
				"UPDATE articles SET modified = 1, unread = 0 " + "WHERE " + BaseColumns._ID
						+ " = ?");

		stmt.bindLong(1, articleId);
		stmt.execute();
		stmt.close();
		
		if (open) {
			OfflineArticlePager af = (OfflineArticlePager) getSupportFragmentManager().findFragmentByTag(FRAG_ARTICLE);
			
			af.setArticleId(articleId);
		} else {
			OfflineHeadlinesFragment hf = (OfflineHeadlinesFragment) getSupportFragmentManager().findFragmentByTag(FRAG_HEADLINES);
			
			hf.setActiveArticleId(articleId);
		}
		
		GlobalState.getInstance().m_selectedArticleId = articleId;
		
		initMenu();
		refresh();
	}
	
	@Override
	protected void initMenu() {
		super.initMenu();

		if (m_menu != null) {
			m_menu.setGroupVisible(R.id.menu_group_feeds, false);

			OfflineHeadlinesFragment hf = (OfflineHeadlinesFragment)getSupportFragmentManager().findFragmentByTag(FRAG_HEADLINES);
			
			m_menu.setGroupVisible(R.id.menu_group_headlines, hf != null && hf.getSelectedArticleCount() == 0);
			m_menu.setGroupVisible(R.id.menu_group_headlines_selection, hf != null && hf.getSelectedArticleCount() != 0);
			
			Fragment af = getSupportFragmentManager().findFragmentByTag(FRAG_ARTICLE);
			
			m_menu.setGroupVisible(R.id.menu_group_article, af != null);
			
			m_menu.findItem(R.id.search).setVisible(false);
		}		
	}

	@Override
	public void onArticleSelected(int articleId) {
		onArticleSelected(articleId, true);		
	}
}

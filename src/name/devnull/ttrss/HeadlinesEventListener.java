package name.devnull.ttrss;

import name.devnull.ttrss.types.Article;
import name.devnull.ttrss.types.ArticleList;

public interface HeadlinesEventListener {
	void onArticleListSelectionChange(ArticleList m_selectedArticles);
	void onArticleSelected(Article article);
	void onArticleSelected(Article article, boolean open);
	void onHeadlinesLoaded(boolean appended);	
}

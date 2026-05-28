package dwsc.corba.news;


/**
 * Generated from IDL interface "NewsService".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 28 May 2026, 06:50:38
 */

public interface NewsServiceOperations
{
	/* constants */
	/* operations  */
	dwsc.corba.news.NewsItem[] listNews();
	dwsc.corba.news.NewsItem getNews(java.lang.String id);
	void publishNews(dwsc.corba.news.NewsItem _news);
	boolean deleteNews(java.lang.String id);
}

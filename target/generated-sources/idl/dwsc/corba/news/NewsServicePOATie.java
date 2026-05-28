package dwsc.corba.news;

import org.omg.PortableServer.POA;

/**
 * Generated from IDL interface "NewsService".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 28 May 2026, 14:16:34
 */

public class NewsServicePOATie
	extends NewsServicePOA
{
	private NewsServiceOperations _delegate;

	private POA _poa;
	public NewsServicePOATie(NewsServiceOperations delegate)
	{
		_delegate = delegate;
	}
	public NewsServicePOATie(NewsServiceOperations delegate, POA poa)
	{
		_delegate = delegate;
		_poa = poa;
	}
	public dwsc.corba.news.NewsService _this()
	{
		org.omg.CORBA.Object __o = _this_object() ;
		dwsc.corba.news.NewsService __r = dwsc.corba.news.NewsServiceHelper.narrow(__o);
		return __r;
	}
	public dwsc.corba.news.NewsService _this(org.omg.CORBA.ORB orb)
	{
		org.omg.CORBA.Object __o = _this_object(orb) ;
		dwsc.corba.news.NewsService __r = dwsc.corba.news.NewsServiceHelper.narrow(__o);
		return __r;
	}
	public NewsServiceOperations _delegate()
	{
		return _delegate;
	}
	public void _delegate(NewsServiceOperations delegate)
	{
		_delegate = delegate;
	}
	public POA _default_POA()
	{
		if (_poa != null)
		{
			return _poa;
		}
		return super._default_POA();
	}
	public boolean deleteNews(java.lang.String id)
	{
		return _delegate.deleteNews(id);
	}

	public dwsc.corba.news.NewsItem getNews(java.lang.String id)
	{
		return _delegate.getNews(id);
	}

	public void publishNews(dwsc.corba.news.NewsItem _news)
	{
_delegate.publishNews(_news);
	}

	public dwsc.corba.news.NewsItem[] listNews()
	{
		return _delegate.listNews();
	}

}

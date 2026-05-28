package dwsc.corba.news;

/**
 * Generated from IDL alias "NewsList".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 28 May 2026, 14:16:34
 */

public final class NewsListHolder
	implements org.omg.CORBA.portable.Streamable
{
	public dwsc.corba.news.NewsItem[] value;

	public NewsListHolder ()
	{
	}
	public NewsListHolder (final dwsc.corba.news.NewsItem[] initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return NewsListHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = NewsListHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		NewsListHelper.write (out,value);
	}
}

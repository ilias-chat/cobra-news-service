package dwsc.corba.news;

/**
 * Generated from IDL struct "NewsItem".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 28 May 2026, 13:54:33
 */

public final class NewsItemHolder
	implements org.omg.CORBA.portable.Streamable
{
	public dwsc.corba.news.NewsItem value;

	public NewsItemHolder ()
	{
	}
	public NewsItemHolder(final dwsc.corba.news.NewsItem initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return dwsc.corba.news.NewsItemHelper.type ();
	}
	public void _read(final org.omg.CORBA.portable.InputStream _in)
	{
		value = dwsc.corba.news.NewsItemHelper.read(_in);
	}
	public void _write(final org.omg.CORBA.portable.OutputStream _out)
	{
		dwsc.corba.news.NewsItemHelper.write(_out, value);
	}
}

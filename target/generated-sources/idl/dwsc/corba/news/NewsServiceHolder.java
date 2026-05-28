package dwsc.corba.news;

/**
 * Generated from IDL interface "NewsService".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 28 May 2026, 14:03:02
 */

public final class NewsServiceHolder	implements org.omg.CORBA.portable.Streamable{
	 public NewsService value;
	public NewsServiceHolder()
	{
	}
	public NewsServiceHolder (final NewsService initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type()
	{
		return NewsServiceHelper.type();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = NewsServiceHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		NewsServiceHelper.write (_out,value);
	}
}

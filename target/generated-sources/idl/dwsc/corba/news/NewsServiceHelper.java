package dwsc.corba.news;


/**
 * Generated from IDL interface "NewsService".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 28 May 2026, 14:03:02
 */

public abstract class NewsServiceHelper
{
	private volatile static org.omg.CORBA.TypeCode _type;
	public static org.omg.CORBA.TypeCode type ()
	{
		if (_type == null)
		{
			synchronized(NewsServiceHelper.class)
			{
				if (_type == null)
				{
					_type = org.omg.CORBA.ORB.init().create_interface_tc("IDL:dwsc/corba/news/NewsService:1.0", "NewsService");
				}
			}
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final dwsc.corba.news.NewsService s)
	{
			any.insert_Object(s);
	}
	public static dwsc.corba.news.NewsService extract(final org.omg.CORBA.Any any)
	{
		return narrow(any.extract_Object()) ;
	}
	public static String id()
	{
		return "IDL:dwsc/corba/news/NewsService:1.0";
	}
	public static NewsService read(final org.omg.CORBA.portable.InputStream in)
	{
		return narrow(in.read_Object(dwsc.corba.news._NewsServiceStub.class));
	}
	public static void write(final org.omg.CORBA.portable.OutputStream _out, final dwsc.corba.news.NewsService s)
	{
		_out.write_Object(s);
	}
	public static dwsc.corba.news.NewsService narrow(final org.omg.CORBA.Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		else if (obj instanceof dwsc.corba.news.NewsService)
		{
			return (dwsc.corba.news.NewsService)obj;
		}
		else if (obj._is_a("IDL:dwsc/corba/news/NewsService:1.0"))
		{
			dwsc.corba.news._NewsServiceStub stub;
			stub = new dwsc.corba.news._NewsServiceStub();
			stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
			return stub;
		}
		else
		{
			throw new org.omg.CORBA.BAD_PARAM("Narrow failed");
		}
	}
	public static dwsc.corba.news.NewsService unchecked_narrow(final org.omg.CORBA.Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		else if (obj instanceof dwsc.corba.news.NewsService)
		{
			return (dwsc.corba.news.NewsService)obj;
		}
		else
		{
			dwsc.corba.news._NewsServiceStub stub;
			stub = new dwsc.corba.news._NewsServiceStub();
			stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
			return stub;
		}
	}
}

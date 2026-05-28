package dwsc.corba.news;


/**
 * Generated from IDL struct "NewsItem".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 28 May 2026, 13:27:19
 */

public abstract class NewsItemHelper
{
	private volatile static org.omg.CORBA.TypeCode _type;
	public static org.omg.CORBA.TypeCode type ()
	{
		if (_type == null)
		{
			synchronized(NewsItemHelper.class)
			{
				if (_type == null)
				{
					_type = org.omg.CORBA.ORB.init().create_struct_tc(dwsc.corba.news.NewsItemHelper.id(),"NewsItem",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("id", org.omg.CORBA.ORB.init().create_string_tc(0), null),new org.omg.CORBA.StructMember("title", org.omg.CORBA.ORB.init().create_string_tc(0), null),new org.omg.CORBA.StructMember("content", org.omg.CORBA.ORB.init().create_string_tc(0), null),new org.omg.CORBA.StructMember("date", org.omg.CORBA.ORB.init().create_string_tc(0), null)});
				}
			}
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final dwsc.corba.news.NewsItem s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}

	public static dwsc.corba.news.NewsItem extract (final org.omg.CORBA.Any any)
	{
		org.omg.CORBA.portable.InputStream in = any.create_input_stream();
		try
		{
			return read (in);
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (java.io.IOException e)
			{
			throw new RuntimeException("Unexpected exception " + e.toString() );
			}
		}
	}

	public static String id()
	{
		return "IDL:dwsc/corba/news/NewsItem:1.0";
	}
	public static dwsc.corba.news.NewsItem read (final org.omg.CORBA.portable.InputStream in)
	{
		dwsc.corba.news.NewsItem result = new dwsc.corba.news.NewsItem();
		result.id=in.read_string();
		result.title=in.read_string();
		result.content=in.read_string();
		result.date=in.read_string();
		return result;
	}
	public static void write (final org.omg.CORBA.portable.OutputStream out, final dwsc.corba.news.NewsItem s)
	{
		java.lang.String tmpResult0 = s.id;
out.write_string( tmpResult0 );
		java.lang.String tmpResult1 = s.title;
out.write_string( tmpResult1 );
		java.lang.String tmpResult2 = s.content;
out.write_string( tmpResult2 );
		java.lang.String tmpResult3 = s.date;
out.write_string( tmpResult3 );
	}
}

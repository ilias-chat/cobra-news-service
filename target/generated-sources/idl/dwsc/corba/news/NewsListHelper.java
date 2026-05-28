package dwsc.corba.news;

/**
 * Generated from IDL alias "NewsList".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 28 May 2026, 13:42:59
 */

public abstract class NewsListHelper
{
	private volatile static org.omg.CORBA.TypeCode _type;

	public static void insert (org.omg.CORBA.Any any, dwsc.corba.news.NewsItem[] s)
	{
		any.type (type ());
		write (any.create_output_stream (), s);
	}

	public static dwsc.corba.news.NewsItem[] extract (final org.omg.CORBA.Any any)
	{
		if ( any.type().kind() == org.omg.CORBA.TCKind.tk_null)
		{
			throw new org.omg.CORBA.BAD_OPERATION ("Can't extract from Any with null type.");
		}
		return read (any.create_input_stream ());
	}

	public static org.omg.CORBA.TypeCode type ()
	{
		if (_type == null)
		{
			synchronized(NewsListHelper.class)
			{
				if (_type == null)
				{
					_type = org.omg.CORBA.ORB.init().create_alias_tc(dwsc.corba.news.NewsListHelper.id(), "NewsList",org.omg.CORBA.ORB.init().create_sequence_tc(0, org.omg.CORBA.ORB.init().create_struct_tc(dwsc.corba.news.NewsItemHelper.id(),"NewsItem",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("id", org.omg.CORBA.ORB.init().create_string_tc(0), null),new org.omg.CORBA.StructMember("title", org.omg.CORBA.ORB.init().create_string_tc(0), null),new org.omg.CORBA.StructMember("content", org.omg.CORBA.ORB.init().create_string_tc(0), null),new org.omg.CORBA.StructMember("date", org.omg.CORBA.ORB.init().create_string_tc(0), null)})));
				}
			}
		}
		return _type;
	}

	public static String id()
	{
		return "IDL:dwsc/corba/news/NewsList:1.0";
	}
	public static dwsc.corba.news.NewsItem[] read (final org.omg.CORBA.portable.InputStream _in)
	{
		dwsc.corba.news.NewsItem[] _result;
		int _l_result0 = _in.read_long();
		try
		{
			 int x = _in.available();
			 if ( x > 0 && _l_result0 > x )
				{
					throw new org.omg.CORBA.MARSHAL("Sequence length too large. Only " + x + " available and trying to assign " + _l_result0);
				}
		}
		catch (java.io.IOException e)
		{
		}
		_result = new dwsc.corba.news.NewsItem[_l_result0];
		for (int i=0;i<_result.length;i++)
		{
			_result[i]=dwsc.corba.news.NewsItemHelper.read(_in);
		}

		return _result;
	}

	public static void write (final org.omg.CORBA.portable.OutputStream _out, dwsc.corba.news.NewsItem[] _s)
	{
		
		_out.write_long(_s.length);
		for (int i=0; i<_s.length;i++)
		{
			dwsc.corba.news.NewsItemHelper.write(_out,_s[i]);
		}

	}
}

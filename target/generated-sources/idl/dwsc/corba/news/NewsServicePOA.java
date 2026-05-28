package dwsc.corba.news;


/**
 * Generated from IDL interface "NewsService".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 28 May 2026, 04:22:58
 */

public abstract class NewsServicePOA
	extends org.omg.PortableServer.Servant
	implements org.omg.CORBA.portable.InvokeHandler, dwsc.corba.news.NewsServiceOperations
{
	static private final java.util.HashMap<String,Integer> m_opsHash = new java.util.HashMap<String,Integer>();
	static
	{
		m_opsHash.put ( "deleteNews", Integer.valueOf(0));
		m_opsHash.put ( "getNews", Integer.valueOf(1));
		m_opsHash.put ( "publishNews", Integer.valueOf(2));
		m_opsHash.put ( "listNews", Integer.valueOf(3));
	}
	private String[] ids = {"IDL:dwsc/corba/news/NewsService:1.0"};
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
	public org.omg.CORBA.portable.OutputStream _invoke(String method, org.omg.CORBA.portable.InputStream _input, org.omg.CORBA.portable.ResponseHandler handler)
		throws org.omg.CORBA.SystemException
	{
		org.omg.CORBA.portable.OutputStream _out = null;
		// do something
		// quick lookup of operation
		java.lang.Integer opsIndex = (java.lang.Integer)m_opsHash.get ( method );
		if ( null == opsIndex )
			throw new org.omg.CORBA.BAD_OPERATION(method + " not found");
		switch ( opsIndex.intValue() )
		{
			case 0: // deleteNews
			{
				java.lang.String _arg0=_input.read_string();
				_out = handler.createReply();
				_out.write_boolean(deleteNews(_arg0));
				break;
			}
			case 1: // getNews
			{
				java.lang.String _arg0=_input.read_string();
				_out = handler.createReply();
				dwsc.corba.news.NewsItemHelper.write(_out,getNews(_arg0));
				break;
			}
			case 2: // publishNews
			{
				dwsc.corba.news.NewsItem _arg0=dwsc.corba.news.NewsItemHelper.read(_input);
				_out = handler.createReply();
				publishNews(_arg0);
				break;
			}
			case 3: // listNews
			{
				_out = handler.createReply();
				dwsc.corba.news.NewsListHelper.write(_out,listNews());
				break;
			}
		}
		return _out;
	}

	public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id)
	{
		return ids;
	}
}

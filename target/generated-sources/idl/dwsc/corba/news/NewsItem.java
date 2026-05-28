package dwsc.corba.news;

/**
 * Generated from IDL struct "NewsItem".
 *
 * @author JacORB IDL compiler V 3.9
 * @version generated at 28 May 2026, 14:03:02
 */

public final class NewsItem
	implements org.omg.CORBA.portable.IDLEntity
{
	/** Serial version UID. */
	private static final long serialVersionUID = 1L;
	public NewsItem(){}
	public java.lang.String id = "";
	public java.lang.String title = "";
	public java.lang.String content = "";
	public java.lang.String date = "";
	public NewsItem(java.lang.String id, java.lang.String title, java.lang.String content, java.lang.String date)
	{
		this.id = id;
		this.title = title;
		this.content = content;
		this.date = date;
	}
}

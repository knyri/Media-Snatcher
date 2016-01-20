/**
 *
 */
package picSnatcher.mediaSnatcher.extension.parser;

import picSnatcher.mediaSnatcher.PageParser;
import picSnatcher.mediaSnatcher.PageReader;
import picSnatcher.mediaSnatcher.Session;
import picSnatcher.mediaSnatcher.extension.OptionPanel;
import simple.net.Uri;
import simple.parser.ml.Page;

/**
 * <hr>
 * <br>Created: Oct 15, 2011
 * @author Kenneth Pierce
 */
public final class VideoFap extends PageParser{

	/**
	 * @param preader
	 * @param session
	 */
	public VideoFap(PageReader preader,Session session){
		super(preader,session);
	}

	/**
	 * @param link
	 * @param page
	 * @return
	 * @see picSnatcher.mediaSnatcher.mediaSnatcher4.PageParser#canHandle(simple.net.Uri, simple.parser.ml.Page)
	 */
	@Override
	public boolean canHandle(Uri link,Page page,String mime){
		return link.getDomain().toLowerCase().equals("videofap.com") && "text/html".equals(mime);
	}

	/**
	 * @return
	 * @see picSnatcher.mediaSnatcher.mediaSnatcher4.PageParser#hasOptions()
	 */
	@Override
	public boolean hasOptions(){
		return false;
	}

	/**
	 * @return
	 * @see picSnatcher.mediaSnatcher.mediaSnatcher4.PageParser#getOptions()
	 */
	@Override
	public OptionPanel getOptions(){
		return null;
	}
	@Override
	public boolean saveOptions(){return true;}

	/**
	 * @param source
	 * @param page
	 * @param referer
	 * @param title
	 * @param basehref
	 * @param depth
	 * @return
	 * @see picSnatcher.mediaSnatcher.mediaSnatcher4.PageParser#processPage(simple.parser.ml.Page, simple.net.Uri, simple.net.Uri, java.lang.String, java.lang.String, int)
	 */
	@Override
	protected boolean processPage(Page source,Uri page,Uri referer,String title,String basehref,int depth){
		/*
		 * get input elements. Find one with id="config"
		 * visit value and parse the XML page.
		 * add the videoLink.getChild(0).getValue() to download list
		 */
		return false;
	}

	@Override
	protected void reset(){
		// TODO Auto-generated method stub

	}
}

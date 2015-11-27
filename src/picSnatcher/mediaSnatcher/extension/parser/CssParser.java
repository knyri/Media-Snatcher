package picSnatcher.mediaSnatcher.extension.parser;

import picSnatcher.mediaSnatcher.PageParser;
import picSnatcher.mediaSnatcher.PageReader;
import picSnatcher.mediaSnatcher.Session;
import picSnatcher.mediaSnatcher.extension.OptionPanel;
import simple.net.Uri;
import simple.parser.ml.Page;

/**
 * TODO: This
 * @author Kenneth Pierce
 *
 */
public class CssParser extends PageParser{

	public CssParser(PageReader preader,Session session){
		super(preader,session);
	}

	@Override
	public boolean canHandle(Uri link,Page page,String mime){
		return "text/css".equals(mime) || link.getFile().toLowerCase().endsWith(".css");
	}

	@Override
	public boolean hasOptions(){return false;}

	@Override
	public OptionPanel getOptions(){return null;}
	@Override
	public boolean saveOptions(){return true;}

	@Override
	protected boolean processPage(Page source,Uri page,Uri referer,String title,String basehref,int depth){
		// TODO Auto-generated method stub
		return false;
	}

}

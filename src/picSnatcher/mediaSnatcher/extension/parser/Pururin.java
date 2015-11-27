package picSnatcher.mediaSnatcher.extension.parser;

import picSnatcher.mediaSnatcher.Constants;
import picSnatcher.mediaSnatcher.PageParser;
import picSnatcher.mediaSnatcher.PageReader;
import picSnatcher.mediaSnatcher.Session;
import picSnatcher.mediaSnatcher.extension.OptionPanel;
import simple.net.Uri;
import simple.parser.ml.Page;
import simple.parser.ml.Tag;
import simple.util.UrlUtil;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

public class Pururin extends PageParser{
	private final Log log=LogFactory.getLogFor(Pururin.class);
	public Pururin(PageReader preader,Session session){
		super(preader,session);
	}

	@Override
	public boolean canHandle(Uri link,Page page,String mime){
		return "pururin.com".equalsIgnoreCase(link.getHost());
	}

	@Override
	public boolean hasOptions(){
		return false;
	}

	@Override
	public OptionPanel getOptions(){
		return null;
	}
	@Override
	public boolean saveOptions(){return true;}

	@Override
	protected boolean processPage(Page source,Uri page,Uri referer,String title,String basehref,int depth){
		if(depth==0)depth=1;
		Uri tmp;
		String link;
		for (Tag tag : source.getTags(Constants.tag_a)){
			if("image-next".equals(tag.getProperty(Constants.atr_class))){
				link=createURL(tag.getProperty(Constants.atr_href),page,basehref);
				if(link!=null){
					link=UrlUtil.URLescape2(link);
					tmp=new Uri(link,page.getScheme());
					addToReadQueue(tmp,page,depth-1);
				}
				link=tag.getChildAt(0).getProperty(Constants.atr_src);
				link=createURL(link,page,basehref);
				if(link==null) break;
				link=UrlUtil.URLescape2(link);
				tmp=new Uri(link,page.getScheme());
				session.addLink(tmp,page,tag.getName().equals(Constants.tag_img));
				break;
			}
		}

		return true;
	}

}

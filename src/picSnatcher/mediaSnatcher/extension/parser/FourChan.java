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

public class FourChan extends PageParser{

	private static final Log log=LogFactory.getLogFor(FourChan.class);
	static{
		log.setPrintTime(true);
		log.setPrintDebug(true);
	}
	public FourChan(final PageReader preader,final Session session){
		super(preader,session);
	}

	@Override
	protected void reset(){}

	@Override
	public boolean canHandle(Uri link,Page page,String mime){
		return "4chan.org".equalsIgnoreCase(link.getDomain());
	}

	@Override
	public boolean hasOptions(){
		return false;
	}

	@Override
	public boolean saveOptions(){
		return true;
	}

	@Override
	public OptionPanel getOptions(){
		return null;
	}

	@Override
	protected boolean processPage(Page source,Uri page,Uri referer,String title,String basehref,int depth){
		log.debug("parsing",page);
//		log.debug(source.toString());
		Uri tmp;
		String link= null;
		int lcount= 0;

		this.addNextLinks(source, page);
		for(final Tag tag: source.getTags(Constants.tag_a)){
			if(!isRunning()) break;
			if("fileThumb".equals(tag.getProperty(Constants.atr_class))){
				continue;
			}
			if(tag.hasProperty(Constants.atr_href)){
				link=tag.getProperty(Constants.atr_href);
			}else{
				//no useful attributes, skip
				continue;
			}
			// log.debug(tag.toStringTagOnly());
			// log.debug("before",link);
			link=createURL(link,page,basehref);
			if(link==null){
				continue;
			}
			lcount++;
			session.setCurrentProgressBarText("Links:" + lcount);
			link= UrlUtil.URLescape2(link);
			// log.debug("after",link);
			tmp= new Uri(link,page.getScheme());
			if(tag.hasProperty(Constants.atr_href)){
				addToReadQueue(tmp, page);
			}
			String file= tmp.getFile();
//			log.debug(file + " --- " + tmp);
			if((file.isBlank() || file.indexOf('.') == -1)){
				session.addLink(tmp, page, false);
			}else{
				log.debug(tag.toString());
				log.debug(tag.getTextContent());
				if(tag.hasProperty(Constants.atr_title)){
					session.addLink(tmp, page, tmp.getFile().substring(0,tmp.getFile().lastIndexOf('.')) + "_" + tag.getProperty(Constants.atr_title).replace("%20"," ").trim(),false);
				}else{
					session.addLink(tmp, page, tmp.getFile().substring(0,tmp.getFile().lastIndexOf('.')) + "_" + tag.getTextContent().replace("%20"," ").trim(),false);
				}
			}
		}// end for
		return true;
	}

}

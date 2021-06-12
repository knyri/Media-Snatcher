/**
 *
 */
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
import simple.util.do_str;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

/** Default Parser.
 * <br>
 * Created: Jan 31, 2009
 *
 * @author Kenneth Pierce
 */
public class GeneralParser extends PageParser{
	/**
	 * @param preader
	 * @param session
	 * @param site
	 */
	public GeneralParser(final PageReader preader,final Session session){
		super(preader,session);
	}

	private static final Log log=LogFactory.getLogFor(GeneralParser.class);
	static{
		log.setPrintTime(true);
		log.setPrintDebug(true);
	}

	@Override
	protected boolean processPage(final Page source,final Uri page,final Uri referer,final String title,final String basehref,final int depth){
		log.debug("parsing",page);
//		log.debug(source.toString());
		//TODO: PageParser.preproccessPage(source,page). Then loop through following the pattern started on line 74
		Uri tmp;
		String link= null;
		int lcount= 0;
		// XXX: start processing
		this.addNextLinks(source, page);
		for(final Tag tag:source){
			if(!isRunning()) break;
			if(tag.hasProperty(Constants.atr_src)){
				if(tag.getName().equals(Constants.tag_img)){
					if(!tag.hasProperty(Constants.atr_src)){
						log.warning("Img missing src attribute", tag.toStringTagOnly());
						continue;
					}
					if(!wantedImageSize(tag)){
						continue;
					}
				}
				link= tag.getProperty(Constants.atr_src);
			}else if(tag.hasProperty(Constants.atr_href)){
				if(do_str.CI.startsWith(tag.getProperty(Constants.atr_href),"javascript",0)||tag.getProperty(Constants.atr_href)=="#"){
					link=PageParser.getLinkFromJavascript(tag);
					if(link==null) continue;
				}else{
					link=tag.getProperty(Constants.atr_href);
				}
			}else if(tag.hasProperty(Constants.atr_background)){
				link=tag.getProperty(Constants.atr_background);
			}else{
				//no useful attributes, skip
				//TODO: javascript check for things like document.location and open window
				continue;
			}
			// log.debug(tag.toStringTagOnly());
			// log.debug("before",link);
			link=createURL(link,page,basehref);
			if(link==null){
				continue;
			}
			lcount++;
			session.setCurrentProgressBarText("Links:"+lcount);
			link=UrlUtil.URLescape2(link);
			// log.debug("after",link);
			tmp=new Uri(link,page.getScheme());
			if(tag.getName().equals(Constants.tag_iframe)){
				addToReadQueueForce(tmp, page);
			}else
			if(tag.hasProperty(Constants.atr_href)){
				addToReadQueue(tmp, page);
			}
			session.addLink(tmp, page, tag.getName().equals(Constants.tag_img));
		}// end for
		return true;
	}

	@Override
	public boolean canHandle(final Uri link,final Page page,String mime){
		return true;
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
	protected void reset(){
		// TODO Auto-generated method stub

	}
}

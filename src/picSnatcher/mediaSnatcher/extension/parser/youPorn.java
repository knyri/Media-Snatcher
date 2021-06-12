package picSnatcher.mediaSnatcher.extension.parser;

import java.util.List;

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

public class youPorn extends PageParser{
	private static final Log log=LogFactory.getLogFor(youPorn.class);
	static{
		log.setPrintTime(true);
		log.setPrintDebug(true);
	}


	public youPorn(PageReader preader,Session session){
		super(preader,session);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean canHandle(Uri link,Page page,String mime){
		return link.getDomain().equalsIgnoreCase("youporn.com");
	}

	@Override
	public boolean hasOptions(){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveOptions(){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OptionPanel getOptions(){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean processPage(Page source,Uri page,Uri referer,String title,String basehref,int depth){
		List<Tag> videos= source.getTags(Constants.tag_video);
		log.debug("videos", videos);
		for(Tag video: videos){
			Uri tmp= new Uri(video.getProperty(Constants.atr_src));
			session.addLink(tmp, page, true);
		}
		Uri tmp;
		String link=null;
		int lcount=0;
		// XXX: start processing
		for(final Tag tag : source.getTags(Constants.tag_a)){
			if(!isRunning()){
				break;
			}
			if(tag.hasProperty(Constants.atr_href)){
				if(do_str.CI.startsWith(tag.getProperty(Constants.atr_href),"javascript",0)||tag.getProperty(Constants.atr_href)=="#"){
					link= PageParser.getLinkFromJavascript(tag);
					if(link == null){
						continue;
					}
				}else{
					link= tag.getProperty(Constants.atr_href);
				}
			}else{
				//no useful attributes, skip
				//TODO: javascript check for things like document.location and open window
				continue;
			}
			// log.debug(tag.toStringTagOnly());
			// log.debug("before",link);
			link= createURL(link,page,basehref);
			if(link == null){
				continue;
			}
			lcount++;
			session.setCurrentProgressBarText("Links:"+lcount);
			link= UrlUtil.URLescape2(link);
			// log.debug("after",link);
			tmp= new Uri(link,page.getScheme());
			if(tag.getName().equals(Constants.tag_iframe)){
				addToReadQueueForce(tmp, page);
			}else
			if(tag.hasProperty(Constants.atr_href)){
				addToReadQueue(tmp, page);
			}
			//session.addLink(tmp,page,false);
		}
		return true;
	}

	@Override
	protected void reset(){
		// TODO Auto-generated method stub

	}
}

package picSnatcher.mediaSnatcher.extension.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import picSnatcher.mediaSnatcher.Constants;
import picSnatcher.mediaSnatcher.OptionKeys;
import picSnatcher.mediaSnatcher.PageParser;
import picSnatcher.mediaSnatcher.PageReader;
import picSnatcher.mediaSnatcher.Session;
import picSnatcher.mediaSnatcher.extension.OptionPanel;
import simple.net.Uri;
import simple.parser.ml.Page;
import simple.parser.ml.Tag;
import simple.util.FixedSizeArrayList;
import simple.util.UrlUtil;
import simple.util.do_str;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

public class e621_net extends PageParser{
	private static final Log log=LogFactory.getLogFor(e621_net.class);
	static{
		log.setPrintTime(true);
		log.setPrintDebug(true);
	}
	private static final OptionPanel optionPanel= new OptionPanel("e621.net", e621_net.class.getClassLoader().getResourceAsStream("e621_netOptions.xml"));
	private final List<WantedFilter> wanted= new ArrayList<>();
	private final List<IgnoreFilter> ignored= new ArrayList<>();
	public e621_net(final PageReader preader,final Session session){
		super(preader,session);
		String text= optionPanel.getItemValue("wanted");
		for(String row: text.split("\n")){
			wanted.add(new WantedFilter(new FixedSizeArrayList<String>(row.split(" "))));
		}
		text= optionPanel.getItemValue("ignored");
		for(String row: text.split("\n")){
			List<String> ignore= new LinkedList<>(),
				exception= new LinkedList<>();
			for(String tag: row.split(" ")){
				if(tag.charAt(0) == '-'){
					exception.add(tag.substring(1));
				}else{
					ignore.add(tag);
				}
			}
			ignored.add(new IgnoreFilter(ignore,exception));
		}
	}

	@Override
	public boolean canHandle(Uri link,Page page,String mime){
		return "e621.net".equalsIgnoreCase(link.getDomain());
	}

	@Override
	public boolean hasOptions(){
		return true;
	}

	@Override
	public boolean saveOptions(){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OptionPanel getOptions(){
		return optionPanel;
	}

	@Override
	protected boolean processPage(Page source,Uri page,Uri referer,String title,String basehref,int depth){
		log.debug("parsing",page);
		//log.debug(source.toString());
		//TODO: PageParser.preproccessPage(source,page). Then loop through following the pattern started on line 74
		Uri tmpLink;
		String link=null;
		int lcount=0;
		// XXX: start processing
		for(final Tag tag:source){
			if(!isRunning()) break;
			if(tag.hasProperty(Constants.atr_src)){
				if(tag.getName().equals(Constants.tag_img)){
					if(!tag.hasProperty(Constants.atr_src)){
						log.warning("Img missing src attribute", tag.toStringTagOnly());
						continue;
					}
					String size;
					if(tag.hasProperty(Constants.atr_width)){
						size= tag.getProperty(Constants.atr_width);
						if(size.endsWith("px") && size.length() > 2){
							size= size.substring(0,size.length()-2);
						}
						try{
							if(Integer.parseInt(size)<Integer.parseInt(session.getOptions().getOption(OptionKeys.snatcher_minImgWidth))){
								continue;
							}
						}catch(NumberFormatException e){
							log.warning("image width not a number", tag.getProperty(Constants.atr_width));
						}
					}
					if(tag.hasProperty(Constants.atr_height)){
						size= tag.getProperty(Constants.atr_height);
						if(size.endsWith("px") && size.length() > 2){
							size= size.substring(0,size.length()-2);
						}
						try{
							if(Integer.parseInt(size)<Integer.parseInt(session.getOptions().getOption(OptionKeys.snatcher_minImgHeight))){
								continue;
							}
						}catch(NumberFormatException e){
							log.warning("image height not a number", tag.getProperty(Constants.atr_height));
						}
					}
				}
				link=tag.getProperty(Constants.atr_src);
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
			tmpLink=new Uri(link,page.getScheme());
			if(tag.getName().equals(Constants.tag_a) && tmpLink.getPath().startsWith("/post/show/")){
				Tag img= null;
				for(Tag t: tag){
					if(t.getName().equals(Constants.tag_img)){
						img= t;
						break;
					}
				}
				if(img != null){
					String rawtags= null;
					if(img.hasProperty(Constants.atr_title)){
						rawtags= img.getProperty(Constants.atr_title);
					}else if(img.hasProperty(Constants.atr_alt)){
						rawtags= img.getProperty(Constants.atr_alt);
					}
					if(rawtags != null){
						int newline= rawtags.indexOf('\n');
						if(newline != -1){
							String[] tags= rawtags.substring(0,newline).trim().split(" ");
							HashSet<String> imgTags= new HashSet<>();
							for(String stag: tags){
								imgTags.add(stag);
							}
							boolean wantedFlag= !wanted.isEmpty();
							for(WantedFilter filter: wanted){
								if(filter.test(imgTags)){
									wantedFlag= true;
									break;
								}
							}
							if(wantedFlag){
								for(IgnoreFilter filter: ignored){
									if(filter.test(imgTags)){
										wantedFlag= false;
										break;
									}
								}
							}
							if(!wantedFlag){
								continue;
							}
						}
					}
				}
			}
			if(tag.getName().equals(Constants.tag_iframe)){
				addToReadQueueForce(tmpLink,page,depth);
			}else
			if(tag.hasProperty(Constants.atr_href)){
				addToReadQueue(tmpLink,page,depth);
			}
			session.addLink(tmpLink,page,tag.getName().equals(Constants.tag_img));
		}// end for
		return true;
	}

	static class WantedFilter{
		private static final Log log= LogFactory.getLogFor(WantedFilter.class);
		static{
			log.setPrintTime(true);
			log.setPrintDebug(true);
		}
		final List<String> wanted;
		public WantedFilter(List<String> want){
			log.debug("new wanted filter", want);
			wanted=want;
		}
		public boolean test(HashSet<String> tags){
			return tags.containsAll(wanted);
		}
	}
	static class IgnoreFilter{
		private static final Log log= LogFactory.getLogFor(IgnoreFilter.class);
		static{
			log.setPrintTime(true);
			log.setPrintDebug(true);
		}
		final List<String> ignore;
		final List<String> exceptions;
		public IgnoreFilter(List<String> nowant, List<String> unless){
			log.debug("new ignore filter", nowant);
			log.debug("unless", unless);
			ignore= nowant;
			exceptions= unless;
		}
		public boolean test(HashSet<String> tags){
			if(!tags.containsAll(ignore)){
				log.debug("not ignored: didn't match", tags);
				return false;
			}
			for(String exception : exceptions){
				if(tags.contains(exception)){
					log.debug("not ignored: had exception", tags);
					return false;
				}
			}
			log.debug("ignored", tags);
			return true;
		}
	}
	@Override
	protected void reset(){
		// TODO Auto-generated method stub

	}
}
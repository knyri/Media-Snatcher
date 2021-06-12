package picSnatcher.mediaSnatcher.extension.parser.booru;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import picSnatcher.mediaSnatcher.Constants;
import picSnatcher.mediaSnatcher.PageParser;
import picSnatcher.mediaSnatcher.PageReader;
import picSnatcher.mediaSnatcher.Session;
import picSnatcher.mediaSnatcher.extension.OptionPanel;
import picSnatcher.mediaSnatcher.extension.OptionPanelListener;
import simple.collections.FixedSizeArrayList;
import simple.net.Uri;
import simple.parser.ml.Page;
import simple.parser.ml.Tag;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

public class BooruParser  extends PageParser implements OptionPanelListener{
	private static final OptionPanel optionPanel= new OptionPanel("Booru Sites", BooruParser.class.getClassLoader().getResourceAsStream("booruOptions.xml"));
	private final List<WantedFilter> wanted= new ArrayList<>();
	private final List<IgnoreFilter> ignored= new ArrayList<>();
	private final List<String> sites= new ArrayList<>();
	private static final Log log=LogFactory.getLogFor(BooruParser.class);
	static{
		log.setPrintTime(true);
		log.setPrintDebug(true);
	}

	public BooruParser(PageReader preader, Session session){
		super(preader,session);
		optionPanel.addListener(this);
	}
	@Override
	public boolean canHandle(Uri link,Page page,String mime){
		String domain= link.getDomain().toLowerCase();
		for(String s: sites){
			if(domain.equals(s)){
				return true;
			}
		}
		return false;
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
//		log.debug(source.toString());
//		log.debug(page.getQuery());
		if(page.getQuery().startsWith("page=post&s=view")){
			boolean imgFound= false;
			for(Tag a: source.getTags(Constants.tag_a)){
				if("original image".equalsIgnoreCase(a.getTextContent().trim())){
					imgFound= true;
					session.addLink(new Uri(createURL(a.getProperty(Constants.atr_href),page,basehref)), page, true);
					break;
				}
			}
			if(!imgFound){
				for(Tag img: source.getTags(Constants.tag_img)){
					if("image".equals(img.getProperty(Constants.atr_id))){
						log.debug("Adding", img.getProperty(Constants.atr_src));
						session.addLink(new Uri(img.getProperty(Constants.atr_src),page.getScheme()), page, true);
					}
				}
			}
		}else{
			for(Tag a: source.getTags(Constants.tag_a)){
				if(a.getProperty(Constants.atr_href).contains("index.php?page=post&amp;s=view") || a.getProperty(Constants.atr_href).contains("index.php?page=post&s=view")){
					log.information("testing",a.getProperty(Constants.atr_href));
					for(Tag img: a.findTag(Constants.tag_img)){
						log.information("tags",img.getProperty(Constants.atr_alt));
						String[] tags= img.getProperty(Constants.atr_alt).trim().split(" ");
						HashSet<String> imgTags= new HashSet<>();
						for(String stag: tags){
							imgTags.add(stag);
						}
						boolean wantedFlag= wanted.isEmpty();
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
						addToCurrentReadQueue(new Uri(createURL(a.getProperty(Constants.atr_href),page,basehref)),page);
					}
				}else if("next".equals(a.getProperty(Constants.atr_alt))){
					log.debug("next alt tag", new Uri(createURL(a.getProperty(Constants.atr_href),page,basehref)).toString());
					addToCurrentReadQueue(new Uri(createURL(a.getProperty(Constants.atr_href),page,basehref)),page);
				}
			}
		}
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
			if(ignore.isEmpty() || !tags.containsAll(ignore)){
//				log.debug("not ignored: didn't match", tags);
				return false;
			}
			for(String exception : exceptions){
				if(tags.contains(exception)){
//					log.debug("not ignored: had exception", tags);
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

	@Override
	public void panelOpened(OptionPanel panel){
		// TODO Auto-generated method stub

	}
	@Override
	public void panelLoaded(OptionPanel p){
		panelClosed(p);
	}
	@Override
	public void panelClosed(OptionPanel panel){
		wanted.clear();
		ignored.clear();
		String text= optionPanel.getItemValue("wanted");
		for(String row: text.split("\n")){
			row= row.trim();
			if(row.isEmpty()){
				continue;
			}
			wanted.add(new WantedFilter(new FixedSizeArrayList<String>(row.split(" "))));
		}
		text= optionPanel.getItemValue("ignored");
		for(String row: text.split("\n")){
			row= row.trim();
			if(row.isEmpty()){
				continue;
			}
			List<String> ignore= new LinkedList<>(),
				exception= new LinkedList<>();
			for(String tag: row.split(" ")){
				if(tag.isEmpty()){
					continue;
				}
				if(tag.charAt(0) == '-'){
					exception.add(tag.substring(1));
				}else{
					ignore.add(tag);
				}
			}
			ignored.add(new IgnoreFilter(ignore,exception));
		}
		text= optionPanel.getItemValue("sites");
		for(String row: text.split("\n")){
			sites.add(row.toLowerCase().trim());
		}
	}
}

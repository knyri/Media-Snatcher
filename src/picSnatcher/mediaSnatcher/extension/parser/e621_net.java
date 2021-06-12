package picSnatcher.mediaSnatcher.extension.parser;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;

import picSnatcher.mediaSnatcher.Constants;
import picSnatcher.mediaSnatcher.PageParser;
import picSnatcher.mediaSnatcher.PageReader;
import picSnatcher.mediaSnatcher.Session;
import picSnatcher.mediaSnatcher.extension.OptionPanel;
import picSnatcher.mediaSnatcher.extension.OptionPanelListener;
import simple.collections.FixedSizeArrayList;
import simple.net.Uri;
import simple.net.http.Client;
import simple.net.http.clientparams.ClientParam;
import simple.net.http.clientparams.StringParam;
import simple.parser.ml.InlineLooseParser;
import simple.parser.ml.Page;
import simple.parser.ml.Tag;
import simple.parser.ml.html.HtmlConstants;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

public class e621_net extends PageParser implements OptionPanelListener{
	private static final Log log=LogFactory.getLogFor(e621_net.class);
	private static boolean loggedin= false;
	static{
		log.setPrintTime(true);
		log.setPrintDebug(true);
	}
	private static final OptionPanel optionPanel= new OptionPanel("e621.net", e621_net.class.getClassLoader().getResourceAsStream("e621_netOptions.xml"));
	private final List<WantedFilter> wanted= new ArrayList<>();
	private final List<IgnoreFilter> ignored= new ArrayList<>();
	public e621_net(final PageReader preader,final Session session){
		super(preader,session);
		optionPanel.addListener(this);
	}
	static final Header[] loginHeaders = new BasicHeader[] {
		new BasicHeader("Content-type","application/x-www-form-urlencoded"),
		new BasicHeader("Referer", "https://e621.net/session/new")
	};
	private static void login(Client httpclient){
		if(loggedin)return;

		log.debug("Attempting login");
		String
			user= optionPanel.getItemValue("user"),
			pass= optionPanel.getItemValue("pass"),
			token= null
		;
		try{
			CloseableHttpResponse response = httpclient.get("https://e621.net/session/new");
			Page p= InlineLooseParser.parse(new InputStreamReader(response.getEntity().getContent()), HtmlConstants.PARSER_CONSTANTS);
			for(Tag t: p.getTags("form")){
				for(Tag i: t.getChildren("input")){
					if(i.hasProperty(Constants.atr_name) && i.getProperty(Constants.atr_name).equals("authenticity_token")){
						token= i.getProperty(Constants.atr_value);
					}
				}
			}
			log.debug(response);

			ClientParam[] nvps = new ClientParam[] {
				new StringParam("authenticity_token", token),
				new StringParam("name", user),
				new StringParam("password", pass),
				new StringParam("remember", "1"),
				new StringParam("commit", "Submit")
			};
			response = httpclient.post("https://e621.net/session",loginHeaders,nvps,Client.PostDataType.UrlEncoded,null);
			EntityUtils.consume(response.getEntity());
		}catch(Exception e){log.error("Could not log in",e);}
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
		log.information(page.getPath());
		boolean found=false;
		if(page.getPath().startsWith("/posts/")){
			for(Tag a: source.getTags(Constants.tag_a)){
				if("Download".equals(a.getTextContent())){
					session.addLink(new Uri(a.getProperty(Constants.atr_href),page.getScheme()), page, true);
					found= true;
					break;
				}
			}
			if(!found){
				for(Tag img: source.getTags(Constants.tag_img)){
					if("image".equals(img.getProperty(Constants.atr_id))){
						log.debug("Adding", img.getProperty(Constants.atr_src));
						session.addLink(new Uri(img.getProperty(Constants.atr_src),page.getScheme()), page, true);
					}
				}
			}
		}else{
			for(Tag a: source.getTags(Constants.tag_a)){
				if(a.getProperty(Constants.atr_href).startsWith("/posts/")){
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
		if(!optionPanel.getItemValue("user").trim().isBlank()){
			login(Session.conCont);
		}
	}
}
/**
 *
 */
package picSnatcher.mediaSnatcher;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import picSnatcher.mediaSnatcher.extension.ForwardRemoverFactory;
import picSnatcher.mediaSnatcher.extension.OptionPanel;
import picSnatcher.mediaSnatcher.extension.parser.GeneralParser;
import simple.io.RWUtil;
import simple.net.Uri;
import simple.parser.ml.Page;
import simple.parser.ml.Tag;
import simple.util.do_str;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

/**
 * Class to parse a page and pull the goodies.
 * <hr>
 * <br>Created: Sep 11, 2011
 * @author Kenneth Pierce
 */
public abstract class PageParser {
	private static final HashMap<PageParser, Method> HANDLERS = new HashMap<PageParser, Method>();
	protected Session session;
	protected PageReader preader;
	private boolean run = true;
	private static final Log log = LogFactory.getLogFor(PageParser.class);
	public final void reset(final PageReader preader, final Session session){
		this.session = session;
		this.preader = preader;
		reset();
	}
	protected abstract void reset();
	public PageParser(final PageReader preader, final Session session) {
		this.session = session;
		this.preader = preader;
	}
	public static final Set<PageParser> getPageParsers(){
		return HANDLERS.keySet();
	}
	/** Register a page parser
	 * @param clazz
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static final void addHandler(final Class<PageParser> clazz) throws SecurityException, NoSuchMethodException {
		log.information("#addHandler", clazz);
		PageParser ppc = null;
		try {
			ppc = clazz.getConstructor(PageReader.class, Session.class).newInstance(null, null);
		} catch (final Exception e) {
			log.error(e);
		}
		if(ppc!=null)
			HANDLERS.put(ppc, clazz.getDeclaredMethod("canHandle", Uri.class, Page.class,String.class));//store the canHandle(..) method
		else
			log.error("Could not add "+clazz);
	}
	/** Finds the appropriate parse for the page.
	 * @param preader The reader
	 * @param ses The session information
	 * @param site The URI of the read page
	 * @param page The parsed page
	 * @param mime mimetype of the page. TODO: needed anymore? 2014-12
	 * @return The page parser for the page.
	 */
	public static final PageParser getHandler(final PageReader preader, final Session ses, final Uri site, final Page page,String mime) {
		PageParser pp = null;
		for(PageParser ppc:  HANDLERS.keySet()){

			log.debug("current PP check",ppc);
			log.debug("PP at index",HANDLERS.get(ppc));
			try {
				if ((Boolean)HANDLERS.get(ppc).invoke(ppc, site, page,mime)) {
					pp=ppc;
					break;
				}
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
				log.error(e);
			}
		}
		if (pp==null){
			log.debug("Parser used","GeneralParser");
			return new GeneralParser(preader, ses);
		}
		pp.reset(preader, ses);
		log.debug("#getHandler", pp.getClass().getCanonicalName());
		return pp;
	}
	protected final boolean wantedImageSize(Tag tag){
		String size;
		if(tag.hasProperty(Constants.atr_width)){
			size= tag.getProperty(Constants.atr_width);
			if(size.endsWith("px") && size.length() > 2){
				size= size.substring(0,size.length()-2);
			}
			try{
				if(Integer.parseInt(size)<Integer.parseInt(session.getOptions().getOption(OptionKeys.snatcher_minImgWidth))){
					return false;
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
					return false;
				}
			}catch(NumberFormatException e){
				log.warning("image height not a number", tag.getProperty(Constants.atr_height));
			}
		}
		return true;
	}
	/** Can the parser parse the page?
	 * @param link URI for the page
	 * @param page The parsed page
	 * @param mime Mimetype of the page TODO: needed anymore? 2014-12
	 * @return True if this parse can parse the page
	 */
	public abstract boolean canHandle(Uri link, Page page,String mime);
	/** Does this parser have an options page?
	 * TODO: option pages for parsers
	 * @return true if this parser has an options page
	 */
	public abstract boolean hasOptions();
	/**
	 * Parsers with no options must return true.
	 * @return True if the options were saved.
	 */
	public abstract boolean saveOptions();
	/**The options page for this parser or null.
	 * @return The options page for this parser or null.
	 */
	public abstract OptionPanel getOptions();
	/**Is it still running?
	 * @return
	 */
	public boolean isRunning() {
		return run;
	}
	/**
	 * Stop the parser.
	 */
	public final void stop() {
		run = false;
	}
	/** The worker of the class. Processes the page source and adds links to the download queue
	 * @param source The page source.
	 * @param page The url of the page.
	 * @param referer The page this page was found on.
	 * @param title The title of the page(if any)
	 * @param basehref The value of &lt;base href="" /&gt; (may be null if none was found)
	 * @param depth The current page depth
	 * @return true or false if the parsing finished successfully. (if you wish to skip the page, simply return true. A false will add it to the retry stack.)
	 */
	protected abstract boolean processPage(final Page source, final Uri page, final Uri referer, final String title, final String basehref, final int depth);
	/* *******************************
	 * ****** UTILITY FUNCTIONS ******
	 * *******************************
	 */
	/**
	 * Pattern that {@link #addNextLinks(Page, Uri, int)} uses
	 */
	private static final Pattern nextLinkText= Pattern.compile("^(>|&(gt|raquo);)|((&[^;]+;)* *next)");
	/**
	 * Searches for an adds (at the same read depth) most links that go to the next page.
	 * @param source The parsed page
	 * @param page The page URI
	 */
	protected final void addNextLinks(Page source, Uri page){
		Matcher nextLinkMatcher= nextLinkText.matcher("");
		for(Tag a: source.getTags(Constants.tag_a)){
			nextLinkMatcher.reset(a.getTextContent().trim().toLowerCase());
			if(!nextLinkMatcher.find()){
//				log.debug("Not next link", a.getTextContent().trim().toLowerCase());
				continue;
			}
			log.debug("Next link", a.getTextContent().trim().toLowerCase());

			String link= a.getProperty(Constants.atr_href);
			if(link == null || link.isEmpty()){
				continue;
			}
			link= link.trim();
			if(link.toLowerCase().startsWith("javascript") || "#".equals(link)){
				link= PageParser.getLinkFromJavascript(a);
				if(link == null){
					continue;
				}
			}

			this.addToCurrentReadQueue(new Uri(link), page);
		}
	}
	/** Fixes/resolves the URL to be in the proper form. Also attempts to remove
	 * any forwarders. Returns NULL if it is not a valid HTTP url.
	 * @param link link from the page
	 * @param page page address
	 * @param basehref base address(null if none)
	 * @return The URL or NULL if the scheme is not HTTP or HTTPS
	 */
	protected static final String createURL(String linko, final Uri page, final String basehref) {
		linko = linko.replaceAll("[\n\r\t\\\\\"\\\\']", "");
		linko = linko.replaceAll("&amp;", "&");
		if (linko.isEmpty()) {
			if (basehref==null){
				return page.getOriginalUri();
			} else {
				linko = basehref;
			}
		}
		if(linko.charAt(0) == '?'){
			linko= page.getHost() + page.getPath() + page.getFile() + linko;
		}
		final Uri link = new Uri(linko,page.getScheme());
		log.debug("link",link.getScheme()+"]["+link.getHost()+"]["+link.getPath()+"]["+link.getFile()+"]["+link.getQuery()+"]["+link.getFragment());
		log.debug("page",page.getScheme()+"]["+page.getHost()+"]["+page.getPath()+"]["+page.getFile()+"]["+page.getQuery()+"]["+page.getFragment());
		//Return null if the scheme is not http[s]
		if (!link.getScheme().isEmpty() && !"http".equalsIgnoreCase(link.getScheme()) && !"https".equalsIgnoreCase(link.getScheme())) return null;
		if (link.getScheme().isEmpty()) {
			//no scheme. Get it from the page
			if (link.getHost().isEmpty()) {
				//no host. Get it from the page.
				if (linko.charAt(0) == '/') {
					linko = page.getScheme()+"://"+page.getHost()+link.toString();
				} else {
					if (basehref != null) {
						//page declared a base href. use it
						linko = basehref + link;
					} else {
						linko = page.getScheme()+"://" + page.getHost() + page.getPath() + link;
					}
				}
			} else {
				linko = page.getScheme()+"://" + link;
			}
		}
		//remove any forwarders
		linko = ForwardRemoverFactory.removeForwarder(linko);
		log.debug(linko);
		return linko;
	}
	/** Adds a link to the read queue at the same depth
	 * @param link Link to add
	 * @param ref The referrer or null
	 */
	protected final void addToCurrentReadQueue(final Uri link, final Uri ref) {
		preader.addToCurrentReadQueue(link, ref);
	}
	/** Adds a link to the read queue
	 * @param link Link to add
	 * @param ref The referrer or null
	 */
	protected final void addToReadQueue(final Uri link, final Uri ref) {
		preader.addToReadQueue(link, ref);
	}
	/** Forces a page to be added to the read queue. This is mainly for pages that contain frames or iframes
	 * @param link The link to add
	 * @param ref the referrer
	 */
	protected final void addToReadQueueForce(final Uri link, final Uri ref) {
		preader.addToReadQueueForce(link, ref);
	}
	/** Attempts to extract URLs from javascript.
	 * @param tag
	 * @return Null if a link could not be extracted.
	 */
	protected static final String getLinkFromJavascript(final Tag tag){
		String javascript=null;
		if(tag.getName().equals(Constants.tag_a)){
			if((tag.getProperty(Constants.atr_href)=="#"||tag.getProperty(Constants.atr_href).trim().equalsIgnoreCase("javascript:;"))&&tag.hasProperty(Constants.atr_onclick)){
				javascript=tag.getProperty(Constants.atr_onclick);
			}else{
				javascript=tag.getProperty(Constants.atr_href);
			}
			if(javascript==null){
				return null;
			}
			int idx=javascript.indexOf("MM_openBrWindow");
			if(idx!=-1){
				idx+=15;
				//skip to char after (
				idx=1+do_str.skipWhitespace(javascript,idx);
				//skip to quote
				idx=do_str.skipWhitespace(javascript,idx);
				final char quote=javascript.charAt(idx);
				idx++;
				return javascript.substring(idx,javascript.indexOf(quote,idx));
			}
		}
		return null;
	}
	/** Attempts to extract URLs from CSS.
	 * Soon to be replaced with a CSS page parser
	 * @param css
	 * @return
	 */
	protected static final List<String> getLinkFromCss(final String css){
		final LinkedList<String> retv=new LinkedList<String>();
		int idx=0;
		while(idx<css.length()){
			idx=do_str.skipWhitespace(css,idx);
			if(do_str.CI.startsWith(css,"url",idx)){
				idx=do_str.skipWhitespace(css,idx);
				if(css.charAt(idx)!='(')continue;
				idx=do_str.skipWhitespace(css,idx+1);
				if(css.charAt(idx)=='\''||css.charAt(idx)=='"'){
					final char quote=css.charAt(idx);
					idx++;
					retv.add(css.substring(idx,css.indexOf(quote)));
				}else{
					retv.add(css.substring(idx,css.indexOf(')')));
				}
			}else idx++;
		}
		return retv;
	}
	/**Proccesses special tags like link, style, and tags with a style attribute.
	 * Limited Javascript support is next on the agenda.
	 * @param page
	 * @return
	 */
	protected final List<String> proccessSpecial(final Page page,final Uri referrer){
		final List<String> retv=new LinkedList<String>();
		Header[] headers= new Header[]{new BasicHeader("Referer",referrer.toString())};
		StringBuilder store= new StringBuilder();
		for(final Tag tag:page){
			if(tag.getName().equals("link")){
				boolean fetch=false;
				if(tag.hasProperty(Constants.atr_rel)&&tag.getProperty(Constants.atr_rel).equals("stylesheet")){
					fetch=true;
				}else if(tag.hasProperty(Constants.atr_type)&&tag.getProperty(Constants.atr_type).equals("text/css")){
					fetch=true;
				}
				if(fetch){
					store.setLength(0);
					try{
						CloseableHttpResponse resp= Session.conCont.get(PageParser.createURL(tag.getProperty(Constants.atr_href),referrer,null),headers);
						RWUtil.readInto(new InputStreamReader(resp.getEntity().getContent(),"UTF-8"),store);
						retv.addAll(PageParser.getLinkFromCss(store.toString()));
					}catch(final IOException e){}
				}
			}else if(tag.hasProperty(Constants.atr_style)){
				retv.addAll(PageParser.getLinkFromCss(tag.getProperty(Constants.atr_style)));
			}
		}
		return retv;
	}
}

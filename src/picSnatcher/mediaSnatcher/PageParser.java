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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;

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
	protected final Session session;
	protected final PageReader preader;
	private boolean run = true;
	private static final Log log = LogFactory.getLogFor(PageParser.class);

	public PageParser(final PageReader preader, final Session session) {
		this.session = session;
		this.preader = preader;
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
			} catch (final IllegalArgumentException e) {
				e.printStackTrace();
			} catch (final IllegalAccessException e) {
				e.printStackTrace();
			} catch (final InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		if (pp==null){
			log.debug("Parser used","GeneralParser");
			return new GeneralParser(preader, ses);
		}
		PageParser ppc = null;
		try {
			ppc = pp.getClass().getConstructor(PageReader.class, Session.class).newInstance(preader, ses);
		} catch (final Exception e) {
			log.error(e);
		}
		if (ppc!=null) {
			log.debug("#getHandler", ppc.getClass().getCanonicalName());
		}
		return ppc;
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
	/** Fixes/resolves the URL to be in the proper form. Also attempts to remove
	 * any forwarders. Returns NULL if it is not a valid HTTP url.
	 * @param link link from the page
	 * @param page page address
	 * @param basehref base address(null if none)
	 * @return The URL or NULL if the scheme is not HTTP
	 */
	protected static final String createURL(String linko, final Uri page, final String basehref) {
		linko = linko.replaceAll("[\n\r\t\\\\\"\\\\']", "");
		linko = linko.replaceAll("&amp;", "&");
		if (linko.isEmpty()) {
			if (basehref==null)
				return page.getOriginalUri();
			else {
				linko = basehref;
			}
		}
		final Uri link = new Uri(linko,page.getScheme());
		log.debug("link",link.getScheme()+"]["+link.getHost()+"]["+link.getPath()+"]["+link.getFile()+"]["+link.getQuery()+"]["+link.getFragment());
		log.debug("page",page.getScheme()+"]["+page.getHost()+"]["+page.getPath()+"]["+page.getFile()+"]["+page.getQuery()+"]["+page.getFragment());
		//Return null if the scheme is not http
		if (!link.getScheme().isEmpty() && !"http".equalsIgnoreCase(link.getScheme())) return null;
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
	/** Adds a link to the read queue
	 * @param link Link to add
	 * @param ref The referrer or null
	 * @param depth Depth that was passed to you
	 */
	protected final void addToReadQueue(final Uri link, final Uri ref, final int depth) {
		preader.addToReadQueue(link, ref, depth);
	}
	/** Forces a page to be added to the read queue. This is mainly for pages that contain frames or iframes
	 * @param link The link to add
	 * @param ref the referrer
	 * @param curdepth the current read depth
	 */
	protected final void addToReadQueueForce(final Uri link, final Uri ref, final int curdepth) {
		preader.addToReadQueueForce(link, ref, curdepth);
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
						HttpResponse resp= Session.conCont.get(PageParser.createURL(tag.getProperty(Constants.atr_href),referrer,null),headers);
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

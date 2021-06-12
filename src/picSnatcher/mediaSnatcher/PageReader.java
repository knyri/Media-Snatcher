/**
 *
 */
package picSnatcher.mediaSnatcher;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;

import simple.collections.CIHashtable;
import simple.collections.Stack;
import simple.html.MimeTypes;
import simple.net.Uri;
import simple.parser.ml.InlineLooseParser;
import simple.parser.ml.Page;
import simple.parser.ml.ParserConstants;
import simple.parser.ml.Tag;
import simple.parser.ml.html.HtmlConstants;
import simple.time.Timer;
import simple.util.TimeUtil;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

/** Reads the pages and manages the Page Parsers.
 * <hr>
 * <br>Created: Jan 21, 2011
 * @author Kenneth Pierce
 */
public final class PageReader{
	private static final Log log = LogFactory.getLogFor(PageReader.class);
	public final static ParserConstants pconst = HtmlConstants.PARSER_CONSTANTS;
	private final Session session;
	private final Uri page;
	private final Timer timer = new Timer();
	private final UriFormatIterator pf;
	private final boolean readDeep;
	private int readDepth = 0;
	private int currentDepth= 0;
	private int pagesRead= 0;
	private LinkedList<QueueElement> currentReadLinks= new LinkedList<QueueElement>();
	private LinkedList<QueueElement> nextReadLinks= new LinkedList<QueueElement>();
	//private int cPages = 0;
	//private final TimeRemainingEstimator tre = TimerFactory.getTRETimer(Algorithm.SAMPLE, 15);
	private boolean run = true;
	private final Stack<Site> redo = new Stack<Site>();
	private PageParser pp;
	public PageReader(final Uri page, final boolean readDeep, final Session ses) {
		this.session = ses;
		this.page = page;
		this.readDeep = readDeep;
		pf = null;
	}
	public PageReader(final UriFormatIterator page, final boolean readDeep, final Session ses) {
		this.session = ses;
		this.pf = page;
		this.readDeep = readDeep;
		this.page = null;
	}
	/**
	 * Does a deep read
	 * @throws IOException
	 */
	private void readDeep() throws IOException{
		log.information("READING DEEP!!!");
		for (currentDepth= 1; currentDepth < readDepth && run; currentDepth++) {
			currentReadLinks= nextReadLinks;
			nextReadLinks= new LinkedList<QueueElement>();
			readCurrentQueue();
		}
		log.information("DONE READING DEEP!!!");
	}
	private final void readCurrentQueue() throws IOException{
		QueueElement cur;
		do {
		while((cur= currentReadLinks.poll()) != null){
			if (!run) {
				log.information("readCurrentQueue() run stopped");
				break;
			}
			log.information("Reading " + cur);
			if (handle(cur.link, cur.referer, currentDepth)){
				session.prTreCount();
				pagesRead++;
				session.addToReadList(cur.toString());
			}
			log.information("QUEUE SIZE: "+currentReadLinks.size());
			session.prUpdateStatus();
		}
		}while(currentReadLinks.size() > 0);// sanity check
	}
	/** Call this function to start the process!
	 * @return true or false if all were read successfully.
	 */
	public final boolean readSite() {
		currentDepth= 0;
		pagesRead= 0;
		//NOTE: Entrance function.
		run = true;
		log.information("Start", new Date(System.currentTimeMillis()).toString());
		timer.reset();
		boolean success = true;
		try {
			Site tmp;
			if (page != null) {
				log.debug("reading by url");
				session.prTotPageAdd(1);
				if (handle(page, null, 0)){
					session.prTreCount();
					session.prUpdateStatus();
					session.addToReadList(page.toString());
				}
			} else {
				log.debug("reading by PictureFormat");
				Uri cur;
				while(run && (cur= pf.next()) != null) {
					if (handle(cur, null, 0)) {
						session.prTreCount();
						pagesRead++;
						session.addToReadList(cur.toString());
					}
					session.prUpdateStatus();
				}
			}//end else
			// read any that were added to the current level
			readCurrentQueue();
			log.information("DONE READING IT ALL!!!");
			session.prUpdateStatus();
			if (readDeep) {
				log.information("READING DEEP!!!");
				readDeep();
				log.information("DONE READING DEEP!!!");
			}//end if(readDeep)

			if (pagesRead == 0){
				log.warning("No pages read: " + pf.toString());
//				session.remove(pf);
			}

			if (!redo.isEmpty()) {
				log.information("READING DO OVERS!!!");
				nextReadLinks= new LinkedList<QueueElement>();
				log.information("LastChance", "Reading last chance items. "+redo.size()+" items in list.");
				while (!redo.isEmpty() && run) {//could possibly cause an infinite loop
					tmp = redo.pop();
					if (handle(tmp.getSite(), tmp.getReferrer(), tmp.getDepth())){
						session.prTreCount();
						session.addToReadList(tmp.toString());
					}
					session.prUpdateStatus();
				}
				if (readDeep) {
					log.information("DEEP READ ON DO OVERS");
					readDeep();
				}
				log.information("DONE READING DO OVERS!!!");
			}
		} catch (final Exception e) {
			log.log(0x3f);
			log.error("FATAL", e);
			success = false;
		} finally {
			log.debug("Stop", "Success: "+success);
			log.information("Stop", new Date(System.currentTimeMillis()).toString());
			log.information("Stop", "Time elapsed: "+TimeUtil.getTime(timer.elapsed()));
			currentReadLinks= new LinkedList<QueueElement>();
			nextReadLinks= new LinkedList<QueueElement>();
		}
		return success;
	}
	/** Reads the page, performs wanted checks, grabs the page title and base href, and calls the worker function.
	 * Call this function to skip the queue and directly read a page.
	 * @param linkr The page to be read.
	 * @param referer The page that the page is on. (may be null)
	 * @param depth The current read depth. (increment by one if calling this directly)
	 * @return true or false if the page was loaded and parsed successfully.
	 * @throws IOException see {@link #readPage(Uri, Uri, int, StringBuilder)}
	 */
	final boolean handle(Uri linkr, final Uri referer, final int depth) throws IOException {
		if (session.isPrevRead(linkr.toString())) return true;
		log.information("Reading", linkr);
		final StringBuilder pageS = new StringBuilder();
		final CIHashtable<Object> feedback = new CIHashtable<Object>();
		if (!readPage(linkr, referer, depth, pageS, feedback)) {
			log.warning("Failed to read the page "+linkr,feedback);
			return false;
		}
		session.addToReadList(linkr.toString());
		if(!linkr.buildCustom(Uri.PATH+Uri.FILE+Uri.QUERY).equals(feedback.get("effectiveuri"))){
			log.debug(linkr.buildCustom(Uri.PATH+Uri.FILE+Uri.QUERY),feedback.get("effectiveuri").toString());
			linkr=new Uri(linkr.buildCustom(Uri.SCHEME+Uri.HOST+Uri.PORT)+new Uri((String)feedback.get("effectiveuri")).buildCustom(Uri.PATH+Uri.FILE+Uri.QUERY));
			log.information("effective URI",linkr);
			if (session.isPrevRead(linkr.toString())){
				log.information("Previously read, skipping",linkr);
				return true;
			}
			session.addToReadList(linkr.toString());
		}
		session.setState("Parsing...");
		final Page page;
		if("text/html".equals(feedback.get("mime"))){
			try {
				page = InlineLooseParser.parse(pageS, pconst);
			} catch (final ParseException e) {
				log.error("Occured on "+pageS,e);
				return false;
			}
		}else{
			session.addLink(linkr,referer,false);
			log.warning(feedback.get("mime")+" is not HTML for URL "+linkr.toString());
			return true;
		}
		final String title = getTitle(page);
		if (!session.getOptions().isWantedTitleCheck(title)) {
			session.setState("skipped: starting next");
			log.warning("SKIP: title is not in the wanted list.");
			return true;
		}
		final String basehref = getBaseURL(page);
		pp = PageParser.getHandler(this, session, linkr, page,(String)feedback.get("mime"));
		//log.debug("base href:"+basehref);
		session.setState("Processing...");
		log.debug("Processing", linkr);
		if (referer == null)
			return pp.processPage(page, linkr, null, title, basehref, depth);
		else
			return pp.processPage(page, linkr, referer, title, basehref, depth);
	}
	protected final boolean readPage(final Uri link, final Uri referer, final int depth, final StringBuilder store, final CIHashtable<Object> feedback) throws IOException {
		session.setStateExt(link.toString());
		final Object wait= new Object();
		long max_count;
		int cur_count;
		//InputStreamReader in = null;
		int retry = 0, start = 0;
		boolean skip = true;
		CloseableHttpResponse resp= null;
		HttpContext context = new BasicHttpContext();
		try{
			if (referer != null) {
				resp=Session.conCont.get(link.toString(),new Header[]{new BasicHeader("Referer",referer.toString())},context);
			}else{
				resp=Session.conCont.get(link.toString(),null,context);
			}

			HttpRequest target = (HttpRequest)context.getAttribute(HttpCoreContext.HTTP_REQUEST);

			feedback.put("effectiveuri", target.getRequestUri());

			if(resp.getCode() != 200){
				// HTTP client follows redirects. Should only get errors here
				resp.getEntity().getContent().close();
				return true;
			}
			//NOTE: rechecking MIME
			final String tmp = resp.getEntity().getContentType();
			String mime= tmp.split(";")[0];
			feedback.put("mime",mime);
			if (!"text/html".equals(mime) && !"text/css".equals(mime)) {
				session.setState("Skipped: starting next");
				feedback.put("skip", mime+" is not text/html or text/css");
				log.debug("readPage", "Skipped: Mime from server: "+tmp);
				resp.getEntity().getContent().close();
				return false;
			} else {
				skip = false;
			}
			if (skip) {
				feedback.put("skip", "skip set to true; general");
				session.setState("Skipped: starting next");
				//session.setStateExt("");
				log.information("Skipped(readPage):"+link.toString());
				log.debug("MIME", MimeTypes.getMime(link));
				resp.getEntity().getContent().close();
				return false;
			}
			//---END PAGE CHECKING
			final char[] buff = new char[1024];
			for(int i= 0; i < 3; i++, retry++){
				session.setState("Reading");
				session.setStateExt(link.toString());
				max_count = resp.getEntity().getContentLength();
				session.setCurrentProgress(0, max_count);
				log.debug("readPage", "page size:"+max_count);
				feedback.put("start", System.currentTimeMillis());

				//TODO: check for character encoding
				try(InputStreamReader in = new InputStreamReader(resp.getEntity().getContent(), "UTF-8")){
					session.setState("Reading...");
	//				log.information("Reading from site.");
					cur_count = 0;
					if (max_count > -1) {
						//known size
						session.setCurrentProgressBarText("0/"+max_count);
						while (run && ((start = in.read(buff)) != -1)) {
							store.append(buff, 0, start);
							cur_count += start;
							session.setCurrentProgress(cur_count, max_count);
							session.setCurrentProgressBarText(cur_count+"/"+max_count);
						}
					} else {
						//unknown size
						session.setCurrentProgressBarText("0/??");
						session.setCurrentProgress(0, 1);
						while (run && ((start = in.read(buff)) != -1)) {
							store.append(buff, 0, start);
							cur_count += start;
							session.setCurrentProgressBarText(cur_count+"/??");
						}
						session.setCurrentProgressBarText(cur_count+"/"+cur_count);
						session.setCurrentProgress(1,1);
					}
					resp.close();
					break;
				} catch (final SocketTimeoutException e) {
					session.setState("Timed out reading. Try "+ (retry+1) +" of 3");
					log.warning("Timeout on read "+ (retry+1) +" of 3", e.getLocalizedMessage());
					store.setLength(0);
				} catch (SocketException e){
					/*
					 * Socket closed is an incomplete read.
					 */
					log.warning("Try "+ (retry+1) +" of 3", e.getLocalizedMessage());
					store.setLength(0);
				} catch (final EOFException e) {
					log.warning("Try "+ (retry+1) +" of 3", e.getLocalizedMessage());
					store.setLength(0);
				} catch (final IOException e) {
					resp.close();
					session.setState("Error:"+ e.getLocalizedMessage());
					log.error("readPage", e);
					feedback.put("stop", System.currentTimeMillis());
					return false;
				}
				resp.close();
				if (retry < 2) {
					synchronized(wait){
						// maybe caused by reading too many in quick succession?
						// anti-crawler logic?
						try{
							wait.wait(3000);
						}catch(InterruptedException e){}
					}
					if (referer != null) {
						resp=Session.conCont.get(link.toString(),new Header[]{new BasicHeader("Referer",referer.toString())},context);
					}else{
						resp=Session.conCont.get(link.toString(),null,context);
					}
					target = (HttpRequest)context.getAttribute(HttpCoreContext.HTTP_REQUEST);
					feedback.put("effectiveuri", target.getRequestUri());
				}
			}//end for
			resp.close();
			if (retry == 3) {
				switch (resp.getCode()) {
				case -1:
					break;
				case java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
				case java.net.HttpURLConnection.HTTP_RESET:
				case java.net.HttpURLConnection.HTTP_INTERNAL_ERROR:
				case 200:
					addRedo(referer, link, depth);
				}
				feedback.put("stop", System.currentTimeMillis());
				log.warning("SKIPPED: Exceeded read retry limit. "+link+". HTTP code: "+resp.getCode());
				return false;
			}
			feedback.put("stop", System.currentTimeMillis());
		}finally{
			if(resp != null) {
				resp.close();
			}
		}
		return true;
	}
	/**
	 * Checks validity and makes a QueueElement for adding to the read queue.
	 * @param link
	 * @param ref
	 * @return The QueueElement or null if it shouldn't be added
	 */
	private final QueueElement getQueueElement(Uri link, final Uri ref){
		link = new Uri(link.trimFragment());
		if (session.isPrevRead(link.toString())) {
			return null;
		}

		QueueElement qe=new QueueElement(link, ref);
		if(currentReadLinks.contains(qe) || nextReadLinks.contains(qe)){
			return null;
		}

		return qe;
	}
	/**
	 * Adds the page to the read queue at the same depth.
	 * Skips the {@link Options#isIgnoredPage(Uri, Uri)} check.
	 * @param link Link to the page.
	 * @param ref Link to the page link was found on.
	 */
	protected final void addToCurrentReadQueueForce(Uri link, final Uri ref) {
		QueueElement qe= getQueueElement(link, ref);
		if(qe == null){
			return;
		}

		if(currentReadLinks.offer(qe)){
			log.information("ReadQueue","Added "+ link +" at depth "+ currentDepth);
			session.prTotPageAdd(1);
		}else{
			log.error("addToCurrentReadQueueForce","Queue rejected offer");
		}
	}
	/**
	 * Adds the page to the read queue at the same depth.
	 * @param link Link to the page.
	 * @param ref Link to the page link was found on.
	 */
	protected final void addToCurrentReadQueue(Uri link, final Uri ref) {
		QueueElement qe= getQueueElement(link, ref);
		if(qe == null){
			return;
		}

		if (session.getOptions().isIgnoredPage(link, ref)) {
			log.debug("ReadQueue:ignored",link);
			return;
		}

		if(currentReadLinks.offer(qe)){
			log.information("ReadQueue","Added "+ link +" at depth "+ currentDepth);
			session.prTotPageAdd(1);
		} else {
			log.error("addToCurrentReadQueue","Queue rejected offer");
		}
	}
	/**
	 * Adds the page to the read queue.
	 * @param link Link to the page.
	 * @param ref Link to the page link was found on.
	 */
	protected final void addToReadQueue(Uri link, final Uri ref) {
		if (!readDeep) return;

		QueueElement qe= getQueueElement(link, ref);
		if(qe == null){
			return;
		}

		if (session.getOptions().isIgnoredPage(link, ref)) {
			log.debug("ReadQueue:ignored",link);
			return;
		}

		if(nextReadLinks.offer(qe)){
			log.information("ReadQueue","Added "+ link +" at depth "+ (currentDepth + 1));
			session.prTotPageAdd(1);
		} else {
			log.error("addToReadQueue","Queue rejected offer");
		}
	}
	/**
	 * Skips the {@link Options#isIgnoredPage(Uri, Uri)} check and adds the page to the read queue.
	 * @param link Link to the page.
	 * @param ref Link to the page link was found on.
	 */
	protected final void addToReadQueueForce(Uri link, Uri ref){
		if (!readDeep) return;

		QueueElement qe= getQueueElement(link, ref);
		if(qe == null){
			return;
		}

		if(nextReadLinks.offer(qe)){
			log.information("ReadQueue","Added "+ link +" at depth "+ (currentDepth + 1));
			session.prTotPageAdd(1);
		} else {
			log.error("addToReadQueueForce","Queue rejected offer");
		}
	}
	/**
	 * @param src the source page
	 * @return the base URL or null
	 */
	protected static String getBaseURL(final Page src) {
		final List<Tag> tags = src.getTags(Constants.tag_base);
		if (tags==null || tags.isEmpty())
			return null;
		else
			return tags.get(0).getProperty(Constants.atr_href);
	}
	/**Returns the content of the title tag.
	 * @param src The source page
	 * @return The content of the title tag or an empty string if there is no title.
	 */
	protected static String getTitle(final Page src) {
		final List<Tag> tags = src.getTags(Constants.tag_title);
		if (tags == null || tags.isEmpty() || !tags.get(0).hasChild())
			return "";
		else
			return tags.get(0).getChild(0).getContent();
	}
	/**
	 * Sets the current read depth (sets the read queue we're processing)
	 * @param i
	 */
	public final void setReadDepth(final int i) {
		if (readDeep) {
			readDepth = i;
		}
	}
	/*private final void updateStatus() {
		tre.setTotalItems(cPages);
		tre.debug(log);
		session.setStatus("URLs: "+session.getLinkCount()+" Retry: "+redo.size()+" Pages read: "+tre.getTotalSampleCount()+"/"+tre.getTotalItems()+" ETC:"+TimerFactory.getTime(tre.getRemaining())+" :: rate: "+tre.getRate());//(cPages-cPagesRead)*(cPagesRead/timer.elapsed()+1)));
		//session.setStatus("URLs: "+session.getLinkCount()+" Retry: "+redo.size()+" Pages read: "+cPagesRead+"/"+cPages+" ETC:"+do_str.getTime(tre.getRemaining()));//(cPages-cPagesRead)*(cPagesRead/timer.elapsed()+1)));
	}*/
	/**
	 * Adds the link to a retry queue. Useful for spotty connections and websites that delegate file hosting to random hosts
	 * @param referer
	 * @param link
	 * @param depth
	 */
	public final void addRedo(final Uri referer, final Uri link, final int depth) {
		final Site tmp = new Site(referer, link, depth);
		if (!redo.contains(tmp)) {
			log.information("Added to retry stack: "+link);
			redo.push(tmp);
		}
	}
	/**
	 * Stops
	 */
	public final void stop() {
		this.run = false;
		if (pp!=null) {
			pp.stop();
		}
	}
}
class QueueElement{
	public final Uri link,referer;
	public QueueElement(Uri link,Uri ref){
		this.link=link;
		this.referer=ref;
	}
	@Override
	public String toString(){
		return link.toString()+" : "+referer;
	}
	@Override
	public boolean equals(Object o){
		if(o==this)return true;
		if(!(o instanceof QueueElement))return false;
		QueueElement qe=(QueueElement)o;
		if(qe.link.equals(link))return true;
		//if(qe.link.equals(link) && qe.referer.equals(referer))return true;
		return false;
	}
}
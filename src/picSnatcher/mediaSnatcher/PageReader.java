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
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import simple.html.MimeTypes;
import simple.net.Uri;
import simple.parser.ml.InlineLooseParser;
import simple.parser.ml.Page;
import simple.parser.ml.ParserConstants;
import simple.parser.ml.Tag;
import simple.parser.ml.html.HtmlConstants;
import simple.time.Timer;
import simple.util.CIHashtable;
import simple.util.Queue;
import simple.util.Stack;
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
	private final static ParserConstants pconst = HtmlConstants.PARSER_CONSTANTS;
	private final Session session;
	private final Uri page;
	private final Timer timer = new Timer();
	private final UriFormatIterator pf;
	private final boolean readDeep;
	private int readDepth = 0;
	//private int cPages = 0;
	//private final TimeRemainingEstimator tre = TimerFactory.getTRETimer(Algorithm.SAMPLE, 15);
	private boolean run = true;
	//private LinkedHashMap<Uri, String> toberead[] = null;
	private Queue<QueueElement> toberead[]=null;
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
	/** Call this function to start the process!
	 * @return true or false if all were read successfully.
	 */
	@SuppressWarnings("unchecked")
	public final boolean readSite() {
		//NOTE: Entrance function.
		run = true;
		log.information("Start", new Date(System.currentTimeMillis()).toString());
		timer.reset();
		boolean success = true;
		int read=0;
		try {
			Site tmp;
			if (readDeep) {
				toberead = new Queue[readDepth];
				toberead[0] = new Queue<QueueElement>();
			/*
				toberead = new LinkedHashMap[readDepth];
				toberead[0] = new LinkedHashMap<Uri, String>();
				//*/
			}
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
				while(run && (cur=pf.next())!=null) {
					if (handle(cur, null, 0)) {
						session.prTreCount();
						read++;
						session.addToReadList(cur.toString());
					}
					session.prUpdateStatus();
				}
				log.information("DONE READING IT ALL!!!");
			}//end else
			session.prUpdateStatus();
			if (readDeep) {
				log.information("READING DEEP!!!");
				QueueElement cur;
				for (int depth = 0; depth < toberead.length && run; depth++) {
					if (depth+1 < toberead.length) {
						toberead[depth+1] = new Queue<QueueElement>();
					}
					while((cur=toberead[depth].poll())!=null){
						if (!run) {
							break;
						}
						if (handle(cur.link, cur.referer, depth+1)){
							session.prTreCount();
							read++;
							session.addToReadList(cur.toString());
						}
						session.prUpdateStatus();
					}
					/*
					for (final Map.Entry<Uri, String> entry : toberead[depth].entrySet()) {
						if (!run) {
							break;
						}
						if (handle(entry.getKey(), entry.getValue(), depth+1)){
							session.prTreCount();
							read++;
							session.addToReadList(entry.toString());
						}
						session.prUpdateStatus();
					}
					//*/
				}
				log.information("DONE READING DEEP!!!");
			}//end if(readDeep)
			if (read==0){
				session.remove(pf);
			}
			//clear the toberead list if there are some redos
			if (!redo.isEmpty()) {
				log.information("READING DO OVERS!!!");
				for (int depth = 0; depth < toberead.length && run; depth++) {
					if (depth+1 < toberead.length) {
						toberead[depth+1].clear();
					}
				}
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
					// deepRead the redos
					log.information("DEEP READ ON DO OVERS");
					QueueElement cur;
					for (int depth = 0; depth < toberead.length && run; depth++) {
						if (!toberead[depth].isEmpty()) {
							while((cur=toberead[depth].poll())!=null){
								if (!run) {
									break;
								}
								if (handle(cur.link, cur.referer, depth+1)){
									session.prUpdateStatus();
									session.addToReadList(cur.toString());
								}
								session.prUpdateStatus();
							}
							/*for (final Map.Entry<Uri, String> entry : toberead[depth].entrySet()) {
								if (!run) {
									break;
								}
								if (handle(entry.getKey(), entry.getValue(), depth+1)){
									session.prUpdateStatus();
									session.addToReadList(entry.toString());
								}
								session.prUpdateStatus();
							}//*/
						}
					}
				}//end if(readDeep)
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
		CloseableHttpResponse resp;
		HttpContext context = new BasicHttpContext();
		if (referer != null) {
			resp=Session.conCont.get(link.toString(),new Header[]{new BasicHeader("Referer",referer.toString())},context);
		}else{
			resp=Session.conCont.get(link.toString(),null,context);
		}

		HttpRequest target = (HttpRequest)context.getAttribute(HttpCoreContext.HTTP_REQUEST);
		feedback.put("effectiveuri",target.getRequestLine().getUri());
		if(resp.getStatusLine().getStatusCode()!=200){
			resp.getEntity().getContent().close();
			return true;
		}
		//NOTE: rechecking MIME
		final String tmp = resp.getEntity().getContentType().getValue();
		String mime=tmp.split(";")[0];
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
		for(int i=0;i<3;i++, retry++){
			session.setState("Reading");
			session.setStateExt(link.toString());
			max_count = resp.getEntity().getContentLength();
			session.setCurrentProgress(0, max_count);
			log.debug("readPage", "page size:"+max_count);
			feedback.put("start", System.currentTimeMillis());

			//TODO: check for character encoding
			try(InputStreamReader in = new InputStreamReader(resp.getEntity().getContent(), "UTF-8")){
				session.setState("Reading...");
				log.information("Reading from site.");
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
				session.setState("Timed out reading. Try "+(retry+1)+" of 3");
				log.warning("Timeout on read "+(retry+1)+" of 3", e.getLocalizedMessage());
				store.setLength(0);
			} catch (SocketException e){
				/*
				 * Socket closed is an incomplete read.
				 */
				log.warning("Try "+(retry+1)+" of 3", e.getLocalizedMessage());
				store.setLength(0);
			} catch (final EOFException e) {
				log.warning("Try "+(retry+1)+" of 3", e.getLocalizedMessage());
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
					// maybe caused by reading too fast?
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
				feedback.put("effectiveuri",target.getRequestLine().getUri());
			}
		}//end for
		resp.close();
		if (retry == 3) {
			switch (resp.getStatusLine().getStatusCode()) {
			case -1:
				break;
			case java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
			case java.net.HttpURLConnection.HTTP_RESET:
			case java.net.HttpURLConnection.HTTP_INTERNAL_ERROR:
			case 200:
				addRedo(referer, link, depth);
			}
			feedback.put("stop", System.currentTimeMillis());
			log.warning("SKIPPED: Exceeded read retry limit. "+link+". HTTP code: "+resp.getStatusLine().getStatusCode());
			return false;
		}
		feedback.put("stop", System.currentTimeMillis());
		return true;
	}
	/** Adds the page to the read queue.
	 * @param link Link to the page.
	 * @param ref Link to the page link was found on.
	 * @param depth The CURRENT read depth. (the depth that was passed to you)
	 */
	protected final void addToReadQueue(Uri link, final Uri ref, final int depth) {
		if (!readDeep) return;
		link = new Uri(link.trimFragment());
		if (depth < toberead.length && !session.isPrevRead(link.toString())) {
			if (toberead[depth]==null) {
				toberead[depth] = new Queue<QueueElement>();
				//toberead[depth] = new LinkedHashMap<Uri, String>();
			}
			QueueElement qe=new QueueElement(link, ref);
			for (int i = 0; i < depth+1; i++) {
				if (toberead[i].contains(qe)) return;
			}
			if (session.getOptions().isIgnoredPage(link, ref)) {
				log.debug("ReadQueue:ignored",link);
				return;
			}
			toberead[depth].offer(qe);
			//toberead[depth].put(link, ref);
			log.information("ReadQueue","Added "+link+" at depth "+depth);
			session.prTotPageAdd(1);
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
	public final void addRedo(final Uri referer, final Uri link, final int depth) {
		final Site tmp = new Site(referer, link, depth);
		if (!redo.contains(tmp)) {
			log.information("Added to retry stack: "+link);
			redo.push(tmp);
		}
	}
	public final void stop() {
		this.run = false;
		if (pp!=null) {
			pp.stop();
		}
	}
	protected final void addToReadQueueForce(Uri link,Uri ref,int depth){
		if (!readDeep) return;
		link = new Uri(link.trimFragment());
		if (depth < toberead.length && !session.isPrevRead(link.toString())) {
			if (toberead[depth]==null) {
				toberead[depth] = new Queue<QueueElement>();
			}
			QueueElement qe=new QueueElement(link, ref);
			for (int i = 0; i < depth+1; i++) {
				if (toberead[i].contains(qe)) return;
			}
			toberead[depth].offer(qe);
			log.information("ReadQueue","ForceAdded "+link+" at depth "+depth);
			session.prTotPageAdd(1);
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
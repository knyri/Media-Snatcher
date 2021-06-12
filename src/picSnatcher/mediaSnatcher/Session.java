/**
 *
 */
package picSnatcher.mediaSnatcher;

import static picSnatcher.mediaSnatcher.OptionKeys.download_keepDownloadLog;
import static picSnatcher.mediaSnatcher.OptionKeys.download_prependPage;
import static picSnatcher.mediaSnatcher.OptionKeys.download_saveExternalUrlList;
import static picSnatcher.mediaSnatcher.OptionKeys.download_saveLinkList;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_repeat;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_saveFile;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_saveFolder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.Timer;

import simple.CIString;
import simple.gui.factory.SwingFactory;
import simple.html.MimeTypes;
import simple.io.FileUtil;
import simple.net.Uri;
import simple.net.http.Client;
import simple.time.TimeRemainingEstimator;
import simple.time.TimerFactory;
import simple.time.TimerFactory.Algorithm;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

/**
 * <br>Created: Jun 24, 2009
 * @author Kenneth Pierce
 */
public class Session implements Runnable {
	public static Client conCont=
//			new Client("127.0.0.1",8888);
			new Client();
	public static final int STATE_READ = 0, STATE_DOWNLOAD = 1;
	private int state = 0;
	private PageReader parser;
	private Downloader downloader;
	/**
	 * page reader page count(total)
	 */
	private int pr_cPages = 0;
	/**
	 * page reader time remaining estimator
	 */
	private final TimeRemainingEstimator pr_tre = TimerFactory.getTRETimer(Algorithm.SAMPLE, 15);
	private final Main main;
	private final Options option;
	private final UrlMap linksOut=new UrlMap();
	private final DownloadMap Links= new DownloadMap();
	private final HashSet<String> dlList = new HashSet<String>();
	private final HashSet<CIString> readList = new HashSet<CIString>();
	private final Object waiter = new Object();
	private boolean run = false;
	private RandomAccessFile dlLog;
	private static final Log log = LogFactory.getLogFor(Session.class);
	public Session(final Main main) throws FileNotFoundException {
		this(main, null);
	}
	public Session(final Main main, final Options options) throws FileNotFoundException {
		this.main = main;
		if (options != null) {
			this.option = options;
		} else {
			option = new Options(main, this);
		}
	}
	public boolean addOutLink(Uri page, Uri link){
		return linksOut.put(page.getOriginalUri(), link);
	}
	/* **************************
	 * **************************
	 * operations on Links
	 * **************************
	 * **************************/
	public void addToDownloadList(final String url) {
		if (!dlList.contains(url)) {
			dlList.add(url);
			if (option.getBoolean(download_keepDownloadLog)) {
				log.information("DLLOG", "Added to list: "+url);
				try {
					dlLog.writeBytes(url+"\r\n");
				} catch (final IOException e) {
					log.warning("DLLOG", e);
				}
			}
		}
	}
	public Options getOptions() {
		return option;
	}
	public boolean isPrevDownloaded(final String link) {
		return dlList.contains(link);
	}
	/**
	 * Has the page been read before this session?
	 * @param link
	 * @return
	 */
	public boolean isPrevRead(final String link){
		return readList.contains(new CIString(link));
	}
	/**
	 * Add a page to the read list
	 * @param link
	 */
	public void addToReadList(final String link) {
		readList.add(new CIString(link));
	}
	/* ***************************
	 * ***************************
	 * Status Functions
	 * ***************************
	 * ***************************/
	/**
	 * Number of links to be downloaded
	 * @return
	 */
	public int getLinkCount() {
		return Links.size();
	}
	public void setTotalProgress(double cur, double max) {
		if (max==0) {
			cur = max = 1;
		}
		main.totPBar.setValue((int)((cur/max)*100));
	}
	public void setCurrentProgress(double cur, double max) {
		if (max==0) {
			cur = max = 1;
		}
		main.curPBar.setValue((int)((cur/max)*100));
	}
	public void setStatus(final String msg) {
		main.setStatus(msg);
	}
	public void setState(final String msg) {
		main.setState(msg);
	}
	public void setStateExt(final String msg) {
		main.setStateExt(msg);
	}
	public void setTotalProgressBarText(final String msg) {
		main.setTopProgressBarText(msg);
	}
	public void setCurrentProgressBarText(final String msg) {
		main.setBottomProgressBarText(msg);
	}
	private int zeroDownloadCount= 0;
	private int[] repeatDelays= new int[]{30,180,300,300,600,3600};
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		run = true;
		option.dumpOptions();
		dlList.clear();
		readList.clear();
		Links.clear();
		linksOut.clear();
		if (option.getBoolean(download_keepDownloadLog)) {
			if (option.getOption(snatcher_saveFile).isEmpty()) {
				main.Save(this);
			}
			final File dlLogFile = new File(option.getOption(snatcher_saveFile)+".dlog");
			log.information("setup", "Save file: "+dlLogFile);
			try {
				dlLog = new RandomAccessFile(dlLogFile,"rw");
			} catch (final IOException e) {
				log.log(0x20);
				log.error("download", e);
				setState("Error making log file.");
				setStateExt("done");
				finish();
				return;
			}
			main.setState("Reading download log...");
			try {
				String tmp = dlLog.readLine();
				while (tmp != null) {
					dlList.add(tmp);
					tmp = dlLog.readLine();
				}
				log.information("setup", "Read "+dlList.size()+" items from download log.");
			}catch(final IOException e) {
				log.log(0x21);
				log.error("setup", e);
				setState("Error reading log file.");
				setStateExt("done");
				finish();
				return;
			}
		}
		main.setState("Starting...");
		do{
			if(option.getOption(snatcher_repeat).equals(Options.TRUE)){
				log.information("setup", "AUTO REPEATING SNATCH");
				main.frame.setTitle("Media Snatcher 4 REPEAT MODE(must manually stop)");
			}
			option.updateWanted();
			setTotalProgressBarText("");
			setCurrentProgressBarText("");
			option.dumpOptions();

			if (option.getOption(snatcher_saveFile).isEmpty()) {
				final File saveFolder = SwingFactory.getDirName(null);
				if (saveFolder==null) {
					log.log(0x22);
					setState("Save folder not selected");
					setStateExt("done");
					log.warning("Aborted: Save folder not selected.(User hit cancel on the select save folder dialog)");
					finish();
					return;
				}
				option.setOption(snatcher_saveFolder, saveFolder.getAbsolutePath()+File.separatorChar);
			} else {
				final String tmp = option.getOption(snatcher_saveFile);
				option.setOption(snatcher_saveFolder, tmp.substring(0, tmp.lastIndexOf(File.separatorChar)+1));
			}
			final int totalItems = main.getList().getItemCount();
			UriFormatIterator pf;
			for(int i=0;i<main.getList().getItemCount();i++){
				pf=main.getList().getItemAt(i);
				this.prTotPageAdd(Math.max(1,pf.getTotalItems()));
			}
			log.information("Start", "Items in list: "+totalItems);
			for (int i = 0;run && i<totalItems;i++) {
				setTotalProgressBarText("Reading item " + (i+1) + "/" + totalItems);
				setTotalProgress((i+1), totalItems);
				log.information("download","Reading item " + (i+1) + "/" + totalItems);
				pf = main.getList().getItemAt(i);
				if(pf==null){
					log.warning("Null item in list", i);
					continue;
				}
				pf.reset();
				parser = new PageReader(pf, option.deepScan(), this);
				parser.setReadDepth(option.getReadDepth());
				parser.readSite();
				log.information("download","Done reading item " + (i+1) + "/" + totalItems);
			}//end for
			log.debug("download", "DONE READING THEM ALL");
			setStateExt("");
			if(option.getOption(download_saveExternalUrlList)==Options.TRUE)
				saveExternalLinkList();
			if(option.getOption(download_saveLinkList)==Options.TRUE)
				saveLinkList();
			if (!run) {log.log(0x30);finish();return;}
			state = STATE_DOWNLOAD;
			downloader = new Downloader(this, Links);
			downloader.run();
			if(option.getOption(snatcher_repeat).equals(Options.TRUE)){
				readList.clear();
				Links.clear();
				linksOut.clear();
				synchronized(waiter){
					final Timer t=new Timer(1000, new ActionListener(){
						int time= repeatDelays[zeroDownloadCount] - 1;
						@Override
						public void actionPerformed(final ActionEvent ae) {
							time--;
							main.setState("Waiting "+time+" seconds before next snatch run.");
						}
					});
					t.setRepeats(true);

					int delay= repeatDelays[zeroDownloadCount];
					if(downloader.getDownloadedCount() == 0){
						if(zeroDownloadCount < repeatDelays.length){
							zeroDownloadCount++;
						}
					}else if(zeroDownloadCount > 0){
						zeroDownloadCount--;
					}

					try{
						if(run){
							main.setState("Waiting " + delay + " seconds before next snatch run.");
							t.start();
							while(delay-- > 0 && run){
								waiter.wait(1000);
							}
						}
					}catch(final InterruptedException e){
						log.error("IGNORE", e);
					}finally{
						t.stop();
					}
				}
			}
		}while(run && option.getOption(snatcher_repeat).equals(Options.TRUE));
		finish();
	}
	private void finish() {
		if (dlLog != null) {
			FileUtil.close(dlLog);
		}
		setState("Done.");
		setStateExt("");
		main.GO.setText("Snatch");
		main.GO.setActionCommand("start");
	}
	public void stop() {
		run = false;
		switch (state) {
		case STATE_READ:
			parser.stop();
			break;
		case STATE_DOWNLOAD:
			downloader.stop();
		}
	}
	private static final String stripExtra(final Uri url) {
		return url.getScheme()+"://"+url.getHost()+url.getPath()+url.getFile();
	}
	public void addLink(final Uri link, final Uri referer, String altFileName, boolean force) {
		if (isPrevDownloaded(link.toString())) {
			log.debug("addLink","Previously downloaded: "+link);
			return;
		}
		if (option.isDownloadIgnored(link, referer)) {
			log.debug("addLink","isIgnored: "+link);
			return;
		}
		if (Links.containsValue(link)) {
			if (option.getOption(download_prependPage).equals(Options.TRUE)) {
				// already in link list. see if we need to update the referrer
				final String key = Links.getKey(link);
				if (referer.getFile().toLowerCase().startsWith("index."))
					// don't update if the referrer is the index page. TODO: Why not?
					return;
				if (key==null) {//shouldn't happen, but doesn't matter if it does
					log.debug("addLink", "Contains null key!");
				} else {
					Links.remove(key, link);
					log.information("addLink", "readded with new referer: "+link);
					force = true;
				}
			} else
				return;
		}
		if (force) {//add if forced
			if (Links.put(stripExtra(referer),link, altFileName)) {
				log.debug("forced:added(addLink)", link.toString());
			}
			return;
		}
		/*
		 * Check the MIME type.
		 * This is just a prefilter based on the extension
		 */
		String ext = link.getFile();
		final int dot = ext.lastIndexOf('.');
		if(dot==-1){
			// no extension add it and deal with it later
			if (Links.put(stripExtra(referer),link,altFileName)){
				log.information("addLink", "added: "+link);
			}
		}else{
			ext = ext.substring(ext.lastIndexOf('.')+1);
			ext = MimeTypes.getMime(ext).get(0);
			if (option.isWantedMIME(ext)) {
				if (Links.put(stripExtra(referer),link,altFileName)){
					log.information("addLink", "added: "+link);
				}
			} else {
				log.debug("addLink", "unwanted MIME: "+link);
			}
		}
	}
	public void addLink(final Uri link, final Uri referer, boolean force) {
		addLink(link, referer, null, force);
	}
	public void dumpOptions() {
		option.dumpOptions();
	}
	void remove(final UriFormatIterator pf){
		main.remove(pf);
	}
	void prTreCount(){
		pr_tre.sample();
	}
	void prTotPageAdd(int cPages){
		pr_cPages+=cPages;
		pr_tre.setTotalItems(pr_cPages);
	}
	void prUpdateStatus(){
		//TODO fix ETC
		setStatus("URLs: "+getLinkCount()+" Pages read: "+pr_tre.getTotalSampleCount()+"/"+pr_tre.getTotalItems()+" ETC:"+TimerFactory.getTime(pr_tre.getRemaining())+" :: rate: "+pr_tre.getRate());
	}
	private void saveExternalLinkList(){
		try {
			final File linkListf=new File(option.getOption(snatcher_saveFile)+"-external.html");
			final FileWriter linkListfw;

			if(!linkListf.exists()){
				if(FileUtil.createFile(linkListf)){
					linkListfw=new FileWriter(linkListf,true);
					linkListfw.write("<!doctype html><html><head><title>External Link List</title></head><body><p>All links open in a new window</p>");
				}else{
					log.error("Failed to create the file for the external links.");
					return;
				}
			} else
				linkListfw=new FileWriter(linkListf,true);

			for(Map.Entry<CIString, List<Uri>> entry : linksOut.entrySet()){
				linkListfw.write("<a href=\""+entry.getKey()+"\" target=\"_blank\">"+
						entry.getKey()+"</a><br>\n<ul>");
				for(Uri uri : entry.getValue()){
					linkListfw.write("\t<li><a href=\""+uri+"\" target=\"_blank\">"+uri+"</a></li>\n");
				}
				linkListfw.write("</ul>\n<hr>\n");
			}
			// Don't finish the page. It will still display correctly and
			//linkListfw.write("</body></html>");
			FileUtil.close(linkListfw);
		} catch (IOException e1) {
			log.error("Could not save external link list",e1);
		}
	}
	private void saveLinkList(){
		try {
			final File linkListf=new File(option.getOption(snatcher_saveFile)+".html");
			final FileWriter linkListfw;

			if(!linkListf.exists()){
				if(FileUtil.createFile(linkListf)){
					linkListfw=new FileWriter(linkListf,true);
					linkListfw.write("<!doctype html><html><head><title>Link List</title></head><body><p>Links open in a new window</p>");
				}else{
					log.error("Failed to create the file for the link list");
					return;
				}
			}else
				linkListfw=new FileWriter(linkListf,true);

			for(CIString entry : readList){
				linkListfw.write("<a href=\""+entry+"\" target=_blank>"+entry+"</a><br>\n");
			}
			FileUtil.close(linkListfw);
		} catch (IOException e1) {
			log.error("Could not save link list",e1);
		}
	}
}

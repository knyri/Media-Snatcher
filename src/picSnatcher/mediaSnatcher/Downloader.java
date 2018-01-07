/**
 *
 */
package picSnatcher.mediaSnatcher;
import static picSnatcher.mediaSnatcher.OptionKeys.download_alternateNumbering;
import static picSnatcher.mediaSnatcher.OptionKeys.download_dateSubfolder;
import static picSnatcher.mediaSnatcher.OptionKeys.download_ppAsDir;
import static picSnatcher.mediaSnatcher.OptionKeys.download_prependPage;
import static picSnatcher.mediaSnatcher.OptionKeys.download_prettyFilenames;
import static picSnatcher.mediaSnatcher.OptionKeys.download_sameFolder;
import static picSnatcher.mediaSnatcher.OptionKeys.download_separateByDomain;
import static picSnatcher.mediaSnatcher.OptionKeys.download_siteFirst;
import static picSnatcher.mediaSnatcher.OptionKeys.download_usePageDirectory;
import static picSnatcher.mediaSnatcher.OptionKeys.download_usePageDomain;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_saveFile;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;

import simple.CIString;
import simple.html.MimeTypes;
import simple.io.FileUtil;
import simple.net.Uri;
import simple.time.TimeRemainingEstimator;
import simple.time.Timer;
import simple.time.TimerFactory;
import simple.time.TimerFactory.Algorithm;
import simple.util.HexUtil;
import simple.util.Stack;
import simple.util.TimeUtil;
import simple.util.UrlUtil;
import simple.util.do_str;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;


/**Downloads the files.
 * <br>Created: Jun 24, 2010
 * @author Kenneth Pierce
 */
public class Downloader implements Runnable {
	private static final String CERROR = "cgsPergerror", C404 = "jIbee404", COTHER = "jibber other",CEXIST="exists";
	private final Log log = LogFactory.getLogFor(Downloader.class);
	private final Stack<Site> redo = new Stack<Site>();
	int retrycount = 0;
	long totalFiles = 0;
	// TODO: fix this thing
	final TimeRemainingEstimator tre = TimerFactory.getTRETimer(Algorithm.SAMPLE, 20);
	double timeAvg = 0; //Milliseconds
	boolean run = true;

	//----Alternate Numbering Variables
	int fileNum = 0,
		entryNum = -1;
	//----End Alternate Numbering Variables

	int cPrevDownloaded = 0,
		cSkipped = 0,
		cErrors = 0,
		cDownloaded = 0,
		cDoOvers = 0,
		cDeleteErrors = 0;
	private final Session session;
	private final UrlMap Links;
	public Downloader(final Session s, final UrlMap links) {
		session = s;
		Links = links;
	}
	@Override
	public void run() {
		System.out.println("download start");
		log.information("start", "start");
		final Options option = session.getOptions();
		option.dumpOptions();
		final Timer timer = new Timer();
		session.setStateExt("");
		session.setState("");
		session.getOptions().updateWanted();
		session.setCurrentProgressBarText("");
		session.setTotalProgressBarText("");
		session.setTotalProgress(0, Links.size());
		tre.setTotalItems(Links.size());

		log.information("setup", "Saving files to "+option.getOption(OptionKeys.snatcher_saveFolder));
		log.information("Number of keys", Links.keySet().size());
		out:
			for (final CIString key : Links.keySet()) {
				if (!run) {
					log.warning("User ended");
					break out;
				}
				entryNum++;
				fileNum = 0;
				log.information("From page",key);
				log.information("Number of entries", Links.get(key).size());
				for (final Uri cUrl : Links.get(key)) {
					session.setStatus("URLs: "+Links.size()+" Retry: "+redo.size()+" Downloaded: "+ cDownloaded+" Est. TTC: "+TimeUtil.getTime(tre.getRemaining())+" :: rate "+tre.getRate());
					retrycount = 0;
					totalFiles++;
					fileNum++;
					session.setTotalProgress((int)totalFiles, Links.size());
					session.setTotalProgressBarText(totalFiles+"/"+Links.size());
					log.information("url",cUrl);
					try {
						downloadItem(cUrl, key.toString());
						tre.sample();
					} catch (final Exception e) {
						log.error("Unhandled", e);
					}
					if (!run) {
						log.warning("User ended");
						break out;
					}
				}//end inner for
			}//end for
		Site tmp;
		log.information("Reading do overs.");
		while(!redo.isEmpty()) {
			session.setStatus("URLs: "+Links.size()+" Retry: "+redo.size()+" Downloaded: "+ cDownloaded);
			session.setCurrentProgressBarText(redo.size()+" redos left. "+cDoOvers+" done already.");
			cDoOvers++;
			retrycount = 0;
			tmp = redo.pop();
			fileNum = tmp.getDepth();
			if (downloadItem(tmp.getSite(), tmp.getReferrer().getOriginalUri())) {
				tre.sample();
			}
		}
		log.information("LastChance", "Done reading do overs.");
		session.setState("Downloaded: "+cDownloaded+" Skipped: "+cSkipped+" Previously DLed: "+cPrevDownloaded+" Errors: "+cErrors+" Do Overs: "+cDoOvers);
		session.setStateExt("done");
		log.information("Downloaded: "+cDownloaded+" Skipped: "+cSkipped+" Previously DLed: "+cPrevDownloaded+" Errors: "+cErrors+" Do Overs: "+cDoOvers
				+" Delete Errors: "+cDeleteErrors);
		log.information(new Date(System.currentTimeMillis()).toString());
		log.information("Time elapsed: "+TimeUtil.getTime(timer.elapsed()));
	}
	private boolean downloadItem(Uri cUrl, final String referer){
		//Shouldn't be needed since addLink() checks this
		/*if (session.isIgnored(cUrl.toString(), new UriParser(referer))) {
			log.information("Skipped: not wanted");
			cSkipped++;
			return false;
		}*/
		String fName = null;//File Name
		File fTmp = null;//File to be written to
		FileOutputStream out = null;
		//----End File Variables
		//----Web Variables
		HttpResponse resp = null;
		InputStream in = null;
		//----End Web Variables
		int bCount = 0;//used to keep track of the number of bytes downloaded
		final byte[] b = new byte[1024];//Buffer
		long size = 0;
		int p_cur = 0;
		session.setState("");
		session.setStateExt("");
		if (!run)
			//log.debug("USERSTOP", "~line 642");
			return false;
		if (session.isPrevDownloaded(cUrl.trimTrailing())) {//NOTE:dup check
			log.information("Already downloaded "+cUrl);
			cPrevDownloaded++;
			return false;
		}
		session.setStateExt(cUrl.toString());
		try{
			if(referer!=null)
				resp=Session.conCont.get(cUrl.toString(),new BasicHeader[]{new BasicHeader("Referer",referer)});
			else
				resp=Session.conCont.get(cUrl.toString());
		}catch(IOException e){
			log.error(e);
			return false;
		}
		try {
			fName = createFileName(cUrl, referer, resp);
			//check for errors
			if (fName == Downloader.CERROR){
				FileUtil.close(resp.getEntity().getContent());
				throw new IOException("connect error");
			}
			if (fName == Downloader.C404) {
				FileUtil.close(resp.getEntity().getContent());
				log.debug("Skip: File not found on server.");
				cSkipped++;
				cErrors++;
				return false;
			}
			if (fName == Downloader.COTHER){FileUtil.close(resp.getEntity().getContent());return false;}
			if (fName==Downloader.CEXIST){FileUtil.close(resp.getEntity().getContent());return true;}

			fTmp = new File(fName);
			if (fTmp.exists()) {
				FileUtil.close(resp.getEntity().getContent());
				cSkipped++;
				log.debug("SKIP","Already exists: "+fTmp);
				return true;
			}
		} catch (final IOException e1) {
			log.error(e1);
			switch (resp.getStatusLine().getStatusCode()) {
			case -1:
				break;
			case 200:
			case java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
			case java.net.HttpURLConnection.HTTP_RESET:
			case java.net.HttpURLConnection.HTTP_INTERNAL_ERROR:
				final Site tmp = new Site(new Uri(referer), cUrl, fileNum, entryNum);
				if (!redo.contains(tmp)) {
					log.information("Added "+cUrl+" to the retry stack.");
					redo.push(tmp);
				}
			}
			session.setState("");
			log.warning("Exceeded retry limit.");
			cErrors++;
			cSkipped++;
			return false;
		}catch(NullPointerException e){
			log.error(cUrl.toString(),e);
			return false;
		}
		log.debug("parsed url: "+cUrl.getOriginalUri());
		if (fTmp.exists()) {
			try{
				FileUtil.close(resp.getEntity().getContent());
			}catch(IllegalStateException e){
				/*
				 * Thrown when there is no content (Length: 0) or connect failure
				 */
			}catch(IOException e){
			}
			cSkipped++;
			log.debug("SKIP","Already exists: "+fTmp);
			return true;
		}
		//FLOW: downloading
		log.information("Snatch", fName + "\t" + cUrl.toString());

		for (int retrycount2 = 1; retrycount2 < 4; retrycount2++) {
			/*
			 * Try 3 times to download the item
			 */
			if (retrycount == -2) {
				try{
				if(referer!=null)
					resp=Session.conCont.get(cUrl.toString(),new BasicHeader[]{new BasicHeader("Referer",referer)});
				else
					resp=Session.conCont.get(cUrl.toString());
				}catch(IOException e){
					log.error(e);
					return false;
				}
			}
			try{
				in = resp.getEntity().getContent();
			}catch(IllegalStateException e1){
				log.error(e1);
				//e1.printStackTrace();
			}catch(IOException e1){
				log.error(e1);
				//e1.printStackTrace();
			}
			if (in == null) {
				//no content
				if (out!=null) {
					if (out.getChannel().isOpen()) {
						FileUtil.close(out);
						log.debug("FILESYSTEM", "Closing OutputStream");
					}
					deleteFile(fTmp);
				}
				switch (resp.getStatusLine().getStatusCode()) {
				//find the error and do something
				case -1:
					break;
				case 200:
				case java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
				case java.net.HttpURLConnection.HTTP_RESET:
				case java.net.HttpURLConnection.HTTP_INTERNAL_ERROR:
					final Site tmp = new Site(new Uri(referer), cUrl, fileNum, entryNum);
					if (!redo.contains(tmp)) {
						log.information("Added "+cUrl+" to the retry stack.");
						redo.push(tmp);
					}
				}
				log.warning("Retry count exceeded while getting input stream. Moving to next.");
				cErrors++;
				return false;
			}
			if (!run)
				return false;
			try {
				if (retrycount2 == 1) {
					fTmp = new File(fName+".tmp");
					FileUtil.ensureDir(fTmp);
				}
				if (fTmp.createNewFile()) {
					log.debug("FILESYSTEM", "Created "+fTmp.getName());
				} else {
					log.error("FILESYSTEM", "Error creating "+fTmp.getName());
				}
				out = new FileOutputStream(fTmp, false);
			} catch (final IOException e) {
				log.error(fTmp.toString());
				log.error("Create new file", e);
				if (out.getChannel().isOpen()) {
					FileUtil.close(out);
					log.debug("FILESYSTEM", "Closing OutputStream");
				}
				FileUtil.close(in);
				deleteFile(fTmp);
				cErrors++;
				cSkipped++;
				return false;
			}
			try {//FLOW: download start
				session.setState("Downloading from "+cUrl.getHost()+ ". Try "+retrycount2+" of 3.");
				session.setStateExt(cUrl.getPath()+cUrl.getFile()+((cUrl.getQuery().isEmpty())?"":"?"+cUrl.getQuery()));
				if (size == 0) {
					size = resp.getEntity().getContentLength();
				}
				session.setCurrentProgress(0, (int)size);
				session.setCurrentProgressBarText("0% of "+size);
				log.debug("File size(server):"+size);
				final TimeRemainingEstimator tre = TimerFactory.getTRETimer(Algorithm.SAMPLE, 20);
				final Timer dlTimer = new Timer();
				if (size != -1) {
					final String fileSize = FileUtil.formatSize(size);
					long curSpeed;
					tre.setTotalItems((int)(size/b.length)+1);
					while (run&((bCount=in.read(b))!=-1)) {
						//1024
						tre.sample();
						p_cur += bCount;
						session.setCurrentProgress(p_cur, (int)size);
						curSpeed = p_cur/(1+(dlTimer.elapsed()/1000));
						//tre.debug(log);
						session.setCurrentProgressBarText((int)(((double)p_cur/(double)size)*100.0)+"% of "+fileSize+" @ "+FileUtil.formatSize(curSpeed)+"/sec | "+TimeUtil.getTime(tre.getRemaining()));
						out.write(b, 0, bCount);
					}
				} else {
					long bytes = 0;
					session.setCurrentProgress(1, 1);
					while (run&((bCount=in.read(b))!=-1)) {
						bytes += bCount;
						session.setCurrentProgressBarText(FileUtil.formatSize(bytes)+" @ "+FileUtil.formatSize(bytes/(1+(dlTimer.elapsed()/1000)))+"/sec");
						out.write(b, 0, bCount);
					}
				}
				log.debug("File size(local):"+fTmp.length());
				if (out.getChannel().isOpen()) {
					FileUtil.close(out);
					log.debug("FILESYSTEM", "Closing OutputStream");
				}
				if (size > 0 && fTmp.length() < size) {
					deleteFile(fTmp);
					if (retrycount2 < 3) {
						log.information("Size mismatch", fTmp.length()+"!="+size+"; Retrying.");
						retrycount = -2;
						continue;
					} else {
						final Site tmp = new Site(new Uri(referer), cUrl, fileNum, entryNum);
						if (!redo.contains(tmp)) {
							log.information("Added "+cUrl+" to the retry stack.");
							redo.push(tmp);
						}
						FileUtil.close(in);
						break;
					}
				}
				retrycount = -1;
				cDownloaded++;
				FileUtil.close(in);
				break;
			} catch (final SocketTimeoutException ste) {
				log.error("download", ste);
			} catch (final EOFException e) {
				log.error("download", e);
			} catch (final IOException e) {
				log.error("Unhandled", e);
			} finally {
				if (retrycount != -1) {
					log.debug("File size(local):"+fTmp.length());
					if (out.getChannel().isOpen()) {
						FileUtil.close(out);
						log.debug("FILESYSTEM", "Closing OutputStream(finally)");
					}
					deleteFile(fTmp);
					if (retrycount2 < 3) {
						log.information("Retrying", retrycount2);
						retrycount = -2;
						continue;
					} else {
						final Site tmp = new Site(new Uri(referer), cUrl, fileNum, entryNum);
						if (!redo.contains(tmp)) {
							log.information("Added "+cUrl+" to the retry stack.");
							redo.push(tmp);
						}
						FileUtil.close(in);
					}
				}
			}
		}
		if (run && retrycount == -1 && fTmp.length() > 0) {
			if (!fTmp.renameTo(new File(fName))) {
				log.error("FILESYSTEM", "Rename failed "+fTmp);
				deleteFile(fTmp);
			} else {//NOTE: success
				log.information("File Saved: "+fTmp.toString());
				session.addToDownloadList(cUrl.trimTrailing());//NOTE: dup check
			}
		} else {
			switch (resp.getStatusLine().getStatusCode()) {
			case -1:
				break;
			case 200:
			case java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
			case java.net.HttpURLConnection.HTTP_RESET:
			case java.net.HttpURLConnection.HTTP_INTERNAL_ERROR:
				final Site tmp = new Site(new Uri(referer), cUrl, fileNum, entryNum);
				if (!redo.contains(tmp)) {
					log.information("Added "+cUrl+" to the retry stack.");
					redo.push(tmp);
				}
			}
			cErrors++;
			cSkipped++;
			if (!run) {
				log.error("Download Failed", "Cancelled by user");
			} else if (fTmp.length() == 0) {
				log.error("Download Failed", "File size is 0.");
			} else {
				log.error("Download Failed", "Retry count exceeded.");
			}
			deleteFile(fTmp);
			return false;
		}
		return true;
	}
	private boolean deleteFile(final File file) {
		if (file.exists()) {
			if (file.delete()) {
				log.debug("FILESYSTEM", "Deleted "+file.getName());
				return true;
			} else {
				log.error("FILESYSTEM", "Delete FAILED "+file.getName());
				cDeleteErrors++;
				return false;
			}
		} else
			return true;
	}
	public void stop() {
		run = false;
	}
	/**Creates a string representing the filename and path.<br>
	 * NOTE: connects to the server
	 * @param file
	 * @param referer
	 * @param cc response from the server. checked for special headers
	 * @return The path of the file
	 * @throws IOException
	 */
	private String createFileName(final Uri file, final String referer, final HttpResponse cc) throws IOException {
		final Options option = session.getOptions();
		String Stmp;
		String fPath= "";
		String fName = file.getFile();
		String ext = null;
		int index = 0;

		/*
		 * FLOW: start file path creation
		 */
		final Uri ref = new Uri(referer);
		String domain;
		if(option.getBoolean(download_usePageDomain)){
			domain= ref.getDomain();
		}else{
			domain= file.getDomain();
		}
		if(option.getBoolean(download_sameFolder)){
			// use the save file as the root folder
			fPath= option.getOption(snatcher_saveFile);
			if(fPath == null || fPath.isEmpty()){
				fPath= "snatcher" + File.separatorChar;
			}else{
				fPath= fPath.substring(fPath.lastIndexOf(File.separatorChar)+1,fPath.length() - 4) + File.separatorChar;
			}
			if(option.getBoolean(download_separateByDomain)){
				fPath+= domain + File.separatorChar;
			}
		}else{
			if(option.getBoolean(download_separateByDomain)){
				fPath+= domain + File.separatorChar;
			}
		}

		if(option.getBoolean(download_dateSubfolder)){
			if(option.getBoolean(download_siteFirst)){
				// domain/date
				fPath += domain + File.separatorChar + LogFactory.getDateStamp() + File.separatorChar;
			}else{
				// date
				fPath += LogFactory.getDateStamp() + File.separatorChar;
				if(!option.getBoolean(download_sameFolder)){
					// date/domain
					fPath+= domain + File.separatorChar;
				}
			}
		}

		if(!option.getBoolean(download_sameFolder)){
			if (option.getBoolean(download_usePageDirectory)) {
				fPath += ref.getPath().substring(1).replace('/',File.separatorChar);
			} else {
				fPath += file.getPath().substring(1).replace('/',File.separatorChar);
			}
		}

		if(option.getBoolean(download_prependPage)){
			index = ref.getFile().lastIndexOf('.');
			if (index==-1) {
				fPath+= ref.getFile();
			} else {
				fPath+= ref.getFile().substring(0, index);
			}
			if(option.getBoolean(download_ppAsDir)){
				fPath+= File.separatorChar;
			}else{
				fPath+= '_';
			}
		}

		Stmp=null;
		try{
			Stmp = cc.getLastHeader("Content-Disposition").getValue();
		}catch(Exception e){}
		if (Stmp != null && !Stmp.isEmpty()) {//did we get one?
			index = Stmp.indexOf("filename");
			int end = 0;
			if (index != -1) {
				index = Stmp.indexOf('=', index)+1;
				end = Stmp.indexOf(';', index);
				if (end != -1) {
					fName = Stmp.substring(index, end);
				} else {
					fName = Stmp.substring(index);
				}
				if (fName.charAt(0)=='"') {
					fName = fName.substring(1, fName.length()-1);
				}
				log.information("Server suggested filename: "+fName);
				//get the extension again
				index = fName.lastIndexOf('.');
				if (index != -1) {
					ext = fName.substring(index);
				}
			}
		}
		//get extension for later
		index = fName.lastIndexOf('.');
		if (index != -1) {
			ext = fName.substring(index);
		}else{
			ext="";
		}
		if (new File(fPath+fName).exists()){
			return Downloader.CEXIST;
		}
		if (option.getBoolean(download_alternateNumbering)) {//alternate numbering scheme
			fName = do_str.padLeft(4, '0', Integer.toString(entryNum)) + "_" +
					do_str.padLeft(3, '0', Integer.toString(fileNum))+ext;
		}

		//FLOW:check extension
		String mime = MimeTypes.getMimeFromContentType(cc.getEntity().getContentType().getValue());
		if(ext.isEmpty()){
			List<String> exts=MimeTypes.getExt(mime);
			if(exts.isEmpty()){
				log.warning("No extension mapped for "+mime+"! Please let the developer know.(kcpiercejr@gmail.com)");
			}else{
				ext=exts.get(0);
			}
		}else{
			List<String> mimes=MimeTypes.getMime(ext);
			if(!mimes.isEmpty())
				mime=mimes.get(0);
		}
		if (!option.isWantedMIME(mime)) {
			log.information("SKIPPED", "Failed MIME test: "+ mime + " : " + file.toString());
			cSkipped++;
			return Downloader.COTHER;
		}
		if (ext != null && !ext.isEmpty()) {//extension present
			if (!MimeTypes.getMime(ext).contains(mime)) {//is it valid for this MIME?
				log.debug("extension not mapped to MIME. ext:"+ext+" | MIME:"+mime);
				Stmp = MimeTypes.getExt(mime).get(0);
				if (Stmp == null || Stmp.isEmpty()) {
					log.warning("No extension mapped for "+mime+"! Please let the developer know.(kcpiercejr@gmail.com)");
				} else {
					fName = fName.substring(0, index)+'.'+Stmp;
				}
			}
		} else {//extension not present
			Stmp = MimeTypes.getExt(mime).get(0);
			if (Stmp == null || Stmp.isEmpty()) {
				log.warning("No extension mapped for "+mime+"! Please let the developer know.(kcpiercejr@gmail.com)");
			} else {
				fName = fName.substring(0, index)+'.'+Stmp;
			}
		}
		/*
		 * Combine the file path and the filename.
		 */
		fName = fPath+UrlUtil.URLescape2(fName);
		log.debug("combined", "fName = "+fName);
		/*
		 * Make the filename pretty if set.
		 */
		if (option.getBoolean(download_prettyFilenames)) {
			final StringBuilder resolved=new StringBuilder(fName.length());
			int lend=0;
			for(int i=0;i<fName.length();i++){
				if(fName.charAt(i)!='%'){resolved.append(fName.charAt(i));continue;}
				lend=i+3;i++;
				if((i+2)>fName.length())continue;
				if(fName.charAt(lend)=='%')
					while(fName.charAt(lend+3)=='%'){
						lend+=3;
						if((lend+3)>fName.length())break;
					}
				resolved.append(
						new String(
								HexUtil.fromHex(
										fName.substring(i,lend)
										.replace("%",""))));
				i=lend-1;
			}
			fName=resolved.toString();
			fName = fName.replaceAll("(_20|_)", " ");
			log.debug("pretty selected", "fName = "+fName);
		}
		if (File.separatorChar == '\\') {
			fName = fName.replaceAll("/","\\\\");
		}
		return option.getOption(OptionKeys.snatcher_saveFolder)+fName;
	}
}

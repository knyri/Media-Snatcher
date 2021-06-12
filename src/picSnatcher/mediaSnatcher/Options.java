// tab width: 2
/**
 *
 */
package picSnatcher.mediaSnatcher;

import static picSnatcher.mediaSnatcher.OptionKeys.download_alternateNumbering;
import static picSnatcher.mediaSnatcher.OptionKeys.download_dateSubfolder;
import static picSnatcher.mediaSnatcher.OptionKeys.download_ingoreStrings;
import static picSnatcher.mediaSnatcher.OptionKeys.download_keepDownloadLog;
import static picSnatcher.mediaSnatcher.OptionKeys.download_ppAsDir;
import static picSnatcher.mediaSnatcher.OptionKeys.download_prependPage;
import static picSnatcher.mediaSnatcher.OptionKeys.download_prettyFilenames;
import static picSnatcher.mediaSnatcher.OptionKeys.download_removeThumbs;
import static picSnatcher.mediaSnatcher.OptionKeys.download_sameFolder;
import static picSnatcher.mediaSnatcher.OptionKeys.download_sameFolder_custom;
import static picSnatcher.mediaSnatcher.OptionKeys.download_sameSite;
import static picSnatcher.mediaSnatcher.OptionKeys.download_saveExternalUrlList;
import static picSnatcher.mediaSnatcher.OptionKeys.download_saveLinkList;
import static picSnatcher.mediaSnatcher.OptionKeys.download_separateByDomain;
import static picSnatcher.mediaSnatcher.OptionKeys.download_siteFirst;
import static picSnatcher.mediaSnatcher.OptionKeys.download_usePageDirectory;
import static picSnatcher.mediaSnatcher.OptionKeys.download_usePageDomain;
import static picSnatcher.mediaSnatcher.OptionKeys.download_wantStrings;
import static picSnatcher.mediaSnatcher.OptionKeys.saveVersion;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_alwaysCheckMIME;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_getArchives;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_getAudio;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_getDocuments;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_getMovies;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_getOther;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_getPictures;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_ignore;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_ignoreTitle;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_maxLogs;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_minImgHeight;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_minImgWidth;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_readDeep;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_readDepth;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_repeat;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_sameSite;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_saveFile;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_saveFolder;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_want;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_wantTitle;
import static picSnatcher.mediaSnatcher.OptionKeys.snatcher_wantedTitles;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import picSnatcher.mediaSnatcher.extension.OptionPanel;
import simple.gui.AboutWindow;
import simple.gui.SDialog;
import simple.gui.factory.SwingFactory;
import simple.html.MimeTypes;
import simple.io.FileUtil;
import simple.net.Uri;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;
import simple.util.logging.LogLevel;

/**
 * <br>Created: Sep 7, 2008
 * @author Kenneth Pierce
 */
public final class Options {
	private static final Log log = LogFactory.getLogFor(Options.class);
	static final AboutWindow HELP_WINDOW = new AboutWindow((java.awt.Frame)null, "Help", false);
	static final String FALSE = "false", TRUE = "true";
	private static final EnumProperties DEFAULTS = new EnumProperties();
	private final LinkedList<OptionPanel> panels= new LinkedList<>();
	static {
		final JTextArea help = new JTextArea("Top Text Box: Enter the URL here. If there is an obvious number sequence you can replace the number with ^gal^ for use with the next section.\n" +
				"Gal Syntax: The number of leading zeros. 0 is one; 00 is two, 000 is three, and so on. You may use any character here.\n" +
				"Gal Start: The starting number.\n" +
				"Gal End: The ending number.\n" +
				"Gal Increment: The counting sequence. For example: A start of 0 and a end of 10 with a increment of 5 will result in the following sequence; 0, 5, 10.\n" +
				"If the start is less than the end then it will count backwards.\n" +
				"----------------------------------------------------\n" +
				"Est TTC: Estimated time to completion. This applies to the current task only.\n" +
				"It will appear to be erratic and random to start. Because the total number of pages, total size of all the files, and the speed at which they will download are unknown" +
		"I implemented it as a running average multiplied by the current known max minus the pages/files already read.");
		help.setWrapStyleWord(true);
		help.setLineWrap(true);
		help.setEditable(false);
		HELP_WINDOW.addCenter(new JScrollPane(help));
		HELP_WINDOW.setSize(325,300);
		HELP_WINDOW.center();
		//NOTE: option defaults
		//Edit to add option!
		DEFAULTS.setProperty(download_removeThumbs, TRUE);
		DEFAULTS.setProperty(snatcher_getPictures, TRUE);
		DEFAULTS.setProperty(snatcher_getMovies, FALSE);
		DEFAULTS.setProperty(snatcher_getAudio, FALSE);
		DEFAULTS.setProperty(snatcher_getArchives, FALSE);
		DEFAULTS.setProperty(snatcher_getDocuments, FALSE);
		DEFAULTS.setProperty(snatcher_getOther, FALSE);
		DEFAULTS.setProperty(download_prettyFilenames, TRUE);
		DEFAULTS.setProperty(snatcher_readDeep, FALSE);
		DEFAULTS.setProperty(snatcher_readDepth, "1");
		DEFAULTS.setProperty(snatcher_sameSite, TRUE);
		DEFAULTS.setProperty(download_sameFolder, FALSE);
		DEFAULTS.setProperty(download_sameFolder_custom,"");
		DEFAULTS.setProperty(download_alternateNumbering, FALSE);
		DEFAULTS.setProperty(download_prependPage, FALSE);
		DEFAULTS.setProperty(download_ppAsDir, FALSE);
		DEFAULTS.setProperty(download_keepDownloadLog, FALSE);
		DEFAULTS.setProperty(download_dateSubfolder, FALSE);
		DEFAULTS.setProperty(download_siteFirst, TRUE);
		DEFAULTS.setProperty(download_usePageDirectory, FALSE);
		DEFAULTS.setProperty(download_ingoreStrings, "");
		DEFAULTS.setProperty(download_wantStrings, "");
		DEFAULTS.setProperty(snatcher_ignore, "");
		DEFAULTS.setProperty(snatcher_want, "");
		DEFAULTS.setProperty(snatcher_wantedTitles, "");
		DEFAULTS.setProperty(snatcher_maxLogs, "5");
		DEFAULTS.setProperty(snatcher_saveFolder, "");
		DEFAULTS.setProperty(snatcher_saveFile, "");
		DEFAULTS.setProperty(snatcher_alwaysCheckMIME, TRUE);
		DEFAULTS.setProperty(snatcher_wantTitle, "");
		DEFAULTS.setProperty(snatcher_ignoreTitle, "");
		DEFAULTS.setProperty(snatcher_repeat,FALSE);
		DEFAULTS.setProperty(snatcher_minImgWidth,"0");
		DEFAULTS.setProperty(snatcher_minImgHeight,"0");
		DEFAULTS.setProperty(saveVersion, "2.0");
		DEFAULTS.setProperty(download_saveExternalUrlList,FALSE);
		DEFAULTS.setProperty(download_saveLinkList,FALSE);
		DEFAULTS.setProperty(download_sameSite,FALSE);
		DEFAULTS.setProperty(download_separateByDomain,FALSE);
		DEFAULTS.setProperty(download_usePageDomain,FALSE);
	}
	private String[] wantedt, ignoredt, ignoredl, wantedl, wantedd, ignoredd;

	private final EnumProperties options = new EnumProperties(DEFAULTS);

	//adv options
	private final JCheckBox
		keepHeaderLog = new JCheckBox("Keep a log of the HTTP headers."),
		alwaysCheckMIME = new JCheckBox("Always check server for MIME type.");
	private final SDialog optionsFrame, advOptions;
	private final Main parent;
	private final JTabbedPane jtPane;
	private final List<ActionListener> uiControllers= new LinkedList<>();
	private final OptionPanel types, download, crawl;
	private final void uiStateUpdated(){
		for(ActionListener al: uiControllers){
			al.actionPerformed(null);
		}
	}
	//	private final Session session;
	public Options(final Main frame, final Session ses) {
		ActionListener uiController;
		jtPane = new JTabbedPane();
		options.putAll(DEFAULTS);
		//FLOW: create UI
		parent = frame;
		optionsFrame = new SDialog(parent.frame,"Options", true);
		//		session = ses;
		//----------------TYPES TAB
		types= new OptionPanel("Types",Options.class.getResourceAsStream("option-panels/Types.xml"));
		addPage(types);
		//-----------DOWNLOAD TAB
		download= new OptionPanel("Download",Options.class.getResourceAsStream("option-panels/Download.xml"));
		addPage(download);
		uiController= new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				JCheckBox
					sameFolder= download.getCheckBox(download_sameFolder.toString()),
					prependPage= download.getCheckBox(download_prependPage.toString()),
					ppPageAsDir= download.getCheckBox(download_ppAsDir.toString()),
					domainSubFolder= download.getCheckBox(download_separateByDomain.toString()),
					dateSubfolder= download.getCheckBox(OptionKeys.download_dateSubfolder.toString()),
					siteFirst= download.getCheckBox(OptionKeys.download_siteFirst.toString());
				siteFirst.setEnabled(dateSubfolder.isSelected());
				if(sameFolder.isSelected()){
					prependPage.setSelected(false);
					prependPage.setEnabled(false);
					domainSubFolder.setEnabled(true);
				}else{
					prependPage.setEnabled(true);
					domainSubFolder.setEnabled(false);
				}
				if(prependPage.isSelected()){
					ppPageAsDir.setEnabled(true);
					sameFolder.setEnabled(false);
				}else{
					sameFolder.setEnabled(true);
				}
			}
		};
		download.getCheckBox(OptionKeys.download_sameFolder.toString()).addActionListener(uiController);
		download.getCheckBox(OptionKeys.download_dateSubfolder.toString()).addActionListener(uiController);
		download.getCheckBox(OptionKeys.download_prependPage.toString()).addActionListener(uiController);
		download.getCheckBox(OptionKeys.download_ppAsDir.toString()).addActionListener(uiController);
		download.getCheckBox(OptionKeys.download_dateSubfolder.toString()).addActionListener(uiController);
		download.getCheckBox(OptionKeys.download_siteFirst.toString()).addActionListener(uiController);
		uiControllers.add(uiController);

//-----------CRAWL TAB
		crawl= new OptionPanel("Crawl",Options.class.getResourceAsStream("option-panels/Crawl.xml"));
		addPage(crawl);
//-----------END TABS
		optionsFrame.addCenter(jtPane);
		JButton bTmp = SwingFactory.makeJButton("Okay", "");
		optionsFrame.addBottom(bTmp);
		optionsFrame.pack();
		bTmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {//FLOW: UI to Data

				for(String key: types.getKeys()){
					options.setProperty(key, types.getItemValue(key));
				}


				for(String key: download.getKeys()){
					options.setProperty(key, download.getItemValue(key));
				}

				for(String key: crawl.getKeys()){
					options.setProperty(key, crawl.getItemValue(key));
				}
//				options.setProperty(snatcher_want.name(), crawl.getItemValue(snatcher_want.name()));
				updateWanted();
				for(OptionPanel panel: panels){
					panel.fireClosed();
				}
				optionsFrame.setVisible(false);
			}
		});
		/* ************************
		 * ** ADV OPTIONS DIALOG **
		 * ************************/
		advOptions = new SDialog(parent.frame,"Advanced Options", true);
		alwaysCheckMIME.setToolTipText("Will bypass the normal extention checking and ask the server every time. This will slow link retrieval.");
		advOptions.addCenter(alwaysCheckMIME);
		keepHeaderLog.setToolTipText("Will keep a log of the HTTP headers sent to and recieved from the servers. This setting is NOT persistant does not affect the items found.");
		advOptions.addCenter(keepHeaderLog);

		bTmp = SwingFactory.makeJButton("Okay", "");
		bTmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {//FLOW: UI to Data
				//Edit to add option!
				options.setProperty(snatcher_alwaysCheckMIME, getTF(alwaysCheckMIME));

				//TODO: see below
				//header log
				//page title
				//options.setProperty(download_wantStrings, wantDlText.getText());
				advOptions.setVisible(false);
			}
		});
		advOptions.addBottom(bTmp);
		advOptions.pack();
		advOptions.setSize(305, advOptions.getHeight());
		for(PageParser pp: PageParser.getPageParsers()){
			if(pp.hasOptions()){
				this.addPage(pp.getOptions());
			}
		}
	}
	protected void updateWanted() {
		if (!options.getProperty(snatcher_wantTitle, "").isEmpty()) {
			//pageTitle.setText(pageTitle.getText().toLowerCase());
			wantedt = options.getProperty(snatcher_wantTitle, "").split(";");
		}
		if (!options.getProperty("snatcher.ingoreTitle", "").isEmpty()) {
			//pageTitle.setText(pageTitle.getText().toLowerCase());
			ignoredt = options.getProperty(snatcher_ignoreTitle, "").split(";");
		}
		if (!options.getProperty(snatcher_ignore, "").isEmpty()) {
			//ignore.setText(ignore.getText().toLowerCase().trim());
			ignoredl = options.getProperty(snatcher_ignore, "").split(";");
			log.debug("ignore url",ignoredl);
		}
		if (!options.getProperty(snatcher_want, "").isEmpty()) {
			//ignore.setText(ignore.getText().toLowerCase().trim());
			wantedl = options.getProperty(snatcher_want, "").split(";");
			log.debug("wanted url",wantedl);
		}
		if (!options.getProperty(download_ingoreStrings, "").isEmpty()) {
			//ignore.setText(ignore.getText().toLowerCase().trim());
			ignoredd = options.getProperty(download_ingoreStrings, "").split(";");
		}
		if (!options.getProperty(download_wantStrings, "").isEmpty()) {
			//ignore.setText(ignore.getText().toLowerCase().trim());
			wantedd = options.getProperty(download_wantStrings, "").split(";");
		}
	}
	private void setupOptions() {//FLOW: data to UI

		for(String key: types.getKeys()){
			types.setItemValue(key, options.getProperty(key));
		}

		for(String key: download.getKeys()){
			download.setItemValue(key, options.getProperty(key));
		}

		for(String key: crawl.getKeys()){
			crawl.setItemValue(key, options.getProperty(key));
		}

		uiStateUpdated();
	}
	protected void showOptions() {
		setupOptions();
		for(OptionPanel panel: panels){
			panel.fireOpened();
		}
		optionsFrame.center();
		optionsFrame.setVisible(true);
	}
	private void setupAdvOptions() {
		alwaysCheckMIME.setSelected(getTF(options.getProperty(snatcher_alwaysCheckMIME)));
	}
	protected void showAdvOptions() {
		setupAdvOptions();
		advOptions.center();
		advOptions.setVisible(true);
	}
	public void addPage(OptionPanel panel){
		panels.add(panel);
		jtPane.add(panel.getTitle(), new JScrollPane(panel.getPanel()));
	}
	protected void savePref() {
		savePref("pref");
	}
	protected void loadPref() {
		loadPref("pref");
	}
	protected void savePref(final String file) {
		final File pref = new File(file+".conf");
		try {
			final PrintWriter out = new PrintWriter(pref);
			options.store(out, "");
			FileUtil.close(out);
		} catch (final Exception e) {
			log.error("savePreferences", e);
		}
	}
	protected void loadPref(final String file) {
		final File pref = new File(file+".conf");
		if (!pref.exists()) {
			if (!"pref".equals(file)) {
				loadPref();
			}
			return;
		}
		try(FileReader in = new FileReader(pref)) {
			EnumProperties tmp=new EnumProperties();
			tmp.load(in);
			if(tmp.getProperty(saveVersion,null)==null){
				final EnumProperties nprop = new EnumProperties();
				//Update old options to new
				nprop.setProperty(download_removeThumbs, getTF(TRUE.equals(tmp.getProperty("download.removeThumbs"))));
				nprop.setProperty(snatcher_getPictures, getTF(TRUE.equals(tmp.getProperty("snatcher.getPictures"))));
				nprop.setProperty(snatcher_getMovies, getTF(TRUE.equals(tmp.getProperty("snatcher.getMovies"))));
				nprop.setProperty(snatcher_getAudio, getTF(TRUE.equals(tmp.getProperty("snatcher.getAudio"))));
				nprop.setProperty(snatcher_getArchives, getTF(TRUE.equals(tmp.getProperty("snatcher.getArchives"))));
				nprop.setProperty(snatcher_getDocuments, getTF(TRUE.equals(tmp.getProperty("snatcher.getDocuments"))));
				nprop.setProperty(snatcher_getOther, getTF(TRUE.equals(tmp.getProperty("snatcher.getOther"))));
				nprop.setProperty(download_prettyFilenames, getTF(TRUE.equals(tmp.getProperty("download.prettyFilenames"))));
				nprop.setProperty(snatcher_readDeep, getTF(TRUE.equals(tmp.getProperty("snatcher.readDeep"))));
				nprop.setProperty(snatcher_sameSite, getTF(TRUE.equals(tmp.getProperty("snatcher.sameSite"))));
				nprop.setProperty(download_sameFolder, getTF(TRUE.equals(tmp.getProperty("download.sameFolder"))));
				nprop.setProperty(download_alternateNumbering, getTF(TRUE.equals(tmp.getProperty("download.alternateNumbering"))));
				nprop.setProperty(download_keepDownloadLog, getTF(TRUE.equals(tmp.getProperty("download.keepDownloadLog"))));
				nprop.setProperty(download_prependPage, getTF(TRUE.equals(tmp.getProperty("download.prependPage"))));
				nprop.setProperty(download_ppAsDir, getTF(TRUE.equals(tmp.getProperty("download.ppAsDir"))));
				nprop.setProperty(download_dateSubfolder, getTF(TRUE.equals(tmp.getProperty("download.dateSubfolder"))));
				nprop.setProperty(download_siteFirst, getTF(TRUE.equals(tmp.getProperty("download.siteFirst"))));
				nprop.setProperty(download_usePageDirectory, getTF(TRUE.equals(tmp.getProperty("download.usePageDirectory"))));
				nprop.setProperty(snatcher_repeat, getTF(TRUE.equals(tmp.getProperty("snatcher.repeat"))));

				nprop.setProperty(snatcher_readDepth, tmp.getProperty("snatcher.readDepth"));
				nprop.setProperty(download_ingoreStrings, tmp.getProperty("download.ignore"));
				nprop.setProperty(download_wantStrings, tmp.getProperty("download.want"));
				nprop.setProperty(snatcher_ignore, tmp.getProperty("snatcher.ignore"));
				nprop.setProperty(snatcher_want, tmp.getProperty("snatcher.want"));
				nprop.setProperty(snatcher_wantedTitles, tmp.getProperty("snatcher.wantedTitles"));
				nprop.setProperty(snatcher_maxLogs, tmp.getProperty("snatcher.maxLogs"));
				nprop.setProperty(snatcher_saveFolder, tmp.getProperty("snatcher.saveFolder"));
				nprop.setProperty(snatcher_saveFile, tmp.getProperty("snatcher.saveFile"));
				nprop.setProperty(snatcher_wantTitle, tmp.getProperty("snatcher.wantTitle"));
				nprop.setProperty(snatcher_ignoreTitle, tmp.getProperty("snatcher.ignoreTitle"));
				nprop.setProperty(snatcher_minImgWidth, tmp.getProperty("snatcher.minImgWidth"));
				nprop.setProperty(snatcher_minImgHeight, tmp.getProperty("snatcher.minImgHeight"));
				nprop.setProperty(saveVersion, "2.0");
				tmp.clear();
				tmp.putAll(nprop);
			}
			options.putAll(tmp);
		} catch (final Exception e) {
			log.error("loadPreferences", e);
		} finally {
			updateWanted();
		}
	}
	private static final String[] thumbs = new String[] {"thumbnail","sample","-thumb","thumb_","thumb-","_thumb","/thumb","/mini","_tn","tn_", "preview", "_small", "/small", "small_","_icon", "ico_","icon_","/icon"};
	public boolean isThumb(String link) {
		if(getTF(options.getProperty(download_removeThumbs)) == false){
			return false;
		}
		link = link.toLowerCase();
		for (int i = 0;i<thumbs.length;i++) {
			if (link.indexOf(thumbs[i])>=0) {
				log.debug("isThumb", "Matched \""+thumbs[i]+"\" in "+link);
				return true;
			}
		}
		return false;
	}
	/**
	 * @param link
	 * @return true if it is in the wanted list or if the list is empty
	 */
	private boolean isInWantedPageList(final String link) {
		if (wantedl != null && wantedl.length != 0) {
			for (int i = 0; i < wantedl.length; i++) {
				if (link.contains(wantedl[i])) {
					log.debug("matched wanted page item "+wantedl[i]);
					return true;
				}
			}
			//if (wantl.length > 0) return false;
		} else{
			return true;
		}
		log.debug("not in wanted page list. "+link);
		return false;
	}
	/**
	 * @param link
	 * @return true if the link is in the ignored list. false otherwise or if the list is empty
	 */
	private boolean isInIgnoredPageList(final String link) {
		if (ignoredl == null || ignoredl.length==0){
			return false;
		}
		for (int i = 0; i < ignoredl.length; i++) {
			if (link.indexOf(ignoredl[i])>=0) {
				log.information("Matched ignored page item \""+ignoredl[i]+"\" in "+link);
				return true;
			}
		}
		log.debug("not in ignored page list.",link);
		return false;
	}
	/**
	 * @param link
	 * @return true if it is in the wanted list or if the list is empty
	 */
	private boolean isInWantedDlList(final String link) {
		if (wantedd != null && wantedd.length != 0) {
			for (int i = 0; i < wantedd.length; i++) {
				if (link.indexOf(wantedd[i])>=0) {
					log.debug("matched wanted DL item ",wantedd[i]);
					return true;
				}
			}
			//if (wantl.length > 0) return false;
		} else
			return true;
		log.debug("not in wanted download list. ",link);
		return false;
	}
	/**
	 * @param link
	 * @return true if the link is in the ignored list. false otherwise or if the list is empty
	 */
	private boolean isInIgnoredDlList(final String link) {
		if (ignoredd == null || ignoredd.length==0) return false;
		for (int i = 0; i < ignoredd.length; i++) {
			if (link.indexOf(ignoredd[i])>=0) {
				log.debug("in ignored list", "Matched ignore DL item \""+ignoredd[i]+"\" in "+link);
				return true;
			}
		}
		log.debug("not in ignored download list.",link);
		return false;
	}
	/**To be used by Page Parsers to determine if a page is ignored.
	 * @param link
	 * @param ref
	 * @return
	 */
	public boolean isIgnoredPage(final Uri link, final Uri ref) {
		if (isWantedPageListCheck(link.toString())) {
			if (!sameSiteCheck(link, ref)){
				return true;
			}
		} else {
			return true;//failed list checks(ignored)
		}
		//it's not ignored; check the mime
		if(log.getPrint(LogLevel.DEBUG)){
			String s= link.getFile();
			log.debug(s.substring(s.lastIndexOf('.') + 1));
		}
		final String mime = MimeTypes.getMime(link).get(0);
		if (mime.isEmpty() || "text/html".equals(mime)){
			return false;
		}
		log.debug("page ignored","mime: "+mime);
		return true;
	}
	/**To be used by the Session to determine if a download link is wanted.
	 * @param link
	 * @param ref
	 * @return
	 */
	public boolean isDownloadIgnored(final Uri link, final Uri ref) {
		if (isWantedListCheck(link.toString())) {
			if (!sameSiteCheck(link, ref))
				return true;
		} else {
			log.debug("failed wanted list check", link);
			return true;//failed list checks(ignored)
		}
		// XXX: Checking mime
		String mime = MimeTypes.getMime(link).get(0);
		if (!mime.equals("")) {
			if (getOption(snatcher_alwaysCheckMIME)==TRUE) {
				try {
					mime = MimeTypes.getMimeType(link, ref.getOriginalUri());
				} catch (final Exception e) {
					log.error("isIgnored(..,..)", e);
					return false;
				}
			}
		}
		if (!isWantedMIME(mime)) {
			log.debug("isIgnored","unwanted mime: "+mime+" for "+link);
			return true;
		}
		return false;
	}
	/**Used by {@link PageReader#handle(Uri, String, int)}
	 * @param title
	 * @return true if wanted.
	 */
	public boolean isWantedTitleCheck(String title) {
		if (ignoredt == null || wantedt == null) {
			updateWanted();
		}
		if (title==null) {
			log.warning("Null title");
			if (wantedt!=null && wantedt.length!=0)
				return false;
			else
				return true;
		}
		title = title.toLowerCase();
		if (isInWantedTitleList(title)) {
			if (isInIgnoredTitleList(title)) return false;
		} else
			return false;
		return true;
	}
	private boolean isInIgnoredTitleList(final String title) {
		if (ignoredt == null || ignoredt.length==0) return false;
		for (int i = 0; i < ignoredt.length; i++) {
			if (title.indexOf(ignoredt[i])>=0) {
				log.debug("in ignored list", "Matched ignore title item \""+ignoredt[i]+"\" in "+title);
				return true;
			}
		}
		return false;
	}
	private boolean isInWantedTitleList(final String title) {
		if (wantedt == null || wantedt.length==0) return true;
		for (int i = 0; i < wantedt.length; i++) {
			if (title.indexOf(wantedt[i])>=0) {
				log.debug("in wanted list", "Matched wanted title item \""+wantedt[i]+"\" in "+title);
				return true;
			}
		}
		return false;
	}
	/**Checks the thumbnail, wanted, and ignored lists
	 * For Download items!
	 * @param link
	 * @return true if it is wanted, false otherwise.
	 */
	private boolean isWantedListCheck(String link) {
		if (ignoredd == null || wantedd == null) {
			updateWanted();
		}
		link = link.toLowerCase();
		if (isThumb(link)) return false;
		if (isInWantedDlList(link)) {
			if (isInIgnoredDlList(link)){
				return false;
			}
		} else {
			return false;
		}
		return true;
	}
	/**Checks the thumbnail, wanted, and ignored lists
	 * @param link
	 * @return true if it is wanted, false otherwise.
	 */
	private boolean isWantedPageListCheck(String link) {
		if (ignoredl == null || wantedl == null) {
			updateWanted();
		}
		link = link.toLowerCase();
		if (isThumb(link)){
			return false;
		}
		if(wantedl == null || wantedl.length == 0){
			if(ignoredl == null || ignoredl.length == 0){
				return true;
			}
			return !isInIgnoredPageList(link);
		}else if (isInWantedPageList(link)) {
			if(ignoredl == null || ignoredl.length == 0){
				return true;
			}
			if (isInIgnoredPageList(link)){
				return false;
			}
		} else{
			return false;
		}
		return true;
	}
	/**Checks to see if the domains match if sameSite() is true.
	 * @param link The page link
	 * @param ref The referral page link.
	 * @return true if the domains match, referral==null, or sameSite()==false
	 */
	private boolean sameSiteCheck(final Uri link, final Uri ref) {
		if (getTF(getOption(download_sameSite)) && ref != null) {
			if (!link.getDomain().equalsIgnoreCase(ref.getDomain())) {
				log.information("same site check","mismatch domains: link["+link+"] -- referrer["+ref.getDomain()+"]");
				return false;
			}
		}
		return true;
	}
	public boolean isWantedMIME(final String mime) {
		return
			(getPics()				&& MimeTypes.isMimeImage(mime)) ||
			(getMovie()			&& MimeTypes.isMimeVideo(mime)) ||
			(getAudio()			&& MimeTypes.isMimeAudio(mime)) ||
			(getArchive()			&& MimeTypes.isMimeArchive(mime)) ||
			(getDocument()	&& MimeTypes.isMimeDocument(mime)) ||
			(getOther()			&& MimeTypes.isMimeOther(mime));
	}
	/**
	 * @return the deepScan
	 */
	protected final boolean deepScan() {
		return getTF(options.getProperty(snatcher_readDeep, FALSE));
	}
	/**
	 * @return the getAudi
	 */
	protected final boolean getAudio() {
		return getTF(options.getProperty(snatcher_getAudio, FALSE));
	}
	/**
	 * @return the getMovs
	 */
	protected final boolean getMovie() {
		return getTF(options.getProperty(snatcher_getMovies, FALSE));
	}
	/**
	 * @return the getOther
	 */
	protected final boolean getOther() {
		return getTF(options.getProperty(snatcher_getOther, FALSE));
	}
	/**
	 * @return the getArchive
	 */
	protected final boolean getArchive() {
		return getTF(options.getProperty(snatcher_getArchives, FALSE));
	}
	/**
	 * @return the getDocument
	 */
	protected final boolean getDocument() {
		return getTF(options.getProperty(snatcher_getDocuments, FALSE));
	}
	/**
	 * @return the getPics
	 */
	protected final boolean getPics() {
		return getTF(options.getProperty(snatcher_getPictures, TRUE));
	}
	/**
	 * @return the keepHeaderLog
	 */
	protected final boolean keepHeaderLog() {
		return keepHeaderLog.isSelected();
	}
	/**
	 * @return the readDepth
	 */
	protected final int getReadDepth() {
		return Integer.parseInt(options.getProperty(snatcher_readDepth, "1"));
	}
	protected final String getIgnoreList() {
		return options.getProperty(snatcher_ignore, "");
	}
	protected final String getTitleList() {
		return options.getProperty(snatcher_wantedTitles, "");
	}
	protected final void setOption(final OptionKeys option, final String value) {
		//log.information(option, value);
		options.setProperty(option, value);
	}
	public final String getOption(final OptionKeys option) {
		return options.getProperty(option);
	}
	public final boolean getBoolean(OptionKeys option){
		return getTF(options.getProperty(option));
	}
	protected final Enumeration<Object> getOptions() {
		return options.keys();
	}
	public void dumpOptions() {
		log.information(options);
	}
// ----------------------------------------------
// --------------UTILITY------------------------
// ----------------------------------------------

	private static final String getTF(JCheckBox box){
		return getTF(box.isSelected());
	}
	public static final String getTF(final boolean test) {
		if (test)
			return TRUE;
		else
			return FALSE;
	}
	public static final boolean getTF(final String test) {
		if(test == TRUE){
			return true;
		}
		if(test == FALSE){
			return false;
		}
		if (TRUE.equalsIgnoreCase(test))
			return true;
		else
			return false;
	}
}

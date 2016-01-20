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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import picSnatcher.mediaSnatcher.extension.OptionPanel;
import simple.gui.AboutWindow;
import simple.gui.SDialog;
import simple.gui.component.JNumberField;
import simple.gui.factory.SJPanel;
import simple.gui.factory.SwingFactory;
import simple.html.MimeTypes;
import simple.io.FileUtil;
import simple.net.Uri;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

/**
 * <br>Created: Sep 7, 2008
 * @author Kenneth Pierce
 */
public final class Options {
	private static final Log log = LogFactory.getLogFor(Options.class);
	static final AboutWindow HELP_WINDOW = new AboutWindow(null, "Help", false);
	static final String FALSE = "false", TRUE = "true";
	private static final EnumProperties DEFAULTS = new EnumProperties();
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
	private final JCheckBox
		remThumbs = new JCheckBox("Attempt removal of thumbnails(may skip wanted files)", true),
		getPics = new JCheckBox("Download Pictures", true),
		getMovies = new JCheckBox("Download Movies"),
		getAudio = new JCheckBox("Download Audio"),
		getOther = new JCheckBox("Download Other"),
		getArchive = new JCheckBox("Download Archives"),
		getDocument = new JCheckBox("Download Documents"),
		prettyFilenames = new JCheckBox("Pretty Filenames", true),
		readDeep = new JCheckBox("Follow links to other pages."),
		sameSite = new JCheckBox("From original site only.", true),
		sameFolder = new JCheckBox("Output all to same folder"),
		alternateNumbering = new JCheckBox("Numbered File Names"),
		keepDlLog = new JCheckBox("Keep a log of downloaded files.(Useful for when you rename files from a daily post)"),
		prependPageName = new JCheckBox("Prepend Page Name. (Useful for image boards)"),
		prependPageNameAsFolder = new JCheckBox("As folder."),
		dateSubfolder = new JCheckBox("Separate runs by date."),
		siteFirst = new JCheckBox("Domain root folder.", true),
		usePageDirectory = new JCheckBox("Use the page's directory as the save directory."),
		infiniteSnatch=new JCheckBox("Loop"),
		saveExternalLinkList=new JCheckBox("Save external link list."),
		downloadSameSite=new JCheckBox("Same site"),
		separateByDomain= new JCheckBox("Domain subfolders"),
		usePageDomain= new JCheckBox("Use page's domain");
	private final JTextField ignore = new JTextField(),
		want = new JTextField(),
		ignoreDlText = new JTextField(),
		wantDlText = new JTextField(),
		wantTitle = new JTextField(),
		ignoreTitle= new JTextField();
	private final JNumberField
		minImgWidth=new JNumberField(),
		minImgHeight=new JNumberField();
	private final JNumberField readDepth = new JNumberField("1",5);
	//adv options
	private final JCheckBox
		keepHeaderLog = new JCheckBox("Keep a log of the HTTP headers."),
		alwaysCheckMIME = new JCheckBox("Always check server for MIME type.");
	private final JTextField pageTitle = new JTextField();
	private final SDialog optionsFrame, advOptions;
	private final Main parent;
	private final JTabbedPane jtPane;
	private final List<ActionListener> uiControllers= new LinkedList<>();
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
		//		session = ses;
		//----------------TYPES TAB
		JPanel JPtmp,page=SJPanel.makeBoxLayoutPanelY();
		optionsFrame = new SDialog(parent.frame,"Options", true);
		remThumbs.setToolTipText("Will attempt to remove thumbnails based on a set of common patterns.");
		page.add(createLine(remThumbs,0));

		getPics.setToolTipText("Will download pictures.");
		page.add(createLine(getPics,0));

		getMovies.setToolTipText("Will download movies. Includes Flash.");
		page.add(createLine(getMovies,0));

		getAudio.setToolTipText("Will download audio files.");
		page.add(createLine(getAudio,0));

		getArchive.setToolTipText("Will download compressed files.(zip, tar, rar, gzip, etc.)");
		page.add(createLine(getArchive,0));

		getDocument.setToolTipText("Will download document files.(doc, ppt, wri, ods, ect.)");
		page.add(createLine(getDocument,0));

		getOther.setToolTipText("Will download anything not defined as a picture, audio, or movie file found on pages.");
		page.add(createLine(getOther,0));
		jtPane.addTab("Types",new JScrollPane(page));
//-----------DOWNLOAD TAB
		uiController= new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				prependPageName.setEnabled(!sameFolder.isSelected());
				prependPageNameAsFolder.setEnabled(prependPageName.isSelected());
				siteFirst.setEnabled(dateSubfolder.isSelected() && !sameFolder.isSelected());
				separateByDomain.setEnabled(sameFolder.isSelected());
			}
		};
		uiControllers.add(uiController);
		page=SJPanel.makeBoxLayoutPanelY();
		keepDlLog.setToolTipText("Will keep a log of previously dowloaded files. Useful when you've deleted unwanted files from a site that you plan to scan again.");
		page.add(createLine(keepDlLog,0));

		prettyFilenames.setToolTipText("Will make filenames more readable by removing some HTML escape sequences. (non-displayable characters and those forbidden in filenames will be left escaped)");
		page.add(createLine(prettyFilenames,0));

		sameFolder.setToolTipText("Will save all files found to one folder. Works best when combined with alternate numbering.");
		page.add(createLine(sameFolder,0));
		sameFolder.addActionListener(uiController);

		separateByDomain.setToolTipText("Separates the files by domain.");
		page.add(createLine(separateByDomain,1));

		dateSubfolder.setToolTipText("Will create a subfolder with today's date and use that as the root folder under the site.");
		page.add(createLine(dateSubfolder,0));
		dateSubfolder.addActionListener(uiController);

		siteFirst.setToolTipText("Will use the domain as the root and the date will be a subfolder.");
		page.add(createLine(siteFirst,1));

		usePageDomain.setToolTipText("Uses the page's domain instead of the file's.");
		page.add(createLine(usePageDomain,0));

		usePageDirectory.setToolTipText("Will use the page's directory to save content found on that page.(combinable with other options)");
		page.add(createLine(usePageDirectory,0));

		prependPageName.setToolTipText("Will prepend the filename of the page that the file was found on minus the extention.");
		page.add(createLine(prependPageName,0));
		prependPageName.addActionListener(uiController);

		prependPageNameAsFolder.setToolTipText("If unchecked the pagename will be added to the filename (page_file). If checked it will create a subfolder (path\\page\\file).");
		page.add(createLine(prependPageNameAsFolder,1));

		alternateNumbering.setToolTipText("Uses a number scheme to name files. ####_### (pageNum_fileNum). Useful when sites give files non-sense names that throw off order.");
		page.add(createLine(alternateNumbering,0));

		downloadSameSite.setToolTipText("Will only download the file if the domains match. (e.g. a file found on a page from www.example.com and that is hosted on static.example.com will be downloaded)");
		page.add(createLine(downloadSameSite,1));
		jtPane.addTab("Download",new JScrollPane(page));

//-----------CRAWL TAB
		page=SJPanel.makeBoxLayoutPanelY();
		infiniteSnatch.setToolTipText("Will start over when done until Stop is pressed or this checkbox is unchecked.");
		page.add(createLine(infiniteSnatch,0));

		readDeep.setToolTipText("Will read links to other pages.");
		page.add(createLine(readDeep,0));

		JPtmp = SJPanel.makeBoxLayoutPanelX();
		JPtmp.add(new JLabel("Follow links "));
		readDepth.setPreferredSize(new Dimension(30, readDepth.getHeight()));
		JPtmp.add(readDepth);
		JPtmp.add(new JLabel(" pages deep."));
		JPtmp.add(Box.createGlue());
		JPtmp.setToolTipText("Will follow links N pages deep.");
		page.add(JPtmp);

		sameSite.setToolTipText("Will only read pages that are on the same website as the original page.");
		page.add(createLine(sameSite,0));

		JPtmp = SJPanel.makeBoxLayoutPanelX();
		JPtmp.add(new JLabel("Minimum image width "));
		JPtmp.add(minImgWidth);
		JPtmp.setToolTipText("This is a best effort algorithm. Depends on the page to tell me what the width is and up to the parser to honor it.");
		page.add(JPtmp);

		JPtmp = SJPanel.makeBoxLayoutPanelX();
		JPtmp.add(new JLabel("Minimum image height "));
		JPtmp.add(minImgHeight);
		JPtmp.setToolTipText("This is a best effort algorithm. Depends on the page to tell me what the height is and up to the parser to honor it.");
		page.add(JPtmp);

		ignore.setToolTipText("Will skip pages with one of the following in it's URL. (leave blank to disable)");
		page.add(SJPanel.makeLabeledPanel(ignore, "Do not read URLs containing(separate with ;):"));

		want.setToolTipText("Reads only pages with these in the URL. If the page matches both a skip rule and a read rule, the skip rule wins.");
		page.add(SJPanel.makeLabeledPanel(want, "Read URLs containing(separate with ;):"));

		ignoreDlText.setToolTipText("Will ignore download items with one of the following in it's URL. (leave blank to disable)");
		page.add(SJPanel.makeLabeledPanel(ignoreDlText, "Do not download URLs containing(separate with ;):"));

		wantDlText.setToolTipText("Downloads items with these in the URL. If the item matches both an ignore item and a want item, the ignore item wins.");
		page.add(SJPanel.makeLabeledPanel(wantDlText, "Download URLs containing(separate with ;):"));

		wantTitle.setToolTipText("Will parse a page if the title contains any of these. If the title matches both an ignore item and a want item, the ignore item wins.");
		page.add(SJPanel.makeLabeledPanel(wantTitle, "Parse page if title contains(separate with ;):"));

		ignoreTitle.setToolTipText("Will skip the page if the title contains any of these. If the title matches both an ignore item and a want item, the ignore item wins.");
		page.add(SJPanel.makeLabeledPanel(ignoreTitle, "Ignore page if title contains(separate with ;):"));
		jtPane.addTab("Crawl",new JScrollPane(page));
//-----------MISC TAB
		page=SJPanel.makeBoxLayoutPanelY();
		saveExternalLinkList.setToolTipText("Will save a list of external links found on the pages.");
		page.add(SJPanel.makeLabeledPanel(saveExternalLinkList, "Save list of external links:"));
		jtPane.addTab("Miscellaneous",new JScrollPane(page));
//-----------END TABS
		optionsFrame.addCenter(jtPane);
		JButton bTmp = SwingFactory.makeJButton("Okay", "");
		optionsFrame.addBottom(bTmp);
		optionsFrame.pack();
		bTmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {//FLOW: UI to Data
				//Edit to add option!
				options.setProperty(download_removeThumbs, getTF(remThumbs));
				options.setProperty(download_prettyFilenames, getTF(prettyFilenames));
				options.setProperty(download_saveExternalUrlList, getTF(saveExternalLinkList));
				options.setProperty(download_alternateNumbering, getTF(alternateNumbering));
				options.setProperty(download_keepDownloadLog, getTF(keepDlLog));
				options.setProperty(download_siteFirst, getTF(siteFirst));
				options.setProperty(download_ingoreStrings, ignoreDlText.getText());
				options.setProperty(download_wantStrings, wantDlText.getText());
				options.setProperty(download_sameSite,getValue(downloadSameSite));

				options.setProperty(download_sameFolder, getTF(sameFolder));
				options.setProperty(download_prependPage, getTF(prependPageName));
				options.setProperty(download_ppAsDir, getTF(prependPageNameAsFolder));
				options.setProperty(download_dateSubfolder, getTF(dateSubfolder));
				options.setProperty(download_usePageDirectory, getTF(usePageDirectory));
				options.setProperty(download_separateByDomain,getValue(separateByDomain));
				options.setProperty(download_usePageDomain, getValue(usePageDomain));

				options.setProperty(snatcher_getPictures, getTF(getPics));
				options.setProperty(snatcher_getMovies, getTF(getMovies));
				options.setProperty(snatcher_getAudio, getTF(getAudio));
				options.setProperty(snatcher_getArchives, getTF(getArchive));
				options.setProperty(snatcher_getOther, getTF(getOther));
				options.setProperty(snatcher_getDocuments, getTF(getDocument));
				options.setProperty(snatcher_readDeep, getTF(readDeep));
				options.setProperty(snatcher_readDepth, readDepth.getText());
				options.setProperty(snatcher_sameSite, getTF(sameSite));
				options.setProperty(snatcher_ignore, ignore.getText());
				options.setProperty(snatcher_want, want.getText());
				options.setProperty(snatcher_wantTitle, wantTitle.getText());
				options.setProperty(snatcher_ignoreTitle, ignoreTitle.getText());
				options.setProperty(snatcher_minImgWidth,minImgWidth.getText());
				options.setProperty(snatcher_minImgHeight,minImgHeight.getText());
				options.setProperty(snatcher_repeat,getTF(infiniteSnatch));
				updateWanted();
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
		pageTitle.setToolTipText("Will ignore pages that do not have one of the following in it's title. (leave blank to disable checking)");
		advOptions.addCenter(new JLabel("Download from pages with one of these in the title:"));
		advOptions.addCenter(SJPanel.makeTitledPanel("(Separate with ';')", new BorderLayout(),pageTitle));
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
		}
		if (!options.getProperty(snatcher_want, "").isEmpty()) {
			//ignore.setText(ignore.getText().toLowerCase().trim());
			wantedl = options.getProperty(snatcher_want, "").split(";");
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
		//Edit to add option!
		setValue(saveExternalLinkList,	getOption(download_saveExternalUrlList));
		setValue(remThumbs,		getOption(download_removeThumbs));
		setValue(getPics,				getOption(snatcher_getPictures));
		setValue(getMovies,			getOption(snatcher_getMovies));
		setValue(getAudio,			getOption(snatcher_getAudio));
		setValue(getArchive,			getOption(snatcher_getArchives));
		setValue(getDocument,		getOption(snatcher_getDocuments));
		setValue(getOther,			getOption(snatcher_getOther));
		setValue(prettyFilenames,	getOption(download_prettyFilenames));
		setValue(readDeep,			getOption(snatcher_readDeep));
		setValue(readDepth,			getOption(snatcher_readDepth));
		setValue(sameSite,			getOption(snatcher_sameSite));
		setValue(sameFolder,		getOption(download_sameFolder));
		setValue(alternateNumbering,	getOption(download_alternateNumbering));
		setValue(keepDlLog,			getOption(download_keepDownloadLog));
		setValue(prependPageName,	getOption(download_prependPage));
		setValue(prependPageNameAsFolder,	getOption(download_ppAsDir));
		setValue(usePageDirectory,	getOption(download_usePageDirectory));
		setValue(dateSubfolder,		getOption(download_dateSubfolder));
		setValue(siteFirst,				getOption(download_siteFirst));
		setValue(minImgWidth,		getOption(snatcher_minImgWidth));
		setValue(minImgHeight,		getOption(snatcher_minImgHeight));
		setValue(infiniteSnatch,		getOption(snatcher_repeat));
		setValue(usePageDomain,	getOption(download_usePageDomain));
		setValue(ignore,				getOption(snatcher_ignore));
		setValue(want,					getOption(snatcher_want));
		setValue(ignoreDlText,		getOption(download_ingoreStrings));
		setValue(wantDlText,			getOption(download_wantStrings));
		setValue(wantTitle,			getOption(snatcher_wantTitle));
		setValue(ignoreTitle,			getOption(snatcher_ignoreTitle));
		setValue(downloadSameSite,	getOption(download_sameSite));
		setValue(separateByDomain,	getOption(download_separateByDomain));

		uiStateUpdated();
	}
	protected void showOptions() {
		setupOptions();
		optionsFrame.center();
		optionsFrame.setVisible(true);
	}
	private void setupAdvOptions() {
		alwaysCheckMIME.setSelected(getTF(options.getProperty(snatcher_alwaysCheckMIME)));
		pageTitle.setText(options.getProperty(snatcher_wantedTitles, ""));
	}
	protected void showAdvOptions() {
		setupAdvOptions();
		advOptions.center();
		advOptions.setVisible(true);
	}
	public void addPage(OptionPanel panel){
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
		if (!remThumbs.isSelected()) return false;
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
		} else
			return true;
		log.debug("not in wanted page list. "+link);
		return false;
	}
	/**
	 * @param link
	 * @return true if the link is in the ignored list. false otherwise or if the list is empty
	 */
	private boolean isInIgnoredPageList(final String link) {
		if (ignoredl == null || ignoredl.length==0) return false;
		for (int i = 0; i < ignoredl.length; i++) {
			if (link.indexOf(ignoredl[i])>=0) {
				log.debug("in ignored list", "Matched ignore page item \""+ignoredl[i]+"\" in "+link);
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
		if (isWantedPageListCheck(link.toString().toLowerCase())) {
			if (!sameSiteCheck(link, ref))
				return true;
		} else return true;//failed list checks(ignored)
		//it's not ignored; check the mime
		final String mime = MimeTypes.getMime(link).get(0);
		if (mime.isEmpty() || "text/html".equals(mime))
			return false;
		log.debug("page ignored","mime: "+mime);
		return true;
	}
	/**To be used by the Session to determine if a download link is wanted.
	 * @param link
	 * @param ref
	 * @return
	 */
	public boolean isIgnored(final Uri link, final Uri ref) {
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
			if (isInIgnoredDlList(link)) return false;
		} else
			return false;
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
		if (isThumb(link)) return false;
		if (isInWantedPageList(link)) {
			if (isInIgnoredPageList(link)) return false;
		} else
			return false;
		return true;
	}
	/**Checks to see if the domains match if sameSite() is true.
	 * @param link The page link
	 * @param ref The referral page link.
	 * @return true if the domains match, referral==null, or sameSite()==false
	 */
	private boolean sameSiteCheck(final Uri link, final Uri ref) {
		if (getTF(getOption(snatcher_sameSite)) && ref != null) {
			if (!link.getDomain().equalsIgnoreCase(ref.getDomain())) {
				log.debug("same site check","mismatch domains: link["+link+"] -- referrer["+ref.getDomain()+"]");
				return false;
			}
		} else
			return true;
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
	private static Box createLine(JComponent comp,int indent){
		Box box=Box.createHorizontalBox();
		if(indent>0)box.add(Box.createHorizontalStrut(25*indent));
		box.add(comp);
		box.add(Box.createGlue());
		return box;
	}
	private static final void setValue(JComponent c, String v){
		if(c instanceof JTextComponent){
			((JTextComponent)c).setText(v);
		}
		if(c instanceof JCheckBox){
			((JCheckBox)c).setSelected(getTF(v));
		}
	}
	private static final String getValue(JComponent c){
		if(c instanceof JTextComponent){
			return ((JTextComponent)c).getText();
		}
		if(c instanceof JCheckBox){
			return getTF(((JCheckBox)c).isSelected());
		}
		return c.toString();
	}
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

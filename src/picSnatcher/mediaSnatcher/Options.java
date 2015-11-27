/**
 *
 */
package picSnatcher.mediaSnatcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

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
	private static final Properties DEFAULTS = new Properties();
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
		DEFAULTS.setProperty(OptionKeys.download_removeThumbs.name(), TRUE);
		DEFAULTS.setProperty(OptionKeys.snatcher_getPictures.name(), TRUE);
		DEFAULTS.setProperty(OptionKeys.snatcher_getMovies.name(), FALSE);
		DEFAULTS.setProperty(OptionKeys.snatcher_getAudio.name(), FALSE);
		DEFAULTS.setProperty(OptionKeys.snatcher_getArchives.name(), FALSE);
		DEFAULTS.setProperty(OptionKeys.snatcher_getDocuments.name(), FALSE);
		DEFAULTS.setProperty(OptionKeys.snatcher_getOther.name(), FALSE);
		DEFAULTS.setProperty(OptionKeys.download_prettyFilenames.name(), TRUE);
		DEFAULTS.setProperty(OptionKeys.snatcher_readDeep.name(), FALSE);
		DEFAULTS.setProperty(OptionKeys.snatcher_readDepth.name(), "1");
		DEFAULTS.setProperty(OptionKeys.snatcher_sameSite.name(), TRUE);
		DEFAULTS.setProperty(OptionKeys.download_sameFolder.name(), FALSE);
		DEFAULTS.setProperty(OptionKeys.download_sameFolder_custom.name(),"");
		DEFAULTS.setProperty(OptionKeys.download_alternateNumbering.name(), FALSE);
		DEFAULTS.setProperty(OptionKeys.download_prependPage.name(), FALSE);
		DEFAULTS.setProperty(OptionKeys.download_ppAsDir.name(), FALSE);
		DEFAULTS.setProperty(OptionKeys.download_keepDownloadLog.name(), FALSE);
		DEFAULTS.setProperty(OptionKeys.download_dateSubfolder.name(), FALSE);
		DEFAULTS.setProperty(OptionKeys.download_siteFirst.name(), TRUE);
		DEFAULTS.setProperty(OptionKeys.download_usePageDirectory.name(), FALSE);
		DEFAULTS.setProperty(OptionKeys.download_ingoreStrings.name(), "");
		DEFAULTS.setProperty(OptionKeys.download_wantStrings.name(), "");
		DEFAULTS.setProperty(OptionKeys.snatcher_ignore.name(), "");
		DEFAULTS.setProperty(OptionKeys.snatcher_want.name(), "");
		DEFAULTS.setProperty(OptionKeys.snatcher_wantedTitles.name(), "");
		DEFAULTS.setProperty(OptionKeys.snatcher_maxLogs.name(), "5");
		DEFAULTS.setProperty(OptionKeys.snatcher_saveFolder.name(), "");
		DEFAULTS.setProperty(OptionKeys.snatcher_saveFile.name(), "");
		DEFAULTS.setProperty(OptionKeys.snatcher_alwaysCheckMIME.name(), TRUE);
		DEFAULTS.setProperty(OptionKeys.snatcher_wantTitle.name(), "");
		DEFAULTS.setProperty(OptionKeys.snatcher_ignoreTitle.name(), "");
		DEFAULTS.setProperty(OptionKeys.snatcher_repeat.name(),FALSE);
		DEFAULTS.setProperty(OptionKeys.snatcher_minImgWidth.name(),"0");
		DEFAULTS.setProperty(OptionKeys.snatcher_minImgHeight.name(),"0");
		DEFAULTS.setProperty(OptionKeys.saveVersion.name(), "2.0");
		DEFAULTS.setProperty(OptionKeys.download_saveExternalUrlList.name(),FALSE);
		DEFAULTS.setProperty(OptionKeys.download_saveLinkList.name(),FALSE);
	}
	private String[] wantedt, ignoredt, ignoredl, wantedl, wantedd, ignoredd;

	private final Properties options = new Properties(DEFAULTS);
	private final JCheckBox
		remThumbs = new JCheckBox("Attempt removal of thumbnails(may skip wanted files)", true),
		getPics = new JCheckBox("Download Pictures", true),
		getMovs = new JCheckBox("Download Movies"),
		getAudi = new JCheckBox("Download Audio"),
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
		prependAsFolder = new JCheckBox("As folder."),
		dateSubfolder = new JCheckBox("Separate runs by date."),
		siteFirst = new JCheckBox("Domain root folder.", true),
		usePageDirectory = new JCheckBox("Use the page's directory as the save directory."),
		infiniteSnatch=new JCheckBox("Loop"),
		saveExternalLinkList=new JCheckBox("Save external link list.");
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
	//	private final Session session;
	public Options(final Main frame, final Session ses) {
		JTabbedPane jtPane = new JTabbedPane();
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

		getMovs.setToolTipText("Will download movies. Includes Flash.");
		page.add(createLine(getMovs,0));

		getAudi.setToolTipText("Will download audio files.");
		page.add(createLine(getAudi,0));

		getArchive.setToolTipText("Will download compressed files.(zip, tar, rar, gzip, etc.)");
		page.add(createLine(getArchive,0));

		getDocument.setToolTipText("Will download document files.(doc, ppt, wri, ods, ect.)");
		page.add(createLine(getDocument,0));

		getOther.setToolTipText("Will download anything not defined as a picture, audio, or movie file found on pages.");
		page.add(createLine(getOther,0));
		jtPane.addTab("Types",new JScrollPane(page));
//-----------SAVE OPTIONS TAB
		page=SJPanel.makeBoxLayoutPanelY();
		keepDlLog.setToolTipText("Will keep a log of previously dowloaded files. Useful when you've deleted unwanted files from a site that you plan to scan again.");
		page.add(createLine(keepDlLog,0));

		prettyFilenames.setToolTipText("Will make filenames more readable by removing some HTML escape sequences. (non-displayable characters and those forbidden in filenames will be left escaped)");
		page.add(createLine(prettyFilenames,0));

		prependPageName.setToolTipText("Will prepend the filename of the page that the file was found on minus the extention.");
		page.add(createLine(prependPageName,0));
		prependPageName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				prependAsFolder.setEnabled(prependPageName.isSelected());
			}
		});

		prependAsFolder.setToolTipText("If unchecked the pagename will be added to the filename (page_file). If checked it will create a subfolder (path\\page\\file).");
		page.add(createLine(prependAsFolder,1));

		dateSubfolder.setToolTipText("Will create a subfolder with today's date and use that as the root folder under the site.");
		page.add(createLine(dateSubfolder,0));
		dateSubfolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				siteFirst.setEnabled(dateSubfolder.isSelected() && !sameFolder.isSelected());
			}
		});

		siteFirst.setToolTipText("Will use the domain as the root and the date will be a subfolder.");
		page.add(createLine(siteFirst,1));

		usePageDirectory.setToolTipText("Will use the page's directory to save content found on that page.(combinable with other options)");
		page.add(createLine(usePageDirectory,0));

		alternateNumbering.setToolTipText("Uses a number scheme to name files. ####_### (pageNum_fileNum). Useful when sites give files non-sense names that throw off order.");
		page.add(createLine(alternateNumbering,0));

		sameFolder.setToolTipText("Will save all files found to one folder. Works best when combined with alternate numbering.");
		page.add(createLine(sameFolder,0));
		jtPane.addTab("Save Options",new JScrollPane(page));
//-----------DOWNLOAD TAB
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
		jtPane.addTab("Download",new JScrollPane(page));
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
				options.setProperty(OptionKeys.download_saveExternalUrlList.name(), getTF(saveExternalLinkList.isSelected()));
				options.setProperty(OptionKeys.download_removeThumbs.name(), getTF(remThumbs.isSelected()));
				options.setProperty(OptionKeys.snatcher_getPictures.name(), getTF(getPics.isSelected()));
				options.setProperty(OptionKeys.snatcher_getMovies.name(), getTF(getMovs.isSelected()));
				options.setProperty(OptionKeys.snatcher_getAudio.name(), getTF(getAudi.isSelected()));
				options.setProperty(OptionKeys.snatcher_getArchives.name(), getTF(getArchive.isSelected()));
				options.setProperty(OptionKeys.snatcher_getDocuments.name(), getTF(getDocument.isSelected()));
				options.setProperty(OptionKeys.snatcher_getOther.name(), getTF(getOther.isSelected()));
				options.setProperty(OptionKeys.download_prettyFilenames.name(), getTF(prettyFilenames.isSelected()));
				options.setProperty(OptionKeys.snatcher_readDeep.name(), getTF(readDeep.isSelected()));
				options.setProperty(OptionKeys.snatcher_readDepth.name(), readDepth.getText());
				options.setProperty(OptionKeys.snatcher_sameSite.name(), getTF(sameSite.isSelected()));
				options.setProperty(OptionKeys.download_sameFolder.name(), getTF(sameFolder.isSelected()));
				options.setProperty(OptionKeys.download_alternateNumbering.name(), getTF(alternateNumbering.isSelected()));
				options.setProperty(OptionKeys.download_keepDownloadLog.name(), getTF(keepDlLog.isSelected()));
				options.setProperty(OptionKeys.download_prependPage.name(), getTF(prependPageName.isSelected()));
				options.setProperty(OptionKeys.download_ppAsDir.name(), getTF(prependAsFolder.isSelected()));
				options.setProperty(OptionKeys.download_dateSubfolder.name(), getTF(dateSubfolder.isSelected()));
				options.setProperty(OptionKeys.download_siteFirst.name(), getTF(siteFirst.isSelected()));
				options.setProperty(OptionKeys.download_usePageDirectory.name(), getTF(usePageDirectory.isSelected()));
				options.setProperty(OptionKeys.snatcher_ignore.name(), ignore.getText());
				options.setProperty(OptionKeys.snatcher_want.name(), want.getText());
				options.setProperty(OptionKeys.download_ingoreStrings.name(), ignoreDlText.getText());
				options.setProperty(OptionKeys.download_wantStrings.name(), wantDlText.getText());
				options.setProperty(OptionKeys.snatcher_wantTitle.name(), wantTitle.getText());
				options.setProperty(OptionKeys.snatcher_ignoreTitle.name(), ignoreTitle.getText());
				options.setProperty(OptionKeys.snatcher_minImgWidth.name(),minImgWidth.getText());
				options.setProperty(OptionKeys.snatcher_minImgHeight.name(),minImgHeight.getText());
				options.setProperty(OptionKeys.snatcher_repeat.name(),getTF(infiniteSnatch.isSelected()));
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
				options.setProperty(OptionKeys.snatcher_alwaysCheckMIME.name(), getTF(alwaysCheckMIME.isSelected()));
				//TODO: see below
				//header log
				//page title
				//options.setProperty(OptionKeys.download_wantStrings.name(), wantDlText.getText());
				advOptions.setVisible(false);
			}
		});
		advOptions.addBottom(bTmp);
		advOptions.pack();
		advOptions.setSize(305, advOptions.getHeight());
	}
	protected void updateWanted() {
		if (!options.getProperty(OptionKeys.snatcher_wantTitle.name(), "").isEmpty()) {
			//pageTitle.setText(pageTitle.getText().toLowerCase());
			wantedt = options.getProperty(OptionKeys.snatcher_wantTitle.name(), "").split(";");
		}
		if (!options.getProperty("snatcher.ingoreTitle", "").isEmpty()) {
			//pageTitle.setText(pageTitle.getText().toLowerCase());
			ignoredt = options.getProperty(OptionKeys.snatcher_ignoreTitle.name(), "").split(";");
		}
		if (!options.getProperty(OptionKeys.snatcher_ignore.name(), "").isEmpty()) {
			//ignore.setText(ignore.getText().toLowerCase().trim());
			ignoredl = options.getProperty(OptionKeys.snatcher_ignore.name(), "").split(";");
		}
		if (!options.getProperty(OptionKeys.snatcher_want.name(), "").isEmpty()) {
			//ignore.setText(ignore.getText().toLowerCase().trim());
			wantedl = options.getProperty(OptionKeys.snatcher_want.name(), "").split(";");
		}
		if (!options.getProperty(OptionKeys.download_ingoreStrings.name(), "").isEmpty()) {
			//ignore.setText(ignore.getText().toLowerCase().trim());
			ignoredd = options.getProperty(OptionKeys.download_ingoreStrings.name(), "").split(";");
		}
		if (!options.getProperty(OptionKeys.download_wantStrings.name(), "").isEmpty()) {
			//ignore.setText(ignore.getText().toLowerCase().trim());
			wantedd = options.getProperty(OptionKeys.download_wantStrings.name(), "").split(";");
		}
	}
	private void setupOptions() {//FLOW: data to UI
		//Edit to add option!
		saveExternalLinkList.setSelected(getTF(options.getProperty(OptionKeys.download_saveExternalUrlList.name(), TRUE)));
		remThumbs.setSelected(getTF(options.getProperty(OptionKeys.download_removeThumbs.name(), TRUE)));
		getPics.setSelected(getTF(options.getProperty(OptionKeys.snatcher_getPictures.name(), TRUE)));
		getMovs.setSelected(getTF(options.getProperty(OptionKeys.snatcher_getMovies.name(), FALSE)));
		getAudi.setSelected(getTF(options.getProperty(OptionKeys.snatcher_getAudio.name(), FALSE)));
		getArchive.setSelected(getTF(options.getProperty(OptionKeys.snatcher_getArchives.name(), FALSE)));
		getDocument.setSelected(getTF(options.getProperty(OptionKeys.snatcher_getDocuments.name(), FALSE)));
		getOther.setSelected(getTF(options.getProperty(OptionKeys.snatcher_getOther.name(), FALSE)));
		prettyFilenames.setSelected(getTF(options.getProperty(OptionKeys.download_prettyFilenames.name(), TRUE)));
		readDeep.setSelected(getTF(options.getProperty(OptionKeys.snatcher_readDeep.name(), FALSE)));
		readDepth.setText(options.getProperty(OptionKeys.snatcher_readDepth.name(), "1"));
		sameSite.setSelected(getTF(options.getProperty(OptionKeys.snatcher_sameSite.name(), TRUE)));
		sameFolder.setSelected(getTF(options.getProperty(OptionKeys.download_sameFolder.name(), FALSE)));
		alternateNumbering.setSelected(getTF(options.getProperty(OptionKeys.download_alternateNumbering.name(), FALSE)));
		keepDlLog.setSelected(getTF(options.getProperty(OptionKeys.download_keepDownloadLog.name(), FALSE)));
		prependPageName.setSelected(getTF(options.getProperty(OptionKeys.download_prependPage.name(), FALSE)));
		prependAsFolder.setSelected(getTF(options.getProperty(OptionKeys.download_ppAsDir.name(), FALSE)));
		prependAsFolder.setEnabled(prependPageName.isSelected());
		usePageDirectory.setSelected(getTF(options.getProperty(OptionKeys.download_usePageDirectory.name())));
		dateSubfolder.setSelected(getTF(options.getProperty(OptionKeys.download_dateSubfolder.name(), FALSE)));
		siteFirst.setSelected(getTF(options.getProperty(OptionKeys.download_siteFirst.name(), TRUE)));
		siteFirst.setEnabled(dateSubfolder.isSelected());
		minImgWidth.setText(options.getProperty(OptionKeys.snatcher_minImgWidth.name()));
		minImgHeight.setText(options.getProperty(OptionKeys.snatcher_minImgHeight.name()));
		infiniteSnatch.setSelected(getTF(options.getProperty(OptionKeys.snatcher_repeat.name())));

		ignore.setText(options.getProperty(OptionKeys.snatcher_ignore.name(), ""));
		want.setText(options.getProperty(OptionKeys.snatcher_want.name(), ""));
		ignoreDlText.setText(options.getProperty(OptionKeys.download_ingoreStrings.name(), ""));
		wantDlText.setText(options.getProperty(OptionKeys.download_wantStrings.name(), ""));
		wantTitle.setText(options.getProperty(OptionKeys.snatcher_wantTitle.name(), ""));
		ignoreTitle.setText(options.getProperty(OptionKeys.snatcher_ignoreTitle.name(), ""));
	}
	protected void showOptions() {
		setupOptions();
		optionsFrame.center();
		optionsFrame.setVisible(true);
	}
	private void setupAdvOptions() {
		alwaysCheckMIME.setSelected(getTF(options.getProperty(OptionKeys.snatcher_alwaysCheckMIME.name())));
		pageTitle.setText(options.getProperty(OptionKeys.snatcher_wantedTitles.name(), ""));
	}
	protected void showAdvOptions() {
		setupAdvOptions();
		advOptions.center();
		advOptions.setVisible(true);
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
		try {
			final FileReader in = new FileReader(pref);
			Properties tmp=new Properties();
			tmp.load(in);
			if(tmp.getProperty(OptionKeys.saveVersion.name(),null)==null){
				final Properties nprop = new Properties();
				//Update old options to new
				nprop.setProperty(OptionKeys.download_removeThumbs.name(), getTF(TRUE.equals(tmp.getProperty("download.removeThumbs"))));
				nprop.setProperty(OptionKeys.snatcher_getPictures.name(), getTF(TRUE.equals(tmp.getProperty("snatcher.getPictures"))));
				nprop.setProperty(OptionKeys.snatcher_getMovies.name(), getTF(TRUE.equals(tmp.getProperty("snatcher.getMovies"))));
				nprop.setProperty(OptionKeys.snatcher_getAudio.name(), getTF(TRUE.equals(tmp.getProperty("snatcher.getAudio"))));
				nprop.setProperty(OptionKeys.snatcher_getArchives.name(), getTF(TRUE.equals(tmp.getProperty("snatcher.getArchives"))));
				nprop.setProperty(OptionKeys.snatcher_getDocuments.name(), getTF(TRUE.equals(tmp.getProperty("snatcher.getDocuments"))));
				nprop.setProperty(OptionKeys.snatcher_getOther.name(), getTF(TRUE.equals(tmp.getProperty("snatcher.getOther"))));
				nprop.setProperty(OptionKeys.download_prettyFilenames.name(), getTF(TRUE.equals(tmp.getProperty("download.prettyFilenames"))));
				nprop.setProperty(OptionKeys.snatcher_readDeep.name(), getTF(TRUE.equals(tmp.getProperty("snatcher.readDeep"))));
				nprop.setProperty(OptionKeys.snatcher_sameSite.name(), getTF(TRUE.equals(tmp.getProperty("snatcher.sameSite"))));
				nprop.setProperty(OptionKeys.download_sameFolder.name(), getTF(TRUE.equals(tmp.getProperty("download.sameFolder"))));
				nprop.setProperty(OptionKeys.download_alternateNumbering.name(), getTF(TRUE.equals(tmp.getProperty("download.alternateNumbering"))));
				nprop.setProperty(OptionKeys.download_keepDownloadLog.name(), getTF(TRUE.equals(tmp.getProperty("download.keepDownloadLog"))));
				nprop.setProperty(OptionKeys.download_prependPage.name(), getTF(TRUE.equals(tmp.getProperty("download.prependPage"))));
				nprop.setProperty(OptionKeys.download_ppAsDir.name(), getTF(TRUE.equals(tmp.getProperty("download.ppAsDir"))));
				nprop.setProperty(OptionKeys.download_dateSubfolder.name(), getTF(TRUE.equals(tmp.getProperty("download.dateSubfolder"))));
				nprop.setProperty(OptionKeys.download_siteFirst.name(), getTF(TRUE.equals(tmp.getProperty("download.siteFirst"))));
				nprop.setProperty(OptionKeys.download_usePageDirectory.name(), getTF(TRUE.equals(tmp.getProperty("download.usePageDirectory"))));
				nprop.setProperty(OptionKeys.snatcher_repeat.name(), getTF(TRUE.equals(tmp.getProperty("snatcher.repeat"))));

				nprop.setProperty(OptionKeys.snatcher_readDepth.name(), tmp.getProperty("snatcher.readDepth"));
				nprop.setProperty(OptionKeys.download_ingoreStrings.name(), tmp.getProperty("download.ignore"));
				nprop.setProperty(OptionKeys.download_wantStrings.name(), tmp.getProperty("download.want"));
				nprop.setProperty(OptionKeys.snatcher_ignore.name(), tmp.getProperty("snatcher.ignore"));
				nprop.setProperty(OptionKeys.snatcher_want.name(), tmp.getProperty("snatcher.want"));
				nprop.setProperty(OptionKeys.snatcher_wantedTitles.name(), tmp.getProperty("snatcher.wantedTitles"));
				nprop.setProperty(OptionKeys.snatcher_maxLogs.name(), tmp.getProperty("snatcher.maxLogs"));
				nprop.setProperty(OptionKeys.snatcher_saveFolder.name(), tmp.getProperty("snatcher.saveFolder"));
				nprop.setProperty(OptionKeys.snatcher_saveFile.name(), tmp.getProperty("snatcher.saveFile"));
				nprop.setProperty(OptionKeys.snatcher_wantTitle.name(), tmp.getProperty("snatcher.wantTitle"));
				nprop.setProperty(OptionKeys.snatcher_ignoreTitle.name(), tmp.getProperty("snatcher.ignoreTitle"));
				nprop.setProperty(OptionKeys.snatcher_minImgWidth.name(), tmp.getProperty("snatcher.minImgWidth"));
				nprop.setProperty(OptionKeys.snatcher_minImgHeight.name(), tmp.getProperty("snatcher.minImgHeight"));
				nprop.setProperty(OptionKeys.saveVersion.name(), "2.0");
				tmp.clear();
				tmp.putAll(nprop);
			}
			options.putAll(tmp);
			//set all true/false to the constants
			//Edit to add option True-False!
			options.setProperty(OptionKeys.download_saveExternalUrlList.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.download_saveExternalUrlList.name()))));
			options.setProperty(OptionKeys.download_removeThumbs.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.download_removeThumbs.name()))));
			options.setProperty(OptionKeys.snatcher_getPictures.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.snatcher_getPictures.name()))));
			options.setProperty(OptionKeys.snatcher_getMovies.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.snatcher_getMovies.name()))));
			options.setProperty(OptionKeys.snatcher_getAudio.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.snatcher_getAudio.name()))));
			options.setProperty(OptionKeys.snatcher_getArchives.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.snatcher_getArchives.name()))));
			options.setProperty(OptionKeys.snatcher_getDocuments.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.snatcher_getDocuments.name()))));
			options.setProperty(OptionKeys.snatcher_getOther.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.snatcher_getOther.name()))));
			options.setProperty(OptionKeys.download_prettyFilenames.name(), getTF(TRUE.equals(options.getProperty("snatcher.prettyFilenames"))));
			options.setProperty(OptionKeys.snatcher_readDeep.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.snatcher_readDeep.name()))));
			options.setProperty(OptionKeys.snatcher_sameSite.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.snatcher_sameSite.name()))));
			options.setProperty(OptionKeys.download_sameFolder.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.download_sameFolder.name()))));
			options.setProperty(OptionKeys.download_alternateNumbering.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.download_alternateNumbering.name()))));
			options.setProperty(OptionKeys.download_keepDownloadLog.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.download_keepDownloadLog.name()))));
			options.setProperty(OptionKeys.download_prependPage.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.download_prependPage.name()))));
			options.setProperty(OptionKeys.download_ppAsDir.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.download_ppAsDir.name()))));
			options.setProperty(OptionKeys.download_dateSubfolder.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.download_dateSubfolder.name()))));
			options.setProperty(OptionKeys.download_siteFirst.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.download_siteFirst.name()))));
			options.setProperty(OptionKeys.download_usePageDirectory.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.download_usePageDirectory.name()))));
			options.setProperty(OptionKeys.snatcher_repeat.name(), getTF(TRUE.equals(options.getProperty(OptionKeys.snatcher_repeat.name()))));
			FileUtil.close(in);
		} catch (final Exception e) {
			log.error("loadPreferences", e);
		} finally {
			updateWanted();
		}
	}
	public static final String getTF(final boolean test) {
		if (test)
			return TRUE;
		else
			return FALSE;
	}
	public static final boolean getTF(final String test) {
		if (TRUE.equalsIgnoreCase(test))
			return true;
		else
			return false;
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
			if (getOption(OptionKeys.snatcher_alwaysCheckMIME)==TRUE) {
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
		if (sameSite() && ref != null) {
			if (!link.getDomain().equalsIgnoreCase(ref.getDomain())) {
				log.debug("same site check","mismatch domains: link["+link+"] -- referrer["+ref.getDomain()+"]");
				return false;
			}
		} else
			return true;
		return true;
	}
	public boolean isWantedMIME(final String mime) {
		return (getPics()&&MimeTypes.isMimeImage(mime)) ||
		(getMovie()&&MimeTypes.isMimeVideo(mime)) ||
		(getAudio()&&MimeTypes.isMimeAudio(mime)) ||
		(getArchive()&&MimeTypes.isMimeArchive(mime)) ||
		(getDocument()&&MimeTypes.isMimeDocument(mime)) ||
		(getOther()&&MimeTypes.isMimeOther(mime));
	}
	/*
	 */
	/**
	 * @return the alternateNumbering
	 */
	protected final boolean alternateNumbering() {
		return getTF(options.getProperty(OptionKeys.download_alternateNumbering.name(), FALSE));
	}
	/**
	 * @return the deepScan
	 */
	protected final boolean deepScan() {
		return getTF(options.getProperty(OptionKeys.snatcher_readDeep.name(), FALSE));
	}
	/**
	 * @return the getAudi
	 */
	protected final boolean getAudio() {
		return getTF(options.getProperty(OptionKeys.snatcher_getAudio.name(), FALSE));
	}
	/**
	 * @return the getMovs
	 */
	protected final boolean getMovie() {
		return getTF(options.getProperty(OptionKeys.snatcher_getMovies.name(), FALSE));
	}
	/**
	 * @return the getOther
	 */
	protected final boolean getOther() {
		return getTF(options.getProperty(OptionKeys.snatcher_getOther.name(), FALSE));
	}
	/**
	 * @return the getArchive
	 */
	protected final boolean getArchive() {
		return getTF(options.getProperty(OptionKeys.snatcher_getArchives.name(), FALSE));
	}
	/**
	 * @return the getDocument
	 */
	protected final boolean getDocument() {
		return getTF(options.getProperty(OptionKeys.snatcher_getDocuments.name(), FALSE));
	}
	/**
	 * @return the getPics
	 */
	protected final boolean getPics() {
		return getTF(options.getProperty(OptionKeys.snatcher_getPictures.name(), TRUE));
	}
	/**
	 * @return the keepDlLog
	 */
	protected final boolean keepDlLog() {
		return getTF(options.getProperty(OptionKeys.download_keepDownloadLog.name(), FALSE));
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
		return Integer.parseInt(options.getProperty(OptionKeys.snatcher_readDepth.name(), "1"));
	}
	/**
	 * @return the remThumbs
	 */
	protected final boolean remThumbs() {
		return getTF(options.getProperty(OptionKeys.download_removeThumbs.name(), TRUE));
	}
	/**
	 * @return the sameFolder
	 */
	protected final boolean sameFolder() {
		return getTF(options.getProperty(OptionKeys.download_sameFolder.name(), FALSE));
	}
	/**
	 * @return the sameSite
	 */
	protected final boolean sameSite() {
		return getTF(options.getProperty(OptionKeys.snatcher_sameSite.name(), TRUE));
	}
	protected final boolean prettyFilenames() {
		return getTF(options.getProperty(OptionKeys.download_prettyFilenames.name(), TRUE));
	}
	protected final String getIgnoreList() {
		return options.getProperty(OptionKeys.snatcher_ignore.name(), "");
	}
	protected final String getTitleList() {
		return options.getProperty(OptionKeys.snatcher_wantedTitles.name(), "");
	}
	protected final void setOption(final OptionKeys option, final String value) {
		//log.information(option.name(), value);
		options.setProperty(option.name(), value);
	}
	public final String getOption(final OptionKeys option) {
		return options.getProperty(option.name());
	}
	protected final Enumeration<Object> getOptions() {
		return options.keys();
	}
	public void dumpOptions() {
		log.information(options);
	}
	private static Box createLine(JComponent comp,int indent){
		Box box=Box.createHorizontalBox();
		if(indent>0)box.add(Box.createHorizontalStrut(25*indent));
		box.add(comp);
		box.add(Box.createGlue());
		return box;
	}
}

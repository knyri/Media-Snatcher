package picSnatcher.mediaSnatcher;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import picSnatcher.mediaSnatcher.extension.ForwardRemoverFactory;
import picSnatcher.mediaSnatcher.extension.forwarder.ForwardRemover;
import simple.CIString;
import simple.gui.AboutWindow;
import simple.gui.component.JNumberField;
import simple.gui.component.SMenuBar;
import simple.gui.factory.SJOptionPane;
import simple.gui.factory.SJPanel;
import simple.gui.factory.SwingFactory;
import simple.html.MimeTypes;
import simple.io.FileUtil;
import simple.io.FilterFactory;
import simple.net.Uri;
import simple.util.do_str;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;
import simple.util.logging.LogLevel;

/** Re-recreation of the perfected code lost due to an unforeseen HDD crash.<br>
 * Surpasses it only because of the added forward removers.<br>
 * Additional Improvements:<br>
 * +Added use of {@link simple.ml.InlineStrictParser} and {@link simple.parser.ml.Page}
 * for easier and improved page processing.
 * <br>
 * This program takes a URL, reads it and based on selections will read the links
 * on the URL in a queued fashion and download the wanted content.<br>
 * Selections can be made to avoid reading unwanted pages through URL and page title
 * filters. Currently, only direct public FTP links can be downloaded.<br>
 * <hr>
 * A primary feature of this program is the ability to use ranges in the URL.<br>
 * For example, NASA has a nice gallery of photos that I like. It is spread across
 * 6 pages with 100 photos per page. I could add each page individually to the
 * program or I could use this feature.<br>
 * Sometimes the URL of the next page is needed to see the variable. In this case
 * it is <i>start</i>. However, the variable name is not important.<br>
 * <i>http://photojournal.jpl.nasa.gov/gallery/universe?start=100</i><br>
 * We enter <i>http://photojournal.jpl.nasa.gov/gallery/universe?start=^gal^</i>
 * in to the URL box. The gallery syntax for this site isn't important, but
 * sometimes leading 0's are needed. A good way to use this is just paste the
 * value of the target variable since I just use the length and not the value.<br>
 * The gallery starts at <i>0</i>, ends at <i>500</i>, and increments by <i>100</i>.
 * The start and end values are determined by the first page of the gallery and
 * the last page of the gallery. The increment is determined by the difference
 * of the controlling value (in our case <i>start</i>) for each page.<hr>
 * Custom page parsers and forwarder removers can be created and added by extending
 * the respective abstract class and adding the fully qualified java name to
 * <i>picSnatcher/mediaSnatcher3/pageparsers.ms3.conf</i> and
 * <i>picSnatcher/mediaSnatcher3/forwarders.ms3.conf</i>
 * <br>Created: Jul 11, 2010
 * @author Kenneth Pierce
 */
public class Main implements ActionListener {
	static {
		LogFactory.setPrint(LogLevel.DEBUG, false);
		LogFactory.setPrint(LogLevel.ERROR, true);
		LogFactory.setPrint(LogLevel.INFORMATION, true);
		LogFactory.setPrint(LogLevel.WARNING, true);
		LogFactory.setPrintTimeStamp(true);
		Uri.setDefaultScheme("http");
		Log log;
		/*
		log = LogFactory.getLogFor(MimeTypes.class);
		log.setPrintDebug(false);
		log = LogFactory.getLogFor(simple.parser.ml.InlineLooseParser.class);
		log.setPrintDebug(false);
		log.setPrintWarning(false);
		/**/
		/*
		log = LogFactory.getLogFor(simple.net.http.Client.class);
		log.setPrintInformation(true);
		log.setPrintWarning(true);
		log.setPrintDebug(true);
		/**/
		/**/ /*
		log = LogFactory.getLogFor(Options.class);
		log.setPrintDebug(true);
		log.setPrintInformation(true);
		/**/ /*
		log = LogFactory.getLogFor(Session.class);
		log.setPrintDebug(true);
		log.setPrintInformation(true);
		/**/ /*
		log = LogFactory.getLogFor(PageParser.class);
		log.setPrintDebug(true);
		log.setPrintInformation(true);
		/**/ /*
		log = LogFactory.getLogFor(PageReader.class);
		log.setPrintDebug(true);
		log.setPrintInformation(true);
		/**/ /*
		log = LogFactory.getLogFor(UrlMap.class);
		log.setPrintDebug(true);
		log.setPrintInformation(true);
		//*/
		//*
		log = LogFactory.getLogFor(Downloader.class);
		log.setPrintInformation(true);
		log.setPrintWarning(true);
		log.setPrintDebug(true);
		/**/
	}
	//** For updating memory usage */
	//private static final Runtime RUNTIME = Runtime.getRuntime();
	//** Attempt to filter out the memory used by the compiler */
	//private static int baseMem = (int)((RUNTIME.maxMemory() - RUNTIME.freeMemory())/1024);
	//** timer for updating the memory usage */
	//private final java.util.Timer timer = new java.util.Timer();
	/** Holds the links to be downloaded. NOTE: may move to Session */
	private final UrlMap Links = new UrlMap();
	private final AboutWindow mimeTypeDialog;
	private static Log log;
	/** The current session */
	private Session session;
	/** The name pretty much explains it */
	protected static final String LOG_FOLDER = "mediaSnatcherLogs"+File.separator;
	private final JTextField siteURL = new JTextField();
	private final JNumberField synGAL = new JNumberField("0",5),
	staGAL = new JNumberField("0",5),
	endGAL = new JNumberField("0",5),
	incGAL = new JNumberField("1",5);
	private final JComboBox<UriFormatIterator> list = new JComboBox<UriFormatIterator>();
	final JButton GO = SwingFactory.makeJButton("Snatch", "start", this);
	private final JButton Add = SwingFactory.makeJButton("Add", "ad", this), rem = SwingFactory.makeJButton("Remove", "rem", this), remAll = SwingFactory.makeJButton("Clear", "cl", this);
	private final JLabel state = new JLabel(" "),//General State
	stateExt = new JLabel(" "),//Extended info about state
	status = new JLabel(" ");//Status text(bottom of frame)
	final JProgressBar totPBar = new JProgressBar(0,100),//progress of items to be downloaded
	curPBar = new JProgressBar(0,100);//progress of current download
	//memUsage = new JProgressBar(0,0);
	private static final javax.swing.filechooser.FileFilter saveFilter = FilterFactory.createFileChooserFilter(".msl" ,"mediaSnatcher List (.msl)"),
	savedFileListFilter = FilterFactory.createFileChooserFilter(".msf", "mediaSnatcher File List (.msf)");
	private final Main main = this;
	protected final JFrame frame;
	/** Entry point. Loads the forward removers from <i>picSnatcher/mediaSnatcher3/forwarders.ms3.conf</i>
	 * and the page parsers from <i>picSnatcher/mediaSnatcher3/pageparsers.ms3.conf</i>. Fails if either cannot be found.
	 * @param args
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void main(final String[] args) throws IOException {
		if(args.length>0){
			if(args[0].equals("debug")){
				LogFactory.setPrint(LogLevel.DEBUG, true);
			}
		}
		LogFactory.setECR(new ErrorCodes());
		LogFactory.setGlobalLogFile(new File(LOG_FOLDER+LogFactory.getDateStamp()+LogFactory.getTimeStamp()+"MS4.log"), false);
		log = LogFactory.getLogFor(Main.class);
		//load the forward removers
		BufferedReader handlers = new BufferedReader(new InputStreamReader(ForwardRemover.class.getClassLoader().getResourceAsStream("picSnatcher/mediaSnatcher/forwarders.ms4.conf")));
		String line;
		do {
			line = handlers.readLine();
			if (line==null) {
				break;
			}
			try {
				ForwardRemoverFactory.addHandler((ForwardRemover) Class.forName(line).newInstance());
			} catch (final Exception e) {	e.printStackTrace(); }
		} while (true);
		FileUtil.close(handlers);
		//load the page parsers
		handlers = new BufferedReader(new InputStreamReader(PageParser.class.getClassLoader().getResourceAsStream("picSnatcher/mediaSnatcher/pageparsers.ms4.conf")));
		line = null;
		do {
			line = handlers.readLine();
			if (line==null) {
				break;
			}
			try {
				PageParser.addHandler((Class<PageParser>) Class.forName(line));
			} catch (final Exception e) {	e.printStackTrace(); }
		} while (true);
		FileUtil.close(handlers);
		new Main();
	}
	public Main() throws FileNotFoundException {
		totPBar.setStringPainted(true);
		curPBar.setStringPainted(true);
		final File folder = new File(LOG_FOLDER);
		final File[] logs = folder.listFiles(FilterFactory.createFilenameFilter(null, "MS4.log"));
		if (logs != null && logs.length > 15) {
			for (int i = logs.length-1; i > 15; i--) {
				logs[i-15].delete();
			}
		}
		final SMenuBar Menu = new SMenuBar(SMenuBar.FILE+SMenuBar.FILE_QUIT+SMenuBar.OPTION+SMenuBar.HELP);
		frame = new JFrame();
		frame.setJMenuBar(Menu);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Media Snatcher 4");
		frame.setResizable(false);

		mimeTypeDialog = new AboutWindow(frame,"Known MIMEs and extensions",true);
		mimeTypeDialog.appendLineBold("Image Mimes");
		mimeTypeDialog.append(MimeTypes.dumpImageMimes());
		mimeTypeDialog.appendLineBold("Video Mimes");
		mimeTypeDialog.append(MimeTypes.dumpVideoMimes());
		mimeTypeDialog.appendLineBold("Document Mimes");
		mimeTypeDialog.append(MimeTypes.dumpDocumentMimes());
		mimeTypeDialog.appendLineBold("Archive Mimes");
		mimeTypeDialog.append(MimeTypes.dumpArchiveMimes());
		mimeTypeDialog.appendLineBold("Other Mimes");
		mimeTypeDialog.append(MimeTypes.dumpOtherMimes());

		//================Create Options Dialog========================
		session = new Session(main);
		JMenuItem miTmp = new JMenuItem("Download Options...");
		miTmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				session.getOptions().showOptions();
			}
		});
		Menu.addToOptionMenu(miTmp);
		miTmp = new JMenuItem("Advanced Options...");
		miTmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				session.getOptions().showAdvOptions();
			}
		});
		Menu.addToOptionMenu(miTmp);
		miTmp = new JMenuItem("Known File Types");
		miTmp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				mimeTypeDialog.setVisible(true);
			}
		});
		Menu.addToHelpMenu(miTmp);
		//================End Options Setup============================
		Menu.addActionListener(SMenuBar.FILE_QUIT, this);
		Menu.addToFileMenu(SwingFactory.makeJMenuItem("Free Memory", new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ae) {
				System.gc();
			}
		}));
		/* Con
		 * * [P1](BorderLayout.CENTER)
		 * * * [siteURL]
		 * * * [memUsage]
		 * * * [P3]
		 * * * [P4]
		 * * * [dlPBar]
		 * * * [cdlPBar]
		 * * [P5](BorderLayout.SOUTH)
		 */
		final Container con = frame.getContentPane();
		final JPanel P0 = new JPanel(new BorderLayout());
		final JPanel P1 = new JPanel(new GridLayout(6,1));//Everything
		final JPanel P3 = new JPanel(new GridLayout(1,4));//gallery input
		final JPanel P4 = new JPanel(new GridLayout(2,1));//status labels
		final JPanel P5 = new JPanel(new GridLayout(1,5));//top row of btn
		final JPanel P6 = new JPanel(new GridLayout(1,5));//bottom row of btn
		final JPanel P7 = new JPanel(new GridLayout(2,1));//P0 and P2

		final JButton Save = SwingFactory.makeJButton("Save", "sv", this);
		final JButton Load = SwingFactory.makeJButton("Load", "ld", this);
		final JButton savePref = SwingFactory.makeJButton("Save Preferences", "svp", this);
		final JButton loadPref = SwingFactory.makeJButton("Load Preferences", "ldp", this);
		final JButton saveFiles = SwingFactory.makeJButton("Save Files", "sf", this);
		final JButton loadFiles = SwingFactory.makeJButton("Load Files", "lf", this);
		saveFiles.setToolTipText("Save file URL list to download at a later date.");
		loadFiles.setToolTipText("Load file URL list to add to download queue.");
		getList().addActionListener(this);
		synGAL.setSelectOnFocus(true);
		staGAL.setSelectOnFocus(true);
		endGAL.setSelectOnFocus(true);
		incGAL.setSelectOnFocus(true);
		state.setFont(new Font("Times New Roman", Font.PLAIN, 12));
		stateExt.setFont(new Font("Courier New", Font.PLAIN, 12));
		status.setFont(new Font("Courier New", Font.PLAIN, 12));

		P3.add(SJPanel.makeLabeledPanel(synGAL, "Gal Syntax"));
		P3.add(SJPanel.makeLabeledPanel(staGAL, "Gal Start"));
		P3.add(SJPanel.makeLabeledPanel(endGAL, "Gal End"));
		P3.add(SJPanel.makeLabeledPanel(incGAL, "Gal Increment"));

		P4.add(state);
		P4.add(stateExt);
		/* ***********************************
		 * add | rem | save | savep | savef
		 * ***********************************
		 * go | remall | load | loadp | loadf
		 * ***********************************
		 */
		P5.add(Add);
		P5.add(rem);
		P5.add(Save);
		P5.add(savePref);
		P5.add(saveFiles);
		P7.add(P5);

		P6.add(GO);
		P6.add(remAll);
		P6.add(Load);
		P6.add(loadPref);
		P6.add(loadFiles);
		P7.add(P6);

		P1.add(siteURL);
		//P1.add(memUsage);
		P1.add(P3);
		P1.add(P4);
		P1.add(getList());
		P1.add(totPBar);
		P1.add(curPBar);
		P1.add(status);

		P0.add(P1);
		P0.add(P7, BorderLayout.SOUTH);

		con.add(P0);
		final JPanel pTmp = new JPanel(new BorderLayout());
		pTmp.add(status);
		pTmp.setBorder(javax.swing.border.LineBorder.createGrayLineBorder());
		con.add(pTmp, BorderLayout.SOUTH);
		setStatus("URLs: 0 Retry: 0 Downloaded: 0");
		frame.pack();

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	void setStatus(final String msg) {
		status.setText(msg);
	}
	@Override
	public void actionPerformed(final ActionEvent ae) {
		final String cmd = SwingFactory.getActionCommand(ae);
		if ("sv".equals(cmd)) {
			Save(session);
		} else if ("ld".equals(cmd)) {
			Load(session);
		} else if ("svp".equals(cmd)) {
			session.getOptions().savePref();
		} else if ("ldp".equals(cmd)) {
			session.getOptions().loadPref();
		} else if (ae.getSource()==getList()){
			if (getList().getItemCount()>0) {
				final UriFormatIterator pTmp = (UriFormatIterator)getList().getSelectedItem();
				if (pTmp==null) {
					log.debug("Action", "Null item in list!");
					return;
				}
				log.debug("Action", "Selected item: "+pTmp);
				siteURL.setText(pTmp.site.toString());
				synGAL.setText(do_str.repeat('0',pTmp.getPaddingLength()));
				staGAL.setText(Integer.toString(pTmp.getStart()));
				endGAL.setText(Integer.toString(pTmp.getEnd()));
				incGAL.setText(Integer.toString(pTmp.getIncrement()));
			}
		} else if ("ad".equals(cmd)) {
			getList().addItem(new UriFormatIterator(siteURL.getText(),synGAL.getText().length(),staGAL.getValueAsInt(),endGAL.getValueAsInt(), incGAL.getValueAsInt()));
			state.setText("Added " + getList().getItemAt(getList().getItemCount()-1));
			log.debug("Action", "Added "+siteURL.getText()+" "+synGAL.getText()+" "+staGAL.getValue()+" "+endGAL.getValue()+" "+incGAL.getValue());
		} else if ("start".equals(cmd)) {
			try {
				session = new Session(this, session.getOptions());
				new Thread(session).start();
				((JButton)ae.getSource()).setText("Stop");
				((JButton)ae.getSource()).setActionCommand("stop");
				log.debug("Action", "Starting.");
			} catch (final Exception e) {
				log.error("FATAL", e);
				reset();
			}
		} else if ("stop".equals(cmd)) {
			if (SJOptionPane.getConfirmation(frame, "Are you sure you want to stop?")) {
				if (session != null) {
					session.stop();
				}
				state.setText("Stopping");
				log.debug("Action", "Stopping.");
			}
		} else if ("rem".equals(cmd)) {
			log.debug("Action", "Removing: "+getList().getSelectedItem());
			getList().removeItemAt(getList().getSelectedIndex());
		} else if ("cl".equals(cmd)) {
			if (SJOptionPane.getConfirmation(frame, "Are you sure you want to remove ALL the items?")) {
				log.debug("Action", "Cleared list");
				getList().removeAllItems();
				Links.clear();
			}
		} else if (ae.getActionCommand().equals(SMenuBar.getMenuCommand(SMenuBar.FILE_QUIT))) {
			System.exit(0);
		} else if ("sf".equals(cmd)) {
			SaveFiles();
		} else if ("lf".equals(cmd)) {
			LoadFiles();
		}
	}
	private boolean SaveFiles() {
		File save = SwingFactory.getFileName(frame, savedFileListFilter);
		if (save == null) {
			log.information("Save Files failed: user canceled dialog box");
			return false;
		}
		if (!save.getAbsolutePath().endsWith(".msf")) {
			save = new File(save.getAbsolutePath()+".msf");
		}
		PrintWriter out = null;
		try {
			out = new PrintWriter(save);
			for (final Map.Entry<CIString, List<Uri>> entry : Links.entrySet()) {
				for (final Uri cUrl : entry.getValue()) {
					out.print(cUrl+"\t"+entry.getKey().toString()+"\n");
				}//end inner for
			}//end for
		} catch (final Exception e) {
			log.error("SaveFiles()",e);
			return false;
		} finally {
			FileUtil.close(out);
		}
		return true;
	}
	private boolean LoadFiles() {
		final File save = SwingFactory.getFileName(frame, savedFileListFilter);
		if (save == null) {
			log.information("Load Files failed: user cancelled dialog box.");
			return false;
		}
		try {
			String sTmp;
			String[] sTmp2;
			final RandomAccessFile in = new RandomAccessFile(save, "r");
			try {
				while ((sTmp = in.readLine())!=null) {
					sTmp2 = sTmp.split("\t");
					if (sTmp2.length == 2) {
						Links.put(sTmp2[1], new Uri(sTmp2[0]));
					} else {
						Links.put(null, new Uri(sTmp2[0]));
					}
				}
			} catch (final EOFException e) {
				log.error("LoadFiles()", e);
				return false;
			} finally {
				FileUtil.close(in);
			}
		} catch (final Exception e) {
			log.error("Load Error",e);
			return false;
		}
		return true;
	}
	boolean Save(final Session ses) {
		File save = SwingFactory.getFileName(frame, saveFilter);
		if (save == null) {
			log.information("Save Session failed: user cancelled dialog box.");
			return false;
		}
		if (!save.getAbsolutePath().endsWith(".msl")) {
			save = new File(save.getAbsolutePath()+".msl");
		}
		final String saveFile = save.toString();
		session.getOptions().setOption(OptionKeys.snatcher_saveFile, saveFile);
		session.getOptions().setOption(OptionKeys.snatcher_saveFolder, saveFile.substring(0, saveFile.lastIndexOf(File.separatorChar)+1));
		log.debug("save", saveFile);
		try {
			session.getOptions().savePref(save.getAbsolutePath());
			final PrintWriter out = new PrintWriter(save);
			out.println("4\r\n");
			UriFormatIterator pTmp = null;
			for (int i = 0;i<getList().getItemCount();i++) {
				pTmp = getList().getItemAt(i);
				log.debug("save", "Site: "+pTmp);
				out.print(pTmp.toString()+"\r\n");
			}
			log.debug("save", "end save");
			FileUtil.close(out);
		} catch (final Exception e) {
			log.error("Save Error",e);
			return false;
		}
		return true;
	}
	private void Load(final Session session) {
		final File save = SwingFactory.getFileName(frame, saveFilter);
		if (save == null)
			return;
		String sTmp = null;
		String[] sTmp2 = null;
		session.getOptions().loadPref(save.getAbsolutePath());
		log.debug("load", save.getAbsolutePath());
		final String saveFile = save.toString();
		session.getOptions().setOption(OptionKeys.snatcher_saveFile, saveFile);
		session.getOptions().setOption(OptionKeys.snatcher_saveFolder, saveFile.substring(0, saveFile.lastIndexOf(File.separatorChar)+1));
		try {
			final RandomAccessFile in = new RandomAccessFile(save, "r");
			try {
				sTmp= in.readLine();
				if("4".equals(sTmp)) {
					while ((sTmp = in.readLine())!=null){
						if(sTmp.isEmpty()) continue;
						getList().addItem(UriFormatIterator.parse(sTmp));
						log.debug("loaded item", sTmp);
					}
				}else if (sTmp!=null)
					do {
						sTmp2 = sTmp.split("\t");
						siteURL.setText(sTmp2[0]);
						if (sTmp2.length>1) {
							synGAL.setText(sTmp2[1]);
							staGAL.setText(sTmp2[2]);
							endGAL.setText(sTmp2[3]);
							incGAL.setText(sTmp2[4]);
						} else {
							log.warning("load", "Only "+sTmp2.length+" args on line. Expected 5.");
							for (short i = 0; i<sTmp2.length; i++) {
								log.warning("load", "arg"+i+": "+sTmp2[i]);
							}
							synGAL.setText("0");
							staGAL.setText("0");
							endGAL.setText("0");
							incGAL.setText("1");
						}
						actionPerformed(new ActionEvent(Add,0,Add.getActionCommand()));
					}while ((sTmp = in.readLine())!=null);
			} catch (final EOFException e) {
				log.error("load()", e);
			}
			FileUtil.close(in);
		} catch (final Exception e) {
			log.error("Load Error",e);
		}
	}

	/**
	 * @return the list
	 */
	public JComboBox<UriFormatIterator> getList() {
		return list;
	}
	/**
	 * @param msg
	 */
	void setState(final String msg) {
		state.setText(msg);
	}
	/**
	 * @param msg
	 */
	void setStateExt(final String msg) {
		stateExt.setText(msg);
	}
	/**
	 * @param msg
	 */
	void setTopProgressBarText(final String msg) {
		totPBar.setString(msg);
	}
	/**
	 * @param msg
	 */
	void setBottomProgressBarText(final String msg) {
		curPBar.setString(msg);
	}
	void reset() {
		for (int i = 0;i<list.getItemCount();i++) {
			(list.getItemAt(i)).reset();
		}
		GO.setText("Snatch");
		GO.setActionCommand("start");
	}
	void remove(final UriFormatIterator pf){
		getList().removeItem(pf);
		log.information("Auto-Removing",pf.toString());
	}
}

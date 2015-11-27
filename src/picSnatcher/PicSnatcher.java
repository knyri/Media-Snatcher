package picSnatcher;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import simple.gui.container.RadioButtonGroup;
import simple.gui.factory.SJPanel;
import simple.io.RWUtil;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

/**
 * Old. Kept for nostalgia.
 * @author Ken Pierce
 */
public class PicSnatcher implements ActionListener {
	/*
	JButton
	JList
	JLabel
	JTextField
	URL
	File
	BufferedInputStream
	OutputStream
	*/
	/*
	1=syntax
	2=GAL# Format(#,##,###,ect)
	3=PIC# Format(#,##,###,ect)
	4=GStart 5=GEnd
	6=PStart 7=PEnd
	------------------------------
	|[1         ]                |
	|----------------------------|
	||--------------------------||
	|||[2   ][3   ]            |||
	|||------------------------|||
	|||[4   ][5   ][6   ][7   ]|||
	Syntax examples
	http://www.example.com/images/galleries/Met/^gal^^pic^.jpg
	http://www.example.com/images/galleries/Met^gal^/^gal^-^pic^.jpg
	*/
	JTextField synURL = new JTextField(30);
	JTextField synGAL = new JTextField("0",10);
	JTextField synPIC = new JTextField("0",10);
	JTextField staPIC = new JTextField("0",5);
	JTextField endPIC = new JTextField("0",5);
	JTextField staGAL = new JTextField("0",5);
	JTextField endGAL = new JTextField("0",5);
	JComboBox<PictureFormat> list = new JComboBox<PictureFormat>();
	JButton GO = new JButton("Snatch");
	JLabel status = new JLabel("Waiting for you");
	JLabel status2 = new JLabel("^_^");
	JLabel dlLabel = new JLabel("(0/0)");
	JProgressBar dlPBar = new JProgressBar(0,0);
	JCheckBox skip = new JCheckBox("Skip Errors",false);
	static final Log d = LogFactory.getLogFor(PicSnatcher.class);
	download dl = new download();
	RadioButtonGroup RBG = new RadioButtonGroup();
	public PicSnatcher() throws FileNotFoundException {
		HttpURLConnection.setFollowRedirects(true);
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container con = frame.getContentPane();
		BoxLayout blTmp = null;
		JPanel P1 = new JPanel();//new GridLayout(6,1));
		blTmp = new BoxLayout(P1,BoxLayout.Y_AXIS);
		P1.setLayout(blTmp);
		JPanel P2  = new JPanel(new GridLayout(1,1));
		JPanel P20 = new JPanel();
		JPanel P3  = new JPanel(new GridLayout(1,2));
		JPanel P4  = new JPanel(new GridLayout(1,4));
		JPanel P5  = new JPanel(new GridLayout(2,1));
		JPanel P50 = new JPanel(new GridLayout(1,1));
		JPanel P51 = new JPanel(new BorderLayout());
		JPanel P6  = new JPanel(new GridLayout(1,6));
		JPanel P60 = new JPanel(new GridLayout(2,1));
		JRadioButton rbFtp = new JRadioButton("ftp://", false);
		JRadioButton rbHttp = new JRadioButton("http://", true);
		JButton Add = new JButton("Add");
		JButton rem = new JButton("Remove");
		JButton Save = new JButton("Save");
		JButton Load = new JButton("Load");
		JButton skip = new JButton("Skip");
		Save.addActionListener(this);
		Load.addActionListener(this);
		GO.addActionListener(this);
		rem.addActionListener(this);
		Add.addActionListener(this);
		list.addActionListener(this);
		skip.addActionListener(this);
		this.skip.addActionListener(this);
		RBG.add(rbFtp);
		RBG.add(rbHttp);
		//P4 and P3 add to P2, add synURL and P2 to P1
		P20.add(rbFtp);
		P20.add(rbHttp);
		P20.add(SJPanel.makeLabeledPanel(synURL,"URL Syntax"));
		P2.add(P20);
		P1.add(P2);
		P3.add(SJPanel.makeLabeledPanel(synGAL, "Gal Syntax"));
		P3.add(SJPanel.makeLabeledPanel(synPIC, "Pic Syntax"));
		P4.add(SJPanel.makeLabeledPanel(staGAL, "Gal Start"));
		P4.add(SJPanel.makeLabeledPanel(endGAL, "Gal End"));
		P4.add(SJPanel.makeLabeledPanel(staPIC, "Pic Start"));
		P4.add(SJPanel.makeLabeledPanel(endPIC, "Pic End"));
		P1.add(P3);
		P1.add(P4);
		P50.add(SJPanel.makeLabeledPanel(status, "Status: "));
		P51.add(dlPBar);
		P51.add(dlLabel,BorderLayout.EAST);
		P5.add(P50);
		P5.add(SJPanel.makeLabeledPanel(P51, "DL Progress: "));
		P1.add(P5);
		P6.add(GO);
		P60.add(skip);
		P60.add(this.skip);
		P6.add(P60);
		P6.add(Add);
		P6.add(rem);
		P6.add(Save);
		P6.add(Load);
		P1.add(list);
		P1.add(SJPanel.wrapInJPanel(status2));
		con.add(P1);
		con.add(P6,BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
	}
	private PicSnatcher(String args[]) throws FileNotFoundException {
		this();
		int tmp = 0;
		for (int i = 0;i<(args.length%6);i++) {
			tmp = (i*6)+i;
			list.addItem(new PictureFormat(args[0+tmp],args[1+tmp],args[2+tmp],Integer.parseInt(args[3+tmp]),Integer.parseInt(args[5+tmp]),Integer.parseInt(args[4+tmp]),Integer.parseInt(args[6+tmp])));
		}
	}
	@Override
	public void actionPerformed (ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			if (e.getSource()==GO) {
				if (e.getActionCommand().equals("Snatch")) {
					dl = null;
					dl = new download();
					dl.skiperr = skip.isSelected();
					dl.start();
					GO.setText("Stop");
				} else if (e.getActionCommand().equals("Stop")) {
					dl.run=false;
					status.setText("Stopping...(will stop after current Item finishes)");
				}
			} else if (((JButton)e.getSource()).getActionCommand().equals("Add")){
				list.addItem(new PictureFormat(RBG.getSelected().getActionCommand()+synURL.getText(),synGAL.getText(),synPIC.getText(),Integer.parseInt(staGAL.getText()),Integer.parseInt(staPIC.getText()),Integer.parseInt(endGAL.getText()),Integer.parseInt(endPIC.getText())));
			} else if (((JButton)e.getSource()).getActionCommand().equals("Remove")){
				list.removeItemAt(list.getSelectedIndex());
			} else if (((JButton)e.getSource()).getActionCommand().equals("Save")){
				Save();
			} else if (((JButton)e.getSource()).getActionCommand().equals("Load")){
				Load();
			} else if (((JButton)e.getSource()).getActionCommand().equals("Skip")) {
				dl.skip = true;
			}
		} else if (e.getSource() instanceof JCheckBox) {
			dl.skiperr = skip.isSelected();
		} else {
			if (list.getItemCount()>0) {
				if (list.getSelectedItem()!=null) {
					PictureFormat pTmp = (PictureFormat)list.getSelectedItem();
					if (pTmp.syntax.startsWith("http://")) {
						RBG.setSelected(1);
						synURL.setText(pTmp.syntax.substring(7));
					} else if (pTmp.syntax.startsWith("ftp://")) {
						RBG.setSelected(0);
						synURL.setText(pTmp.syntax.substring(6));
					}
					synGAL.setText(pTmp.Gfmt);
					synPIC.setText(pTmp.Pfmt);
					staPIC.setText(Integer.toString(pTmp.Pstart));
					endPIC.setText(Integer.toString(pTmp.Pend));
					staGAL.setText(Integer.toString(pTmp.Gstart));
					endGAL.setText(Integer.toString(pTmp.Gend));
				}
			}
		}
	}
	private void Save() {
		File save = new File("savePicList.txt");
		FileWriter out;
		try{
			out=new FileWriter(save, false);
			PictureFormat pTmp = null;
			for (int i = 0;i<list.getItemCount();i++) {
				pTmp = list.getItemAt(i);
				out.write(pTmp.syntax+"\t");
				out.write(pTmp.Gfmt+"\t");
				out.write(pTmp.Pfmt+"\t");
				out.write(Integer.toString(pTmp.Gstart)+"\t");
				out.write(Integer.toString(pTmp.Gend)+"\t");
				out.write(Integer.toString(pTmp.Pstart)+"\t");
				out.write(Integer.toString(pTmp.Pend)+"\n");
			}
			out.close();
		}catch(IOException e){
			//log.error(e);
			//e.printStackTrace();
		}
	}
	private void Load() {
		File save = new File("savePicList.txt");
		FileReader in;
		try{
			in=new FileReader(save);
			String sTmp = null;
			try {
				String[] eTmp = null;
				while ((sTmp = RWUtil.readUntil(in,'\n'))!=null) {
					eTmp = sTmp.split("\t");
					if (eTmp.length<2) {break;}
					list.addItem(new PictureFormat(eTmp[0],eTmp[1],eTmp[2],Integer.parseInt(eTmp[3]),Integer.parseInt(eTmp[5]),Integer.parseInt(eTmp[4]),Integer.parseInt(eTmp[6])));
				}
			} catch (Exception e) {
				d.debug("Load Error",e.toString());
			} finally {try{
				in.close();
			}catch(IOException e){
				//log.error(e);
				//e.printStackTrace();
			}}
		}catch(FileNotFoundException e1){
			//log.error(e1);
			//e1.printStackTrace();
		}
	}
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length>0) {
			new PicSnatcher(args);
		} else {
			new PicSnatcher();
		}
	}
	public class PictureFormat {
		final int Gstart;
		final int Pstart;
		int Gnum = 0;
		int Pnum = 0;
		int Gend = 0;
		int Pend = 0;
		String syntax;
		String Gfmt;
		String Pfmt;
		public PictureFormat(String syntax,String Gfmt,String Pfmt,int Gstart,int Pstart,int Gend,int Pend) {
			this.syntax = syntax;
			this.Gfmt = Gfmt;
			this.Pfmt = Pfmt;
			Gnum = Gstart;
			this.Gstart = Gstart;
			Pnum = Pstart;
			this.Pstart = Pstart;
			this.Gend = Gend;
			this.Pend = Pend;
		}
		public void reset() {Gnum = Gstart;Pnum = Pstart;}
		public URL next() {
			if (Pnum>Pend) {Gnum++;Pnum=Pstart;}
			if (Gnum>Gend) {return null;}
			StringBuffer SBtmp = new StringBuffer(Integer.toString(Pnum));
			int Itmp = Pfmt.length() - SBtmp.length();
			for (int i=0;i<Itmp;i++) {
				SBtmp.insert(0,"0");
			}
			String link = syntax;
			link = link.replaceAll("\\^pic\\^",new String(SBtmp));
			link = link.replaceAll("\\^pic",new String(SBtmp));
			SBtmp = new StringBuffer(Integer.toString(Gnum));
			Itmp = Gfmt.length() - SBtmp.length();
			for (int i=0;i<Itmp;i++) {
				SBtmp.insert(0,"0");
			}
			link = link.replaceAll("\\^gal\\^",new String(SBtmp));
			link = link.replaceAll("\\^gal",new String(SBtmp));
			Pnum++;
			try {
				return new URL(link);
			} catch(Exception e) {return null;}
		}
		@Override
		public String toString() {return syntax;}
	}
	private class download extends Thread {
		boolean run = false;
		boolean skip = false;
		boolean skiperr = false;
		public download() {}
		/*
		(Gend-Gstart+1)*(Pend-Pstart+1)
		(Gstart-Gnum+1)*(Pend-Pstart+1)+(Pstart-Pnum+1)
		*/
		@Override
		public void run() {
			run = true;
			int tot = 0;
			int totCount = 0;
			PictureFormat pic = null;
			for (int i=0;i<list.getItemCount();i++) {
				pic = list.getItemAt(i);
				tot += (pic.Gend-pic.Gstart+1)*(pic.Pend-pic.Pstart+1);
			}
			for (int i=0;i<list.getItemCount();i++) {
				pic = list.getItemAt(i);
				pic.reset();
				list.setSelectedIndex(i);
				URL tmp = null;
				String fName = null;
				File fTmp = null;
				File fDir = null;
				URLConnection con = null;
				int Ptot = (pic.Gend-pic.Gstart+1)*(pic.Pend-pic.Pstart+1);
				int Pcur = 0;
				int size = 0;
				while ((run)&&((tmp=pic.next())!=null)) {
					size = 0;
					totCount++;
					Pcur++;
					d.debug("URL",tmp.toString());
					if ((!tmp.getHost().endsWith("/"))&&(!tmp.getFile().startsWith("/"))) {
						fName = tmp.getHost()+"/"+tmp.getFile();
					} else if ((tmp.getHost().endsWith("/"))&&(tmp.getFile().startsWith("/"))){
						fName = tmp.getHost()+tmp.getFile().substring(1);
					} else {
						fName = tmp.getHost()+tmp.getFile();
					}
					fName = fName.replaceAll("/","\\\\");
					fTmp = new File(fName.substring(0,fName.lastIndexOf("\\")));
					try {
						status.setText("Connecting "+tmp.getHost());
						con = tmp.openConnection();
						if (con instanceof HttpURLConnection) {
							//Get's alternate file name if supplied by server
							if (((HttpURLConnection)con).getHeaderField("Content-Disposition") != null) {
								String fnametmp = ((HttpURLConnection)con).getHeaderField("Content-Disposition");
								fnametmp = fnametmp.substring(fnametmp.indexOf("filename=\"")+10);
								fName = fnametmp.substring(0, fnametmp.indexOf("\""));
								fnametmp = null;
							}
						}
						FileOutputStream out = null;
						fDir = fTmp;//currently has directory only
						fTmp = new File(fName);//Make the file with name
						if (fTmp.exists()) {d.debug("Error","Already Exists");continue;}
						fTmp = new File(fName+".tmp");
						dlPBar.setMaximum(con.getContentLength());
						status2.setText("Current Item("+Integer.toString(Pcur)+"/"+Integer.toString(Ptot)+") Total Item("+Integer.toString(totCount)+"/"+Integer.toString(tot)+")");
						if (dlPBar.getMaximum()==-1) {d.debug("Error","HTTP 404 Not Found");continue;}
						//if (con.getContentLength()<5000) {d.debug("Error","File to small "+con.getContentLength()+" < 5Kb");continue;}
						BufferedInputStream in = new BufferedInputStream(con.getInputStream());
						//Make the directories and the file
						fDir.mkdirs();
						fTmp.createNewFile();
						out = new FileOutputStream(fTmp);
						byte[] b = new byte[1024*6];
						if (con.getContentLength()<=5000) {//in case we remove the restriction
							b = new byte[con.getContentLength()];
						}
						status.setText("Downloading "+tmp.getFile());//update status
						int count = 0;
						while (((count=in.read(b))!=-1)&&(!skip)) {
							size+=count;
							dlPBar.setValue(size);
							dlLabel.setText("("+size+"/"+dlPBar.getMaximum()+")");
							out.write(b, 0, count);
						}
						in.close();
						out.flush();
						out.close();
						if (!skip) {
							if (fTmp.length()<con.getContentLength()-15) {
								if (!skiperr) {
									totCount-=Pcur;
									Pcur=0;
									pic.reset();
									d.debug("Retry", "Didn't get it all, trying again.");
								}
							} else {
								fTmp.renameTo(new File(fName));
							}
						} else {
							skip = false;
						}
					}catch(FileNotFoundException fnfe){
						d.information("File not on server.");
					}
					catch(Exception ex) {
						d.debug("Error",ex);
					}
				}
			}
			run = false;
			status.setText("Done");
			GO.setText("Snatch");
		}
	}
}
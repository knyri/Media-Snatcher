/**
 *
 */
package picSnatcher.mediaSnatcher.extension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import picSnatcher.mediaSnatcher.Options;
import simple.CIString;
import simple.gui.factory.SJPanel;
import simple.parser.ml.InlineLooseParser;
import simple.parser.ml.Page;
import simple.parser.ml.Tag;
import simple.util.CIHashtable;

/**
 * All names are case insensitive.
 * It's up to the extension to manage dependency. That is, disabling sub-checkboxes if the parent is unchecked.
 * Sample XML:
 * &lt;OptionPanel&gt;
 * 	&lt;text name="username" tooltip="Enter your username" label="Username:" value="" /&gt;
 * 	&lt;text name="password" tooltip="Enter you password" label="Password:" value="" /&gt;
 * &lt;/OptionPanel&gt;
 * <hr>
 * <br>Created: Jan 21, 2011
 * @author Kenneth Pierce
 */
public final class OptionPanel {
	private final String title;
	private final JPanel center = SJPanel.makeBoxLayoutPanelY();
	private final CIHashtable<JComponent> items = new CIHashtable<JComponent>();
	private LinkedList<OptionPanelListener> listeners= new LinkedList<>();
	public void addListener(OptionPanelListener listener){
		listeners.add(listener);
	}
	public void fireClosed(){
		for(OptionPanelListener listener : listeners){
			try{
				listener.panelClosed(this);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	public void fireOpened(){
		for(OptionPanelListener listener : listeners){
			try{
				listener.panelOpened(this);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	public OptionPanel(String title) {
		this.title= title;
	}
	public JTextArea getTextArea(String name){
		return (JTextArea) getComponent(name);
	}
	public JCheckBox getCheckBox(String name){
		return (JCheckBox) getComponent(name);
	}
	public JTextField getTextField(String name){
		return (JTextField) getComponent(name);
	}
	public JComponent getComponent(String name){
		return items.get(name);
	}
	/**
	 * Builds the option page based on the XML source
	 * @param title
	 * @param source
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 */
	public OptionPanel(String title, File source){
		this(title);
		try(FileReader in= new FileReader(source)){
			Page page= InlineLooseParser.parse(in);
			Tag tag= page.getRoot("OptionPanel");
			if(tag == null)
				throw new IllegalArgumentException("Not a valid OptionPanel document.");
			for(Tag t : tag){
				if(t.getName().equals(Tag.CDATA) || t.getName().equals(Tag.HTMLCOMM)){
					continue;
				}
				build(t);
			}
		}catch(IOException | ParseException e){
			JTextArea error= new JTextArea();
			error.append("ERROR: Failed to load options\n");
			error.append(e.toString());
			center.add(error);
		}
	}
	/**
	 * Builds the option page based on the XML source
	 * @param title
	 * @param source
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 */
	public OptionPanel(String title, InputStream source){
		this(title);
		try(InputStreamReader in= new InputStreamReader(source)){
			Page page= InlineLooseParser.parse(in);
			Tag tag= page.getRoot("OptionPanel");
			if(tag == null)
				throw new IllegalArgumentException("Not a valid OptionPanel document.");
			for(Tag t : tag){
				if(t.getName().equals(Tag.CDATA) || t.getName().equals(Tag.HTMLCOMM)){
					continue;
				}
				build(t);
			}
		}catch(Exception e){
			JTextArea error= new JTextArea();
			error.append("ERROR: Failed to load options\n");
			error.append(e.toString());
			center.add(error);
		}
	}
	private final CIString
		atrValue= new CIString("value").intern(),
		tagLabel= new CIString("label").intern(),
		tagTextArea= new CIString("textarea").intern(),
		tagText= new CIString("text").intern(),
		tagCheck= new CIString("check").intern(),
		atrName= new CIString("name").intern(),
		atrTooltip= new CIString("tooltip").intern(),
		atrLabel= tagLabel,
		atrIndent= new CIString("indent").intern()
		;

	private void build(Tag tag){
		if(tag.getName().equals(tagLabel)){
			if(!tag.hasProperty(atrValue)){
				throw new IllegalStateException("value is a required attribute for label.");
			}
			addLabel(tag.getProperty(atrValue));
			return;
		}


		final String name, label, tooltip;
		int indent= 0;

		name= tag.getProperty(atrName);
		if(name == null){
			throw new IllegalStateException("name is a required attribute.");
		}

		label= tag.getProperty(atrLabel);
		if(label == null){
			throw new IllegalStateException("label is a required attribute.");
		}

		if(tag.hasProperty(atrIndent)){
			indent= Integer.parseInt(tag.getProperty(atrIndent),10);
		}

		tooltip= tag.getProperty(atrTooltip);

		if(tag.getName().equals(tagTextArea)){
			addTextArea(name, label, tooltip, indent);
		}else if(tag.getName().equals(tagText)){
			addTextField(name, label, tooltip, indent);
		}else if(tag.getName().equals(tagCheck)){
			addCheckBox(name, label, tooltip, indent);
		}
		if(tag.getProperty(atrValue) != null){
			this.setItemValue(name,tag.getProperty(atrValue));
		}
		for(Tag t : tag){
			build(t);
		}
	}
	public String getTitle(){
		return title;
	}
	/**
	 * Returns the center panel
	 * @return The center panel
	 */
	public JPanel getPanel() {return center;}
	/**
	 * @param name Name used to get this value
	 * @param display Label
	 * @param toolTip Tooltip
	 */
	public void addCheckBox(String name, String display, String toolTip, int indent) {
		JComponent c = new JCheckBox(display);
		c.setToolTipText(toolTip);
		items.put(name, c);
		add(c, indent);
	}
	/**
	 * @param display Label
	 */
	public void addLabel(String display) {
		add(new JLabel(display), 0);
	}
	/**
	 * @param name Name used to get this value
	 * @param display Label
	 * @param toolTip Tooltip
	 */
	public void addTextField(String name, String display, String toolTip, int indent) {
		JComponent c = new JTextField();
		c.setToolTipText(toolTip);
		items.put(name, c);
		JPanel tmp = SJPanel.makeBoxLayoutPanelX();
		tmp.add(new JLabel(display));
		tmp.add(c);
		add(tmp, indent);
	}
	/**
	 * @param name Name used to get this value
	 * @param display Label
	 * @param toolTip Tooltip
	 */
	public void addTextArea(String name, String display, String toolTip, int indent) {
		JComponent c = new JTextArea();
		c.setToolTipText(toolTip);
		items.put(name, c);
		c= new JScrollPane(c);
		c.setSize(300,300);
		JComponent tmp = SJPanel.makeBoxLayoutPanelY();
		tmp.add(new JLabel(display));
		tmp.add(c);
		add(tmp, indent);
	}
	private void add(JComponent c, int indent){
		center.add(createLine(c, indent));
	}
	/**
	 * @param name Name of the element
	 * @param value Value to set
	 */
	public void setItemValue(String name, String value) {
		JComponent c = items.get(name);
		if (c==null)
			return;
		if (c instanceof JCheckBox)
			((JCheckBox)c).setSelected(Options.getTF(value));
		if (c instanceof JTextComponent)
			((JTextComponent)c).setText(value);
	}
	/**
	 * @param name
	 * @return Null if the item is not found. True will be {@link Options#TRUE}. False will be {@link Options#FALSE}
	 */
	public String getItemValue(String name) {
		JComponent c = items.get(name);
//		System.out.print(name + " ");
//		System.out.println(c);
		if (c==null){
			return null;
		}
		if (c instanceof JCheckBox){
			return Options.getTF(((JCheckBox)c).isSelected());
		}
		if (c instanceof JTextComponent){
			return ((JTextComponent)c).getText();
		}
		return null;
	}
	/**
	 * @return An enumeration of all the keys
	 */
	public Set<String> getKeys() {
		return new Set<String>() {
			final Set<CIString> keys = items.keySet();
			@Override
			public boolean add(String arg0){
				return false;
			}

			@Override
			public boolean addAll(Collection<? extends String> arg0){
				return false;
			}

			@Override
			public void clear(){}

			@Override
			public boolean contains(Object arg0){
				return false;
			}

			@Override
			public boolean containsAll(Collection<?> arg0){
				return false;
			}

			@Override
			public boolean isEmpty(){
				return keys.isEmpty();
			}

			@Override
			public Iterator<String> iterator(){
				return new Iterator<String>(){
					Iterator<CIString> strings= keys.iterator();
					@Override
					public boolean hasNext(){
						return strings.hasNext();
					}

					@Override
					public String next(){
						return strings.next().toString();
					}

				};
			}

			@Override
			public boolean remove(Object arg0){
				return false;
			}

			@Override
			public boolean removeAll(Collection<?> arg0){
				return false;
			}

			@Override
			public boolean retainAll(Collection<?> arg0){
				return false;
			}

			@Override
			public int size(){
				return 0;
			}

			@Override
			public Object[] toArray(){
				return null;
			}

			@Override
			public <T>T[] toArray(T[] arg0){
				return null;
			}};
	}

	private static Box createLine(JComponent comp,int indent){
		Box box=Box.createHorizontalBox();
		if(indent>0)box.add(Box.createHorizontalStrut(25*indent));
		box.add(comp);
		box.add(Box.createGlue());
		return box;
	}
}

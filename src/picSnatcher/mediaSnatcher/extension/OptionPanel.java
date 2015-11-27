/**
 *
 */
package picSnatcher.mediaSnatcher.extension;

import java.awt.FlowLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
 * <hr>
 * <br>Created: Jan 21, 2011
 * @author Kenneth Pierce
 */
public final class OptionPanel {
	private final String title;
	private final JPanel center = SJPanel.makeBoxLayoutPanelY();
	private final CIHashtable<JComponent> items = new CIHashtable<JComponent>();
	public OptionPanel(String title) {
		this.title= title;
	}
	/**
	 * Builds the option page based on the XML source
	 * @param title
	 * @param source
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 */
	public OptionPanel(String title, File source) throws FileNotFoundException, IOException, ParseException{
		this(title);
		try(FileReader in= new FileReader(source)){
			Page page= InlineLooseParser.parse(in);
			Tag tag= page.getRoot("OptionPanel");
			if(tag == null)
				throw new IllegalArgumentException("Not a valid OptionPanel document.");
			for(Tag t : tag){
				build(t);
			}
		}
	}
	private final CIString
		atrName= new CIString("name").intern(),
		atrTooltip= new CIString("tooltip").intern(),
		atrLabel= new CIString("label").intern(),
		atrValue= new CIString("value").intern();

	private void build(Tag tag){
		final String name, label, tooltip;
		name= tag.getProperty(atrName);
		if(name == null){
			throw new IllegalStateException("name is a required attribute.");
		}

		label= tag.getProperty(atrLabel);
		if(label == null){
			throw new IllegalStateException("label is a required attribute.");
		}

		tooltip= tag.getProperty(atrTooltip);

		if(tag.getName().equals("text")){
			addTextField(name, label, tooltip);
		}else if(tag.getName().equals("check")){
			if(!tag.getParent().getName().equals("optionpanel")){
				addSubCheckBox(name,label,tooltip,tag.getParent().getProperty(atrName));
			}else{
				addCheckBox(name, label, tooltip);
			}
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
	public void addCheckBox(String name, String display, String toolTip) {
		JComponent c = new JCheckBox(display);
		c.setToolTipText(toolTip);
		items.put(name, c);
		center.add(c);
	}
	/**
	 * @param name Name used to get this value
	 * @param display Label
	 * @param toolTip Tooltip
	 * @param parentName Name of the parent checkbox
	 */
	public void addSubCheckBox(String name, String display, String toolTip, String parentName) {
		JComponent c = new JCheckBox(display);
		c.setToolTipText(toolTip);
		items.put(name, c);
		JPanel tmp = new JPanel(new FlowLayout(FlowLayout.CENTER));
		tmp.add(c);
		center.add(tmp);
	}
	/**
	 * @param name Name used to get this value
	 * @param display Label
	 * @param toolTip Tooltip
	 */
	public void addTextField(String name, String display, String toolTip) {
		JComponent c = new JTextField();
		c.setToolTipText(toolTip);
		items.put(name, c);
		JPanel tmp = SJPanel.makeBoxLayoutPanelX();
		tmp.add(new JLabel(display));
		center.add(tmp);
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
		if (c instanceof JTextField)
			((JTextField)c).setText(value);
	}
	/**
	 * @param name
	 * @return Null if the item is not found. True will be {@link Options#TRUE}. False will be {@link Options#FALSE}
	 */
	public String getItemValue(String name) {
		JComponent c = items.get(name);
		if (c==null)
			return null;
		if (c instanceof JCheckBox)
			return Options.getTF(((JCheckBox)c).isSelected());
		if (c instanceof JTextField)
			return ((JTextField)c).getText();
		return null;
	}
	/**
	 * @return An enumeration of all the keys
	 */
	public Enumeration<String> getKeys() {
		return new Enumeration<String>() {
			final Enumeration<CIString> keys = Collections.enumeration(items.keySet());
			@Override
			public boolean hasMoreElements() {
				return keys.hasMoreElements();
			}

			@Override
			public String nextElement() {
				return keys.nextElement().toString();
			}};
	}
}
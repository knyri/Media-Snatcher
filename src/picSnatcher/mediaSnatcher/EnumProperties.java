package picSnatcher.mediaSnatcher;

import java.util.Properties;

public final class EnumProperties extends Properties{

	private static final long serialVersionUID=-2377299914312844367L;
	public EnumProperties(){}

	public EnumProperties(Properties defaults){
		super(defaults);
	}

	public String getProperty(OptionKeys key,String defaultValue){
		return super.getProperty(key.name(),defaultValue);
	}
	public String getProperty(OptionKeys key){
		return super.getProperty(key.name());
	}
	public synchronized Object setProperty(OptionKeys key,String value){
		return super.setProperty(key.name(),value);
	}

}

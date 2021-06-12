package picSnatcher.mediaSnatcher.extension;

import simple.net.Uri;

public final class Util{

	private Util(){}
	/**
	 * Strips the query and fragment off the URI
	 * @param url
	 * @return
	 */
	public static final String stripExtra(final Uri url) {
		return url.getScheme()+"://"+url.getHost()+url.getPath()+url.getFile();
	}
}

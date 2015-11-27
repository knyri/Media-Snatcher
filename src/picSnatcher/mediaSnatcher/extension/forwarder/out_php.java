/**
 *
 */
package picSnatcher.mediaSnatcher.extension.forwarder;

import picSnatcher.mediaSnatcher.extension.ForwardRemoverFactory;
import simple.net.Uri;

/**	http://.../out.php?p=80&link=pics&url=ht.../35/index.files/b/05.jpg
 * <br>Created: Sep 15, 2010
 * @author Kenneth Pierce
 */
public final class out_php extends ForwardRemover {
	static {
		ForwardRemoverFactory.addHandler(new out_php());
	}
	/* (non-Javadoc)
	 * @see picSnatcher.mediaSnatcher3.ForwardRemover#canHandle(java.lang.String)
	 */
	@Override
	public boolean canHandle(Uri src) {
		if (src.getFile().equals("out.php")) {
			if (src.getQuery("link", "").equals("pic"))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see picSnatcher.mediaSnatcher3.ForwardRemover#process(java.lang.String)
	 */
	@Override
	public String process(Uri src) {
		return src.getQuery("url");
	}

}

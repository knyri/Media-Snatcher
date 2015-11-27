/**
 *
 */
package picSnatcher.mediaSnatcher.extension.forwarder;

import simple.net.Uri;

/**
 * <br>Created: Oct 9, 2010
 * @author Kenneth Pierce
 */
public class googleImages extends ForwardRemover {

	/* (non-Javadoc)
	 * @see picSnatcher.mediaSnatcher3.ForwardRemover#canHandle(simple.net.UriParser)
	 */
	@Override
	public boolean canHandle(Uri link) {
		if (link.getDomain().equals("google.com"))
			if (link.getFile().equals("imgres"))
				if (link.getQuery("imgurl")!=null)
					return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see picSnatcher.mediaSnatcher3.ForwardRemover#process(simple.net.UriParser)
	 */
	@Override
	public String process(Uri link) {
		return link.getQuery("imgurl");
	}

}

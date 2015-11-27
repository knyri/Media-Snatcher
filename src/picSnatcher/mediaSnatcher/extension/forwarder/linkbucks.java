/**
 *
 */
package picSnatcher.mediaSnatcher.extension.forwarder;

import simple.net.Uri;

/**
 * <br>Created: Oct 9, 2010
 * @author Kenneth Pierce
 */
public final class linkbucks extends ForwardRemover {

	/* (non-Javadoc)
	 * @see picSnatcher.mediaSnatcher3.ForwardRemover#canHandle(simple.net.UriParser)
	 */
	@Override
	public boolean canHandle(Uri link) {
		return link.getDomain().equals("linkbucks.com");
	}

	/* (non-Javadoc)
	 * @see picSnatcher.mediaSnatcher3.ForwardRemover#process(simple.net.UriParser)
	 */
	@Override
	public String process(Uri link) {
		String tmp = link.toString();
		return tmp.substring(tmp.indexOf("/url/")+5);
	}

}

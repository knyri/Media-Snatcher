/**
 *
 */
package picSnatcher.mediaSnatcher.extension.forwarder;

import simple.net.Uri;

/**imagePassn=http://...uryiff/images/6647/..._JK_color_anonib.jpg
 * <br>Created: Sep 15, 2010
 * @author Kenneth Pierce
 */
public final class imagePassn extends ForwardRemover {

	/* (non-Javadoc)
	 * @see picSnatcher.mediaSnatcher3.ForwardRemover#canHandle(java.lang.String)
	 */
	@Override
	public boolean canHandle(Uri link) {
		return link.getQuery().indexOf("imagePassn=")>0;
	}

	/* (non-Javadoc)
	 * @see picSnatcher.mediaSnatcher3.ForwardRemover#process(java.lang.String)
	 */
	@Override
	public String process(Uri link) {
		return link.getQuery("imagePassn");
	}

}

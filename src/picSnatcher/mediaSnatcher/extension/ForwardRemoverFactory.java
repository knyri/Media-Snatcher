/**
 *
 */
package picSnatcher.mediaSnatcher.extension;


import java.util.LinkedList;
import java.util.List;

import picSnatcher.mediaSnatcher.extension.forwarder.ForwardRemover;
import simple.net.Uri;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

/** Translates the forwarder URL into the target URL.<br>
 * Custom forwarders need to be registered before they can be used.
 * <br>Created: Sep 15, 2010
 * @author Kenneth Pierce
 */
public abstract class ForwardRemoverFactory {
	private static final Log log = LogFactory.getLogFor(ForwardRemoverFactory.class);
	private static final List<ForwardRemover> HANDLERS = new LinkedList<>();
	/**
	 * Find a ForwardRemover for the URL
	 * @param link The link to remove the forwarder from.
	 * @return
	 */
	public static final ForwardRemover getHandler(Uri link) {
		ForwardRemover fr = null;
		for (ForwardRemover frct: HANDLERS) {
			if (frct.canHandle(link)) {
				fr = frct;
				break;
			}
		}
		return fr;
	}
	public static final ForwardRemover getHandler(String link) {
		return getHandler(new Uri(link));
	}
	/** Adds a singleton instance to the list.
	 * @param clazz an instance of the remover
	 */
	public static final void addHandler(ForwardRemover clazz) {
		if (HANDLERS.contains(clazz)) return;
		log.information("added forward handler", clazz);
		HANDLERS.add(clazz);
	}
	/**Removes the forwarder, if any, afflicting the link.
	 * @param link URL to be cleansed.
	 * @return The cleansed URL if a handler was found or the original.
	 */
	public static final String removeForwarder(String link) {
		Uri p = new Uri(link);
		ForwardRemover rem = getHandler(link);
		if (rem != null)
			return rem.process(p);
		else
			return link;
	}

}

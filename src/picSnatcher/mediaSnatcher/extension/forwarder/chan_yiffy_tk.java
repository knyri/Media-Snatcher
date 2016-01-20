/**
 *
 */
package picSnatcher.mediaSnatcher.extension.forwarder;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import simple.net.Uri;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

/**
 * <br>Created: Nov 25, 2010
 * @author Kenneth Pierce
 */
public final class chan_yiffy_tk extends ForwardRemover {
	private final static Log log = LogFactory.getLogFor(chan_yiffy_tk.class);
	/* (non-Javadoc)
	 * @see picSnatcher.mediaSnatcher4.ForwardRemover#canHandle(simple.net.UriParser)
	 */
	@Override
	public boolean canHandle(Uri link) {
		if ("chan.yiffy.tk".equalsIgnoreCase(link.getHost()))
			if ("/view/".equalsIgnoreCase(link.getPath()))
				return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see picSnatcher.mediaSnatcher4.ForwardRemover#process(simple.net.UriParser)
	 */
	@Override
	public String process(Uri link) {
		try {
			Socket sock = new Socket(link.getHost(), 80);
			InputStream in = sock.getInputStream();
			StringBuilder buf = new StringBuilder(100);
			int b = 0;
			while ((b=in.read())!=-1) {
				buf.append((char)b);
			}
			if (!sock.isClosed())
				sock.close();
			b = buf.indexOf("Location:");
			if (b==-1) return link.toString();
			b += 10;//10 for the space
			return link.getScheme()+link.getHost()+buf.substring(b, buf.indexOf("\r", b));
		} catch (UnknownHostException e) {
			log.warning(e);
		} catch (IOException e) {
			log.warning(e);
		}
		return link.toString();
	}

}

/**
 *
 */
package picSnatcher.mediaSnatcher;

import simple.net.Uri;

/**
 * <br>Created: Jan 30, 2009
 * @author Kenneth Pierce
 */
public final class Site extends simple.net.http.Site {
	private final int depth, entry;
	public Site(Uri referer, Uri site, int depth) {
		super(referer, site);
		entry = 0;
		this.depth = depth;
	}
	public Site(Uri referer, Uri site, int depth, int entry) {
		super(referer, site);
		this.entry = entry;
		this.depth = depth;
	}
	public int getDepth() {
		return depth;
	}
	public int getFileNum() {
		return depth;
	}
	public int getEntryNum() {
		return entry;
	}
}

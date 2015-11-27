package picSnatcher.mediaSnatcher.extension.forwarder;

import simple.net.Uri;

public abstract class ForwardRemover implements Comparable<ForwardRemover>{

	public ForwardRemover(){
		// TODO Auto-generated constructor stub
	}
	@Override
	public final boolean equals(Object o) {
		if (o==null) return false;
		return this.getClass().getCanonicalName().equals(o.getClass().getCanonicalName());
	}
	@Override
	public final int compareTo(ForwardRemover o) {
		return this.getClass().getCanonicalName().compareTo(o.getClass().getCanonicalName());
	}

	/** Determines if this instance can handle this link.
	 * @param link
	 * @return
	 */
	public abstract boolean canHandle(Uri link);
	/** Processes the URL and returns the target.
	 * @param link URL to be processed
	 * @return The target URL.
	 */
	public abstract String process(Uri link);

}

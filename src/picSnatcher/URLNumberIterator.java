/**
 * 
 */
package picSnatcher;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Vector;

import simple.NumberIterator;

/**
 * <br>Created: Jul 16, 2010
 * @author Kenneth Pierce
 */
public final class URLNumberIterator implements Iterator<URL> {
	private final Vector<String> urlparts = new Vector<String>();
	private final Vector<NumberIterator> numparts = new Vector<NumberIterator>();
	private final String numcache[];
	private final String original;
	private final int total_count;
	public URLNumberIterator(String url) throws ParseException {
		this(url, '[',']');
	}
	public URLNumberIterator(String url, char sep) throws ParseException {
		this(url, sep, sep);
	}
	public URLNumberIterator(String url, char start, char end) throws ParseException {
		original = url;
		int istart = 0, iend = -1;
		istart = url.indexOf(start);
		if (istart < 0) {
			urlparts.add(url);
			numparts.add(new NumberIterator("0...0"));
			numcache = null;
			total_count = 1;
			return;
		}
		int total = 1;
		try {
			NumberIterator tmp;
			do {//extract the parts
				urlparts.add(url.substring(iend+1, istart));
				iend = url.indexOf(end, istart+1);
				tmp = new NumberIterator(url.substring(istart+1, iend));
				total *= tmp.size();
				numparts.add(tmp);
				istart = url.indexOf(start, iend+1);
			} while (istart > 0);
			if (iend < url.length()-1)
				urlparts.add(url.substring(iend+1));
			numparts.trimToSize();
			urlparts.trimToSize();
		} catch (IndexOutOfBoundsException e) {
			if (iend < 0) throw new ParseException("Missing matching "+end+" for "+start+" at index "+istart, istart);
		}
		total_count = total;
		numcache = new String[numparts.size()];
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		for (NumberIterator n : numparts)
			if (n.hasNext()) return true;
		return false;
	}
	public void reset() {
		int i = 0;
		for (NumberIterator n : numparts) {
			n.reset();
			if (n != numparts.lastElement()) {
				numcache[i] = n.next();
			}
			i++;
		}
	}
	private boolean iterate(int i) {
		if (numcache==null) return true;
		NumberIterator tmp;
		if (i<=0) return false;
		tmp = numparts.get(i);
		if (tmp.hasNext()) {
			numcache[i] = tmp.next();
			return true;
		} else if (i > 0 && iterate(i-1)) {
			tmp.reset();
			numcache[i] = tmp.next();
			return true;
		} else {
			return false;
		}
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public URL next() {
		if (!iterate(numparts.size()-1))
			return null;
		StringBuilder ret = new StringBuilder();
		int end = Math.min(numcache.length, urlparts.size());
		for (int i = 0; i < end; i++) {
			ret.append(urlparts.get(i));
			ret.append(numcache[i]);
		}
		if (end < numcache.length)
			ret.append(numcache[numcache.length-1]);
		else if (end < urlparts.size())
			ret.append(urlparts.lastElement());
		try {
			return new URL(ret.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
	}
	public String toString() {
		return original;
	}
	public int size() {
		return total_count;
	}

}

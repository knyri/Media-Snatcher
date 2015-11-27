package picSnatcher;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Pattern;

import simple.html.MimeTypes;
import simple.net.Uri;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

/**
 * Old. Kept for nostalgia
 * @author Ken Pierce
 *
 */
public class LinkSnatcher implements Iterable<URL> {
	final static Log d = LogFactory.getLogFor(LinkSnatcher.class);
	final URL site;
	final Uri urlP;
	final Vector<URL> links = new Vector<URL>();
	final Vector<String> read = new Vector<String>();
	final boolean readDeep;
	int readDepth = 1;
	final Pattern filter;
	public LinkSnatcher(URL page, boolean readDeep) throws FileNotFoundException {
		this(page, readDeep, null);
	}
	public LinkSnatcher(URL page, boolean readDeep, Pattern filter) throws FileNotFoundException {
		site = page;
		this.readDeep = readDeep;
		this.filter = filter;
		urlP = new Uri(page.toString());
	}
	public void setReadDepth(int i) {
		readDepth = i;
	}
	public void remove(int ind) {
		if (ind>-1&&ind<links.size()) {
			links.remove(ind);
		}
	}

	public void remove(URL url) {
		links.removeElement(url);
	}

	public URL[] getLinks() {
		URL[] tmp = new URL[links.size()];
		tmp = links.toArray(tmp);
		return tmp;
	}

	public Enumeration<URL> enumeration() {
		return links.elements();
	}

	@Override
	public Iterator<URL> iterator() {
		return links.iterator();
	}

	public Vector<URL> vector() {
		return links;
	}

	public URL getSite() {
		return site;
	}
	private void addSite(URL site) {
//System.out.println("Site: "+site.toString()+" || Filter: "+filter.pattern());
		if (filter==null||filter.matcher(site.toString()).matches())
			links.add(site);
	}

	public boolean readSite() throws IOException {
		System.out.println("Site: "+site+"\nRead depth: "+readDepth+"\nRead Deep Enabled? "+readDeep);
		readDeep(site, 0);
		/*
		URL uTmp = null;
		StringBuffer sTmp = null;
			sTmp = new StringBuffer(1024);
			BufferedInputStream in = new BufferedInputStream(site.openStream());
			int b = 0;
			while ((b = in.read()) != -1) {
				sTmp.append((char) b);
			}
			in.close();
			b = 0;
			StringBuffer sTmp2 = sTmp;
			sTmp = new StringBuffer((sTmp.toString()).toLowerCase());

			String src = null;
			while ((b = sTmp.indexOf("<script")) != -1) {
				int end = sTmp.indexOf("</script>") + 8;
				sTmp.delete(b, end);
				sTmp2.delete(b, end);
			}
			b = 0;
			while (true) {
				b = sTmp.indexOf("src", b);
				if (b == -1) {
					break;
				}
				b = sTmp.indexOf("=", b);
				while (sTmp.charAt(++b) == ' ' || sTmp.charAt(b) == '\r'
						|| sTmp.charAt(b) == '\n' || sTmp.charAt(b) == '\t') {}
				if (sTmp.charAt(b) == '\'' || sTmp.charAt(b) == '"') {
					src = sTmp2.substring(
							b + 1,
							Math.min(sTmp.indexOf(">", b + 1), sTmp.indexOf(
									new String(new char[] { sTmp.charAt(b) }),
									b + 1))).trim();
				} else {
					src = sTmp2.substring(
							b,
							Math.min(sTmp.indexOf(" ", b + 1), sTmp.indexOf(
									">", b + 1))).trim();
				}
				if (src.startsWith("\"") || src.startsWith("'")) {
					src = src.substring(1, src.length() - 1);
				}
				if (src.endsWith("'") || src.endsWith("\"")) {
					src = src.substring(0, src.length()-2);
				}

				src = src.replaceAll("[\n|\r|\t]", "");
				if (src.startsWith("/")) {
					src = site.getProtocol() + "://" + site.getHost() + src;
				}
				if (!src.startsWith(site.getProtocol())) {
					if (!src.startsWith(site.getHost())) {
						src = site.getProtocol() + "://"
								+ site.getHost()
								+ site.getPath().substring(0, site.getPath().lastIndexOf("/") + 1)
								+ src;
					} else {
						src = site.getProtocol() + "://" + src;
					}
				}// http://www.imagecash.net/gallery.php?gid=184598&owner=BioX
				addSite(new URL(src));
			}

			b = 0;
			while (true) {
				b = sTmp.indexOf("href", b);
				if (b == -1) {
					break;
				}
				b = sTmp.indexOf("=", b);
				while (sTmp.charAt(++b) == ' ' || sTmp.charAt(b) == '\r'
						|| sTmp.charAt(b) == '\n' || sTmp.charAt(b) == '\t') {
				}
				if (sTmp.charAt(b) == '\'' || sTmp.charAt(b) == '"') {
					src = sTmp2.substring(
							b + 1,
							Math.min(sTmp.indexOf(">", b + 1), sTmp.indexOf(
									new String(new char[] { sTmp.charAt(b) }),
									b + 1))).trim();
				} else {
					src = sTmp2.substring(
							b,
							Math.min(sTmp.indexOf(" ", b + 1), sTmp.indexOf(
									">", b + 1))).trim();
				}

				src = src.replaceAll("[\n|\r|\t]", "");
				if (src.startsWith("\"") || src.startsWith("'")) {
					src = src.substring(1, src.length() - 1);
				}
				if (src.startsWith("/")) {
					src = site.getProtocol() + "://" + site.getHost() + src;
				}
				if (!src.startsWith(site.getProtocol())) {
					if (!src.startsWith(site.getHost())) {
						src = site.getProtocol()
								+ "://"
								+ site.getHost()
								+ site.getPath().substring(0,
										site.getPath().lastIndexOf("/") + 1)
								+ src;
					}
				}
				uTmp = new URL(src);
				addSite(uTmp);
				if (readDeep) {
					readDeep(uTmp, 1);
				}
			}*/
		return true;
	}

	private void readDeep(URL link, int depth) throws IOException {
		StringBuffer sTmp = null;
		String linkt = link.toString();

		Uri parser = new Uri(linkt);
		if (!urlP.getHost().equals(parser.getHost())) {
			d.debug("host compare", "skipped "+linkt);
			return;
		}
		String ext = parser.getFile();
		if (ext.indexOf(".")!=-1) {
			ext = ext.substring(ext.indexOf(".")+1);
		}
		d.debug("site", linkt);
		d.debug("Ext", ext);

		boolean skip = true;
		ext = MimeTypes.getMime(ext.toLowerCase()).get(0);
		d.debug("mime", ext);
		if (ext!=null&&ext.equals("text/html")&&!read.contains(linkt)) {
			skip = false;
			System.out.println("Match: "+ext+linkt);
			d.debug("match", ext+" "+linkt);
		}
			sTmp = new StringBuffer(1024);
			URLConnection con = link.openConnection();
			if (skip) {
				try {
					con.connect();
					if (!con.getContentType().equals("text/html")) {con.getInputStream().close();return;}
					skip = false;
				} catch (Exception e) {//don't want a timeout or file not found to kill us
					d.debug("con.connect()", e);
					return;
				}
			}
			if (skip) {return;}
			System.out.println("Reading site: "+linkt);
			d.debug("Reading Site", linkt);
			read.add(link.toString());
			System.out.println(depth);
			d.debug("depth", String.valueOf(depth));
			BufferedInputStream in;
			try {
				in = new BufferedInputStream(con.getInputStream());
			} catch (Exception e) {//don't let an unknown host kill us
				d.debug("con.getInputStream()",e);
				return;
			}
			int b = 0;
			while ((b = in.read()) != -1) {
				sTmp.append((char) b);
			}
			in.close();
			b = 0;
			StringBuffer sTmp2 = sTmp;
			sTmp = new StringBuffer((sTmp.toString()).toLowerCase());

			String src = null;
			while ((b = sTmp.indexOf("<script")) != -1) {
				int end = sTmp.indexOf("</script>") + 8;
				sTmp.delete(b, end);
				sTmp2.delete(b, end);
			}
			b = 0;
			while (true) {
				b = sTmp.indexOf("src", b);
				if (b == -1) {
					break;
				}
				b = sTmp.indexOf("=", b);
				while (sTmp.charAt(++b) == ' ' || sTmp.charAt(b) == '\r'
						|| sTmp.charAt(b) == '\n' || sTmp.charAt(b) == '\t') {}
				if (sTmp.charAt(b) == '\'' || sTmp.charAt(b) == '"') {
					src = sTmp2.substring(
							b + 1,
							Math.min(sTmp.indexOf(">", b + 1), sTmp.indexOf(
									new String(new char[] { sTmp.charAt(b) }),
									b + 1))).trim();
				} else {
					src = sTmp2.substring(
							b,
							Math.min(sTmp.indexOf(" ", b + 1), sTmp.indexOf(
									">", b + 1))).trim();
				}
				if (src.startsWith("\"") || src.startsWith("'")) {
					src = src.substring(1, src.length() - 1);
				}
				if (src.endsWith("'") || src.endsWith("\"")) {
					src = src.substring(0, src.length()-2);
				}

				src = src.replaceAll("[\n|\r|\t]", "");
				if (src.startsWith("/")) {
					src = site.getProtocol() + "://" + site.getHost() + src;
				}
				if (!src.startsWith(site.getProtocol())) {
					if (!src.startsWith(site.getHost())) {
						src = site.getProtocol() + "://"
								+ site.getHost()
								+ site.getPath().substring(0, site.getPath().lastIndexOf("/") + 1)
								+ src;
					} else {
						src = site.getProtocol() + "://" + src;
					}
				}
				addSite(new URL(src));
			}

			b = 0;
			while (true) {
				b = sTmp.indexOf("href", b);
				if (b == -1) {
					break;
				}
				b = sTmp.indexOf("=", b);
				while (sTmp.charAt(++b) == ' ' || sTmp.charAt(b) == '\r'
						|| sTmp.charAt(b) == '\n' || sTmp.charAt(b) == '\t') {
				}
				if (sTmp.charAt(b) == '\'' || sTmp.charAt(b) == '"') {
					src = sTmp2.substring(
							b + 1,
							Math.min(sTmp.indexOf(">", b + 1), sTmp.indexOf(
									new String(new char[] { sTmp.charAt(b) }),
									b + 1))).trim();
				} else {
					src = sTmp2.substring(
							b,
							Math.min(sTmp.indexOf(" ", b + 1), sTmp.indexOf(
									">", b + 1))).trim();
				}

				src = src.replaceAll("[\n|\r|\t]", "");
				if (src.startsWith("\"") || src.startsWith("'")) {
					src = src.substring(1, src.length() - 1);
				}
				if (src.startsWith("/")) {
					src = site.getProtocol() + "://" + site.getHost() + src;
				}
				if (!src.startsWith(site.getProtocol())) {
					if (!src.startsWith(site.getHost())) {
						src = site.getProtocol()
								+ "://"
								+ site.getHost()
								+ site.getPath().substring(0,
										site.getPath().lastIndexOf("/") + 1)
								+ src;
					}
				}
				addSite(new URL(src));
				if (readDeep&&depth<readDepth) {
					readDeep(new URL(src), depth+1);
				}
			}
		System.out.println("Done");
	}
}
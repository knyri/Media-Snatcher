package picSnatcher.mediaSnatcher;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import picSnatcher.mediaSnatcher.DownloadItems.DownloadItem;
import simple.CIString;
import simple.net.Uri;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

public class DownloadMap implements Iterable<DownloadItems> {
	private static final Log log = LogFactory.getLogFor(UrlMap.class);
	private final LinkedHashMap<CIString, DownloadItems> map = new LinkedHashMap<>();
	private int urlCount = 0;
	public int size() {
		return urlCount;
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(String key) {
		return map.containsKey(new CIString(key));
	}

	public boolean containsValue(Uri tmp) {
		synchronized (this) {
			for (DownloadItems links : map.values()) {
				if (links.contains(tmp)) {
					return true;
				}
			}
		}
		return false;
	}

	public DownloadItems get(String key) {
		return map.get(new CIString(key));
	}
	public DownloadItems get(CIString key) {
		return map.get(key);
	}

	public boolean put(String key, Uri value, String altFileName) {
		CIString ckey = new CIString(key);
		synchronized(this) {
			if (map.containsKey(ckey)) {//check for key to avoid null pointer
				if (!map.get(ckey).contains(value)) {//check for value to avoid duplicates
					if (map.get(ckey).add(value, altFileName))
						urlCount++;
					else
						return false;
				} else
					return false;
			} else {//key doesn't exist, create
				DownloadItems tmp = new DownloadItems();
				if (tmp.add(value, altFileName)) {
					map.put(ckey, tmp);
					urlCount++;
				} else
					return false;
			}
		}
		log.information("added",value+" --on-- "+key);
		return true;
	}

	public void clear() {
		map.clear();
		urlCount = 0;
		log.debug("Map cleared!");
	}

	@SuppressWarnings("unlikely-arg-type")
	public String getKey(Uri link) {
		//if (!map.containsValue(value)) return null;
		for (Map.Entry<CIString, DownloadItems> entry : entrySet()) {
			for (DownloadItem url : entry.getValue()) {
				if (url.equals(link)) return entry.getKey().toString();
			}
		}
		return null;
	}

	public void remove(String key, Uri value) {
		if (map.get(new CIString(key)).remove(value)) {
			urlCount--;
			log.debug("removed",value+" --at-- "+key);
		}
	}

	public DownloadItems remove(String key) {
		DownloadItems tmp = map.remove(new CIString(key));
		urlCount -= tmp.size();
		log.debug("removed key",key);
		return tmp;
	}

	public Set<CIString> keySet() {
		return map.keySet();
	}

	public Collection<DownloadItems> values() {
		return map.values();
	}

	public Set<Map.Entry<CIString, DownloadItems>> entrySet() {
		return map.entrySet();
	}
	@Override
	public Iterator<DownloadItems> iterator() {
		return map.values().iterator();
	}

	public int removeDuplicates() {
		// it's a HashSet, duplicates are impossible
		return 0;
	}
}
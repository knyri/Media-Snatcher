/**
 *
 */
package picSnatcher.mediaSnatcher;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import simple.CIString;
import simple.net.Uri;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;


/**
 * Created: Jun 1, 2006
 * @author KP
 */
public class UrlMap implements Iterable<List<Uri>> {
	private static final Log log = LogFactory.getLogFor(UrlMap.class);
	private final LinkedHashMap<CIString, List<Uri>> map = new LinkedHashMap<>();
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
			for (List<Uri> links : map.values()) {
				if (links.contains(tmp)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<Uri> get(String key) {
		return map.get(new CIString(key));
	}
	public List<Uri> get(CIString key) {
		return map.get(key);
	}

	public boolean put(String key, Uri value) {
		CIString ckey = new CIString(key);
		synchronized(this) {
			if (map.containsKey(ckey)) {//check for key to avoid null pointer
				if (!map.get(ckey).contains(value)) {//check for value to avoid duplicates
					if (map.get(ckey).add(value))
						urlCount++;
					else
						return false;
				} else
					return false;
			} else {//key doesn't exist, create
				List<Uri> tmp = new LinkedList<Uri>();
				if (tmp.add(value)) {
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

	public String getKey(Uri link) {
		//if (!map.containsValue(value)) return null;
		for (Map.Entry<CIString, List<Uri>> entry : entrySet()) {
			for (Uri url : entry.getValue()) {
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

	public List<Uri> remove(String key) {
		List<Uri> tmp = map.remove(new CIString(key));
		urlCount -= tmp.size();
		log.debug("removed key",key);
		return tmp;
	}

	public Set<CIString> keySet() {
		return map.keySet();
	}

	public Collection<List<Uri>> values() {
		return map.values();
	}

	public Set<Map.Entry<CIString, List<Uri>>> entrySet() {
		return map.entrySet();
	}
	@Override
	public Iterator<List<Uri>> iterator() {
		return map.values().iterator();
	}

	public int removeDuplicates() {
		int removed = 0;
		synchronized(this) {
			for (List<Uri> cur : this) {
				for (int i = 0; i<cur.size(); i++) {
					for (int j = i+1; j<cur.size(); j++) {
						if (cur.get(i).equals(cur.get(j))) {
							cur.remove(j);
							j--;
							urlCount--;
							removed++;
						}
					}
				}
			}
		}
		return removed;
	}
}

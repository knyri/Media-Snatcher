package picSnatcher.mediaSnatcher;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import simple.net.Uri;

public class DownloadItems implements Iterable<DownloadItems.DownloadItem>{
	private final Set<DownloadItem> items= new LinkedHashSet<>();
	public DownloadItems(){
	}
	public boolean add(Uri uri, String altName){
		return items.add(new DownloadItem(uri, altName));
	}
	public int size(){
		return items.size();
	}
	public boolean remove(DownloadItem item){
		return items.remove(item);
	}
	@SuppressWarnings("unlikely-arg-type")
	public boolean remove(Uri uri){
		for(DownloadItem item: items){
			if(item.equals(uri)){
				return items.remove(item);
			}
		}
		return true;
	}
	@SuppressWarnings("unlikely-arg-type")
	public boolean contains(Uri uri){
		for(DownloadItem item: items){
			if(item.equals(uri)){
				return true;
			}
		}
		return false;
	}
	public Set<DownloadItem> items(){
		return Collections.unmodifiableSet(items);
	}

	public static class DownloadItem {
		public final Uri uri;
		public final String altName;
		public DownloadItem(Uri uri, String altName){
			this.uri= uri;
			this.altName= altName;
		}
		@Override
		public boolean equals(Object o){
			return uri.equals(o);
		}
		@Override
		public int hashCode(){
			return uri.hashCode();
		}
		@Override
		public String toString(){
			return uri.toString();
		}
	}
	public DownloadItem[] toArray(){
		return items.toArray(new DownloadItem[items.size()]);
	}

	@Override
	public Iterator<DownloadItems.DownloadItem> iterator(){
		return items.iterator();
	}
}

/**
 *
 */
package picSnatcher.mediaSnatcher;

import simple.net.Uri;
import simple.util.do_str;

/**Fundamental data structure for iterating over multiple URL files that follow
 * a number range pattern.
 * <br>Created: Jun 24, 2010
 * @author Kenneth Pierce
 */
public class UriFormatIterator {
	public final String site;
	private final int start,
		end,
		inc,
		digits;
	private int cur;
	/**
	 * @param site The URL
	 * @param syn The number of digits expressed in 0's. 00 for 2, 000 for 3, ...
	 * @param st Starting number
	 * @param en Ending number
	 * @param in Increment. Can be negative
	 * @deprecated Use the other constructor
	 */
	@Deprecated
	public UriFormatIterator(String site, String syn, int st, int en, int in) {
		this(site, syn.length(), st, en, in);
	}
	/**
	 * @param uri The URI with ^gal^ marker
	 * @param digits The number of digits to pad to
	 * @param start The starting number
	 * @param end The ending number
	 * @param increment The number to increment by
	 */
	public UriFormatIterator(String uri, int digits, int start, int end, int increment) {
		site= uri;
		this.start= start;
		this.end= end;
		cur= start;
		this.digits= digits;
		if(0 == increment){
			if(start <= end)
				inc= 1;
			else
				inc= -1;
		}else if(end < start && increment > 0){
			inc= -1 * increment;
		}else{
			inc= increment;
		}
	}
	@Override
	public String toString() {
		return site.replaceAll("\\^gal\\^",
				"["+do_str.padLeft(digits,'0',String.valueOf(start))+
				"-"+do_str.padLeft(digits,'0',String.valueOf(end))+","+inc+"]");
	}
	/**
	 * Parses the output from the toString() method.
	 * @param format
	 * @return
	 */
	public static UriFormatIterator parse(String format){
		int istart= 0, iend= 0, sep= 0,
				start, end, inc=0, digits;
		istart= format.indexOf('[');
		if(-1 == istart)// Plain URI
			return new UriFormatIterator(format,0,0,0,1);
		istart+=1;

		iend= format.indexOf(']', istart);
		if(-1 == iend)// No end bracket
			return new UriFormatIterator(format,0,0,0,1);

		sep= format.indexOf('-', istart);
		if(-1 == sep)// No range
			return new UriFormatIterator(format,0,0,0,1);

		try{
			start= Integer.parseInt(format.substring(istart,sep), 10);
			digits= sep-istart;
			istart= sep+1;
			sep= format.indexOf(',',istart);
			if(-1 == sep){
				end= Integer.parseInt(format.substring(istart,iend), 10);
				digits= Math.min(digits, iend-istart);
			}else{
				end= Integer.parseInt(format.substring(istart,sep), 10);
				digits= Math.min(digits, sep-istart);
				inc= Integer.parseInt(format.substring(sep+1,iend), 10);
			}
		}catch(NumberFormatException e){
			return new UriFormatIterator(format,0,0,0,1);
		}

		format= format.substring(0,format.indexOf('[')) + "^gal^" + format.substring(iend+1);
		return new UriFormatIterator(format,digits,start,end,inc);
	}
	/**
	 * Get the next URI in the sequence. Returns null if the end has been reached
	 * @return The next Uri or null if the end has been reached.
	 */
	public Uri next() {
		// Check to see if we reached the end
		if (inc > 0) {
			if (cur>end) return null;
		} else {
			if (cur<end) return null;
		}
		Uri tmp = new Uri(this.site.replaceAll("\\^gal\\^", do_str.padLeft(digits,'0',Integer.toString(cur))));
		cur += inc;
		return tmp;
	}
	/**
	 * Resets the sequence
	 */
	public void reset() {
		cur = start;
	}
	public int getStart(){
		return start;
	}
	public int getEnd(){
		return end;
	}
	public int getIncrement(){
		return inc;
	}
	public int getCurrent(){
		return cur;
	}
	public int getTotalItems(){
		// Should always be positive, but doing the ABS just in case.
		return Math.abs((end-start)/inc);
	}
	/** A 0 to 100 indicator of the progress
	 * @return
	 */
	public int getProgress(){
		return Math.abs((cur*100)/(end-start));
	}
	public int getPaddingLength(){
		return digits;
	}
	public String getUri(){
		return site;
	}
}

/**
 *
 */
package picSnatcher.mediaSnatcher;

import simple.util.logging.ErrorCodeResolver;

/**
 * <br>Created: Nov 15, 2010
 * @author Kenneth Pierce
 */
public class ErrorCodes implements ErrorCodeResolver {
	private static final String empty = "unknown";
	private static final String[][] errors = new String[16][16];
	private static final String[] states = new String[16];
	static {
		states[0] = "idle";
		errors[0][0] = "";
		states[1] = "general session";
		errors[1][0] = "User cancelled save dialog.";
		errors[1][1] = "User cancelled load dialog.";
		states[2] = "starting session";
		errors[2][0] = "Could not create download log.";
		errors[2][1] = "Could not read from download log.";
		errors[2][2] = "User aborted selecting save folder.";
		states[3] = "running session";
		errors[3][0] = "User aborted run";
		states[4] = "reading page";
		states[5] = "parsing page";
		states[6] = "downloading item";
		for (int i = 0; i < 16; i++) {
			if (states[i]==null)
				states[i]=empty;
			for (int j = 0; j < 16; j++) {
				if (errors[i][j]==null)
					errors[i][j]=empty;
			}
		}
	}
	/* (non-Javadoc)
	 * @see simple.util.logging.ErrorCodeResolver#getErrorString(int)
	 */
	@Override
	public String getErrorString(int code) {
		return states[code>>4]+":"+errors[code>>4][code&0x0f];
	}
}

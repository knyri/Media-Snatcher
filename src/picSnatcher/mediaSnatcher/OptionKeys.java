/**
 *
 */
package picSnatcher.mediaSnatcher;

/**
 * Option constants
 * @author super
 *
 */
public enum OptionKeys {
	//Edit to add option!
	saveVersion,
	download_removeThumbs,
	download_prettyFilenames,
	/**
	 * Download all to the same folder
	 */
	download_sameFolder,
	/**
	 * Use this folder instead of domain
	 */
	download_sameFolder_custom,
	/**
	 * Use numbers instead of the file name
	 */
	download_alternateNumbering,
	/**
	 * Prepend page to file name
	 */
	download_prependPage,
	/**
	 * Prepend page as directory instead
	 */
	download_ppAsDir,
	download_keepDownloadLog,
	/**
	 * Separate runs by date
	 * date/host
	 */
	download_dateSubfolder,
	/**
	 * Put host first
	 * host/date
	 */
	download_siteFirst,
	/**
	 * Put the files contained on that page in the same
	 * directory as the page.
	 */
	download_usePageDirectory,
	download_usePageDomain,
	download_sameSite,
	download_separateByDomain,
	download_ingoreStrings,
	download_wantStrings,
	snatcher_getPictures,
	snatcher_getMovies,
	snatcher_getAudio,
	snatcher_getArchives,
	snatcher_getDocuments,
	snatcher_getOther,
	snatcher_readDeep,
	snatcher_readDepth,
	snatcher_sameSite,
	snatcher_ignore,
	snatcher_want,
	snatcher_wantedTitles,
	snatcher_maxLogs,
	snatcher_saveFolder,
	snatcher_saveFile,
	snatcher_alwaysCheckMIME,
	snatcher_wantTitle,
	snatcher_ignoreTitle,
	snatcher_repeat,
	snatcher_minImgWidth,
	snatcher_minImgHeight,
	download_saveExternalUrlList,
	download_saveLinkList
}

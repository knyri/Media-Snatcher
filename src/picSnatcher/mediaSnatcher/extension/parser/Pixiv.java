/**
 *
 */
package picSnatcher.mediaSnatcher.extension.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import picSnatcher.mediaSnatcher.PageParser;
import picSnatcher.mediaSnatcher.PageReader;
import picSnatcher.mediaSnatcher.Session;
import picSnatcher.mediaSnatcher.extension.OptionPanel;
import simple.net.Uri;
import simple.net.http.Client;
import simple.net.http.clientparams.ClientParam;
import simple.net.http.clientparams.StringParam;
import simple.parser.ml.Page;
import simple.util.logging.LogFactory;

/**
 * @author super
 *
 */
public class Pixiv extends PageParser {
	private static final simple.util.logging.Log log=LogFactory.getLogFor(Pixiv.class);
	private static boolean loggedin=false;
	private static final Properties props= new Properties();
	static {
		try(FileInputStream in= new FileInputStream("pixiv_parser.conf")){
			props.load(in);
		}catch(FileNotFoundException e){

		}catch(IOException e){
			log.error("Failed to load options", e);
		}
	}

	public Pixiv(PageReader preader, Session session) {
		super(preader, session);
	}

	@Override
	public boolean canHandle(Uri link, Page page, String mime) {
		return "pixiv.net".equals(link.getHost().toLowerCase()) && "text/html".equals(mime);
	}

	@Override
	public boolean hasOptions() {
		return false;
	}

	@Override
	public OptionPanel getOptions() {
		return null;
	}
	@Override
	public boolean saveOptions(){
		File f= new File("pixiv_parser.conf");
		if(!f.exists()){
			try{
				if(!f.createNewFile()){
					log.error("Failed to create options file.");
					return false;
				}
			}catch(IOException e){
				log.error("Failed to create options file.",e);
				return false;
			}
		}
		try(FileOutputStream out= new FileOutputStream(f)){
			props.store(out,"");
		}catch(IOException e){
			log.error("Failed to save options file.",e);
			return false;
		}
		return true;
	}

	@Override
	protected boolean processPage(Page source, Uri page, Uri referer,
			String title, String basehref, int depth) {
		login(Session.conCont);
		// TODO Auto-generated method stub
		return false;
	}
	static final String
		loginUri= "https://ssl.pixiv.net/login.php";
	static final Header[] loginHeaders = new BasicHeader[] {
		new BasicHeader("Content-type","application/x-www-form-urlencoded"),
		new BasicHeader("Referer", "https://ssl.pixiv.net/login.php")
	};
	private static void login(Client httpclient){
		if(loggedin)return;
		log.debug("Attempting login");
		try{
			HttpResponse response = httpclient.get(loginUri);
			EntityUtils.consume(response.getEntity());
			log.debug(response);

			ClientParam[] nvps = new ClientParam[] {
				new StringParam("mode", "login"),
				new StringParam("pixiv_id", props.getProperty("user")),
				new StringParam("pass", props.getProperty("pass")),
				new StringParam("skip", "1")
			};
			response = httpclient.post(loginUri,loginHeaders,nvps,Client.PostDataType.UrlEncoded,null);
			HttpEntity entity = response.getEntity();
			EntityUtils.consume(entity);
		}catch(Exception e){log.error("Could not log in",e);}
	}
}

package picSnatcher.mediaSnatcher.extension.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import picSnatcher.mediaSnatcher.Constants;
import picSnatcher.mediaSnatcher.PageParser;
import picSnatcher.mediaSnatcher.PageReader;
import picSnatcher.mediaSnatcher.Session;
import picSnatcher.mediaSnatcher.extension.OptionPanel;
import simple.net.Uri;
import simple.net.http.Client;
import simple.net.http.clientparams.ClientParam;
import simple.net.http.clientparams.StringParam;
import simple.parser.ml.Page;
import simple.parser.ml.Tag;
import simple.util.logging.LogFactory;

public final class FurAffinity extends PageParser {
	private static final OptionPanel optionPanel= new OptionPanel("Fur Affinity", FurAffinity.class.getClassLoader().getResourceAsStream("login_template.xml"));
	private static final simple.util.logging.Log log=LogFactory.getLogFor(FurAffinity.class);
	private static boolean loggedin=false;
	private static final Properties props= new Properties();
	static {
		try(FileInputStream in= new FileInputStream("furaffinity_parser.conf")){
			props.load(in);
		}catch(FileNotFoundException e){

		}catch(IOException e){
			log.error("Failed to load options", e);
		}
		optionPanel.setItemValue("username",props.getProperty("user"));
		optionPanel.setItemValue("password",props.getProperty("pass"));
	}
	public FurAffinity(PageReader preader, Session session) {
		super(preader, session);
	}

	@Override
	public boolean canHandle(Uri link, Page page,String mime) {
		log.debug("CanHandle",link);
		if(link.getDomain().equalsIgnoreCase("furaffinity.net") && "text/html".equals(mime)){
			return true;
		}
		return false;
	}

	@Override
	public boolean hasOptions(){
		return true;
	}

	@Override
	public OptionPanel getOptions(){
		return optionPanel;
	}
	@Override
	public boolean saveOptions(){
		props.setProperty("user",optionPanel.getItemValue("username"));
		props.setProperty("pass",optionPanel.getItemValue("password"));
		File f= new File("furaffinity_parser.conf");
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
		if(page.getPath().startsWith("/view/")||page.getPath().startsWith("/full/")){
			List<Tag> tags = source.getTags("script");
			int index=0;
			boolean fHasContent=false;
			for(Tag tag:tags){
				String content = tag.getChild(0).getContent();
				index=content.indexOf("full_url");
				if(index!=-1){
					index=content.indexOf('"',index)+1;
					Uri url=new Uri(content.substring(index, content.indexOf('"',index)),page.getScheme());
					session.addLink(url, page, false);
					fHasContent=true;
					break;
				}
			}
			if(!fHasContent){
				tags=source.getTags("object");
				for(Tag tag:tags){
					if("application/x-shockwave-flash".equals(tag.getProperty(Constants.atr_type))){
						if(tag.hasProperty(Constants.atr_data))
							session.addLink(new Uri(tag.getProperty(Constants.atr_data),page.getScheme()), page, false);
					}
				}
			}
		}else if(page.getPath().startsWith("/gallery/")||
				 page.getPath().startsWith("/scraps/")||
				 page.getPath().startsWith("/favorites/")){
			boolean fHasContent=false;
			List<Tag> tags;
			tags=source.getTags("a");
			for(Tag tag:tags){
				if(tag.getProperty(Constants.atr_href)!=null && tag.getProperty(Constants.atr_href).startsWith("/view/")){
					addToReadQueue(new Uri(createURL(tag.getProperty(Constants.atr_href), page, basehref)), page, depth);
					fHasContent=true;
				}
			}
			//* TODO: make this possible in the page read
			/* only search for the next button if this page has content.
			 * FurAffinity will produce a page even if it is past the number of images available
			 */
			if(fHasContent){
				tags=source.getTags("button");
				for(Tag tag:tags){
					if(tag.getChild(0).getContent().equalsIgnoreCase("Next")){
						addToReadQueue(new Uri(createURL(tag.getParent().getProperty(Constants.atr_action), page, basehref)), page, depth==0?depth:depth-1);
						break;
					}
				}
			}
			//*/
		}
		return true;
	}
	static final String
		loginUri= "https://www.furrffinity.net/login/",
		loginPostUri= "https://www.furaffinity.net/login/?ref=https://www.furaffinity.net/";
	static final Header[] loginHeaders = new BasicHeader[] {
		new BasicHeader("Content-type","application/x-www-form-urlencoded"),
		new BasicHeader("Referer", "https://www.furaffinity.net/login/"),
		new BasicHeader("Origin","https://www.furaffinity.net")
	};
	private static void login(Client httpclient){
		if(loggedin)return;
		log.debug("Attempting login");
		try{
			HttpResponse response = httpclient.get(loginUri);
			EntityUtils.consume(response.getEntity());
			log.debug(response);

			ClientParam[] nvps= new ClientParam[] {
				new StringParam("action", "login"),
				new StringParam("retard_protection", "1"),
				new StringParam("name", props.getProperty("user")),
				new StringParam("pass", props.getProperty("pass")),
				new StringParam("login", "Login to&nbsp;FurAffinity")
			};

			response = httpclient.post(loginPostUri,loginHeaders,nvps,Client.PostDataType.UrlEncoded,null);
			HttpEntity entity = response.getEntity();
			EntityUtils.consume(entity);
		}catch(Exception e){log.error("Could not log in",e);}
	}

	@Override
	protected void reset(){
		// TODO Auto-generated method stub

	}

}

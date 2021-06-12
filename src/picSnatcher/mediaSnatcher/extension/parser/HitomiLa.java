package picSnatcher.mediaSnatcher.extension.parser;

import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import picSnatcher.mediaSnatcher.PageParser;
import picSnatcher.mediaSnatcher.PageReader;
import picSnatcher.mediaSnatcher.Session;
import picSnatcher.mediaSnatcher.extension.OptionPanel;
import simple.io.FileUtil;
import simple.io.WriterOutputStream;
import simple.net.Uri;
import simple.parser.ml.Page;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

public class HitomiLa extends PageParser{
	private static final Log log = LogFactory.getLogFor(HitomiLa.class);
	public HitomiLa(PageReader preader,Session session){
		super(preader,session);
	}

	@Override
	protected void reset(){
	}

	@Override
	public boolean canHandle(Uri link,Page page,String mime){
		return  "hitomi.la".equals(link.getHost().toLowerCase()) && "text/html".equals(mime);
	}

	@Override
	public boolean hasOptions(){
		return false;
	}

	@Override
	public boolean saveOptions(){
		return false;
	}

	@Override
	public OptionPanel getOptions(){
		return null;
	}

	private static final Pattern images= Pattern.compile("\"name\":\"([^\"]+)");
	@Override
	protected boolean processPage(Page source,Uri page,Uri referer,String title,String basehref,int depth){
		if(page.getPath().startsWith("/galleries") || page.getPath().startsWith("/reader")){
			String galleryId= page.getFile().substring(0, page.getFile().indexOf('.'));
			log.information(galleryId);
			try(CloseableHttpResponse resp= Session.conCont.get("http://hitomi.la/galleries/" + galleryId + ".js")){
				StringWriter content= new StringWriter();
				FileUtil.copy(resp.getEntity().getContent(), new WriterOutputStream(content), 1024);
				Matcher imageList= images.matcher(content.toString());
				while(imageList.find()){
					session.addLink(new Uri("http://a.hitomi.la/galleries/" + galleryId + '/' + imageList.group(1)), page, true);
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return true;
	}

}

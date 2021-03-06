package picSnatcher.mediaSnatcher.extension.parser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import picSnatcher.mediaSnatcher.Constants;
import picSnatcher.mediaSnatcher.PageReader;
import picSnatcher.mediaSnatcher.Session;
import simple.net.Uri;
import simple.parser.ml.InlineLooseParser;
import simple.parser.ml.Page;
import simple.parser.ml.Tag;
import simple.util.logging.Log;
import simple.util.logging.LogFactory;

public class g_e_hentai_org extends GeneralParser{
	private static final Log log = LogFactory.getLogFor(g_e_hentai_org.class);
	public g_e_hentai_org(PageReader preader,Session session){
		super(preader,session);
	}

	@Override
	protected boolean processPage(Page source,Uri page,Uri referer,String title,String basehref,int depth){
		for(Tag h1 : source.getTags(Constants.tag_h1)){
			if("Content Warning".equals(h1.getTextContent())){
				for(Tag a : source.getTags(Constants.tag_a)){
					if("View Gallery".equals(a.getTextContent())){
						session.setState("Bypassing content block...");
						log.debug("Bypassing content block",page);
						CloseableHttpResponse resp= null;
						String uri;
						if(page.getQuery().isEmpty()){
							uri= page.toString()+"?nw=always";
						}else{
							uri= page.toString()+"&nw=always";
						}
						if(referer != null){
							try{
								resp= Session.conCont.get(uri,new Header[]{new BasicHeader("Referer",referer.toString())});
							}catch(Exception e){
								return false;
							}
						}else{
							try{
								resp= Session.conCont.get(uri);
							}catch(Exception e){
								return false;
							}
						}
						try(InputStreamReader in = new InputStreamReader(resp.getEntity().getContent(), "UTF-8")){
							source= InlineLooseParser.parse(in,PageReader.pconst);
						}catch(IOException | ParseException e){
							return false;
						}
						try{
							resp.close();
						}catch(IOException e){
							e.printStackTrace();
						}
						return processPage(source,page,referer,title,basehref,depth);
					}
				}
			}
		}
		for(Tag link : source.getTags(Constants.tag_a)){
			if("next".equals(link.getProperty("id"))){
				this.addToCurrentReadQueue(new Uri(link.getProperty(Constants.atr_href)),page);
				break;
			}
		}
		return super.processPage(source,page,referer,title,basehref,depth);
	}

	@Override
	public boolean canHandle(Uri link,Page page,String mime){
		if("g.e-hentai.org".equals(link.getHost()) || "e-hentai.org".equals(link.getHost())){
			return true;
		}
		return false;
	}

}

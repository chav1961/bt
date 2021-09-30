package chav1961.bt.lucenewrapper.interfaces;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public interface Document2Save {
	String getTitle();
	String getAuthor();
	String getAnnotation();
	String getText();
	InputStream getContent();
	Date created();
	Set<String> getTags();
	Map<String,String> getKeywords();
}

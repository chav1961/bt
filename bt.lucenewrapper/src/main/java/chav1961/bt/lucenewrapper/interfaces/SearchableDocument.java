package chav1961.bt.lucenewrapper.interfaces;

import java.util.Date;
import java.util.UUID;

public interface SearchableDocument extends Document2Save {
	public interface Highlights {
		long getPosition();
		int getPage();
		int getLine();
		String getContent();
		boolean isHighlighted();
	}
	
	UUID getId();
	DocumentState getDocumentState();
	Date lastModified();
	Iterable<Highlights> highlights();
	double getScore();
}

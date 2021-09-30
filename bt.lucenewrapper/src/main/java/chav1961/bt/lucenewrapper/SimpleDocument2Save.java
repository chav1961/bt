package chav1961.bt.lucenewrapper;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import chav1961.bt.lucenewrapper.interfaces.Document2Save;

public class SimpleDocument2Save implements Document2Save {
	private static final Map<String, String>	EMPTY_MAP = new HashMap<>(); 
	
	private final Set<String>			tags = new HashSet<>();
	private final Map<String, String>	keywords = new HashMap<>();
	private String						title;
	private String						author;
	private String						annotation;
	private String						content;
	private Date						created = new Date(System.currentTimeMillis());

	public SimpleDocument2Save() {
		this.title = "";
		this.author = "";
		this.annotation = "";
		this.content = "";
	}
	
	public SimpleDocument2Save(final Document2Save another) throws NullPointerException {
		if (another == null) {
			throw new NullPointerException("Another document can't be null");
		}
		else {
			this.title = another.getTitle();
			this.author = another.getAuthor();
			this.annotation = another.getAnnotation();
			this.content = another.getText();
			this.tags.addAll(another.getTags());
			this.keywords.putAll(another.getKeywords());
		}
	}

	public SimpleDocument2Save(final String title, final String author, final String annotation, final String content, final String... tags) {
		this(title, author, annotation, content, EMPTY_MAP, tags);
	}

	public SimpleDocument2Save(final String title, final String author, final String annotation, final String content, final Map<String, String> keywords, final String... tags) {
		this.title = title;
		this.author = author;
		this.annotation = annotation;
		this.content = content;
		this.keywords.putAll(keywords);
		this.tags.addAll(Arrays.asList(tags));
	}
	
	@Override
	public String getTitle() {
		return title == null ? "" : title;
	}

	@Override
	public String getAuthor() {
		return author == null ? "" : author;
	}

	@Override
	public String getAnnotation() {
		return annotation == null ? "" : annotation;
	}

	@Override
	public String getText() {
		return content == null ? "" : content;
	}

	@Override
	public InputStream getContent() {
		return null;
	}

	@Override
	public Date created() {
		return created;
	}

	@Override
	public Set<String> getTags() {
		return tags;
	}

	@Override
	public Map<String, String> getKeywords() {
		return keywords;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public void setText(String content) {
		this.content = content;
	}
	
	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((keywords == null) ? 0 : keywords.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		SimpleDocument2Save other = (SimpleDocument2Save) obj;
		if (annotation == null) {
			if (other.annotation != null) return false;
		} else if (!annotation.equals(other.annotation)) return false;
		if (author == null) {
			if (other.author != null) return false;
		} else if (!author.equals(other.author)) return false;
		if (content == null) {
			if (other.content != null) return false;
		} else if (!content.equals(other.content)) return false;
		if (created == null) {
			if (other.created != null) return false;
		} else if (!created.equals(other.created)) return false;
		if (keywords == null) {
			if (other.keywords != null) return false;
		} else if (!keywords.equals(other.keywords)) return false;
		if (tags == null) {
			if (other.tags != null) return false;
		} else if (!tags.equals(other.tags)) return false;
		if (title == null) {
			if (other.title != null) return false;
		} else if (!title.equals(other.title)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "SimpleDocument2Save [tags=" + tags + ", keywords=" + keywords + ", title=" + title + ", author=" + author + ", annotation=" + annotation + ", content=" + content + ", created=" + created + "]";
	}
}

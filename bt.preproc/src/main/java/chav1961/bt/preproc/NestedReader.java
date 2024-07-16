package chav1961.bt.preproc;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.CharUtils;

public class NestedReader implements Closeable {
	private static final int		INITIAL_BUFFER_SIZE = 8192;	
	private static final char[]		NL = new char[] {'\n'};
	
	private final List<StackRecord>	stack = new ArrayList<>(16);
	private final int				bufferSize;

	public NestedReader(final Reader reader) throws IOException {
		this(reader, INITIAL_BUFFER_SIZE);
	}	
	
	public NestedReader(final Reader reader, final int bufferSize) throws IOException {
		if (reader == null) {
			throw new NullPointerException("Reader toa dd can't be null");
		}
		else if (bufferSize <= 0) {
			throw new IllegalArgumentException("Buffer size ["+bufferSize+"] must be greater than 0");
		}
		else {
			this.bufferSize = bufferSize;
			pushSource(null, reader, false);
		}
	}

	public boolean next(final Line line) throws IOException {
		if (line == null) {
			throw new NullPointerException("Line to store parameters can't be null");
		}
		else if (getDepth() <= 0) {
			return false;
		}
		else if (hasNext()){
			final StackRecord	rec = stack.get(0); 
			
			line.content = rec.buffer;
			line.lineNo = rec.lineNo;
			line.from = rec.from;
			line.len = rec.len;
			line.source = rec.uri;
			return true;
		}
		else {
			popSource();
			return next(line);
		}
	}

	public void pushSource(final URL source) throws IOException {
		pushSource(source, Charset.defaultCharset());
	}
	
	public void pushSource(final URL source, final Charset charset) throws IOException {
		if (source == null) {
			throw new NullPointerException("Source URL to push can't be null");
		}
		else if (charset == null) {
			throw new NullPointerException("Source charset to push can't be null");
		}
		else {
			final InputStream	is = source.openStream();
			final Reader		rdr = new InputStreamReader(is, charset) {
										@Override
										public void close() throws IOException {
											super.close();
											is.close();
										}
								};
			
			try {
				pushSource(source.toURI(), rdr, true);
			} catch (URISyntaxException e) {
				throw new IOException(e);
			}
		}
	}

	public void popSource() throws IOException {
		if (getDepth() <= 0) {
			throw new IllegalStateException("Source stack exhausted");
		}
		else {
			final StackRecord	rec = stack.remove(0);
			
			if (rec.closeRequired) {
				rec.rdr.close();
			}
		}
	}
	
	public int getDepth() {
		return stack.size();
	}
	
	@Override
	public void close() throws IOException {
		while(getDepth() > 0) {
			popSource();
		}
	}

	private void pushSource(final URI uri, final Reader reader, final boolean closeRequired) throws IOException {
		stack.add(0, new StackRecord(uri, reader, bufferSize, closeRequired));
		appendBuffer(stack.get(0));
	}

	private boolean hasNext() throws IOException {
		final StackRecord	rec = stack.get(0);
		
		if (hasNextInside(rec)) {
			return true;
		}
		else {
			shiftBuffer(rec);
			if (appendBuffer(rec)) {
				while (!hasNextInside(rec)) {
					expandBuffer(rec);
					if (!appendBuffer(rec)) {
						return false;
					}
				}
				return true;
			}
			else {
				return false;
			}
		}
	}

	private boolean hasNextInside(final StackRecord	rec) {
		final int 	newStart = rec.from + rec.len;
		
		for(int index = newStart, maxIndex = rec.to; index < maxIndex; index++) {
			if (rec.buffer[index] == '\n') {
				rec.from = newStart;
				rec.len = index - newStart + 1;
				rec.lineNo++;
				return true;
			}
		}
		return false;
	}

	private void shiftBuffer(final StackRecord rec) {
		final int	newStart = rec.from + rec.len;
		
		System.arraycopy(rec.buffer, newStart, rec.buffer, 0, rec.to - newStart);
		rec.to -= newStart;
		rec.from = 0;
		rec.len = 0;
	}

	private boolean appendBuffer(final StackRecord rec) throws IOException {
		final int	available = rec.buffer.length - rec.to;
		int	len = rec.rdr.read(rec.buffer, rec.to, available);
		
		if (len < 0) {
			return false;
		}
		else if (len == available) {
			rec.to = rec.buffer.length;
			return true;
		}
		else {
			rec.to += len;
			do {
				len = rec.rdr.read(rec.buffer, rec.to, rec.buffer.length - rec.to);
				if (len <= 0) {
					if (rec.buffer[rec.to - 1] != '\n') {
						if (rec.to == rec.buffer.length - 1) {
							expandBuffer(rec);
						}
						rec.buffer[rec.to++] = '\n';
					}
					break;
				}
				else {
					rec.to += len;
				}
			} while (rec.to < rec.buffer.length);
			return true;
		}
	}

	private void expandBuffer(final StackRecord rec) {
		rec.buffer = Arrays.copyOf(rec.buffer, 2 * rec.buffer.length);
	}
	
	public static class Line {
		public char[]	content;
		public int		lineNo;
		public int		from;
		public int		len;
		public URI		source;
	
		public Line() {
			this.content = null;
			this.lineNo = 0;
			this.from = 0;
			this.len = 0;
			this.source = null;
		}

		public Line(final int lineNo, final char[] content, final int from, final int len, final URI source) {
			this.content = content;
			this.lineNo = lineNo;
			this.from = from;
			this.len = len;
			this.source = source;
		}

		@Override
		public String toString() {
			return "Line [content=" + Arrays.toString(content) + ", lineNo=" + lineNo + ", from=" + from + ", len=" + len + ", source=" + source + "]";
		}
		
		public static Line of(final CharSequence content) {
			if (content == null) {
				throw new NullPointerException("Content can't be null");
			}
			else if (content.charAt(content.length() - 1) != '\n') {
				throw new IllegalArgumentException("Content must be terminated with '\\n'");
			}
			else {
				return new Line(0, CharUtils.toCharArray(content), 0, content.length(), null);
			}			
		}
	}
	
	private static class StackRecord {
		final URI		uri;
		final Reader	rdr;
		final boolean	closeRequired;
		char[]			buffer;
		int				lineNo;
		int				from;
		int				to;
		int				len;

		private StackRecord(final URI uri, final Reader rdr, final int bufferSize, final boolean closeRequired) {
			this.uri = uri;
			this.rdr = rdr;
			this.closeRequired = closeRequired;
			this.buffer = new char[bufferSize];
			this.lineNo = 0;
			this.from = 0;
			this.to = 0;
			this.len = 0;
		}

		@Override
		public String toString() {
			return "StackRecord [rdr=" + rdr + ", closeRequired=" + closeRequired + ", buffer="
					+ Arrays.toString(buffer) + ", lineNo=" + lineNo + ", from=" + from + ", to=" + to + ", len=" + len
					+ "]";
		}
	}
}
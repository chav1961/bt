package chav1961.bt.matrix.macros;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NestedReader implements Closeable {
	private final List<StackRecord>	stack = new ArrayList<>();
	
	public NestedReader(final Reader reader) throws IOException {
		if (reader == null) {
			throw new NullPointerException("Reader toa dd can't be null");
		}
		else {
			pushSource(reader, false);
		}
	}

	public boolean next(final Line line) throws IOException {
		if (line == null) {
			throw new NullPointerException("Line to store parameetrs can't be null");
		}
		else if (getDepth() == 0) {
			return false;
		}
		else if (hasNext()){
			final StackRecord	rec = stack.get(0); 
			
			line.content = rec.buffer;
			line.lineNo = rec.lineNo;
			line.from = rec.from;
			line.len = rec.len;
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
			
			pushSource(rdr, true);
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

	private void pushSource(final Reader reader, final boolean closeRequired) {
		stack.add(0, new StackRecord(reader, closeRequired));
	}

	private boolean hasNext() throws IOException {
		final StackRecord	rec = stack.get(0);
		
		if (hasNextInside(rec)) {
			return true;
		}
		else {
			shiftBuffer(rec);
			appendBuffer(rec);
			while (!hasNextInside(rec)) {
				expandBuffer(rec);
				if (!appendBuffer(rec)) {
					return false;
				}
			}
			return true;
		}
	}

	private boolean hasNextInside(final StackRecord	rec) {
		for(int index = 0, maxIndex = rec.to - rec.from; index < maxIndex; index++) {
			if (rec.buffer[rec.from + index] == '\n') {
				rec.len = index;
				rec.to += index + 1;
				return true;
			}
		}
		return false;
	}

	private void shiftBuffer(final StackRecord rec) {
		System.arraycopy(rec.buffer, rec.from, rec.buffer, 0, rec.to - rec.from);
		rec.to -= rec.from;
		rec.from = 0;
		rec.len = 0;
	}

	private boolean appendBuffer(final StackRecord rec) {
		// TODO Auto-generated method stub
		return false;
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

		public Line(final int lineNo, final char[] content, final int from, final int to, final int len, final URI source) {
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
	}
	
	private static class StackRecord {
		private static final int	INITIAL_BUFFER_SIZE = 8192; 
		
		final Reader	rdr;
		final boolean	closeRequired;
		char[]			buffer;
		int				lineNo;
		int				from;
		int				to;
		int				len;
		
		public StackRecord(final Reader rdr, final boolean closeRequired) {
			this.rdr = rdr;
			this.closeRequired = closeRequired;
			this.buffer = new char[INITIAL_BUFFER_SIZE];
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
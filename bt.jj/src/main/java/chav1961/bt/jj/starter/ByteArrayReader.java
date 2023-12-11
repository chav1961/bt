package chav1961.bt.jj.starter;

class ByteArrayReader {
	private final byte[]	content;
	private int 			displ;
	
	public ByteArrayReader(final byte[] content) {
		this.content = content;
		this.displ = 0;
	}
	
	public int read() {
		return content[displ++];
	}
	
	public int readU2() {
		return (content[displ++] << 8) & 0xFF00 | (content[displ++] & 0xFF);
	}

	public int readU4() {
		return (content[displ++] << 24) & 0xFF000000 | (content[displ++] << 16) & 0xFF0000 | (content[displ++] << 8) & 0xFF00 | (content[displ++] & 0xFF); 
	}

	public long readU8() {
		return 0L | (content[displ++] << 56) & 0xFF0000000000L | (content[displ++] << 48) & 0xFF0000000000L | (content[displ++] << 40) & 0xFF0000000000L | (content[displ++] << 40) & 0xFF0000000000L | (content[displ++] << 32) & 0xFF00000000L | (content[displ++] << 24) & 0xFF000000 | (content[displ++] << 16) & 0xFF0000 | (content[displ++] << 8) & 0xFF00 | (content[displ++] & 0xFF);
	}
	
	public int read(byte[] buffer) {
		return read(buffer, 0, buffer.length);
	}
	
	public int read(byte[] buffer, int to, int len) {
		for (int index = 0; index < len; index++) {
			buffer[to + index] = content[displ + index];
		}
		displ += len;
		return len;
	}
	
	public char[] readUTF() {
		final int		length = readU2();
		int				count = 0;
		
		for(int index = displ; index < displ + length; index++, count++) {
			if ((content[index] & 0xE0) == 0xE0) {
				index += 2;
			}
			else if ((content[index] & 0xC0) == 0xC0) {
				index++;
			}
		}
		final char[]	result = new char[count];
		int		from = displ;
		
		for(int index = 0; index < result.length; index++) {
			if ((content[from] & 0x80) == 0) {
				result[index] = (char)content[from++];
			}
			else if ((content[from] & 0xE0) == 0xE0) {
				result[index] = (char)(((content[from] & 0x0F) << 12) + ((content[from + 1] & 0x3F) << 6) + (content[from + 2] & 0x3F));
				from += 3;
			}
			else {
				result[index] = (char)(((content[from] & 0x1F) << 6) + (content[from + 2] & 0x3F));
				from += 2;
			}
		}
		displ += length;
		return result;
	}
}

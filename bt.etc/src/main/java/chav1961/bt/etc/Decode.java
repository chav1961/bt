package chav1961.bt.etc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

public class Decode {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		walk(new File("e:/chav1961/temp/mzinana"));
	}

	private static void walk(final File f) {
		if (f.isFile()) {
			if (f.getName().endsWith(".java")) {
				decode(f);
			}
		}
		else {
			f.listFiles((ff)->{
				walk(ff);
				return false;
			});
		}
	}
	
	private static void decode(final File fIn) {
		// TODO Auto-generated method stub
		final File	fOut = new File(fIn.getParentFile(), fIn.getName()+".new");
		
		System.err.println("Parse "+fIn.getAbsolutePath());
		
		try(final Reader			rdr = new FileReader(fIn);
			final BufferedReader	brdr = new BufferedReader(rdr);
			final Writer			wr = new FileWriter(fOut, Charset.forName("cp1251"));
			final PrintWriter		ps = new PrintWriter(wr)) {
			
			String	line;
			
			while ((line = brdr.readLine()) != null) {
				final StringBuilder	sb = new StringBuilder();
				int		from = 0, to;
				
				while ((to = line.indexOf("\\u", from)) >= 0) {
					final int	value = Integer.parseInt(line.substring(to + 2, to +6 ), 16);
							
					sb.append(line, from, to).append((char)value);
					from = to + 6;
				}
				sb.append(line.substring(from));
				
				ps.println(sb.toString());
			}
			ps.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

package chav1961.bt.winsl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Demo {
	public static void main(final String[] args) throws IOException {
	    String host = "127.0.0.1";
	    short port = 8080;

	    if(args.length >= 1) {
	    	port = Short.parseShort(args[0]);
	    }

	    if(args.length >= 2) {
	    	host = args[1];
	    }

	    try(final ServerSocket server = new ServerSocket(port, 0, InetAddress.getByName(host))) {
	    	while(true) {
		        try(final Socket 		client = server.accept();
		        	final OutputStream	os = client.getOutputStream();
			        final PrintWriter 	out = new PrintWriter(os, true)) {
			        
			        out.write("HTTP/1.1 200 OK\n");
			        out.flush();
			        out.close();
		        }
	    	}
	    }
	}
}

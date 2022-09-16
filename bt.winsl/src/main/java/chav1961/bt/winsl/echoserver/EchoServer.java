package chav1961.bt.winsl.echoserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map.Entry;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import chav1961.purelib.basic.PureLibSettings;

public class EchoServer {
	public static final int		DEFAULT_PORT = 8080;
	public static HttpServer 	server = null;

	public static void main(final String[] args) throws IOException {
		if (args.length == 0) {
			server = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
		}
		else {
			try {
			    server = HttpServer.create(new InetSocketAddress(Integer.valueOf(args[0])), 0);
			} catch (NumberFormatException exc) {
				server = HttpServer.create(new InetSocketAddress(DEFAULT_PORT), 0);
			}
		}
		  
		final HttpContext context = server.createContext("/");
		
		context.setHandler(EchoServer::handleRequest);
		server.start();
	}

	public static void terminate(String[] args) throws IOException {
		server.stop(0);
	}	
	
	private static void handleRequest(final HttpExchange exchange) throws IOException {
		final String	response = printRequestInfo(exchange);
		
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes().length);
		
		try(final OutputStream 	os = exchange.getResponseBody()) {
			
			os.write(response.getBytes(PureLibSettings.DEFAULT_CONTENT_ENCODING));
		}
	}

	private static String printRequestInfo(final HttpExchange exchange) {
		final StringBuilder	sb = new StringBuilder();
		final Headers 		requestHeaders = exchange.getRequestHeaders();
		final String 		requestMethod = exchange.getRequestMethod();
		final URI 			requestURI = exchange.getRequestURI();

		sb.append("<!DOCTYPE html>\n");
		sb.append("<html>\n");
		sb.append("<head>\n");
		sb.append("<title>WINSL HTTP echo sever</title>\n");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		sb.append("</head>\n");
		sb.append("<body>\n");
		sb.append("<h3>Request from").append(requestURI).append(" ...</h3>\n");
		sb.append("<p><b>Request method:</b> ").append(requestMethod).append("</p>\n");
		sb.append("<p><b>Headers:</b> \n");
		sb.append("<ul>\n");
		for (Entry<String, List<String>> item : requestHeaders.entrySet()) {
			sb.append("<li>").append(item.getKey()).append(" = ").append(item.getValue()).append("</li>");
		}
		sb.append("</ul>\n");
		sb.append("</p>\n");
		sb.append("</body>\n");
		sb.append("</html>\n");
		
		return sb.toString();
  }
}
package chav1961.bt.nanoftp;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;

import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

public class Application {
	public static final String	ARG_FTP_PORT = "port";
	public static final String	ARG_FTP_ROOT = "root";
	public static final String	ARG_IP_MASK = "mask";
	public static final String	ARG_DEBUG_TRACE = "d";

	public static void main(String[] args) {
		final ArgParser	parser = new ApplicationArgParser();

		try(final LoggerFacade	logger = LoggerFacade.Factory.newInstance(URI.create(LoggerFacade.LOGGER_SCHEME+":err:/"))) {
			final ArgParser		parsed = parser.parse(args);
			final File			root = parsed.getValue(ARG_FTP_ROOT, File.class);
			final int			ftpPort = parsed.getValue(ARG_FTP_PORT, int.class);
			final boolean		needDebug = parsed.getValue(ARG_DEBUG_TRACE, boolean.class);
			final String		mask = parsed.getValue(ARG_IP_MASK, String.class);
			
			if (!mask.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
				throw new CommandLineParametersException("IP address mask invalid, must have NNN.NNN.NNN.NNN format");
			}
			else if (root.exists() && root.isDirectory() && root.canRead()) {
				final SimpleValidator	validator = new SimpleValidator(root);
				final InetAddress 		ipMask = InetAddress.getByName(mask);
				
				try(final ServerSocket	ss = new ServerSocket(ftpPort)) {

					Runtime.getRuntime().addShutdownHook(new Thread(()->{
						try {
							ss.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}));
					if (needDebug) {
						logger.message(Severity.info, "Nano FTP server listen on %1$d port", ftpPort);
					}
					for (;;) {
						try {
							final Socket		sock = ss.accept();
							
							if (isAddressAvailable((InetSocketAddress)sock.getRemoteSocketAddress(), ipMask, (InetSocketAddress)ss.getLocalSocketAddress())) {
								final FTPSession 	w = new FTPSession(sock, logger, root, validator, needDebug);
								final Thread		t = new Thread(w);
				
								t.setDaemon(true);
								t.start();
							}
							else {
								try {
									sock.close();
								} catch (IOException exc) {
								}
							}
						} catch (IOException exc) {
							break;
						}
					}
					if (needDebug) {
						logger.message(Severity.info, "Nano FTP server stopped");
					}
				}
			}
			else {
				throw new CommandLineParametersException("FTP root ["+root.getAbsolutePath()+"] is not exists, not a directory or not available for you");
			}
		} catch (CommandLineParametersException e) {
			System.err.println(e.getLocalizedMessage());
			System.err.println(parser.getUsage("bt.nanoftp"));
			System.exit(128);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(129);
		}
	}

	private static boolean isAddressAvailable(final InetSocketAddress remote, final InetAddress mask, final InetSocketAddress local) {
//		final byte[]	left = remote.getAddress().getAddress();
//		final byte[]	and = local.getAddress().getAddress();
//		final byte[]	right = local.getAddress().getAddress();
//		
//		for(int index = 0; index < left.length; index++) {
//			if ((left[index] & and[index]) != (right[index] & and[index])) {
//				return false;
//			}
//		}
		return true;
	}

	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new IntegerArg(ARG_FTP_PORT, true, false, "FTP server port to connect", new long[][]{new long[]{0, Short.MAX_VALUE}}),
			new FileArg(ARG_FTP_ROOT, true, true, "Root directory for FTP server users"),
			new StringArg(ARG_IP_MASK, false, "Available IP clients mask", "255.255.255.0"),
			new BooleanArg(ARG_DEBUG_TRACE, false, "Turn on debug trace on stderr", false)
		};
		
		private ApplicationArgParser() {
			super(KEYS);
		}
	}
}

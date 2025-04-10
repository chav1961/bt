package chav1961.bt.nanoftp;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.ProgressIndicator;

public class FTPSession implements Runnable, LoggerFacadeOwner {
	private static final String IPV4_ID = "1";
	private static final String IPV6_ID = "2";
	private static final File[]	EMPTY_FILE_ARRAY = new File[0];

	private static enum Commands {
		REIN(false, false, "", "Reset and reinitialize connection"),
		USER(false, false, "<UserName>", "Type user name to logon"),
		PASS(false, false, "<Password>", "Type password to logon"),
		CWD(false, false, "<NewDirectory>", "Change working directory"),
		CDUP(false, false, "", "Change current directory to it's parent"),
		LIST(false, false, "[<Directory>]", "List current or typed directory content in Unix 'ls' format"),
  		NLST(false, false, "[<Directory>]", "List names from current or typed directory"),
		STAT(false, false, "[<File>]", "Get status of the server, transmission or file/directory"),
		PWD(false, false, "", "Print current working directory name"),
  		XPWD(false, false, "", "Print current working directory name"),
		STRU(false, false, "{F|R|P}", "Define structiure of the file to transfer"),
		PASV(false, false, "", ""),
  		EPSV(false, false, "", ""),
		SYST(false, false, "", "Print OS name"),
		NOOP(false, false, "", "No operation. Usually used as 'ping'"),
		FEAT(false, false, "", "Get list of features for the given server"),
  		PORT(false, false, "", ""),
  		EPRT(false, false, "", ""),
  		RETR(false, false, "<File2Read>", "Read file content"),
  		MKD(false, false, "<NewDirectory>", "Create new directory on the server"),
		XMKD(false, false, "<NewDirectory>", "Create new directory on the server"),
  		RMD(false, false, "<Directory2Remove>", "Remove directory typed"),
  		XRMD(false, false, "<Directory2Remove>", "Remove directory typed"),
  		DELE(false, false, "<File2Remove>", "Remove file typed"),
  		TYPE(false, false, "{{A|E} [{N|T|A}] | I | L <byteSize>}", "Set transmission content type"),
  		MODE(false, false, "{S|B|C}", "Set transmission mode"),
  		SITE(false, false, "<Command> [<parameters>]", "Execute command in the server"),
  		ALLO(false, false, "<Space>", "Try to allocate space for file to store"),
  		APPE(false, false, "<File2Append>", "Append file content"),
  		STOR(false, false, "<File2Write>", "Write file content"),
  		REST(false, false, "<Marker>", "Restore transfer to typed marker"),
  		RNFR(false, false, "<File2Rename>", "Begin to rename file"),
  		RNTO(false, false, "<RenamedFileName>", "End to rename file"),
  		HELP(false, false, "[<CommandAbbr>]", "Print either command list or typed command description"),
  		SIZE(false, true, "", ""),
  		QUIT(true, false, "", "Close connection and quit");
		
		private final boolean	exitRequred;
		private final boolean	isFeature;
		private final String	args;
		private final String	descriptor;
		
		private Commands(final boolean exitRequired, final boolean isFeature, final String args, final String descriptor) {
			this.exitRequred = exitRequired;
			this.isFeature = isFeature;
			this.args = args;
			this.descriptor = descriptor;
		}

		public boolean isExitRequired() {
			return exitRequred;
		}

		public boolean isFeature() {
			return isFeature;
		}
		
		public String getArgs() {
			return args;
		}
		
		public String getDescriptor() {
			return descriptor;
		}
	}	
	
	private static enum MessageType {
		MSG_OPEN_CONN_FOR_LIST(125, " Opening ASCII mode data connection for file list.\r\n"),
		MSG_OPEN_BIN_CONN_FOR_FILE(150, " Opening binary mode data connection for file %1$s\r\n"),
		MSG_OPEN_ASCII_CONN_FOR_FILE(150, " Opening ASCII mode data connection for file %1$s\r\n"),
		MSG_COMMAND_OK(200, " Command OK\r\n"),
		MSG_COMMAND_IGNORED(202, " Command recognized but ignored.\r\n"),
		MSG_SYSTEM_STATUS(211, " System status OK\r\n"),
		MSG_EXTENSIONS_START(211, "-Extensions supported:\r\n"),
		MSG_EXTENSIONS_END(211, " END\r\n"),
		MSG_COMMANDS_START(211, "-Commands supported:\r\n"),
		MSG_COMMANDS_END(211, " END\r\n"),
		MSG_COMMANDS_HELP(211, " Command: %1$s %2$s - %3$s\r\n"),
		MSG_COMMANDS_HELP_MISSING(211, " Command %1$s is not supported\r\n"),
		MSG_TRANSFER_STATUS(213, " File transfer status: transferring %1$d bytes, error bit is %1$b\r\n"),
		MSG_FILE_SIZE(213, " %1$d\r\n"),
		MSG_SYSTEM(215, " %1$s\r\n"),
		MSG_WELCOME(220, " Welcome to the nano FTP-Server\r\n"),
		MSG_CONNECTION_RESET(220, " Connection reset. Type 'USER' or 'ACCT' command to connect\r\n"),
		MSG_CLOSING_CONN(221, " Closing connection\r\n"),
		MSG_TRANSFER_COMPLETED(226, " Transfer completed\r\n"),
		MSG_TRANSFER_COMPLETED_DETAILED(226, " Transfer completed, %1$d bytes transmitted, avg speed is %2$.3f bytes/sec.\r\n"),
		MSG_ENTERING_PASSIVE_MODE(227, " Entering Passive Mode (%1$s,%2$s,%3$s,%4$s,%5$d,%6$d)\r\n"),
		MSG_ENTERING_EXTENDED_PASSIVE_MODE(229, " Entering Extended Passive Mode (|||%1$d|)\r\n"),
		MSG_WELCOME_USER_LOGGED(230, "-Welcome to server\r\n"),
		MSG_USER_LOGGED(230, " User logged in successfully\r\n"),
		MSG_DIRECTORY_CREATED(250, " Directory %1$s successfully created\r\n"),	  
		MSG_DIRECTORY_CHANGED(250, " The current directory has been changed to %1$s\r\n"),
		MSG_DIRECTORY_REMOVED(250, " Directory %1$s successfully removed\r\n"),	  
		MSG_FILE_REMOVED(250, " File %1$s successfully removed\r\n"),	  
		MSG_CURRENT_DIR(257, " \"%1$s\"\r\n"),	  
		MSG_USER_NAME_OK(331, " User name okay, need password\r\n"),
		MSG_AWAITING_CONTINUATION(350, " Requested file action pending further information.\r\n"),
		MSG_NO_DATA_CONNECTION(425, " No data connection was established\r\n"),
		MSG_UNKNOWN_COMMAND(500, " Unknown command\r\n"),
		MSG_ILLEGAL_ARGUMENT(501, " Illegal argument [%1$s]\r\n"),
		MSG_MISSING_FILE_NAME(501, " File name missing\r\n"),
		MSG_MISSING_RNFR_BEFORE_RNTO(503, " RNTO command without RNFR preceding\r\n"),
		MSG_UNSUPPORTED_ARGUMENT(504, " Argument [%1$s] is not supported by the server\r\n"),
		MSG_TRANSFER_MODE_NOT_SET(504, " Transfer mode is not set yet\r\n"),
		MSG_USER_ALREADY_LOGGED(530," User already logged in\r\n"),
		MSG_USER_NOT_LOGGED(530," Not logged in\r\n"),
		MSG_USER_NOT_ENTERED(530," User name is not entered yet\r\n"),
		MSG_FAILURE_FILE_UNAVAILABLE(550, " Requested action not taken. File %1$s unavailable.\r\n"),
		MSG_FAILURE_FILE_NOT_EXISTS(550, " File %1$s does not exist\r\n"),
		MSG_FAILURE_FILE_ALREADY_EXISTS(550, " File %1$s already exist\r\n"),
		MSG_FAILURE_DIRECTORY_NOT_CREATED(550, " Failed to create new directory %1$s\r\n");
		;
		  
		private final int		code;
		private final String	message;
		  
		private MessageType(final int code, final String message) {
			this.code = code;
			this.message = message;
		}
		  
		public int getCode() {	
			return code;
		}
		  
		public String getMessage() {
			return message;
		}
	}
  
	private static enum ConnectionMode {
		ACTIVE,
		PASSIVE,
		NONE
	}

	private static enum TransferType {
		ASCII,
		BINARY,
		UNKNOWN
	}

	private static enum LoggingStatus {
		NOTLOGGEDIN,
		USERNAMEENTERED,
		LOGGEDIN
	}

	@FunctionalInterface
	private static interface Sender {
		void send(String content) throws IOException;
	}
	
	private final Socket 			controlSocket;
	
	private final LoggerFacade		logger;
	private final int				dataPort;
	private final File 				root;
	private final DataConnection	conn = new DataConnection();
	private final boolean 			debugMode;
	private final SimpleValidator	validator;

	private String 				currDirectory;
	private Writer 				controlOutWriter;
	private TransferType 		transferMode;
	private LoggingStatus 		currentLoggingStatus;
	private String				currentUser;
	private long				restoreLocation;
	private File				oldFile;
	private volatile DataCopier	copier = null;
  
  /**
   * <p>Constructor of the class instance</p>
   * 
   * @param client client socket to interact. Can't be null.
   * @param validator validator to check user/password credentials. Can't be null.
   * @param debugMode turn on debug trace
   */
  public FTPSession(final Socket client, final int dataPort, final LoggerFacade logger, final File root, final SimpleValidator validator, final boolean debugMode) {
    this.controlSocket = client;
    this.dataPort = dataPort;
    this.logger = logger;
    this.validator = validator;
    this.debugMode = debugMode;
    this.root = root;
    clearSettings();
  }

  @Override
  public LoggerFacade getLogger() {
	  return logger;
  }
  
  @Override
  public void run() {
	  debug("FTP session started, current working directory is <" + this.currDirectory + ">");

	  try(final BufferedReader	controlIn = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
		  final Writer			controlOutWriter = new OutputStreamWriter(controlSocket.getOutputStream())) {
		  String	line;

		  this.controlOutWriter = controlOutWriter;
		  sendAnswer(MessageType.MSG_WELCOME);
		  while ((line = controlIn.readLine()) != null) {
			  if (!executeCommand(line)) {
				  break;
			  }
		  }
	  } catch (Exception e) {
		  e.printStackTrace();
	  } finally {
		  try {
			  controlSocket.close();
			  debug("FTP session ended");
		  } catch (IOException e) {
			  debug("Could not close socket");
		  }
	  }
  }

  /**
   * Main command dispatcher method. Separates the command from the arguments and
   * dispatches it to single handler functions.
   * 
   * @param c the raw input from the socket consisting of command and arguments
   * @throws IOException 
   */
  private boolean executeCommand(final String c) throws IOException {
	  final int 	blank = c.indexOf(' ');
	  final String 	command = blank == -1 ? c : c.substring(0, blank);
	  final String 	args = blank == -1 ? "" : c.substring(blank + 1).trim();
	  
	  debug("Command: " + command + ", args: <" + args + ">");
	  try {
		  final Commands	cmd = Commands.valueOf(command.toUpperCase());
		  	
		  try {
			  switch (cmd) {
			  	case REIN :
			  		handleRein();
			  		break;
			  	case USER :
			  		handleUser(args);
			  		break;
			  	case PASS :
			  		handlePass(args);
			  		break;
			  	case CDUP :
			  		handleCwd("..");
			  		break;
			  	case CWD :
			  		handleCwd(args);
			  		break;
			  	case STAT :
			  		handleStat(args);
			  		break;
			  	case LIST :
			  		handleList(args);
			  		break;
			  	case NLST :
			  		handleNlst(args);
			  		break;
			  	case PWD : case XPWD :
			  		handlePwd();
			  		break;
			  	case PASV :
			  		handlePasv();
			  		break;
			  	case EPSV :
			  		handleEpsv();
			  		break;
			  	case SYST :
			  		handleSyst();
			  		break;
			  	case FEAT :
			  		handleFeat();
			  		break;
			  	case PORT :
			  		handlePort(args);
			  		break;
			  	case EPRT :
			  		handleEPort(args);
			  		break;
			  	case RETR :
			  		handleRetr(args);
			  		break;
			  	case MKD : case XMKD :
			  		handleMkd(args);
			  		break;
			  	case RMD : case XRMD :
			  		handleRmd(args);
			  		break;
			  	case DELE :
			  		handleDele(args);
			  		break;
			  	case SITE :
			  		handleSite();
			  		break;
			  	case TYPE :
			  		handleType(args);
			  		break;
			  	case MODE :
			  		handleMode(args);
			  		break;
			  	case ALLO :
			  		handleAllo(args);
			  		break;
			  	case APPE :
			  		handleStor(args, true);
			  		break;
			  	case STOR :
			  		handleStor(args, false);
			  		break;
			  	case REST :
			  		handleRest(args);
			  		break;
			  	case RNFR :
			  		handleRnfr(args);
			  		break;
			  	case RNTO :
			  		handleRnto(args);
			  		break;
			  	case NOOP :
			  		handleNoop();
			  		break;
			  	case HELP :
			  		handleHelp(args);
			  		break;
			  	case STRU :
			  		handleStru(args);
			  		break;
			  	case SIZE :
			  		handleSize(args);
			  		break;
			  	case QUIT :
			  		handleQuit();
			  		break;
			  	default:
			  		throw new UnsupportedOperationException("Command ["+c+"] is not supported yet");
			  }
			  if (cmd != Commands.RNFR) {
				  oldFile = null;
			  }
			  return !cmd.isExitRequired();
		  } catch (IllegalArgumentException exc) {
			  exc.printStackTrace();
			  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT);
			  return true;
		  }
	  } catch (IllegalArgumentException exc) {
		  sendAnswer(MessageType.MSG_UNKNOWN_COMMAND);
		  return true;
	  }
  }

  /**
   * Handler for REIN command. Re-initialize user connection.
   * 
   * @throws IOException 
   */
  private void handleRein() throws IOException {
	  clearSettings();
	  sendAnswer(MessageType.MSG_CONNECTION_RESET);
  }  
  
  /**
   * Handler for USER command. User identifies the client.
   * 
   * @param userName user name entered by the user
   * @throws IOException 
   */
  private void handleUser(final String userName) throws IOException {
	  if (Utils.checkEmptyOrNullString(userName)) {
		  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, userName);
	  }
	  else {
		  switch (currentLoggingStatus) {
			case LOGGEDIN		:
				sendAnswer(MessageType.MSG_USER_ALREADY_LOGGED);
				break;
			case USERNAMEENTERED:
			case NOTLOGGEDIN	:
			    if (validator.isUserExists(userName)) {
					sendAnswer(MessageType.MSG_USER_NAME_OK);
					currentUser = userName;
					currentLoggingStatus = LoggingStatus.USERNAMEENTERED;
			    } else {
			    	sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
			    }
				break;
			default:
				throw new UnsupportedOperationException("Logging status ["+currentLoggingStatus+"] is not supported yet");
		  }
	  }
  }

  /**
   * Handler for PASS command. PASS receives the user password and checks if it's
   * valid.
   * 
   * @param password Password entered by the user
   * @throws IOException 
   */
  private void handlePass(final String password) throws IOException {
	  if (Utils.checkEmptyOrNullString(password)) {
		  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, password);
	  }
	  else {
		  switch (currentLoggingStatus) {
			case LOGGEDIN		:
				sendAnswer(MessageType.MSG_USER_ALREADY_LOGGED);
				break;
			case NOTLOGGEDIN	:
				sendAnswer(MessageType.MSG_USER_NOT_ENTERED);
				break;
			case USERNAMEENTERED:
				if (validator.areCredentialsValid(currentUser, password.toCharArray())) {
					currentLoggingStatus = LoggingStatus.LOGGEDIN;
					sendAnswer(MessageType.MSG_WELCOME_USER_LOGGED);
					sendAnswer(MessageType.MSG_USER_LOGGED);
				}
				else {
					sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
				}
				break;
			default:
				throw new UnsupportedOperationException("Logging status ["+currentLoggingStatus+"] is not supported yet");
		  }
	  }
  }

  /**
   * Handler for CWD (change working directory) command.
   * 
   * @param args New directory to be created
   * @throws IOException 
   */
  private void handleCwd(final String args) throws IOException {
	  if (isCommandAvailableNow()) {
		  final File	current = getFileDesc(args);
		  
		  if (current.exists() && current.isDirectory()) {
			  currDirectory = getFileName(current);
			  sendAnswer(MessageType.MSG_DIRECTORY_CHANGED, currDirectory);
		  } else {
			  debug("Not found: <"+current.getAbsolutePath()+">");
			  sendAnswer(MessageType.MSG_FAILURE_FILE_UNAVAILABLE, getFileName(current));
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }

  /**
   * Handler for STAT command. Lists the directory content in a long format
   * 
   * @param args The directory to be listed
   * @throws IOException 
   */
  private void handleStat(final String args) throws IOException {
	  if (isCommandAvailableNow()) {
		  if (Utils.checkEmptyOrNullString(args)) {
			  if (copier == null) {
				  sendAnswer(MessageType.MSG_SYSTEM_STATUS);
			  }
			  else {
				  final DataCopier temp = copier;
				  sendAnswer(MessageType.MSG_TRANSFER_STATUS, temp.processed, temp.error);
			  }
		  }
		  else {
			  final File	current = getFileDesc(args);
			  final File[] 	dirContent = getDirContent(current);
			
			  if (dirContent == null) {
				  sendAnswer(MessageType.MSG_FAILURE_FILE_NOT_EXISTS, getFileName(current));
			  } else {
				  sendDirContent(dirContent, this::sendCommandLine);
			  }
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }
  
  /**
   * Handler for LIST command. Lists the directory content in a long format
   * 
   * @param args The directory to be listed
   * @throws IOException 
   */
  // https://cr.yp.to/ftp/list/binls.html
  private void handleList(final String args) throws IOException {
	  if (isCommandAvailableNow()) {
		  if (!conn.isConnectionValid()) {
			  sendAnswer(MessageType.MSG_NO_DATA_CONNECTION);
		  } 
		  else {
			  final File	current = getFileDesc(args == null || args.startsWith("-") ? "" : args);
			  final File[] 	dirContent = getDirContent(current);
			
			  if (dirContent == null) {
				  sendAnswer(MessageType.MSG_FAILURE_FILE_NOT_EXISTS, getFileName(current));
			  } else {
				  sendDirContent(dirContent, this::sendDataLine);
				  closeDataConnection();
			  }
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }

  
  /**
   * Handler for NLST (Named List) command. Lists the directory content in a short
   * format (names only)
   * 
   * @param args The directory to be listed
   * @throws IOException 
   */
  // https://cr.yp.to/ftp/list/binls.html
  private void handleNlst(String args) throws IOException {
	  if (isCommandAvailableNow()) {
		  if (!conn.isConnectionValid()) {
			  sendAnswer(MessageType.MSG_NO_DATA_CONNECTION);
		  } 
		  else if (args == null || isFileNameValid(args)) {
			  final File	current = getFileDesc(args == null ? "" : args);
			  final File[] 	dirContent = getDirContent(current);
			
			  if (dirContent == null) {
				  sendAnswer(MessageType.MSG_FAILURE_FILE_NOT_EXISTS, getFileName(current));
			  } else {
				  sendAnswer(MessageType.MSG_OPEN_CONN_FOR_LIST);

				  for (File content : dirContent) {
					  sendDataLine(content.getName());
				  }

				  sendAnswer(MessageType.MSG_TRANSFER_COMPLETED);
				  closeDataConnection();
			  }
		  }
		  else {
			  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, args);
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }

  /**
   * Handler for PWD (Print working directory) command. Returns the path of the
   * current directory back to the client.
   * @throws IOException 
   */
  private void handlePwd() throws IOException {
	  if (isCommandAvailableNow()) {
		  sendAnswer(MessageType.MSG_CURRENT_DIR, getFileName(getFileDesc(currDirectory)));
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }
  
  /**
   * Open a new data connection socket and wait for new incoming connection from
   * client. Used for passive mode.
   * 
   * @param port Port on which to listen for new incoming connection
   */
  private SocketAddress openDataConnectionPassive(int port) {
	  conn.close();
	  return conn.openPassive(port);
  }

  /**
   * Open a new data connection socket and wait for new incoming connection from
   * client. Used for passive mode.
   * 
   * @param port Port on which to listen for new incoming connection
   */
  private void waitDataConnectionPassive(int port) {
	  conn.waitPassive(port);
  }
  
  /**
   * Connect to client socket for data connection. Used for active mode.
   * 
   * @param ipAddress Client IP address to connect to
   * @param port      Client port to connect to
   */
  private void openDataConnectionActive(String ipAddress, int port) {
	  if (conn.mode != ConnectionMode.ACTIVE) {
		  if (conn.mode != ConnectionMode.NONE) {
			  conn.close();
		  }
		  conn.openActive(ipAddress, port);
	  }
  }

  /**
   * Close previously established data connection sockets and streams
   */
  private void closeDataConnection() {
	  conn.close();
      debug("Data connection was closed");
  }


  /**
   * A helper for the NLST command. The directory name is obtained by appending
   * "args" to the current directory
   * 
   * @param args The directory to list
   * @return an array containing names of files in a directory. If the given name
   *         is that of a file, then return an array containing only one element
   *         (this name). If the file or directory does not exist, return nul.
   */
  private File[] getDirContent(final File current) {
	  if (current.exists()) {
		  if (current.isDirectory()) {
			  final File[] items = current.listFiles();
		    	
			  return items == null ? EMPTY_FILE_ARRAY : items; 
		  }
		  else {
			  return new File[] {current};
		  }
	  }
	  else {
		  return null;
	  }
  }

  /**
   * Handler for the PORT command. The client issues a PORT command to the server
   * in active mode, so the server can open a data connection to the client
   * through the given address and port number.
   * 
   * @param args The first four segments (separated by comma) are the IP address.
   *             The last two segments encode the port number (port = seg1*256 +
   *             seg2)
   * @throws IOException 
   */
  private void handlePort(String args) throws IOException {
	  if (isCommandAvailableNow()) {
		  if (args.matches("\\d+,\\d+,\\d+,\\d+,\\d+,\\d+")) {
			  final String[] 	content = args.split(",");
			  final String 		hostName = content[0].trim() + '.' + content[1].trim() + '.' + content[2].trim() + '.' + content[3].trim();
			  final int 		port = Integer.parseInt(content[4].trim()) * 256 + Integer.parseInt(content[5].trim());
			
			  openDataConnectionActive(hostName, port);
			  sendAnswer(MessageType.MSG_COMMAND_OK);
		  }
		  else {
			  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, args);
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }

  /**
   * Handler for the EPORT command. The client issues an EPORT command to the
   * server in active mode, so the server can open a data connection to the client
   * through the given address and port number.
   * 
   * @param args This string is separated by vertical bars and encodes the IP
   *             version, the IP address and the port number
   * @throws IOException 
   */
  private void handleEPort(final String args) throws IOException {
	  // Example arg: |2|::1|58770| or |1|132.235.1.2|6275|
	  if (isCommandAvailableNow()) {
		  if (args.matches("\\|\\d\\|.*\\|\\d+\\|")) {
				final String[] splitArgs = args.split("\\|");
				
				if (!IPV4_ID.equals(splitArgs[1]) || !IPV6_ID.equals(splitArgs[1])) {
					sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, args);
				}
				else {
					openDataConnectionActive(splitArgs[2], Integer.parseInt(splitArgs[3]));
					sendAnswer(MessageType.MSG_COMMAND_OK);
				}
		  }
		  else {
			  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, args);
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }

  /**
   * Handler for PASV command which initiates the passive mode. In passive mode
   * the client initiates the data connection to the server. In active mode the
   * server initiates the data connection to the client.
   * @throws IOException 
   */
  private void handlePasv() throws IOException {
    final SocketAddress	addr = openDataConnectionPassive(0);
    final String	host = ((InetSocketAddress)addr).getAddress().getHostAddress();
    final int		port = ((InetSocketAddress)addr).getPort();
    final String[]	ip = host.split("\\."); 
    final int 		p1 = port / 256;
    final int 		p2 = port % 256;
    
    sendAnswer(MessageType.MSG_ENTERING_PASSIVE_MODE, ip[0], ip[1], ip[2], ip[3], p1, p2);
    waitDataConnectionPassive(port);
  }

  /**
   * Handler for EPSV command which initiates extended passive mode. Similar to
   * PASV but for newer clients (IPv6 support is possible but not implemented
   * here).
   * @throws IOException 
   */
  private void handleEpsv() throws IOException {
	  if (isCommandAvailableNow()) {
		  final SocketAddress	addr = openDataConnectionPassive(0);
		  final int	port = ((InetSocketAddress)addr).getPort();
		  
		  sendAnswer(MessageType.MSG_ENTERING_EXTENDED_PASSIVE_MODE, port);
		  waitDataConnectionPassive(port);
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }

  /**
   * Handler for the QUIT command.
   * @throws IOException 
   */
  private void handleQuit() throws IOException {
	  sendAnswer(MessageType.MSG_CLOSING_CONN);
  }

  /**
   * Handler for the SYST command. Return OS name.
   * @throws IOException 
   */
  private void handleSyst() throws IOException {
	  sendAnswer(MessageType.MSG_SYSTEM, System.getProperty("os.name"));
  }

  /**
   * Handler for the FEAT (features) command. Feat transmits the
   * abilities/features of the server to the client. Needed for some ftp clients.
   * This is just a dummy message to satisfy clients, no real feature information
   * included.
   * @throws IOException 
   */
  private void handleFeat() throws IOException {
	  sendAnswer(MessageType.MSG_EXTENSIONS_START);
	  for(Commands item : Commands.values()) {
		  if (item.isFeature()) {
			  sendCommandLine(' ' + item.name() + '\r' + '\n');
		  }
	  }
	  sendAnswer(MessageType.MSG_EXTENSIONS_END);
  }

  /**
   * Handler for the MKD (make directory) command. Creates a new directory on the
   * server.
   * 
   * @param args Directory name
   * @throws IOException 
   */
  private void handleMkd(String args) throws IOException {
	  if (isCommandAvailableNow()) {
		  if (isFileNameValid(args)) {
			  final File dir = getFileDesc(args);

			  if (!dir.mkdir()) {
				  sendAnswer(MessageType.MSG_FAILURE_DIRECTORY_NOT_CREATED);
				  debug("Failed to create new directory");
			  }
			  else {
				  sendAnswer(MessageType.MSG_DIRECTORY_CREATED, getFileName(dir));
			  }
		  }
		  else {
			  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, args);
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }

  /**
   * Handler for RMD (remove directory) command. Removes a directory.
   * 
   * @param dir directory to be deleted.
   * @throws IOException 
   */
  private void handleRmd(String dir) throws IOException {
	  if (isCommandAvailableNow()) {
		  if (isFileNameValid(dir)) {
			  final File d = getFileDesc(dir);

			  if (d.exists() && d.isDirectory()) {
				  d.delete();

				  sendAnswer(MessageType.MSG_DIRECTORY_REMOVED, getFileName(d));
			  } 
			  else {
				  sendAnswer(MessageType.MSG_FAILURE_FILE_UNAVAILABLE, getFileName(d));
			  }
		  } else {
			  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, dir);
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }

  /**
   * Handler for DELE (remove file) command. Removes a file.
   * 
   * @param file file to be deleted.
   * @throws IOException 
   */
  private void handleDele(String file) throws IOException {
	  if (isCommandAvailableNow()) {
		  if (isFileNameValid(file)) {
			  File f = getFileDesc(file);

			  if (f.exists() && f.isFile()) {
				  f.delete();

				  sendAnswer(MessageType.MSG_FILE_REMOVED, getFileName(f));
			  } 
			  else {
				  sendAnswer(MessageType.MSG_FAILURE_FILE_UNAVAILABLE, getFileName(f));
			  }
		  } else {
			  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, file);
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }

  /**
   * Handler for the SITE command. Simply ignore it.
   * 
   * @throws IOException 
   */
  private void handleSite() throws IOException {
	  sendAnswer(MessageType.MSG_COMMAND_IGNORED);
  }
  
  
  /**
   * Handler for the TYPE command. The type command sets the transfer mode to
   * either binary or ascii mode
   * 
   * @param mode Transfer mode: "a" for Ascii. "i" for image/binary.
   * @throws IOException 
   */
  private void handleType(final String mode) throws IOException {
	  if (isCommandAvailableNow()) {
		  final String[]	modes = mode.split("\\s+");
		  
		  switch (modes[0].charAt(0)) {
		  	case 'A' : case 'a' :  
		  		if (modes.length > 1) {
		  			if ("NTCntc".indexOf(modes[1].charAt(0)) < 0) {
				  		sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, mode);
		  			}
		  			else if ("Nn".indexOf(modes[1].charAt(0)) < 0) {
				  		sendAnswer(MessageType.MSG_UNSUPPORTED_ARGUMENT, mode);
		  			}
		  			else {
				  		transferMode = TransferType.ASCII;
						sendAnswer(MessageType.MSG_COMMAND_OK);
		  			}
		  		}
		  		else {
			  		transferMode = TransferType.ASCII;
					sendAnswer(MessageType.MSG_COMMAND_OK);
		  		}
				break;
		  	case 'I' : case 'i' :  
		  		transferMode = TransferType.BINARY;
		  		sendAnswer(MessageType.MSG_COMMAND_OK);
				break;
		  	case 'L' : case 'l' :
		  		if (modes.length > 1) {
		  			try {
		  				if (Integer.parseInt(modes[1]) != 8) {
					  		sendAnswer(MessageType.MSG_UNSUPPORTED_ARGUMENT, mode);
		  				}
		  				else {
		  			  		transferMode = TransferType.BINARY;
		  			  		sendAnswer(MessageType.MSG_COMMAND_OK);
		  				}
		  			} catch (NumberFormatException exc) {
				  		sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, mode);
		  			}
		  		}
		  		else {
			  		sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, mode);
		  		}
		  		break;
		  	case 'E' : case 'e' :
		  		sendAnswer(MessageType.MSG_UNSUPPORTED_ARGUMENT, mode);
		  	default :
		  		sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, mode);
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }

  /**
   * Handler for the MODE command. The type command sets the transfer stream type
   * 
   * @param mode Transfer stream type: "S" for stream. "B" for block, "C" for compressed.
   * @throws IOException 
   */
  private void handleMode(final String mode) throws IOException {
	  if (isCommandAvailableNow()) {
		  if ("SsBbCc".indexOf(mode) < 0) {
			  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, mode);
		  }
		  else if ("BbCc".indexOf(mode) >= 0) {
			  sendAnswer(MessageType.MSG_UNSUPPORTED_ARGUMENT, mode);
		  }
		  else {
			  sendAnswer(MessageType.MSG_COMMAND_OK, mode);
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }
  
  /**
   * Handler for the RETR (retrieve) command. Retrieve transfers a file from the
   * ftp server to the client.
   * 
   * @param file The file to transfer to the user
   * @throws IOException 
   */
  private void handleRetr(String file) throws IOException {
	  if (isCommandAvailableNow()) {
		  final File f = getFileDesc(file);

		  if (!f.exists()) {
			  sendAnswer(MessageType.MSG_FAILURE_FILE_NOT_EXISTS);
		  }
		  else {
			  final long	start = System.currentTimeMillis();
			  final long	end;
			  long	size = 0;
			  
			  switch (transferMode) {
			  	case ASCII:
			        sendAnswer(MessageType.MSG_OPEN_ASCII_CONN_FOR_FILE, f.getName());
			
			        debug("Starting file transmission of " + f.getName() + " in ASCII mode");
			        try(final Reader 	rin = new FileReader(f);
			        	final Writer	rout = new OutputStreamWriter(conn.getOutputStream())) {

			        	try {
				        	if (restoreLocation > 0) {
				        		rin.skip(restoreLocation);
				        		restoreLocation = 0;
				        	}
			  	        	size = Utils.copyStream(rin, rout);
			        	} catch (IOException e) {
			        		debug("Could not read from or write to file streams");
			        	}
			        } catch (IOException e) {
			        	debug("Could not create file streams");
			        } finally {
			        	closeDataConnection();
			        }
			        debug("Completed file transmission of " + f.getName());
			        end = System.currentTimeMillis();
			        sendAnswer(MessageType.MSG_TRANSFER_COMPLETED_DETAILED, size, 0.001 * size / Math.max(1, end - start));
					break;
				case BINARY:
			        sendAnswer(MessageType.MSG_OPEN_BIN_CONN_FOR_FILE, f.getName());
			
			        debug("Starting file transmission of " + f.getName() + " in BINARY mode");
			        try(final InputStream 	fin = new FileInputStream(f);
			        	final OutputStream	fout = conn.getOutputStream()){
			        	
			        	try {
				        	if (restoreLocation > 0) {
				        		fin.skip(restoreLocation);
				        		restoreLocation = 0;
				        	}
			        		size = Utils.copyStream(fin, fout);
			        	} catch (IOException e) {
			        		debug("Could not read from or write to file streams");
			        	}
			        } catch (Exception e) {
			        	debug("Could not create file streams");
			        } finally {
			        	closeDataConnection();
			        }
			        debug("Completed file transmission of " + f.getName());
			        end = System.currentTimeMillis();
			        sendAnswer(MessageType.MSG_TRANSFER_COMPLETED_DETAILED, size, 0.001 * size / Math.max(1, end - start));
					break;
				case UNKNOWN :
					sendAnswer(MessageType.MSG_TRANSFER_MODE_NOT_SET);
					break;
				default :
					throw new UnsupportedOperationException("Transafer mode ["+transferMode+"] is not supporte yet");
			  }
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }

  /**
   * Handler for ALLO (Allocate) command. Allocate space on the disk for future file.
   * Will be ignored.
   * 
   * @param space size to allocate
   * @throws IOException 
   */
  private void handleAllo(final String size) throws IOException {
	  if (isCommandAvailableNow()) {
		  if (Utils.checkEmptyOrNullString(size)) {
			  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, size);
		  }
		  else {
			  try{
				  Long.parseLong(size);
				  sendAnswer(MessageType.MSG_COMMAND_IGNORED);
			  } catch (NumberFormatException exc) {
				  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, size);
			  }
		  }
	  }
  }  
  
  /**
   * Handler for STOR (Store) command. Store receives a file from the client and
   * saves it to the ftp server. If server file already exists, it will be overwritten.
   * 
   * @param file The file that the user wants to store on the server
   * @throws IOException 
   */
  private void handleStor(final String file, final boolean append) throws IOException {
	  if (isCommandAvailableNow()) {
		  if (Utils.checkEmptyOrNullString(file)) {
			  sendAnswer(MessageType.MSG_MISSING_FILE_NAME);
		  } 
		  else {
			  final File 	f = getFileDesc(file);
			  final long	start = System.currentTimeMillis();
			  final long	end;
			  long			size = 0;

			  switch (transferMode) {
			  	case ASCII		:
		            sendAnswer(MessageType.MSG_OPEN_ASCII_CONN_FOR_FILE, f.getName());
					
		            try(final Reader	rdr = new InputStreamReader(conn.getInputStream());
		            	final Writer 	rout = new OutputStreamWriter(new FileOutputStream(f, append))){
			            
		            	try {
		            		size = Utils.copyStream(rdr, rout);
			            } catch (IOException e) {
			            	debug("Could not read/write streams");
			            }
		            } catch (IOException e) {
		            	debug("Could not create streams");
		            }
		            end = System.currentTimeMillis();
		            sendAnswer(MessageType.MSG_TRANSFER_COMPLETED_DETAILED, size, 0.001 * size / Math.max(1, end - start));
		            debug("Completed receiving file " + f.getName());

		            break;						
				case BINARY		:
		            sendAnswer(MessageType.MSG_OPEN_BIN_CONN_FOR_FILE, f.getName());

		            debug("Start receiving file " + f.getName() + " in BINARY mode");
		            try(final InputStream 	fin = conn.getInputStream(); 
		            	final OutputStream 	fout = new FileOutputStream(f, append)){
			            
			            try {
			            	size = Utils.copyStream(fin, fout);
			            } catch (IOException e) {
			            	debug("Could not read from or write to file streams");
			            }
		            } catch (Exception e) {
		            	debug("Could not create file streams");
		            } finally {
		            	closeDataConnection();
		            }			            	
		            debug("Completed receiving file " + f.getName());
		            end = System.currentTimeMillis();
		            sendAnswer(MessageType.MSG_TRANSFER_COMPLETED_DETAILED, size, 0.001 * size / Math.max(1, end - start));

		            break;
				case UNKNOWN	:
		  			sendAnswer(MessageType.MSG_TRANSFER_MODE_NOT_SET);
		  			break;
		  		default :
		  			throw new UnsupportedOperationException("Transafer mode ["+transferMode+"] is not supporte yet");
			  }
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }

  /**
   * Handler for REST (Restore) command. Simply ignored.
   * 
   * @param file The marker inside the last file
   * @throws IOException 
   */
  private void handleRest(final String displ) throws IOException {
	  if (isCommandAvailableNow()) {
		  try{
			  restoreLocation = Long.parseLong(displ);
			  sendAnswer(MessageType.MSG_COMMAND_IGNORED);
		  } catch (NumberFormatException exc) {
			  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, displ);
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }  

  /**
   * Handler for RNFR (Rename from) command. Stores file name to rename.
   * 
   * @param file The file to rename
   * @throws IOException 
   */
  private void handleRnfr(final String file) throws IOException {
	  if (isCommandAvailableNow()) {
		  final File	f = getFileDesc(file);
		  
		  if (f.exists() && f.isFile()) {
			  oldFile = f;
			  sendAnswer(MessageType.MSG_AWAITING_CONTINUATION);
		  }
		  else {
			  sendAnswer(MessageType.MSG_FAILURE_FILE_NOT_EXISTS);
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }  

  /**
   * Handler for RNTO (Rename to) command. REnames file.
   * 
   * @param file The file to rename
   * @throws IOException 
   */
  private void handleRnto(final String file) throws IOException {
	  if (isCommandAvailableNow()) {
		  final File	f = getFileDesc(file);
		  
		  if (f.exists()) {
			  sendAnswer(MessageType.MSG_FAILURE_FILE_ALREADY_EXISTS, getFileName(f));
		  }
		  else if (oldFile == null) {
			  sendAnswer(MessageType.MSG_MISSING_RNFR_BEFORE_RNTO);
		  }
		  else {
			  if (oldFile.renameTo(f)) {
				  sendAnswer(MessageType.MSG_COMMAND_OK);
			  }
			  else {
				  sendAnswer(MessageType.MSG_FAILURE_FILE_UNAVAILABLE, getFileName(f));
			  }
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }  

  /**
   * Handler for NOOP (No Operation). Requires simple response to it.
   * 
   * @throws IOException 
   */
  private void handleNoop() throws IOException {
	  sendAnswer(MessageType.MSG_COMMAND_OK);
  }  

  /**
   * Handler for HELP (Help). Requires simple response to it.
   * 
   * @throws IOException 
   */
  private void handleHelp(final String name) throws IOException {
	  if (Utils.checkEmptyOrNullString(name)) {
		  sendAnswer(MessageType.MSG_COMMANDS_START);
		  for(Commands item : Commands.values()) {
			  if (!item.isFeature()) {
				  sendCommandLine(' ' + item.name() + ' ' + item.getArgs() + '\r' + '\n');
			  }
			  else {
				  sendCommandLine(' ' + item.name() + ' ' + item.getArgs() + " (feature)\r\n");
			  }
		  }
		  sendAnswer(MessageType.MSG_COMMANDS_END);
	  }
	  else {
		  try {
			  final Commands	c = Commands.valueOf(name.trim().toUpperCase());
			  
			  sendAnswer(MessageType.MSG_COMMANDS_HELP, c.name(), c.getArgs(), c.getDescriptor());
		  } catch (IllegalArgumentException exc) {
			  sendAnswer(MessageType.MSG_COMMANDS_HELP_MISSING, name);
		  }
	  }
  }  

  /**
   * Handler for STRU (File structure). Only 'STRU F' command is supported.
   * 
   * @throws IOException 
   */
  private void handleStru(final String parm) throws IOException {
	  if (Utils.checkEmptyOrNullString(parm) || "|F|R|P|".indexOf(parm) < 0) {
		  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, parm);
	  }
	  else if ("F".equals(parm)) {
		  sendAnswer(MessageType.MSG_COMMAND_OK);
	  }
	  else {
		  sendAnswer(MessageType.MSG_UNSUPPORTED_ARGUMENT, parm);
	  }
  }  
  
  
  /**
   * Handler for SIZE (Size) command. Restore size of the file typed.
   * 
   * @param file The file to get size
   * @throws IOException 
   */
  private void handleSize(final String file) throws IOException {
	  if (isCommandAvailableNow()) {
		  final File	f = getFileDesc(file);
		  
		  if (f.exists() && f.isFile()) {
			  sendAnswer(MessageType.MSG_FILE_SIZE, f.length());
		  }
		  else {
			  sendAnswer(MessageType.MSG_FAILURE_FILE_NOT_EXISTS);
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }  
  
  private void debug(final String msg) {
	  if (debugMode) {
		  getLogger().message(Severity.debug, "Thread " + Thread.currentThread().getName() + ": " + msg);
	  }
  }

  private void sendAnswer(final MessageType msg, final Object... parameters) throws IOException {
	  final String	result = msg.getCode()+msg.getMessage().formatted(parameters);
      
      sendCommandLine(result);
	  controlOutWriter.flush();
  }

  private void sendCommandLine(final String line) throws IOException {
      debug("Answer: "+line);
	  controlOutWriter.write(line);
  }  

  private void sendDataLine(final String msg) throws IOException {
    if (!conn.isConnectionValid()) {
    	debug("Cannot send message, because no data connection is established");
    	sendAnswer(MessageType.MSG_NO_DATA_CONNECTION);
    } else {
        final Writer	wr = conn.getWriter();
        
    	debug("Data: "+msg);
        wr.write(msg);
		wr.write('\r');
		wr.write('\n');
    }
  }

  private void sendDirContent(final File[] dirContent, final Sender sender) throws IOException {
	  sendAnswer(MessageType.MSG_OPEN_CONN_FOR_LIST);

	  for (File content : dirContent) {
		  final Path		path = content.toPath();
		  final Set<PosixFilePermission> 	permissions = getFilePermissions(content);
		  final Calendar	cal = Calendar.getInstance();
		  
		  cal.setTimeInMillis(content.lastModified());					  
		  sender.send("%1$c%2$c%3$c%4$c%5$c%6$c%7$c%8$c%9$c%10$c 1 %11$s %12$s %13$13d %14$3s %15$3d %16$02d:%17$02d %18$s".formatted(
				  	content.isDirectory() ? 'd' : '-',
				  	permissions.contains(PosixFilePermission.OWNER_READ) ? 'r' : '-',
				  	permissions.contains(PosixFilePermission.OWNER_WRITE) ? 'w' : '-',
				  	permissions.contains(PosixFilePermission.OWNER_EXECUTE) ? 'x' : '-',
				  	permissions.contains(PosixFilePermission.GROUP_READ) ? 'r' : '-',
				  	permissions.contains(PosixFilePermission.GROUP_WRITE) ? 'w' : '-',
				  	permissions.contains(PosixFilePermission.GROUP_EXECUTE) ? 'x' : '-',
				  	permissions.contains(PosixFilePermission.OTHERS_READ) ? 'r' : '-',
				  	permissions.contains(PosixFilePermission.OTHERS_WRITE) ? 'w' : '-',
				  	permissions.contains(PosixFilePermission.OTHERS_EXECUTE) ? 'x' : '-',
				  	Files.getOwner(path).getName(),
				  	getFileGroup(content),
				  	content.length(),
				  	cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH),
				  	cal.get(Calendar.DAY_OF_MONTH),
				  	cal.get(Calendar.HOUR_OF_DAY),
				  	cal.get(Calendar.MINUTE),
				  	content.getName()
				  ));
	  }
	  sendAnswer(MessageType.MSG_TRANSFER_COMPLETED);
  }
  
  private void clearSettings() {
	  this.currDirectory = "/";
	  this.transferMode = TransferType.UNKNOWN;
	  this.currentLoggingStatus = LoggingStatus.NOTLOGGEDIN;
	  this.currentUser = null;
	  this.restoreLocation = -1;
	  this.oldFile = null;
  }
  
  private boolean isCommandAvailableNow() {
	  return currentLoggingStatus == LoggingStatus.LOGGEDIN;
  }
  
  private boolean isFileNameValid(final String args) {
	  return args != null && !args.startsWith("-");
  }
  
  private File getFileDesc(final String args) {
	final File	current;
	  
	if (Utils.checkEmptyOrNullString(args)) {
		current = new File(root, currDirectory);
	} else if (args.startsWith("/")) {
		current = new File(root, args);
	} else if (".".equals(args)) {
		current = new File(root, currDirectory);
	} else if ("..".equals(args)) {
		if ("/".equals(currDirectory)) {
			current = root.getAbsoluteFile();
		}
		else {
			current = new File(root, currDirectory).getParentFile();
		}
	} else {
		current = new File(new File(root, currDirectory), args);
	}
	return current;
  }

  private String getFileName(final File	file) {
	  final String	currentName = file.getAbsolutePath();
	  final String	rootName = root.getAbsolutePath();
	
	  if (rootName.length() >= currentName.length()) {
		  return "/";
	  }
	  else {
		  return currentName.substring(rootName.length()).replace('\\', '/');
	  }
  }

  private Set<PosixFilePermission> getFilePermissions(final File file) throws IOException {
	  final Path	path = file.toPath();
	  
	  try {
		  return Files.getPosixFilePermissions(path, LinkOption.NOFOLLOW_LINKS);
	  } catch (UnsupportedOperationException exc) {
		  final Set<PosixFilePermission>	result = new HashSet<>();
		  
		  if (file.canRead()) {
			  result.add(PosixFilePermission.OWNER_READ);
			  result.add(PosixFilePermission.GROUP_READ);
			  result.add(PosixFilePermission.OTHERS_READ);
		  }
		  if (file.canWrite()) {
			  result.add(PosixFilePermission.OWNER_WRITE);
			  result.add(PosixFilePermission.GROUP_WRITE);
			  result.add(PosixFilePermission.OTHERS_WRITE);
		  }
		  if (file.canExecute()) {
			  result.add(PosixFilePermission.OWNER_EXECUTE);
			  result.add(PosixFilePermission.GROUP_EXECUTE);
			  result.add(PosixFilePermission.OTHERS_EXECUTE);
		  }
		  return result;
	  }
  }

  private String getFileGroup(final File file) throws IOException {
	  final Path	path = file.toPath();
	  
	  try {
		  return Files.readAttributes(path, PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS).group().getName();
	  } catch (UnsupportedOperationException exc) {
		  return Files.getOwner(path, LinkOption.NOFOLLOW_LINKS).getName();
	  }
  }  

  private class DataConnection {
	  private ConnectionMode	mode = ConnectionMode.NONE;
	  private ServerSocket 		dataSocket;
	  private Socket 			dataConnection;
	  private OutputStream		os;
	  private Writer			writer;

	  void openActive(final String ipAddress, final int port) {
		  if (mode == ConnectionMode.NONE) {
			  try {
				  dataConnection = new Socket(ipAddress, port);
				  os = dataConnection.getOutputStream();
				  writer = new OutputStreamWriter(os);
				  mode = ConnectionMode.ACTIVE;
				  debug("Data connection - Active Mode - established");
		      } catch (IOException e) {
		    	  debug("Could not connect to client data socket");
		      }
		  }
		  else {
			  throw new IllegalStateException("Attempt to open already opened connection");
		  }
	  }

	  SocketAddress openPassive(final int port) {
		  if (mode == ConnectionMode.NONE) {
			  try {
			      dataSocket = new ServerSocket(port);
			      return dataSocket.getLocalSocketAddress();
			  } catch (IOException e) {
			      debug("Could not create data connection (port "+port+")");
			      return null;
			  }
		  }
		  else {
			  throw new IllegalStateException("Attempt to open already opened connection");
		  }
	  }	  
	  
	  void waitPassive(final int port) {
		  if (mode == ConnectionMode.NONE) {
			  try {
			      dataConnection = dataSocket.accept();
			      os = dataConnection.getOutputStream();
			      writer = new OutputStreamWriter(os);
			      mode = ConnectionMode.PASSIVE;
			      dataSocket.close();
			      debug("Data connection - Passive Mode - established");
			  } catch (IOException e) {
			      debug("Could not create data connection (port "+port+")");
			  }
		  }
		  else {
			  throw new IllegalStateException("Attempt to open already opened connection");
		  }
	  }
	  
	  boolean isConnectionValid() {
		return dataConnection != null && !dataConnection.isClosed();	  
	  }

	  InputStream getInputStream() throws IOException {
		  if (mode == ConnectionMode.NONE) {
			  throw new IllegalStateException("Attempt to get stream on closed socket"); 
		  }
		  else {
			  return dataConnection.getInputStream();
		  }
	  }

	  OutputStream getOutputStream() throws IOException {
		  if (mode == ConnectionMode.NONE) {
			  throw new IllegalStateException("Attempt to get stream on closed socket"); 
		  }
		  else {
			  return os;
		  }
	  }

	  Writer getWriter() {
		  if (mode == ConnectionMode.NONE) {
			  throw new IllegalStateException("Attempt to get stream on closed socket"); 
		  }
		  else {
			  return writer;
		  }
	  }
	  
	  void close() {
		  if (mode!= ConnectionMode.NONE) {
			  try {
			      mode = ConnectionMode.NONE;
			      writer.flush();
			      writer.close();
			      dataConnection.close();
			      dataConnection = null;
			      if (dataSocket != null && !dataSocket.isClosed()) {
			        dataSocket.close();
			        dataSocket = null;
			      }
			  } catch (IOException e) {
		        debug("Could not close data connection");
		        e.printStackTrace();
		      }
		  }
	  }
  }
  
  private static class DataCopier extends Thread implements ProgressIndicator {
	  private static final int	OP_RETR_BIN = 0;
	  private static final int	OP_RETR_ASCII = 1;
	  private static final int	OP_STOR_BIN = 2;
	  private static final int	OP_STOR_ASCII = 3;
	  private static final AtomicInteger	UNIQUE = new AtomicInteger(1);
	  
	  private final int				operation;
	  private final int				unique = UNIQUE.incrementAndGet();
	  private final InputStream		is;
	  private final Reader			rdr;
	  private final OutputStream	os;
	  private final Writer			wr;
	  private final File			file;
	  private final boolean			append;
	  private volatile long			total = 0;
	  private volatile long			processed = 0;
	  private volatile boolean		terminate = false;
	  private volatile boolean		processing = false;
	  private volatile boolean		error = false;
	  
	  private DataCopier(final InputStream from, final File to, final boolean append) {
		  this.operation = OP_STOR_BIN;
		  this.is = from;
		  this.rdr = null;
		  this.os = null;
		  this.wr = null;
		  this.file = to;
		  this.append = append;
		  prepare();
	  }

	  private DataCopier(final Reader from, final File to, final boolean append) {
		  this.operation = OP_STOR_ASCII;
		  this.is = null;
		  this.rdr = from;
		  this.os = null;
		  this.wr = null;
		  this.file = to;
		  this.append = append;
		  prepare();
	  }
	  
	  private DataCopier(final File from, final OutputStream to) {
		  this.operation = OP_RETR_BIN;
		  this.is = null;
		  this.rdr = null;
		  this.os = to;
		  this.wr = null;
		  this.file = from;
		  this.append = false;
		  prepare();
	  }

	  private DataCopier(final File from, final Writer to) {
		  this.operation = OP_RETR_ASCII;
		  this.is = null;
		  this.rdr = null;
		  this.os = null;
		  this.wr = to;
		  this.file = from;
		  this.append = false;
		  prepare();
	  }
	  
	  @Override
	  public void run() {
		  switch (operation) {
		  	case OP_RETR_BIN	:
		  		start("", file.length());
		  		try(final InputStream	from = new FileInputStream(file)) {
		  			Utils.copyStream(from, os, this);
				} catch (IOException e) {
					error = true;
				}
		  		end();
		  	case OP_RETR_ASCII	:
		  		start("", file.length());
		  		try(final InputStream	from = new FileInputStream(file);
		  			final Reader		fromR = new InputStreamReader(from)) {
		  			Utils.copyStream(fromR, wr, this);
				} catch (IOException e) {
					error = true;
				}
		  		end();
		  	case OP_STOR_BIN	:
		  		start("");
		  		try(final OutputStream	to = new FileOutputStream(file, append)) {
		  			Utils.copyStream(is, to, this);
				} catch (IOException e) {
					error = true;
		  		}
		  		end();
		  	case OP_STOR_ASCII	:
		  		start("");
		  		try(final OutputStream	to = new FileOutputStream(file, append);
		  			final Writer		toW = new OutputStreamWriter(to)) {
		  			Utils.copyStream(rdr, toW, this);
				} catch (IOException e) {
					error = true;
		  		}
		  		end();
		  		break;
		  	default :
		  		throw new UnsupportedOperationException("Operation type ["+operation+"] is not supported yet");
		  }
	  }

	  @Override
	  public void start(final String caption, long total) {
		  this.total = total;
		  this.processing = true;
	  }

	  @Override
	  public void start(final String caption) {
	  }

	  @Override
	  public void interrupt() {
		  super.interrupt();
		  terminate = true;
	  }
	
	  @Override
	  public boolean processed(final long processed) {
		  return !terminate;
	  }

	  @Override
	  public void end() {
		  this.processing = false;
	  }
	  
	  private void prepare() {
		  setName("Data copier ["+unique+"] for "+file.getName());
		  setDaemon(true);
	  }
  }
}

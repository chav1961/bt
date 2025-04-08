package chav1961.bt.nanoftp;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;

/**
 *
 */
public class FTPSession implements Runnable, LoggerFacadeOwner {
	private static final String IPV4_ID = "1";
	private static final String IPV6_ID = "2";
	private static final String HANDLE_A = "A";
	private static final String HANDLE_I = "I";
	private static final File[]	EMPTY_FILE_ARRAY = new File[0];

	private static enum Commands {
		USER(false, false),
		PASS(false, false),
		CWD(false, false),
		LIST(false, false),
  		NLST(false, false),
		PWD(false, false),
  		XPWD(false, false),
		PASV(false, false),
  		EPSV(false, false),
		SYST(false, false),
		FEAT(false, false),
  		PORT(false, false),
  		EPRT(false, false),
  		RETR(false, false),
  		MKD(false, false),
		XMKD(false, false),
  		RMD(false, false),
  		XRMD(false, false),
  		DELE(false, false),
  		TYPE(false, false),
  		STOR(false, false),
  		QUIT(true, false);
		
		private final boolean	exitRequred;
		private final boolean	isFeature;
		
		private Commands(final boolean exitRequired, final boolean isFeature) {
			this.exitRequred = exitRequired;
			this.isFeature = isFeature;
		}

		public boolean isExitRequired() {
			return exitRequred;
		}

		public boolean isFeature() {
			return isFeature;
		}
	}	
	
	private static enum MessageType {
		MSG_OPEN_CONN_FOR_LIST(125, " Opening ASCII mode data connection for file list.\r\n"),
		MSG_OPEN_BIN_CONN_FOR_FILE(150, " Opening binary mode data connection for file %1$s\r\n"),
		MSG_OPEN_ASCII_CONN_FOR_FILE(150, " Opening ASCII mode data connection for file %1$s\r\n"),
		MSG_COMMAND_OK(200, " Command OK\r\n"),
		MSG_EXTENSIONS_START(211, "-Extensions supported:\r\n"),
		MSG_EXTENSIONS_END(211, " END\r\n"),
		MSG_SYSTEM(215, " Nano FTP-Server\r\n"),
		MSG_WELCOME(220, " Welcome to the nano FTP-Server\r\n"),
		MSG_CLOSING_CONN(221, " Closing connection\r\n"),
		MSG_TRANSFER_COMPLETED(226, " Transfer completed.\r\n"),
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
		MSG_NO_DATA_CONNECTION(425, " No data connection was established\r\n"),
		MSG_UNKNOWN_COMMAND(501, " Unknown command\r\n"),
		MSG_MISSING_FILE_NAME(501, " File name missing\r\n"),
		MSG_ILLEGAL_ARGUMENT(504, " Illegal argument [%1$s]\r\n"),
		MSG_TRANSFER_MODE_NOT_SET(504, " Transfer mode is not set yet\r\n"),
		MSG_USER_ALREADY_LOGGED(530," User already logged in\r\n"),
		MSG_USER_NOT_LOGGED(530," Not logged in\r\n"),
		MSG_USER_NOT_ENTERED(530," User name is not entered yet\r\n"),
		MSG_FAILURE_FILE_UNAVAILABLE(550, " Requested action not taken. File %1$s unavailable.\r\n"),
		MSG_FAILURE_FILE_NOT_EXISTS(550, " File %1$s does not exist\r\n"),
		MSG_FAILURE_FILE_ALREADY_EXISTS(550, " File %1$s already exists\r\n"),
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

	private final Socket 			controlSocket;
	private final LoggerFacade		logger;
	private final File 				root;
	private final DataConnection	conn = new DataConnection();
	private final boolean 			debugMode;
	private final SimpleValidator	validator;

	private String 			currDirectory;
	private Writer 			controlOutWriter;
	private TransferType 	transferMode = TransferType.UNKNOWN;
	private LoggingStatus 	currentLoggingStatus = LoggingStatus.NOTLOGGEDIN;
	private String			currentUser = null;
  
  /**
   * <p>Constructor of the class instance</p>
   * 
   * @param client client socket to interact. Can't be null.
   * @param validator validator to check user/password credentials. Can't be null.
   * @param debugMode turn on debug trace
   */
  public FTPSession(final Socket client, final LoggerFacade logger, final File root, final SimpleValidator validator, final boolean debugMode) {
    this.controlSocket = client;
    this.logger = logger;
    this.validator = validator;
    this.debugMode = debugMode;
    this.root = root;
    this.currDirectory = "/";
  }

  @Override
  public LoggerFacade getLogger() {
	  return logger;
  }
  
  @Override
  public void run() {
	  debugOutput("FTP session started, current working directory is <" + this.currDirectory + ">");

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
			  debugOutput("FTP session ended");
		  } catch (IOException e) {
			  debugOutput("Could not close socket");
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
	  final String 	args = blank == -1 ? "" : c.substring(blank + 1);
	  
	  debugOutput("Command: " + command + ", args: <" + args + ">");
	  try {
		  final Commands	cmd = Commands.valueOf(command.toUpperCase());
		  	
		  switch (cmd) {
		  	case USER :
		  		handleUser(args);
		  		break;
		  	case PASS :
		  		handlePass(args);
		  		break;
		  	case CWD :
		  		handleCwd(args);
		  		break;
		  	case LIST : case NLST :
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
		  	case TYPE :
		  		handleType(args);
		  		break;
		  	case STOR :
		  		handleStor(args);
		  		break;
		  	case QUIT :
		  		handleQuit();
		  		break;
		  	default:
		  		throw new UnsupportedOperationException("Command ["+c+"] is not supported yet");
		  }
		  return !cmd.isExitRequired();
	  } catch (IllegalArgumentException exc) {
	  		sendAnswer(MessageType.MSG_UNKNOWN_COMMAND);
	  		return true;
	  }
  }

  /**
   * Handler for USER command. User identifies the client.
   * 
   * @param username Username entered by the user
   * @throws IOException 
   */
  private void handleUser(final String username) throws IOException {
	  switch (currentLoggingStatus) {
		case LOGGEDIN		:
			sendAnswer(MessageType.MSG_USER_ALREADY_LOGGED);
			break;
		case USERNAMEENTERED:
		case NOTLOGGEDIN	:
		    if (validator.isUserExists(username)) {
				sendAnswer(MessageType.MSG_USER_NAME_OK);
				currentUser = username;
				currentLoggingStatus = LoggingStatus.USERNAMEENTERED;
		    } else {
		    	sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
		    }
			break;
		default:
			throw new UnsupportedOperationException("Logging status ["+currentLoggingStatus+"] is not supported yet");
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
			  debugOutput("Not found: <"+current.getAbsolutePath()+">");
			  sendAnswer(MessageType.MSG_FAILURE_FILE_UNAVAILABLE, getFileName(current));
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
				  sendAnswer(MessageType.MSG_FAILURE_FILE_NOT_EXISTS, current.getAbsolutePath());
			  } else {
				  sendAnswer(MessageType.MSG_OPEN_CONN_FOR_LIST);

				  for (File content : dirContent) {
					  sendData(content.getName());
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
  private int openDataConnectionPassive(int port) {
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
      debugOutput("Data connection was closed");
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
			  final String 		hostName = content[0] + '.' + content[1] + '.' + content[2] + '.' + content[3];
			  final int 		port = Integer.parseInt(content[4]) * 256 + Integer.parseInt(content[5]);
			
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
    // Using fixed IP for connections on the same machine
    // For usage on separate hosts, we'd need to get the local IP address from
    // somewhere
    // Java sockets did not offer a good method for this
    final String myIp = InetAddress.getLocalHost().getHostAddress();
    final String myIpSplit[] = myIp.split("\\.");
    final int	port = openDataConnectionPassive(0); 
    final int 	p1 = port / 256;
    final int 	p2 = port % 256;
    
    sendAnswer(MessageType.MSG_ENTERING_PASSIVE_MODE, myIpSplit[0], myIpSplit[1], myIpSplit[2], myIpSplit[3], p1, p2);
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
		  final int port = openDataConnectionPassive(0); 
		  
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
   * Handler for the SYST command.
   * @throws IOException 
   */
  private void handleSyst() throws IOException {
	  sendAnswer(MessageType.MSG_SYSTEM);
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
			  sendLine(item.name() + '\r' + '\n');
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
				  debugOutput("Failed to create new directory");
			  }
			  else {
				  sendAnswer(MessageType.MSG_DIRECTORY_CREATED, dir.getAbsolutePath());
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

				  sendAnswer(MessageType.MSG_DIRECTORY_REMOVED, d.getAbsolutePath());
			  } 
			  else {
				  sendAnswer(MessageType.MSG_FAILURE_FILE_UNAVAILABLE, d.getAbsolutePath());
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

				  sendAnswer(MessageType.MSG_FILE_REMOVED, f.getAbsolutePath());
			  } 
			  else {
				  sendAnswer(MessageType.MSG_FAILURE_FILE_UNAVAILABLE, f.getAbsolutePath());
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
   * Handler for the TYPE command. The type command sets the transfer mode to
   * either binary or ascii mode
   * 
   * @param mode Transfer mode: "a" for Ascii. "i" for image/binary.
   * @throws IOException 
   */
  private void handleType(String mode) throws IOException {
	  if (isCommandAvailableNow()) {
		  if (HANDLE_A.equalsIgnoreCase(mode)) {
			  transferMode = TransferType.ASCII;
			  sendAnswer(MessageType.MSG_COMMAND_OK);
		  } 
		  else if (HANDLE_I.equalsIgnoreCase(mode)) {
			  transferMode = TransferType.BINARY;
			  sendAnswer(MessageType.MSG_COMMAND_OK);
		  } else {
			  sendAnswer(MessageType.MSG_ILLEGAL_ARGUMENT, mode);
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
		  	switch (transferMode) {
		  		case ASCII:
		  	        sendAnswer(MessageType.MSG_OPEN_ASCII_CONN_FOR_FILE, f.getName());

		  	        debugOutput("Starting file transmission of " + f.getName() + " in ASCII mode");
		  	        try(final Reader 	rin = new FileReader(f);
		  	        	final Writer	rout = new OutputStreamWriter(conn.getOutputStream())) {
		  	        	
		  	        	try {
			  	        	Utils.copyStream(rin, rout);
		  	        	} catch (IOException e) {
		  	        		debugOutput("Could not read from or write to file streams");
		  	        	}
		  	        } catch (IOException e) {
		  	        	debugOutput("Could not create file streams");
		  	        } finally {
		  	        	closeDataConnection();
		  	        }
		  	        debugOutput("Completed file transmission of " + f.getName());

		  	        sendAnswer(MessageType.MSG_TRANSFER_COMPLETED);
		  			break;
		  		case BINARY:
		  	        sendAnswer(MessageType.MSG_OPEN_BIN_CONN_FOR_FILE, f.getName());

		  	        debugOutput("Starting file transmission of " + f.getName() + " in BINARY mode");
		  	        try(final InputStream 	fin = new FileInputStream(f);
		  	        	final OutputStream	fout = conn.getOutputStream()){

		  	        	try {
		  	        		Utils.copyStream(fin, fout);
		  	        	} catch (IOException e) {
		  	        		debugOutput("Could not read from or write to file streams");
		  	        	}
		  	        } catch (Exception e) {
		  	        	debugOutput("Could not create file streams");
		  	        } finally {
		  	        	closeDataConnection();
		  	        }
		  	        debugOutput("Completed file transmission of " + f.getName());

		  	        sendAnswer(MessageType.MSG_TRANSFER_COMPLETED);
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
   * Handler for STOR (Store) command. Store receives a file from the client and
   * saves it to the ftp server.
   * 
   * @param file The file that the user wants to store on the server
   * @throws IOException 
   */
  private void handleStor(String file) throws IOException {
	  if (isCommandAvailableNow()) {
		  if (file == null) {
			  sendAnswer(MessageType.MSG_MISSING_FILE_NAME);
		  } 
		  else {
			  final File f = getFileDesc(file);

			  if (f.exists()) {
				  sendAnswer(MessageType.MSG_FAILURE_FILE_ALREADY_EXISTS, file);
			  }
			  else {
				  switch (transferMode) {
				  	case ASCII		:
			            sendAnswer(MessageType.MSG_OPEN_ASCII_CONN_FOR_FILE, f.getName());
						
			            try(final Reader	rdr = new InputStreamReader(conn.getInputStream());
			            	final Writer 	rout = new OutputStreamWriter(new FileOutputStream(f))){
				            
			            	try {
			            		Utils.copyStream(rdr, rout);
				            } catch (IOException e) {
				            	debugOutput("Could not read/write streams");
				            }
			            } catch (IOException e) {
			            	debugOutput("Could not create streams");
			            }
			            sendAnswer(MessageType.MSG_TRANSFER_COMPLETED);
			            debugOutput("Completed receiving file " + f.getName());

			            break;						
					case BINARY		:
			            sendAnswer(MessageType.MSG_OPEN_BIN_CONN_FOR_FILE, f.getName());

			            debugOutput("Start receiving file " + f.getName() + " in BINARY mode");
			            try(final InputStream 	fin = conn.getInputStream(); 
			            	final OutputStream 	fout = new FileOutputStream(f)){
				            
				            try {
				            	Utils.copyStream(fin, fout);
				            } catch (IOException e) {
				            	debugOutput("Could not read from or write to file streams");
				            }
			            } catch (Exception e) {
			            	debugOutput("Could not create file streams");
			            } finally {
			            	closeDataConnection();
			            }			            	
			            debugOutput("Completed receiving file " + f.getName());
			            sendAnswer(MessageType.MSG_TRANSFER_COMPLETED);

			            break;
					case UNKNOWN	:
			  			sendAnswer(MessageType.MSG_TRANSFER_MODE_NOT_SET);
			  			break;
			  		default :
			  			throw new UnsupportedOperationException("Transafer mode ["+transferMode+"] is not supporte yet");
				  }
		      }
		  }
	  }
	  else {
		  sendAnswer(MessageType.MSG_USER_NOT_LOGGED);
	  }
  }

  private void debugOutput(final String msg) {
	  if (debugMode) {
		  getLogger().message(Severity.debug, "Thread " + Thread.currentThread().getName() + ": " + msg);
	  }
  }

  private void sendLine(final String line) throws IOException {
	  controlOutWriter.write(line);
  }  
  
  private void sendAnswer(final MessageType msg, final Object... parameters) throws IOException {
	  final String	result = msg.getCode()+msg.getMessage().formatted(parameters);
      
      debugOutput("Answer: "+result);
      sendLine(result);
	  controlOutWriter.flush();
  }
  
  /**
   * Send a message to the connected client over the data connection.
   * 
   * @param msg Message to be sent
   * @throws IOException 
   */
  private void sendData(final String msg) throws IOException {
    if (!conn.isConnectionValid()) {
    	sendAnswer(MessageType.MSG_NO_DATA_CONNECTION);
    	debugOutput("Cannot send message, because no data connection is established");
    } else {
        debugOutput("Data: "+msg);
		conn.getWriter().write(msg + '\r' + '\n');
    }
  }
  
  private boolean isCommandAvailableNow() {
	  return currentLoggingStatus == LoggingStatus.LOGGEDIN;
  }
  
  private boolean isFileNameValid(final String args) {
	  return args != null && !args.startsWith("-");
  }
  
  private File getFileDesc(final String args) {
	final File	current;
	  
	if (args == null) {
		current = new File(root, currDirectory);
	} else if ("..".equals(args)) {
		current = new File(root, currDirectory).getParentFile();
	} else if (".".equals(args)) {
		current = new File(root, currDirectory);
	} else if (args.startsWith("/")) {
		current = new File(root, args);
	} else {
		current = new File(new File(root, currDirectory), args);
	}
	return current;
  }

  private String getFileName(final File	file) {
	  final String	currentName = file.getAbsolutePath();
	  final String	rootName = root.getAbsolutePath();
	
	  if (rootName.length() == currentName.length()) {
		  return "/";
	  }
	  else {
		  return currentName.substring(rootName.length());
	  }
  }
  
  private class DataConnection {
	  private ConnectionMode	mode = ConnectionMode.NONE;
	  private int 				dataPort;
	  private ServerSocket 		dataSocket;
	  private Socket 			dataConnection;
	  private OutputStream		os;
	  private Writer			writer;
	  private TransferType 		transferMode = TransferType.ASCII;

	  void openActive(final String ipAddress, final int port) {
		  if (mode == ConnectionMode.NONE) {
			  try {
				dataConnection = new Socket(ipAddress, port);
				dataPort = port;
				os = dataConnection.getOutputStream();
				writer = new OutputStreamWriter(os);
				mode = ConnectionMode.ACTIVE;
				debugOutput("Data connection - Active Mode - established");
		      } catch (IOException e) {
		        debugOutput("Could not connect to client data socket");
		        e.printStackTrace();
		      }
		  }
		  else {
			  throw new IllegalStateException("Attempt to open already opened connection");
		  }
	  }

	  int openPassive(final int port) {
		  if (mode == ConnectionMode.NONE) {
			  try {
			      dataSocket = new ServerSocket(port);
			      dataPort = dataSocket.getLocalPort();
			      return dataPort;
			  } catch (IOException e) {
			      debugOutput("Could not create data connection (port "+port+")");
			      e.printStackTrace();
			      return -1;
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
			      debugOutput("Data connection - Passive Mode - established");
			  } catch (IOException e) {
			      debugOutput("Could not create data connection (port "+port+")");
			      e.printStackTrace();
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
		        debugOutput("Could not close data connection");
		        e.printStackTrace();
		      }
		  }
	  }
  }
}

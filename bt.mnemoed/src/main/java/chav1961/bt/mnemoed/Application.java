package chav1961.bt.mnemoed;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JSplitPane;

import chav1961.bt.mnemoed.controls.CardWindow;
import chav1961.bt.mnemoed.controls.EditorPane;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ConsoleCommandException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator.LRUPersistence;

public class Application extends JFrame implements LocaleChangeListener, AutoCloseable {
	private static final long serialVersionUID = -7543002777932405693L;
	public static final String				ARG_HELP_PORT = "helpport";
	public static final String				PROP_FILE = ".mnemoed";
	public static final String				PROP_LRU_PREFIX = "lru.";
	
	private final File						f = new File("./"+PROP_FILE);
	private final ContentMetadataInterface 	app;
	private final Localizer					localizer;
	private final int 						localHelpPort;
	private final CountDownLatch			latch;
	private final FileSystemInterface		fsi;
	private final SubstitutableProperties	props = new SubstitutableProperties();
	private final JMenuBar					menu;
	private final JFileContentManipulator	manipulator;
	private final CardWindow				cardWindow;
	private final JStateString				state;
	
	public Application(final ContentMetadataInterface app, final Localizer parent, final int localHelpPort, final CountDownLatch latch) throws NullPointerException, LocalizationException, IllegalArgumentException, IOException {
		if (app == null) {
			throw new NullPointerException("Metadata interface can't be null");
		}
		else if (parent == null) {
			throw new NullPointerException("Parent localizer can't be null");
		}
		else if (latch == null) {
			throw new NullPointerException("Latch to notify closure can't be null");
		}
		else {
			this.app = app;
			this.localizer = LocalizerFactory.getLocalizer(app.getRoot().getLocalizerAssociated());
			this.localHelpPort = localHelpPort;
			this.latch = latch;
			this.menu = SwingUtils.toJComponent(app.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class);
			this.cardWindow = new CardWindow(localizer);
			this.fsi = FileSystemFactory.createFileSystem(URI.create("fsys:file://./"));
			this.state = new JStateString(localizer);
			
			if (f.exists() && f.isFile()) {
				try(final InputStream	is = new FileInputStream(f)) {
					props.load(is);
				} catch (IOException e) {
					state.message(Severity.warning, "Property file ["+f+"] loading failure");
				}
			}
			
			this.manipulator = new JFileContentManipulator(fsi, localizer, ()->cardWindow.getInputStream(), ()->cardWindow.getOutputStream(), new LRUPersistence() {
				@Override
				public void saveLRU(final List<String> lru) throws IOException {
					for (int index = 1; props.containsKey(PROP_LRU_PREFIX+index); index++) {
						props.remove(props.getProperty(PROP_LRU_PREFIX+index));
					}
					for (int index = 1; index < lru.size(); index++) {
						props.setProperty(PROP_LRU_PREFIX+index, lru.get(index));
					}
				}
				
				@Override
				public void loadLRU(final List<String> lru) throws IOException {
					for (int index = 1; props.containsKey(PROP_LRU_PREFIX+index); index++) {
						lru.add(props.getProperty(PROP_LRU_PREFIX+index));
					}
				}
			});
			
			final JSplitPane	left = new JSplitPane();
			final JSplitPane	total = new JSplitPane(JSplitPane.VERTICAL_SPLIT, left, cardWindow);
			
			getContentPane().add(total, BorderLayout.CENTER);
			getContentPane().add(state, BorderLayout.SOUTH);
			SwingUtils.assignActionListeners(menu,this);
			SwingUtils.centerMainWindow(this,0.75f);
			SwingUtils.assignExitMethod4MainWindow(this,()->{exitApplication();});
			localizer.addLocaleChangeListener(this);
			
			fillLocalizedStrings();
			pack();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public void close() throws ContentException {
		try(final OutputStream	os = new FileOutputStream(f)) {
			props.store(os,"");
		} catch (IOException e) {
			state.message(Severity.warning, "Property file ["+f+"] saving failure");
		}
		try{fsi.close();
		} catch (IOException e) {
			state.message(Severity.warning, "File system ["+fsi.toString()+"] closing failure");
		}
		dispose();
	}

	private void exitApplication() throws IOException {
		try{manipulator.close();
			latch.countDown();
		} catch (UnsupportedOperationException exc) {
		}
	}

	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		try{final ArgParser						parser = new ApplicationArgParser().parse(args);
			final int							helpPort = !parser.isTyped(ARG_HELP_PORT) ? getFreePort() : parser.getValue(ARG_HELP_PORT, int.class);
			final SubstitutableProperties		props = new SubstitutableProperties(Utils.mkProps(
													 NanoServiceFactory.NANOSERVICE_PORT, ""+helpPort
													,NanoServiceFactory.NANOSERVICE_ROOT, "fsys:xmlReadOnly:root://chav1961.bt.mnemoed/chav1961/bt/mnemoed/helptree.xml"
													,NanoServiceFactory.NANOSERVICE_CREOLE_PROLOGUE_URI, Application.class.getResource("prolog.cre").toString() 
													,NanoServiceFactory.NANOSERVICE_CREOLE_EPILOGUE_URI, Application.class.getResource("epilog.cre").toString() 
												));
		
			try(final LoggerFacade				logger = new SystemErrLoggerFacade();
				final InputStream				is = Application.class.getResourceAsStream("application.xml");
				final Localizer					localizer = new PureLibLocalizer();
				final NanoServiceFactory		service = new NanoServiceFactory(logger,props)) {
				final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
				final CountDownLatch			latch = new CountDownLatch(1);
				
				try(final Application	app = new Application(xda,localizer,helpPort,latch)) {
					
					app.setVisible(true);
					service.start();
					latch.await();
					service.stop();
				}
			} catch (IOException | EnvironmentException | InterruptedException  e) {
				e.printStackTrace();
				System.exit(129);
			}
		} catch (ConsoleCommandException | CommandLineParametersException e) {
			e.printStackTrace();
			System.exit(128);
		} catch (IOException | ContentException e) {
			e.printStackTrace();
			System.exit(129);
		}
		System.exit(0);
	}

	private static int getFreePort() throws IOException {
		try (ServerSocket 	socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}
	
	static class ApplicationArgParser extends ArgParser {
		public ApplicationArgParser() {
			super(new IntegerArg(ARG_HELP_PORT,false,"help system port",0));
		}
	}

}

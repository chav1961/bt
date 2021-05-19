package chav1961.bt.mnemoed;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import chav1961.bt.mnemoed.controls.CardWindow;
import chav1961.bt.mnemoed.controls.EditorPane;
import chav1961.bt.mnemoed.controls.settings.AboutProjectSettings;
import chav1961.bt.mnemoed.util.ResourcesManager;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ConsoleCommandException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.MimeParseException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.ui.swing.AutoBuiltForm;
import chav1961.purelib.ui.swing.SimpleNavigatorTree;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JDialogContainer;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator.FileContentChangeType;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator.FileContentChangedEvent;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator.LRUPersistence;
import chav1961.purelib.ui.swing.useful.JStateString;

public class Application extends JFrame implements LocaleChangeListener, AutoCloseable {
	private static final long serialVersionUID = -7543002777932405693L;
	public static final String				ARG_HELP_PORT = "helpport";
	public static final String				PROP_FILE = ".mnemoed";
	public static final String				PROP_LRU_PREFIX = "lru.";
	
	public static final String				I18N_APPLICATION_TITLE = "application.title";
	public static final String				I18N_ABOUT_TITLE = "application.about.title";
	public static final String				I18N_ABOUT_CONTENT = "application.about.content";

	private static final String				EDITOR_WINDOW = "editorWindow";
	private static final String				POPUP_WINDOW = "popupWindow";
	private static final URL[]				EMPTY_URLS = new URL[0];
	
	private final File						f = new File("./"+PROP_FILE);
	private final ContentMetadataInterface 	app;
	private final Localizer					localizer;
	private final int 						localHelpPort;
	private final CountDownLatch			latch;
	private final SimpleNavigatorTree<ContentNodeMetadata>	leftMenu;
	private final FileSystemInterface		fsi;
	private final SubstitutableProperties	props = new SubstitutableProperties();
	private final JMenuBar					menu;
	private final JFileContentManipulator	manipulator;
	private final ActionListener			lruListener = (e)->openLRU(e.getActionCommand());
	private final ActionListener			navigatorListener;
	private final CardWindow				cardWindow;
	private final JStateString				state;
	
	private ResourcesManager				rm = null;
	
	public Application(final ContentMetadataInterface app, final Localizer parent, final int localHelpPort, final CountDownLatch latch) throws NullPointerException, LocalizationException, IllegalArgumentException, IOException, ContentException {
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
			this.leftMenu = new SimpleNavigatorTree<ContentNodeMetadata>(localizer,app.byUIPath(URI.create("ui:/model/navigation.top.navigator"))) {
								private static final long serialVersionUID = 1L;

								@Override
								protected JPopupMenu getPopupMenu(final TreePath path, final ContentNodeMetadata meta) {
									return internalGetPopupMenu(path, meta, app);
								}
							};
			this.navigatorListener = SwingUtils.buildAnnotatedActionListener(this);
			this.cardWindow = new CardWindow(localizer);
			this.fsi = FileSystemFactory.createFileSystem(URI.create("fsys:file://./"));
			this.state = new JStateString(localizer);

			parent.push(localizer);
			
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
			this.manipulator.addFileContentChangeListener((e)->SwingUtilities.invokeLater(()->changeState(e)));
			changeState(new FileContentChangedEvent() {
				@Override
				public FileContentChangeType getChangeType() {
					return FileContentChangeType.LRU_LIST_REFRESHED;
				}

				@Override
				public JFileContentManipulator getOwner() {
					return manipulator;
				}
			});
			
			leftMenu.addActionListener((e)->{internalCallNavigator(e.getActionCommand());});
			
			final JSplitPane	left = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(leftMenu), new JLabel("?????"));
			final JSplitPane	total = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, cardWindow);

			state.setBorder(new EtchedBorder());
			
			getContentPane().add(menu, BorderLayout.NORTH);
			getContentPane().add(total, BorderLayout.CENTER);
			getContentPane().add(state, BorderLayout.SOUTH);
			SwingUtils.assignActionListeners(menu,this);
			SwingUtils.centerMainWindow(this,0.75f);
			SwingUtils.assignExitMethod4MainWindow(this,()->{exitApplication();});
			localizer.addLocaleChangeListener(this);
			
			fillLocalizedStrings(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
			
			cardWindow.add(new EditorPane(app,localizer), EDITOR_WINDOW);
			cardWindow.select(EDITOR_WINDOW);
			pack();
			
			state.message(Severity.info, "Ready");
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings(oldLocale, newLocale);
		SwingUtils.refreshLocale(menu, oldLocale, newLocale);
		SwingUtils.refreshLocale(leftMenu, oldLocale, newLocale);
		SwingUtils.refreshLocale(state, oldLocale, newLocale);
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

	@OnAction("action:/newFile")
	private void newFile() throws IOException {
		manipulator.newFile();
		manipulator.setModificationFlag();
		refreshMenu();
	}

	@OnAction("action:/openFile")
	private void openFile() throws IOException {
		try{manipulator.openFile();
			refreshMenu();
		} catch (IOException e) {
			state.message(Severity.error,"Error opening project: "+e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/saveFile")
	private void saveFile() throws IOException {
		try{manipulator.saveFile();
			refreshMenu();
		} catch (IOException e) {
			state.message(Severity.error,"Error saving project: "+e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/saveFileAs")
	private void saveFileAs() throws IOException {
		try{manipulator.saveFileAs();
			refreshMenu();
		} catch (IOException e) {
			state.message(Severity.error,"Error opening keystore: "+e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/exit")
	private void exitApplication() throws IOException {
		try{manipulator.close();
			latch.countDown();
		} catch (UnsupportedOperationException exc) {
		}
	}

	@OnAction(value="action:/play",async=true)
	private void play() throws IOException {
	}
	
	@OnAction(value="action:/simulator",async=true)
	private void simulator() throws IOException {
	}

	@OnAction("action:/builtin.languages")
	private void selectLang(final Hashtable<String,String[]> langs) throws LocalizationException {
		localizer.getParent().setCurrentLocale(SupportedLanguages.valueOf(langs.get("lang")[0]).getLocale());
	}
	
	@OnAction("action:/helpContent")
	private void helpContent() throws IOException {
	}
	
	@OnAction("action:/helpAbout")
	private void helpAbout() throws IOException {
		try{final JEditorPane 	pane = new JEditorPane("text/html",null);
			final Icon			icon = new ImageIcon(this.getClass().getResource("avatar.jpg"));
			
			try(final Reader	rdr = localizer.getContent(I18N_ABOUT_CONTENT,new MimeType("text","x-wiki.creole"),new MimeType("text","html"))) {
				pane.read(rdr,null);
			}
			pane.setEditable(false);
			pane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			pane.setPreferredSize(new Dimension(300,300));
			pane.addHyperlinkListener(new HyperlinkListener() {
								@Override
								public void hyperlinkUpdate(final HyperlinkEvent e) {
									if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
										try{Desktop.getDesktop().browse(e.getURL().toURI());
										} catch (URISyntaxException | IOException exc) {
											exc.printStackTrace();
										}
									}
								}
			});
			
			JOptionPane.showMessageDialog(this,pane,localizer.getValue(I18N_ABOUT_TITLE),JOptionPane.PLAIN_MESSAGE,icon);
		} catch (LocalizationException | MimeParseException | IOException e) {
			state.message(Severity.error,e.getLocalizedMessage());
		}
	}

	@OnAction("action:/project.sources.newGroup")
	private void projectSourcesNewGroup(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new source group: "+parameters);
	}

	@OnAction("action:/project.sources.newItem")
	private void projectSourcesNewItem(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new source item: "+parameters);
	}

	@OnAction("action:/project.sources.removeAll")
	private void projectSourcesRemoveAll(final Map<String,String[]> parameters) throws IOException {
		System.err.println("remove all sources : "+parameters);
	}
	
	@OnAction("action:/project.checkers.templates.newGroup")
	private void projectCheckersTemplatesNewGroup(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new checker template group: "+parameters);
	}

	@OnAction("action:/project.checkers.templates.newItem")
	private void projectCheckersTemplatesNewItem(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new checker template item: "+parameters);
	}
	
	@OnAction("action:/project.checkers.templates.removeAll")
	private void projectCheckersTemplatesRemoveAll(final Map<String,String[]> parameters) throws IOException {
		System.err.println("remove all checker templates: "+parameters);
	}
	
	@OnAction("action:/project.checkers.tree.newGroup")
	private void projectCheckersTreeNewGroup(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new checker group: "+parameters);
	}
	
	@OnAction("action:/project.checkers.tree.newItem")
	private void projectCheckersTreeNewItem(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new checker item: "+parameters);
	}
	
	@OnAction("action:/project.checkers.tree.removeAll")
	private void projectCheckersTreeRemoveAll(final Map<String,String[]> parameters) throws IOException {
		System.err.println("remove all checkers: "+parameters);
	}
	
	@OnAction("action:/project.navigators.templates.newGroup")
	private void projectNavigatorsTemplatesNewGroup(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new navigator template group: "+parameters);
	}
	
	@OnAction("action:/project.navigators.templates.newItem")
	private void projectNavigatorsTemplatesNewItem(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new navigator template item: "+parameters);
	}
			
	@OnAction("action:/project.navigators.templates.removeAll")
	private void projectNavigatorsTemplatesRemoveAll(final Map<String,String[]> parameters) throws IOException {
		System.err.println("remove all navigator templates: "+parameters);
	}
	
	@OnAction("action:/project.navigators.roots.newGroup")
	private void projectNavigatorsRootsNewGroup(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new navigator group: "+parameters);
	}
	
	@OnAction("action:/project.navigators.roots.newItem")
	private void projectNavigatorsRootsNewItem(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new navigator item: "+parameters);
	}
	
	@OnAction("action:/project.navigators.roots.removeAll")
	private void projectNavigatorsRootsRemoveAll(final Map<String,String[]> parameters) throws IOException {
		System.err.println("remove all navigator group: "+parameters);
	}
	
	@OnAction("action:/project.screen.monitoring.newGroup")
	private void projectScreenMonitoringNewGroup(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new screen monitoring group: "+parameters);
	}
	
	@OnAction("action:/project.screen.monitoring.newItem")
	private void projectScreenMonitoringNewItem(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new screen monitoring item: "+parameters);
	}
	
	@OnAction("action:/project.screen.monitoring.removeAll")
	private void projectScreenMonitoringRemoveAll(final Map<String,String[]> parameters) throws IOException {
		System.err.println("remove all monitoring: "+parameters);
	}

	@OnAction("action:/project.screen.logs.newGroup")
	private void projectScreenLogsNewGroup(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new screen logs group: "+parameters);
	}
	
	@OnAction("action:/project.screen.logs.newItem")
	private void projectScreenLogsNewItem(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new screen log: "+parameters);
	}
	
	@OnAction("action:/project.screen.logs.removeAll")
	private void projectScreenLogsRemoveAll(final Map<String,String[]> parameters) throws IOException {
		System.err.println("remove all logs: "+parameters);
	}
	
	@OnAction("action:/project.screen.history.newGroup")
	private void projectScreenHistoryNewGroup(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new history group: "+parameters);
	}
	
	@OnAction("action:/project.screen.history.newItem")
	private void projectScreenHistoryNewItem(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new history log: "+parameters);
	}
	
	@OnAction("action:/project.screen.history.removeAll")
	private void projectScreenHistoryRemoveAll(final Map<String,String[]> parameters) throws IOException {
		System.err.println("remove all histories: "+parameters);
	}
	
	@OnAction("action:/project.admin.systemSettings")
	private void projectSystemSettings() throws IOException {
		System.err.println("systemSettings");
	}
	
	@OnAction("action:/project.admin.securitySettings")
	private void projectSecuritySettings() throws IOException {
		System.err.println("securitySettings");
	}
	
	@OnAction("action:/project.admin.roleTree.newGroup")
	private void projectAdminRoleTreeNewGroup(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new role group: "+parameters);
	}
	
	@OnAction("action:/project.admin.roleTree.newItem")
	private void projectAdminRoleTreeNewItem(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new role: "+parameters);
	}
	
	@OnAction("action:/project.admin.roleTree.removeAll")
	private void projectAdminRoleTreeRemoveAll(final Map<String,String[]> parameters) throws IOException {
		System.err.println("remove all roles: "+parameters);
	}

	@OnAction("action:/project.admin.userGroups.newGroup")
	private void projectAdminUserGroupsNewGroup(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new user group: "+parameters);
	}
	
	@OnAction("action:/project.admin.userGroups.newItem")
	private void projectAdminUserGroupsNewItem(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new user: "+parameters);
	}
	
	@OnAction("action:/project.admin.userGroups.removeAll")
	private void projectAdminUserGroupsRemoveAll(final Map<String,String[]> parameters) throws IOException {
		System.err.println("remove all users: "+parameters);
	}
	
	@OnAction("action:/project.admin.policiesTree.newGroup")
	private void projectAdminPoliciesTreeNewGroup(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new policy group: "+parameters);
	}
	
	@OnAction("action:/project.admin.policiesTree.newItem")
	private void projectAdminPoliciesTreeNewItem(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new policy: "+parameters);
	}
	
	@OnAction("action:/project.admin.policiesTree.removeAll")
	private void projectAdminPoliciesTreeRemoveAll(final Map<String,String[]> parameters) throws IOException {
		System.err.println("remove all policies: "+parameters);
	}
	
	@OnAction("action:/project.admin.stationsTree.newGroup")
	private void projectAdminStationsTreeNewGroup(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new station group: "+parameters);
	}
	
	@OnAction("action:/project.admin.stationsTree.newItem")
	private void projectAdminStationsTreeNewItem(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new station: "+parameters);
	}
	
	@OnAction("action:/project.admin.stationsTree.removeAll")
	private void projectAdminStationsTreeRemoveAll(final Map<String,String[]> parameters) throws IOException {
		System.err.println("remove all stations: "+parameters);
	}

	@OnAction("action:/project.admin.calendarsTree.newGroup")
	private void projectAdminCalendarsTreeNewGroup(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new calendar group: "+parameters);
	}
	
	@OnAction("action:/project.admin.calendarsTree.newItem")
	private void projectAdminCalendarsTreeNewItem(final Map<String,String[]> parameters) throws IOException {
		System.err.println("new calendar: "+parameters);
	}
	
	@OnAction("action:/project.admin.calendarsTree.removeAll")
	private void projectAdminCalendarsTreeRemoveAll(final Map<String,String[]> parameters) throws IOException {
		System.err.println("remove all calendars: "+parameters);
	}

	@OnAction("action:/project.admin.simulatorSettings")
	private void projectSimulatorSettings() throws IOException {
		System.err.println("simulatorSettings");
	}
	
	@OnAction("action:/project.admin.lifeCycle.applicationBuild")
	private void projectLifeCycleBuild() throws IOException {
		System.err.println("applicationBuild");
	}

	@OnAction("action:/project.admin.lifeCycle.applicationDeployment")
	private void projectLifeCycleDeploy() throws IOException {
		System.err.println("applicationDeployment");
	}

	@OnAction("action:/project.admin.lifeCycle.repositoryManagement")
	private void projectLifeCycleRepo() throws IOException {
		System.err.println("repositoryManagement");
	}

	@OnAction("action:/project.admin.lifeCycle.updateSoftware")
	private void projectLifeCycleUpdateSoftware() throws IOException {
		System.err.println("updateSoftware");
	}

	@OnAction("action:/project.admin.references.documentation")
	private void projectRefDocumentation() throws IOException {
		System.err.println("documentation");
	}
	
	@OnAction("action:/project.admin.references.resources")
	private void projectRefResources() {
		if (rm != null) {
			cardWindow.select(POPUP_WINDOW);
		}
		else {
			try{final FileSystemInterface	fsi = FileSystemFactory.createFileSystem(URI.create("fsys:file:./"));
				final String				lastWindow = cardWindow.selected();
				
				rm = new ResourcesManager(app,localizer,state,fsi,()->
										{	if (lastWindow != null && lastWindow.isEmpty()) {
												cardWindow.select(lastWindow);
											}
											rm.setVisible(false);
											cardWindow.remove(rm);
											rm = null;
											try{fsi.close();
											} catch (IOException e) {
											}
										}
									);
				cardWindow.add(rm, POPUP_WINDOW);
				cardWindow.select(POPUP_WINDOW);
			} catch (LocalizationException | ContentException | IOException e) {
				state.message(Severity.error, "Error opening resource manager : "+e.getLocalizedMessage(), e);
			}
		}
	}

	@OnAction("action:/project.admin.references.help")
	private void projectRefHelp() throws IOException {
		System.err.println("help");
	}

	@OnAction("action:/project.admin.references.about")
	private void projectRefAbout() throws IOException {
		try(final SimpleURLClassLoader		loader = new SimpleURLClassLoader(EMPTY_URLS, this.getClass().getClassLoader())) {
			final AboutProjectSettings		aps = new AboutProjectSettings(state);
			final ContentMetadataInterface	mdi = ContentModelFactory.forAnnotatedClass(aps.getClass());
			final AutoBuiltForm<AboutProjectSettings>	form = new AutoBuiltForm<AboutProjectSettings>(mdi, localizer, loader, aps, aps);
			
			aps.allowUnnamedModuleAccess(loader.getUnnamedModule());
			form.setPreferredSize(new Dimension(300,230));
			if (new JDialogContainer<>(localizer, this, extractCaptionFromAnnotation(aps.getClass()), form).showDialog()) {
				
			}
		} catch (LocalizationException | ContentException e) {
			e.printStackTrace();
			state.message(Severity.error, e.getLocalizedMessage(), e);
		}
	}
	
	private void changeState(final FileContentChangedEvent event) {
		final JMenuItem		save = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.save");
		final JMenuItem		saveAs = (JMenuItem)SwingUtils.findComponentByName(menu, "menu.file.saveAs");
		
		switch (event.getChangeType()) {
			case FILE_LOADED		:
				save.setEnabled(false);
				saveAs.setEnabled(true);
			case FILE_STORED_AS		:
				fillLRUSubmenu(manipulator);
				break;
			case MODIFICATION_FLAG_CLEAR	:
				save.setEnabled(false);
				break;
			case MODIFICATION_FLAG_SET		:
				save.setEnabled(true);
				break;
			case NEW_FILE_CREATED	:
				saveAs.setEnabled(true);
				break;
			case FILE_STORED		:
				break;
			case LRU_LIST_REFRESHED	:
				fillLRUSubmenu(manipulator);
				break;
			default:
				throw new UnsupportedOperationException("Change event type ["+event.getChangeType()+"] is not supported yet");
		}
	}
	
	private void fillLRUSubmenu(final JFileContentManipulator manipulator) {
		final JMenu	lru = (JMenu)SwingUtils.findComponentByName(menu, "menu.file.lru");
		boolean		added = false;

		for (int index = 0; index < lru.getMenuComponentCount(); index++) {
			((JMenuItem)lru.getMenuComponent(index)).removeActionListener(lruListener);
		}
		lru.removeAll();
		for (String item : manipulator.getLastUsed()) {
			final String	f = new File(item).getAbsolutePath().replace(File.separatorChar,'/');
			final JMenuItem	mi = new JMenuItem(f);
			final String	name = item; 
			
			mi.addActionListener(lruListener);
			mi.setActionCommand(name);
			lru.add(mi);
			added = true;
		}
		lru.setEnabled(added);
	}

	private void openLRU(final String name) {
		try{manipulator.openLRUFile(name);
			refreshMenu();
		} catch (IOException e) {
			state.message(Severity.error,"Error opening file ["+name+"] : "+e.getLocalizedMessage());
		}
	}
	
	private void refreshMenu() {

	}

	private void internalCallNavigator(final String actionCommand) {
		if (actionCommand != null && !actionCommand.isEmpty()) {
			navigatorListener.actionPerformed(new ActionEvent(this,0,actionCommand));
		}
	}

	private JPopupMenu internalGetPopupMenu(final TreePath path, final ContentNodeMetadata meta, final ContentMetadataInterface app) {
		final ContentNodeMetadata	menuMeta = app.byUIPath(URI.create("ui:/model/navigation.top.navigator."+meta.getName())); 
		
		if (menuMeta != null) {
			final JPopupMenu	popup = SwingUtils.toJComponent(menuMeta, JPopupMenu.class);
			
			SwingUtils.assignActionListeners(popup, this, (as,obj,metaData,cargo)->{
				final ContentNodeMetadata	md = (ContentNodeMetadata)((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject(); 
				
				if (md != null) {
					return as+"?name="+md.getName()+(md.getApplicationPath() != null ? "&appUri="+md.getApplicationPath() : "");
				}
				else {
					return as;
				}
			});
			return popup;
		}
		else {
			return null;
		}
	}
	
	private String extractCaptionFromAnnotation(final Class<?> clazz) {
		if (clazz.isAnnotationPresent(LocaleResource.class)) {
			return clazz.getAnnotation(LocaleResource.class).value();
		}
		else {
			throw new IllegalArgumentException("Class ["+clazz.getCanonicalName()+"] is not annotated with ["+LocaleResource.class.getCanonicalName()+"]");
		}
	}
	
	private void fillLocalizedStrings(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		setTitle(localizer.getValue(I18N_APPLICATION_TITLE));
	}
	
	public static void main(String[] args) {
		try{final ArgParser						parser = new ApplicationArgParser().parse(args);
			final int							helpPort = !parser.isTyped(ARG_HELP_PORT) ? getFreePort() : parser.getValue(ARG_HELP_PORT, int.class);
			final SubstitutableProperties		props = new SubstitutableProperties(Utils.mkProps(
													 NanoServiceFactory.NANOSERVICE_PORT, ""+helpPort
													,NanoServiceFactory.NANOSERVICE_ROOT, "fsys:xmlReadOnly:root://chav1961.bt.mnemoed.Application/chav1961/bt/mnemoed/helptree.xml"
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

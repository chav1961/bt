package chav1961.bt.mnemoed.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Locale;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import chav1961.bt.mnemoed.interfaces.OnCloseCallback;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.ui.swing.SimpleNavigatorTree;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JCloseButton;
import chav1961.purelib.ui.swing.useful.LabelledLayout;

/**
 * name
 * description
 * content
 * type
 * reference 
 */
public class ResourcesManager extends JPanel implements LocaleChangeListener {
	private static final long 		serialVersionUID = -8665431845309620560L;
	public static final  String		RES_TITLE = "resourceManager.title";
	public static final  String		RES_TYPE_LABEL = "resourceManager.typeLabel";
	public static final  String		RES_TYPE_CONTENT_TT = "resourceManager.typeLabel.tt";
	public static final  String		RES_DESCRIPTOR_LABEL = "resourceManager.descriptorLabel";
	public static final  String		RES_DESCRIPTOR_CONTENT_TT = "resourceManager.descriptorLabel.tt";
	
	private final ContentMetadataInterface		mdi;
	private final Localizer						localizer;
	private final LoggerFacade					logger;
	private final FileSystemInterface			fsi;
	private final JsonNode						root;
	private final JCloseButton					closeButton;
	private final SimpleNavigatorTree<JsonNode>	leftTree;
	private final JLabel		resourceTitle = new JLabel("", JLabel.CENTER);
	private final JLabel		resourceTypeLabel = new JLabel();
	private final JComboBox		resourceType = new JComboBox();  
	private final JLabel		resourceDescriptorLabel = new JLabel();
	private final JTextArea		resourceDescriptor = new JTextArea();
	
	public ResourcesManager(final ContentMetadataInterface mdi, final  Localizer localizer, final LoggerFacade logger, final FileSystemInterface fsi, final OnCloseCallback closeCallback) throws NullPointerException, IllegalArgumentException, ContentException, LocalizationException {
		if (mdi == null) {
			throw new NullPointerException("Metadata interface can't be null");
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (fsi == null) {
			throw new NullPointerException("File system interface can't be null");
		}
		else if (closeCallback == null) {
			throw new NullPointerException("Close callback can't be null");
		}
		else {
			this.mdi = mdi;
			this.localizer = localizer;
			this.logger = logger;
			this.fsi = fsi;
			this.closeButton = new JCloseButton(localizer, (e)->{
				try{closeCallback.onClose();
				} catch (ContentException exc) {
					logger.message(Severity.error, "Error closing resource manager ("+exc.getLocalizedMessage()+")",exc);
				}
			});
			
			final JPanel		closePanel = new JPanel(new BorderLayout());
			final JPanel		rightTopPanel = new JPanel();
			final JPanel		rightPanel = new JPanel();
			
			rightTopPanel.setLayout(new LabelledLayout(10,3));
			rightTopPanel.add(resourceTypeLabel,LabelledLayout.LABEL_AREA);
			rightTopPanel.add(resourceType,LabelledLayout.CONTENT_AREA);
			rightTopPanel.add(resourceDescriptorLabel,LabelledLayout.LABEL_AREA);
			rightTopPanel.add(new JScrollPane(resourceDescriptor),LabelledLayout.CONTENT_AREA);
			
			resourceDescriptor.setColumns(50);
			resourceDescriptor.setRows(10);
			
			rightPanel.setLayout(new BorderLayout());
			rightPanel.add(rightTopPanel,BorderLayout.NORTH);
			
			closePanel.add(resourceTitle,BorderLayout.CENTER);
			closePanel.add(closeButton,BorderLayout.EAST);

			try{if (fsi.open("/content.json").exists()) {
					try(final Reader 			rdr = fsi.charRead("UTF-8");
						final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
						
						parser.next();
						this.root = JsonUtils.loadJsonTree(parser);
					}
				}
				else {
					this.root = new JsonNode(JsonNodeType.JsonObject,new JsonNode("root").setName(SimpleNavigatorTree.JSON_NAME));
				}
				this.leftTree = new SimpleNavigatorTree<JsonNode>(localizer,root) {
									private static final long serialVersionUID = 1L;

									@Override
									protected JPopupMenu getPopupMenu(final TreePath path, final JsonNode meta) {
										if (path != null) {
											final JPopupMenu	popup = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.resourcemanager.popup")), JPopupMenu.class);
											
											SwingUtils.assignActionListeners(popup, ResourcesManager.this, (actionCommand, item, metaData, cargo) -> {
												return actionCommand+"?item="+concatPathNames(path);
											});
											return popup;
										}
										else {
											return null;
										}
									}
								};
				this.leftTree.setRootVisible(true);
			} catch (IOException exc) {
				throw new ContentException(exc);
			}
			final JSplitPane	pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,new JScrollPane(leftTree), rightPanel);
			
			setLayout(new BorderLayout());
			add(pane,BorderLayout.CENTER);
			add(closePanel,BorderLayout.NORTH);
			pane.setDividerLocation(200);
			
			fillLocalizedStrings();
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		leftTree.localeChanged(oldLocale, newLocale);
	}


	@OnAction("action:/resourcemanager.popup.newGroup")
	private void newForder(final Map<String,String[]> parameters) {
		System.err.println("New folder: "+parameters);
	}
	
	@OnAction("action:/resourcemanager.popup.newItem")
	private void newResource(final Map<String,String[]> parameters) {
		System.err.println("New resource: "+parameters);
	}
	
	@OnAction("action:/resourcemanager.popup.removeAll")
	private void removeAllResources(final Map<String,String[]> parameters) {
		System.err.println("Remove all resources: "+parameters);
	}
	
	private String concatPathNames(final TreePath path) {
		final StringBuilder	sb = new StringBuilder();
		
		for (Object item : path.getPath()) {
			sb.append('/').append(((JsonNode)((DefaultMutableTreeNode)item).getUserObject()).getChild(SimpleNavigatorTree.JSON_NAME).getStringValue());
		}
		return sb.toString();
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		resourceTitle.setText(localizer.getValue(RES_TITLE));
		resourceTypeLabel.setText(localizer.getValue(RES_TYPE_LABEL));
		resourceType.setToolTipText(localizer.getValue(RES_TYPE_CONTENT_TT));
		resourceDescriptorLabel.setText(localizer.getValue(RES_DESCRIPTOR_LABEL));
		resourceDescriptor.setToolTipText(localizer.getValue(RES_DESCRIPTOR_CONTENT_TT));
	}
}

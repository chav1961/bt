package chav1961.bt.mnemoed.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

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
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.ui.swing.useful.LabelledLayout;

/**
 * name
 * description
 * content
 * type
 * reference 
 */
public class ResourcesManager extends JPanel implements LocaleChangeListener {
	private static final long 	serialVersionUID = -8665431845309620560L;
	private static final Icon	DELETE_ICON = new ImageIcon(ResourcesManager.class.getResource("delete.png"));

	private final Localizer				localizer;
	private final LoggerFacade			logger;
	private final FileSystemInterface	fsi;
	private final JsonNode				root;
	private final JButton				closeButton = new JButton(DELETE_ICON);
	private final JTree					leftTree;
	private final JLabel				resourceTitle = new JLabel("", JLabel.CENTER);
	private final JLabel				resourceTypeLabel = new JLabel();
	private final JComboBox				resourceType = new JComboBox();  
	private final JLabel				resourceDescriptorLabel = new JLabel();
	private final JTextArea				resourceDescriptor = new JTextArea();
	
	public ResourcesManager(final Localizer localizer, final LoggerFacade logger, final FileSystemInterface fsi, final OnCloseCallback closeCallback) throws NullPointerException, IllegalArgumentException, ContentException {
		if (localizer == null) {
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
			this.localizer = localizer;
			this.logger = logger;
			this.fsi = fsi;
			
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
			closeButton.setPreferredSize(new Dimension(DELETE_ICON.getIconWidth(),DELETE_ICON.getIconHeight()));
			closeButton.setBorder(null);
			closeButton.addActionListener((e)->{
				try{closeCallback.onClose();
				} catch (ContentException exc) {
					logger.message(Severity.error, "Error closing resource manager ("+exc.getLocalizedMessage()+")",exc);
				}
			});

			try{if (fsi.open("/content.json").exists()) {
					try(final Reader 			rdr = fsi.charRead("UTF-8");
						final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
						
						this.root = JsonUtils.loadJsonTree(parser);
					}
				}
				else {
					this.root = new JsonNode(JsonNodeType.JsonObject);
					this.root.addChild(new JsonNode("root").setName("name"));
				}
				this.leftTree = new JTree(new DefaultTreeModel(reflect2Tree(this.root)));
			} catch (IOException exc) {
				throw new ContentException(exc);
			}
			leftTree.setCellRenderer(new DefaultTreeCellRenderer() {
				private static final long serialVersionUID = 2925193734243439537L;

				@Override
				public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
					final JLabel	label = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
					final JsonNode	node = (JsonNode)((DefaultMutableTreeNode)value).getUserObject();
	
					if (node.hasName("name")) {
						label.setText(node.getChild("name").getStringValue());
					}
					else {
						label.setText("--- unknown ---");
					}
					return label;
				}
			});
			
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
		// TODO Auto-generated method stub
		fillLocalizedStrings();
	}

	private static DefaultMutableTreeNode reflect2Tree(final JsonNode node) {
		switch (node.getType()) {
			case JsonObject		:
				final DefaultMutableTreeNode	result = new DefaultMutableTreeNode(node, node.childrenCount() > 0);
				
				if (node.hasName("content")) {
					for (JsonNode item : node.getChild("content").children()) {
						result.add(reflect2Tree(item));
					}
				}
				return result;
			default :
				throw new UnsupportedOperationException("Node type ["+node.getType()+"] is not supported yet");
		}
	}

	
	private void fillLocalizedStrings() {
		resourceTitle.setText("--- TITLE ---");
		resourceTypeLabel.setText("type:");
		resourceType.setToolTipText("combo...");
		resourceDescriptorLabel.setText("descriptor :");
		resourceDescriptor.setText("text...");
	}
}

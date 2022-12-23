package chav1961.bt.security.swing;

import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import chav1961.bt.security.interfaces.KeyStoreControllerException;
import chav1961.bt.security.interfaces.KeyStoreEntryType;
import chav1961.bt.security.interfaces.SecurityProcessingException;
import chav1961.bt.security.internal.DropDialog;
import chav1961.bt.security.internal.InternalUtils;
import chav1961.bt.security.internal.PasteDialog;
import chav1961.bt.security.keystore.KeyStoreController;
import chav1961.bt.security.keystore.KeyStoreDesKeyEntry;
import chav1961.bt.security.keystore.KeyStoreEntry;
import chav1961.bt.security.keystore.KeyStoreItem;
import chav1961.bt.security.keystore.KeyStoreRsaKeyEntry;
import chav1961.bt.security.keystore.KeyStoreTrustedCertificateEntry;
import chav1961.bt.security.keystore.KeyStoreUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.LocalizedFormatter;

public class KeyStoreTree extends JTree {
	private static final long 				serialVersionUID = -9174686085959197531L;
	private static final String				ASK_DELETE_TITLE = "aaa";
	private static final String				ASK_DELETE_MESSAGE = "aaa";
	private static final String				ASK_COPY_TITLE = "aaa";
	private static final String				ASK_COPY_MESSAGE = "aaa";
	
	private static final DataFlavor			ENTRY_DATA_FLAVOR;
	private static final DataFlavor[]		ENTRY_DATA_FLAVOR_LIST;
	
	static {
		ENTRY_DATA_FLAVOR = new DataFlavor(KeyStoreSerializedEntry.class, "Keystore entry");
		ENTRY_DATA_FLAVOR_LIST = new DataFlavor[]{ENTRY_DATA_FLAVOR};
	}
	
	private final Localizer					localizer;
	private final KeyStoreController		controller;
	private final DefaultMutableTreeNode	root;

	public KeyStoreTree(final KeyStoreController controller) throws KeyStoreControllerException {
		this(InternalUtils.LOCALIZER, controller);
	}	
	
	public KeyStoreTree(final Localizer localizer, final KeyStoreController controller) throws KeyStoreControllerException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (controller == null) {
			throw new NullPointerException("Key store controller can't be null");
		}
		else {
			this.localizer = localizer;
			this.controller = controller;
			((DefaultTreeModel)getModel()).setRoot(this.root = buildTree(controller));
			setRootVisible(false);
			try{
				setCellRenderer(SwingUtils.getCellRenderer(KeyStoreItem.class, new FieldFormat(String.class), TreeCellRenderer.class));
				setCellEditor(SwingUtils.getCellEditor(KeyStoreItem.class, new FieldFormat(String.class), TreeCellEditor.class));
			} catch (EnvironmentException e) {
				throw new KeyStoreControllerException(e);
			}
			addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mouseExited(MouseEvent e) {}
				@Override public void mouseEntered(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				
				@Override
				public void mouseClicked(final MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
						final TreePath	path = getPathForLocation(e.getX(), e.getY());
						
						if (path == null) {
							getPopupMenu().show(KeyStoreTree.this, e.getX(), e.getY());
						}
						else {
							getPopupMenu(((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject()).show(KeyStoreTree.this, e.getX(), e.getY());
						}
					}
				}
			});
			SwingUtils.assignActionKey(this, SwingUtils.KS_DELETE, (e)->delete(e), SwingUtils.ACTION_DELETE);
			SwingUtils.assignActionKey(this, SwingUtils.KS_COPY, (e)->copy(e), SwingUtils.ACTION_COPY);
			SwingUtils.assignActionKey(this, SwingUtils.KS_PASTE, (e)->paste(e), SwingUtils.ACTION_PASTE);
			SwingUtils.assignActionKey(this, SwingUtils.KS_CONTEXTMENU, (e)->contextMenu(e), SwingUtils.ACTION_CONTEXTMENU);
			new KeyStoreTreeTransferHandler(this);
		}
	}

	protected JPopupMenu getPopupMenu(final Object node) {
		return null;
	}

	protected JPopupMenu getPopupMenu() {
		return null;
	}

	protected boolean hasKeyStoreEntryInClipboard() {
		return Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(ENTRY_DATA_FLAVOR);
	}
	
	protected boolean rename(final Object node, final String newName) {
		return false;
	}

	protected boolean delete(final Object parent, final Object node) {
		if (Objects.equals(controller, parent) && (node instanceof KeyStoreEntry)) {
			try{
				controller.deleteEntry(((KeyStoreEntry)node).getAlias());
				return true;
			} catch (KeyStoreControllerException e) {
				SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
				return false;
			}
		}
		else {
			return false;
		}
	}

	protected boolean insert(final Object parent, final KeyStoreEntry entry, final String aliasName, final char[] password) {
		return false;
	}
	
	protected boolean copy(final Object parent, final Object node, final char[] password) {
		if (Objects.equals(controller, parent) && (node instanceof KeyStoreEntry)) {
			try{final KeyStoreSerializedEntry	se = KeyStoreSerializedEntry.of((KeyStoreEntry)node);
				final Transferable				t = new KeyStoreEntryTransferrable(se);
				
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(t, (cl,tr)->se.close());
				return true;
			} catch (SecurityProcessingException e) {
				SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
				return false;
			}
		}
		else {
			return false;
		}
	}

	protected boolean paste(final Object node, final char[] password) {
		return false;
	}
	
	protected char[] askPassword(final Object parent, final String alias) {
		return null;
	}
	
	private DefaultMutableTreeNode buildTree(final Object node) throws KeyStoreControllerException {
		if (node instanceof KeyStoreController) {
			final DefaultMutableTreeNode	result = new DefaultMutableTreeNode(node);
			
			for (KeyStoreEntry item : ((KeyStoreController)node).getKeyStoreEntries()) {
				result.add(buildTree(item));
			}
			return result;
		}
		else if (node instanceof KeyStoreRsaKeyEntry) {
			final DefaultMutableTreeNode	result = new DefaultMutableTreeNode(node);

			DefaultMutableTreeNode	chain = result;
			
			for (Certificate item : ((KeyStoreRsaKeyEntry)node).getCertificateChain()) {
				final DefaultMutableTreeNode	leaf = new DefaultMutableTreeNode(item);
				
				chain.add(leaf);
				chain = leaf;
			}
			return result;
		}
		else if (node instanceof KeyStoreDesKeyEntry) {
			return new DefaultMutableTreeNode(node);
		}
		else if (node instanceof KeyStoreTrustedCertificateEntry) {
			return new DefaultMutableTreeNode(node);
		}
		else {
			throw new UnsupportedOperationException("Key store entry type ["+node.getClass().getCanonicalName()+"] is not supported yet");
		}
	}

	private void delete(final ActionEvent e) {
		if (getSelectionCount() == 1) {
			final TreePath					path = getSelectionPath();
			final DefaultMutableTreeNode	leaf = (DefaultMutableTreeNode)path.getLastPathComponent(); 
			final KeyStoreEntry				entry = (KeyStoreEntry)leaf.getUserObject();
			final Object					parent = ((DefaultMutableTreeNode)leaf.getParent()).getUserObject();
			
			if (new JLocalizedOptionPane(localizer).confirm(this, new LocalizedFormatter(ASK_DELETE_MESSAGE, entry.getAlias()), ASK_DELETE_TITLE, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				if (delete(parent, entry)) {
					((DefaultTreeModel)getModel()).removeNodeFromParent(leaf);
				}
			}
		}
	}

	private void copy(final ActionEvent e) {
		if (getSelectionCount() == 1) {
			final TreePath					path = getSelectionPath();
			final DefaultMutableTreeNode	leaf = (DefaultMutableTreeNode)path.getLastPathComponent(); 
			final KeyStoreEntry				entry = (KeyStoreEntry)leaf.getUserObject();
			final Object					parent = ((DefaultMutableTreeNode)leaf.getParent()).getUserObject();
			
			if (new JLocalizedOptionPane(localizer).confirm(this, new LocalizedFormatter(ASK_COPY_MESSAGE, entry.getAlias()), ASK_COPY_TITLE, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				final char[]	pwd = askPassword(parent, entry.getAlias());

				if (pwd != null) {
					try{
						copy(parent, entry, pwd);
					} finally {
						Arrays.fill(pwd, ' ');
					}
				}
			}
		}
	}
	
	private boolean paste(final ActionEvent e) {
		if (hasKeyStoreEntryInClipboard()) {
			try{final PasteDialog	pd = new PasteDialog();
				
				pd.alias = controller.generateUniqueAliasName();
				pd.password = new char[0];
				pd.passwordRetyped = new char[0];
			
				if (true) {
					try(KeyStoreSerializedEntry	entry = (KeyStoreSerializedEntry)Toolkit.getDefaultToolkit().getSystemClipboard().getData(ENTRY_DATA_FLAVOR)) {
	
						if (insert(controller, entry.toKeyStoreEntry(pd.alias), pd.password)) {
							((DefaultTreeModel)getModel()).nodeStructureChanged(root);
						}
					} finally {
						Arrays.fill(pd.password, ' ');
						Arrays.fill(pd.passwordRetyped, ' ');
					}
				}
			} catch (KeyStoreControllerException | HeadlessException | UnsupportedFlavorException | IOException | SecurityProcessingException exc) {
				SwingUtils.getNearestLogger(this).message(Severity.error, exc, exc.getLocalizedMessage());
			}
		}
		return false;
	}

	private boolean insert(final Object parent, final KeyStoreEntry entry, final char[] password) {
		return insert(parent, entry, entry.getAlias(), password);
	}
	
	private boolean drop(final List<File> content) {
		boolean	result = true;
		
		try(final DropDialog	dd = new DropDialog()) {
			
			for (File item : content) {
				dd.filePath = item.getAbsolutePath();
				dd.aliasName = item.getName().lastIndexOf('.') > 0 ? item.getName().substring(0, item.getName().lastIndexOf('.')) : item.getName();
				dd.type = KeyStoreUtils.detectKeyStoreEntryTypeByFileName(item.getName());
				dd.password = new char[0];
				dd.passwordRetyped = new char[0];
				
				if (true) {
					try{final KeyStoreEntry	entry = KeyStoreUtils.loadKeyStoreEntry(item, dd.type, KeyStoreUtils.detectKeyStoreEntryOptionsByFileName(item.getName()));
						
						insert(controller, entry, dd.aliasName, dd.password);
					} catch (IOException e) {
						SwingUtils.getNearestLogger(this).message(Severity.error, e, e.getLocalizedMessage());
						result = false;
					}
				}
			}
		}
		return result;		
	}
	
	private void contextMenu(final ActionEvent e) {
		if (getSelectionCount() == 1) {
			final TreePath					path = getSelectionPath();
			final DefaultMutableTreeNode	leaf = (DefaultMutableTreeNode)path.getLastPathComponent(); 
			final KeyStoreEntry				entry = (KeyStoreEntry)leaf.getUserObject();
			final Rectangle					bounds = getPathBounds(path);

			getPopupMenu(entry).show(this, (int)bounds.getCenterX(), (int)bounds.getCenterY());
		}
		else {
			getPopupMenu().show(this, 0, 0);
		}
	}

	private static class KeyStoreEntryTransferrable implements Transferable {
		private final KeyStoreSerializedEntry	se;
		
		private KeyStoreEntryTransferrable(final KeyStoreSerializedEntry se) {
			this.se = se;
		}
		
		@Override
		public boolean isDataFlavorSupported(final DataFlavor flavor) {
			return Objects.equals(flavor, ENTRY_DATA_FLAVOR);
		}
		
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return ENTRY_DATA_FLAVOR_LIST;
		}
		
		@Override
		public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (isDataFlavorSupported(flavor)) {
				return se;
			}
			else {
				return null;
			}
		}
	};
	
	private static class KeyStoreTreeTransferHandler implements DragGestureListener, DragSourceListener, DropTargetListener {
		private KeyStoreTree tree;
		private DragSource dragSource; // dragsource
		private DropTarget dropTarget; //droptarget
		private DefaultMutableTreeNode draggedNode; 
		private DefaultMutableTreeNode draggedNodeParent; 

		protected KeyStoreTreeTransferHandler(KeyStoreTree tree) {
			this.tree = tree;
			dragSource = new DragSource();
			dragSource.createDefaultDragGestureRecognizer(tree, DnDConstants.ACTION_COPY, this);
			dropTarget = new DropTarget(tree, DnDConstants.ACTION_COPY, this);
		}

		public boolean canPerformAction(KeyStoreTree target, DefaultMutableTreeNode draggedNode, int action, Point location) {
			TreePath pathTarget = target.getPathForLocation(location.x, location.y);
			if (pathTarget == null) {
				target.setSelectionPath(null);
				return(false);
			}
			target.setSelectionPath(pathTarget);
			if(action == DnDConstants.ACTION_COPY) {
				return(true);
			}
			else
			if(action == DnDConstants.ACTION_MOVE) {	
				DefaultMutableTreeNode parentNode =(DefaultMutableTreeNode)pathTarget.getLastPathComponent();				
				if (draggedNode.isRoot() || parentNode == draggedNode.getParent() || draggedNode.isNodeDescendant(parentNode)) {					
					return(false);	
				}
				else {
					return(true);
				}				 
			}
			else {		
				return(false);	
			}
		}

		public boolean executeDrop(KeyStoreTree tree, DefaultMutableTreeNode draggedNode, DefaultMutableTreeNode newParentNode, int action) {
			return false;
//			if (action == DnDConstants.ACTION_COPY) {
//				DefaultMutableTreeNode newNode = target.makeDeepCopy(draggedNode);
//				((DefaultTreeModel)target.getModel()).insertNodeInto(newNode,newParentNode,newParentNode.getChildCount());
//				TreePath treePath = new TreePath(newNode.getPath());
//				target.scrollPathToVisible(treePath);
//				target.setSelectionPath(treePath);	
//				return(true);
//			}
//			if (action == DnDConstants.ACTION_MOVE) {
//				draggedNode.removeFromParent();
//				((DefaultTreeModel)target.getModel()).insertNodeInto(draggedNode,newParentNode,newParentNode.getChildCount());
//				TreePath treePath = new TreePath(draggedNode.getPath());
//				target.scrollPathToVisible(treePath);
//				target.setSelectionPath(treePath);
//				return(true);
//			}
//			return(false);
		}
		
		/* Methods for DragSourceListener */
		public void dragDropEnd(DragSourceDropEvent dsde) {
			if (dsde.getDropSuccess() && dsde.getDropAction()==DnDConstants.ACTION_MOVE && draggedNodeParent != null) {
				((DefaultTreeModel)tree.getModel()).nodeStructureChanged(draggedNodeParent);				
			}
		}
		public final void dragEnter(DragSourceDragEvent dsde)  {
			int action = dsde.getDropAction();
			if (action == DnDConstants.ACTION_COPY)  {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
			} 
			else {
				if (action == DnDConstants.ACTION_MOVE) {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
				} 
				else {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				}
			}
		}
		public final void dragOver(DragSourceDragEvent dsde) {
			int action = dsde.getDropAction();
			if (action == DnDConstants.ACTION_COPY) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
			} 
			else  {
				if (action == DnDConstants.ACTION_MOVE) {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
				} 
				else  {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				}
			}
		}
		public final void dropActionChanged(DragSourceDragEvent dsde)  {
			int action = dsde.getDropAction();
			if (action == DnDConstants.ACTION_COPY) {
				dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
			}
			else  {
				if (action == DnDConstants.ACTION_MOVE) {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
				} 
				else {
					dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				}
			}
		}
		public final void dragExit(DragSourceEvent dse) {
		   dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
		}	
			
		/* Methods for DragGestureListener */
		public final void dragGestureRecognized(DragGestureEvent dge) {
			TreePath path = tree.getSelectionPath(); 
			if (path != null) { 
				draggedNode = (DefaultMutableTreeNode)path.getLastPathComponent();
				draggedNodeParent = (DefaultMutableTreeNode)draggedNode.getParent();
				try {
					dragSource.startDrag(dge, DragSource.DefaultMoveNoDrop , new KeyStoreEntryTransferrable(KeyStoreSerializedEntry.of((KeyStoreEntry)draggedNode.getUserObject())), this);
				} catch (InvalidDnDOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}	 
		}

		/* Methods for DropTargetListener */

		public final void dragEnter(DropTargetDragEvent dtde) {
			Point pt = dtde.getLocation();
			int action = dtde.getDropAction();
			if (canPerformAction(tree, draggedNode, action, pt)) {
				dtde.acceptDrag(action);			
			}
			else {
				dtde.rejectDrag();
			}
		}

		public final void dragExit(DropTargetEvent dte) {
		}

		public final void dragOver(DropTargetDragEvent dtde) {
			Point pt = dtde.getLocation();
			int action = dtde.getDropAction();
			if (canPerformAction(tree, draggedNode, action, pt)) {
				dtde.acceptDrag(action);			
			}
			else {
				dtde.rejectDrag();
			}
		}

		public final void dropActionChanged(DropTargetDragEvent dtde) {
			Point pt = dtde.getLocation();
			int action = dtde.getDropAction();
			if (canPerformAction(tree, draggedNode, action, pt)) {
				dtde.acceptDrag(action);			
			}
			else {
				dtde.rejectDrag();
			}
		}

		public final void drop(DropTargetDropEvent dtde) {
			try {
				int action = dtde.getDropAction();
				Transferable transferable = dtde.getTransferable();
				Point pt = dtde.getLocation();
				
				if (transferable.isDataFlavorSupported(ENTRY_DATA_FLAVOR) && canPerformAction(tree, draggedNode, action, pt)) {
					TreePath pathTarget = tree.getPathForLocation(pt.x, pt.y);
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) transferable.getTransferData(ENTRY_DATA_FLAVOR);
					DefaultMutableTreeNode newParentNode =(DefaultMutableTreeNode)pathTarget.getLastPathComponent();
					if (executeDrop(tree, node, newParentNode, action)) {
						dtde.acceptDrop(action);				
						dtde.dropComplete(true);
						return;					
					}
				}
				dtde.rejectDrop();
				dtde.dropComplete(false);
			}		
			catch (Exception e) {	
				dtde.rejectDrop();
				dtde.dropComplete(false);
			}	
		}
	}
	
	private static class KeyStoreSerializedEntry implements Serializable, AutoCloseable {
		private static final long 		serialVersionUID = -1816384766384324140L;
		
		private final KeyStoreEntryType	type;
		private final byte[]			privateContent;
		private final byte[][]			publicContent;
		
		private KeyStoreSerializedEntry(final KeyStoreEntryType type, final byte[] privateContent, final byte[][] publicContent) {
			this.type = type;
			this.privateContent = privateContent;
			this.publicContent = publicContent;
		}
		
		@Override
		public void close() throws RuntimeException {
			if (privateContent != null) {
				Arrays.fill(privateContent, (byte)0);
			}
			if (publicContent != null) {
				for (byte[] item : publicContent) {
					if (item != null) {
						Arrays.fill(item, (byte)0);
					}
				}
				Arrays.fill(publicContent, (byte)0);
			}
		}

		private <T extends KeyStoreEntry> T toKeyStoreEntry(final String alias) throws SecurityProcessingException {
			try {
				switch (type) {
					case CERTIFICATE	:
						return (T) new KeyStoreTrustedCertificateEntry(alias, CertificateFactory.getInstance("RSA").generateCertificate(new ByteArrayInputStream(publicContent[0])));
					case DES_KEY		:
						break;
					case RSA_KEY		:
						break;
					default:
						throw new UnsupportedOperationException("Key store entry type ["+type+"] is not supported yet");
				}
				return null;
			} catch (CertificateException exc) {
				throw new SecurityProcessingException(exc.getLocalizedMessage(), exc);
			}
		}
		
		private static KeyStoreSerializedEntry of(final KeyStoreEntry entry) throws SecurityProcessingException {
			if (entry == null) {
				throw new NullPointerException("Keys tore entry can't be null"); 
			}
			else {
				try{switch (entry.getEntryType()) {
						case CERTIFICATE	: 
							return of((KeyStoreTrustedCertificateEntry)entry);
						case DES_KEY		: 
							return of((KeyStoreDesKeyEntry)entry);
						case RSA_KEY		: 
							return of((KeyStoreRsaKeyEntry)entry);
						default				: 
							throw new UnsupportedOperationException("Key store entry type ["+entry.getEntryType()+"] is not supported yet");
					}
				} catch (CertificateEncodingException exc) {
					throw new SecurityProcessingException(exc.getLocalizedMessage(), exc); 
				}
			}
		}
		
		private static KeyStoreSerializedEntry of(final KeyStoreRsaKeyEntry entry) throws CertificateEncodingException {
			final byte[][]	certs = new byte[entry.getCertificateChain().length][];
			
			for(int index = 0; index < certs.length; index++) {
				certs[index] = entry.getCertificateChain()[index].getEncoded();
			}
			return new KeyStoreSerializedEntry(KeyStoreEntryType.RSA_KEY, entry.getKey().getEncoded(), certs);
		}

		private static KeyStoreSerializedEntry of(final KeyStoreDesKeyEntry entry) {
			return new KeyStoreSerializedEntry(KeyStoreEntryType.DES_KEY, entry.getKey().getEncoded(), null);
		}

		private static KeyStoreSerializedEntry of(final KeyStoreTrustedCertificateEntry entry) throws CertificateEncodingException {
			return new KeyStoreSerializedEntry(KeyStoreEntryType.CERTIFICATE, null, new byte[][] {entry.getCertificate().getEncoded()});
		}
	}
}

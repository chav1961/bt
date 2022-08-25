package chav1961.bt.paint.dialogs;

import java.util.Arrays;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.swing.SwingUtils;

public class SubstitutionTable extends JTable {
	private static final long 		serialVersionUID = 1L;
	
	private static final String		KEY_TABLE_KEY = "chav1961.bt.paint.dialogs.SubstitutionTable.key";
	private static final String		KEY_TABLE_VALUE = "chav1961.bt.paint.dialogs.SubstitutionTable.value";
	
	private final Localizer					localizer;
	private final SubstitutableProperties	props;
	private final TableModel				model;
	
	public SubstitutionTable(final Localizer localizer, final SubstitutableProperties props) throws PaintScriptException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (props == null) {
			throw new NullPointerException("Properties can't be null");
		}
		else {
			this.localizer = localizer;
			this.props = props;
			this.model = new SubstitutionTableModel(props);
			
			try{this.setDefaultRenderer(String.class, SwingUtils.getCellRenderer(String.class, null, TableCellRenderer.class));
				this.setDefaultEditor(String.class, SwingUtils.getCellEditor(String.class, null, TableCellEditor.class));
			} catch (EnvironmentException  exc) {
				throw new PaintScriptException(exc); 
			}
			setModel(model);
		}
	}
	
	public SubstitutableProperties getProps() {
		return props;
	}
	
	private class SubstitutionTableModel extends DefaultTableModel {
		private static final long 	serialVersionUID = 1L;
		
		private final SubstitutableProperties	props;
		private final String[]					keys;
		
		private SubstitutionTableModel(final SubstitutableProperties content) {
			this.props = content;
			this.keys = new String[content.size()];
			props.availableKeys().toArray(this.keys);
			Arrays.sort(this.keys);
		}
		
		@Override
		public int getRowCount() {
			return props == null ? 0 : props.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			switch (columnIndex) {
				case 0 	: 
					return localizer.getValue(KEY_TABLE_KEY);
				case 1 	: 
					return localizer.getValue(KEY_TABLE_VALUE);
				default :
					throw new UnsupportedOperationException(); 
			}
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			switch (columnIndex) {
				case 0 	: 
					return String.class;
				case 1 	: 
					return String.class;
				default :
					throw new UnsupportedOperationException(); 
			}
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0 	: 
					return false;
				case 1 	: 
					return true;
				default :
					throw new UnsupportedOperationException(); 
			}
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0 	: 
					return keys[rowIndex];
				case 1 	: 
					return props.getProperty(keys[rowIndex]);
				default :
					throw new UnsupportedOperationException(); 
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			props.setProperty(keys[rowIndex], (String)aValue);
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}
}

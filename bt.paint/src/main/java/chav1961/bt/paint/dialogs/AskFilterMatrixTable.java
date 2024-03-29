package chav1961.bt.paint.dialogs;


import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.ui.swing.SwingUtils;

public class AskFilterMatrixTable extends JTable {
	private static final long 	serialVersionUID = 1L;
	
	private final int			size;
	private final float[][]		content;
	private final TableModel	model;
	
	public AskFilterMatrixTable(final int size) throws PaintScriptException {
		if (size < 0 || size % 2 == 0) {
			throw new IllegalArgumentException("Size ["+size+"] must be positive odd");
		}
		else {
			this.size = size;
			this.content = new float[size][size];
			this.model = new FloatTableModel(content);
			
			try{this.setDefaultRenderer(Float.class, SwingUtils.getCellRenderer(Float.class, new FieldFormat(Float.class, "10.4ms"), TableCellRenderer.class));
				this.setDefaultEditor(Float.class, SwingUtils.getCellEditor(Float.class, new FieldFormat(Float.class, "10.4ms"), TableCellEditor.class));
			} catch (EnvironmentException  exc) {
				throw new PaintScriptException(exc); 
			}
			setModel(model);
		}
	}
	
	public float[] getMaxtrix() {
		final float[]	result = new float[size*size];
		int				index = 0;
		
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				result[index++] = content[x][y];
			}
		}
		return result;
	}
	
	private static class FloatTableModel extends DefaultTableModel {
		private static final long 	serialVersionUID = 1L;
		
		private final float[][]		content;
		
		private FloatTableModel(final float[][] content) {
			this.content = content; 
		}
		
		@Override
		public int getRowCount() {
			return content == null ? 0 : content.length;
		}

		@Override
		public int getColumnCount() {
			return content[0].length;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			return "#"+(columnIndex+1);
		}

		@Override
		public Class<Float> getColumnClass(final int columnIndex) {
			return Float.class;
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return true;
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			return Float.valueOf(content[rowIndex][columnIndex]);
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (aValue instanceof Number) {
				content[rowIndex][columnIndex] = ((Number)aValue).floatValue();
			}
			else if (aValue instanceof String) {
				content[rowIndex][columnIndex] = Float.valueOf(aValue.toString()).floatValue();
			}
			else {
				throw new UnsupportedOperationException(); 
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}
}

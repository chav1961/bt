package chav1961.bt.paint.dialogs;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class FilterMatrixTable extends JTable {
	private static final long 	serialVersionUID = 1L;
	
	private final int			size;
	private final Float[][]		content;
	private final String[]		columns;
	private final TableModel	model;
	
	public FilterMatrixTable(final int size) {
		if (size < 0 || size % 2 == 0) {
			throw new IllegalArgumentException("Size ["+size+"] must be positive odd");
		}
		else {
			this.size = size;
			this.content = new Float[size][size];
			this.columns = new String[size];
			for (int x = 0; x < size; x++) {
				columns[x] = ""+(x+1);
				for (int y = 0; y < size; y++) {
					content[x][y] = 0f;
				}
			}
			this.model = new DefaultTableModel(content, columns) {
								@Override
								public void setValueAt(Object aValue, int row, int column) {
									content[row][column] = (Float)aValue;
									super.setValueAt(aValue, row, column);
								}
							};
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
}

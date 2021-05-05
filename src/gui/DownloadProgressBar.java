package gui;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class DownloadProgressBar extends JProgressBar implements TableCellRenderer{
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		Integer v = (Integer)value;//这一列必须都是integer类型(0-100)
		setStringPainted(true);
		setValue(v);
		return this;
	}

}
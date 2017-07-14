package org.magic.gui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;

import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.magic.api.beans.MagicCardStock;
import org.magic.api.beans.MagicEdition;

public class StockTableRenderer extends DefaultTableRenderer {

	Component pane;
	
	
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row, int column) {
    	pane = super.getTableCellRendererComponent(table, value, isSelected,hasFocus, row, column);
    	pane.setForeground(Color.BLACK);
		if(((MagicCardStock)table.getValueAt(row, 0)).isUpdate())
    		pane.setBackground(Color.GREEN);
    	else
    		pane.setBackground(table.getBackground());
		return pane;
    }
	
    }
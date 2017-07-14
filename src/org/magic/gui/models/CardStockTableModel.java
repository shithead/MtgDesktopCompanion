package org.magic.gui.models;

import java.sql.SQLException;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.magic.api.beans.EnumCondition;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicCardStock;
import org.magic.api.beans.MagicCollection;
import org.magic.api.beans.MagicEdition;
import org.magic.services.MTGControler;
import org.magic.services.ThreadManager;


public class CardStockTableModel extends DefaultTableModel {

	
	static final String columns[] = new String[]{"Id","Card","Edition","Collection","Condition","Qte","Language","Comment"};
	List<MagicCardStock> list;
	
	
	public List<MagicCardStock> getList() {
		return list;
	}

	
	public void init()
	{
		ThreadManager.getInstance().execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					list = MTGControler.getInstance().getEnabledDAO().getStocks();
					fireTableDataChanged();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}, "load stocks");
	}
	
	public CardStockTableModel() {
			init();
	}
	
	
	@Override
	public int getColumnCount() {
		return columns.length;
	}
	
	
	@Override
	public int getRowCount() {
		if(list!=null)
			return list.size();
		
		return 0;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex)
		{
			case 0 : return MagicCardStock.class;
			case 1 : return MagicCard.class;
			case 2 : return MagicEdition.class;
			case 3 : return MagicCollection.class;
			case 4 : return EnumCondition.class;
			case 5 : return Integer.class;
			case 6 : return String.class;
			case 7 : return String.class;
			
			default : return super.getColumnClass(columnIndex);
		}
		
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return column>2;
	}
	
	
	@Override
	public String getColumnName(int column) {
		return columns[column];
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		
		switch(column)
		{
			case 0 : return list.get(row);
			case 1 : return list.get(row).getMagicCard();
			case 2 : return list.get(row).getMagicCard().getEditions().get(0);
			case 3 : return list.get(row).getMagicCollection();
			case 4 : return list.get(row).getCondition();
			case 5 : return list.get(row).getQte();
			case 6 : return list.get(row).getLanguage();
			case 7 : return list.get(row).getComment();
			
		default : return "";
		}
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int column) {
		switch(column)
		{
			case 3 : list.get(row).setMagicCollection(new MagicCollection(aValue.toString()));break;
			case 4 : list.get(row).setCondition((EnumCondition)aValue);break;
			case 5 : list.get(row).setQte((Integer)aValue);break;
			case 6 : list.get(row).setLanguage(String.valueOf(aValue));break;
			case 7 : list.get(row).setComment(String.valueOf(aValue));break;
		}
		list.get(row).setUpdate(true);
	}


	public void remove(MagicCardStock selected) {
		list.remove(selected);
		fireTableDataChanged();
		
	}
	

	public void add(MagicCardStock selected) {
		list.add(selected);
		fireTableDataChanged();
		
	}
	
	
	
}

package org.magic.gui.models;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;
import org.magic.api.beans.MagicCardAlert;
import org.magic.api.beans.MagicEdition;
import org.magic.services.MTGControler;
import org.magic.services.MTGLogger;

public class CardAlertTableModel extends DefaultTableModel {

	private transient Logger logger = MTGLogger.getLogger(this.getClass());

	static final String[] columns = new String[] { MTGControler.getInstance().getLangService().getCapitalize("CARD"),
			MTGControler.getInstance().getLangService().getCapitalize("EDITION"),
			MTGControler.getInstance().getLangService().getCapitalize("MAX_BID"),
			MTGControler.getInstance().getLangService().getCapitalize("OFFERS"),
			MTGControler.getInstance().getLangService().getCapitalize("DAILY"),
			MTGControler.getInstance().getLangService().getCapitalize("WEEKLY"),
			MTGControler.getInstance().getLangService().getCapitalize("PC_DAILY")
			};

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public int getRowCount() {
		try {
			if (MTGControler.getInstance().getEnabledDAO().listAlerts() != null)
				return MTGControler.getInstance().getEnabledDAO().listAlerts().size();

		} catch (Exception e) {
			logger.error(e);
		}

		return 0;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return MagicCardAlert.class;
		case 1:
			return List.class;
		case 2:
			return Double.class;
		case 3:
			return Integer.class;
		case 4:
			return Double.class;
		case 5:
			return Double.class;
		case 6:
			return Double.class;
		default:
			return super.getColumnClass(columnIndex);
		}

	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return (column == 2 || column==1);
	}

	@Override
	public String getColumnName(int column) {
		return columns[column];
	}

	@Override
	public Object getValueAt(int row, int column) {
		switch (column) {
		case 0:
			return MTGControler.getInstance().getEnabledDAO().listAlerts().get(row);
		case 1:
			return MTGControler.getInstance().getEnabledDAO().listAlerts().get(row).getCard().getEditions();
		case 2:
			return MTGControler.getInstance().getEnabledDAO().listAlerts().get(row).getPrice();
		case 3:
			return MTGControler.getInstance().getEnabledDAO().listAlerts().get(row).getOffers().size();
		case 4:
			return MTGControler.getInstance().getEnabledDAO().listAlerts().get(row).getShake().getPriceDayChange();
		case 5:
			return MTGControler.getInstance().getEnabledDAO().listAlerts().get(row).getShake().getPriceWeekChange();
		case 6:
			return MTGControler.getInstance().getEnabledDAO().listAlerts().get(row).getShake().getPercentDayChange();
		default:
			return "";
		}

	}

	@Override
	public void setValueAt(Object aValue, int row, int column) {
		MagicCardAlert alert = MTGControler.getInstance().getEnabledDAO().listAlerts().get(row);
		
		if (column == 1) {
			MagicEdition ed = (MagicEdition) aValue;
			alert.getCard().getEditions().remove(ed);
			alert.getCard().getEditions().add(0, (MagicEdition) aValue);
		}
		
		if(column==2) {
			alert.setPrice(Double.parseDouble(aValue.toString()));
		}
		
		try {
			MTGControler.getInstance().getEnabledDAO().updateAlert(alert);
			fireTableDataChanged();
		} catch (Exception e) {
			logger.error("error set value " + aValue, e);
		}
		
	}

}

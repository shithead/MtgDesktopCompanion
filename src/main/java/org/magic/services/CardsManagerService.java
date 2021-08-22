package org.magic.services;

import static org.magic.tools.MTG.getEnabledPlugin;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicCardStock;
import org.magic.api.beans.MagicCollection;
import org.magic.api.beans.MagicEdition;
import org.magic.api.interfaces.MTGCardsProvider;
import org.magic.api.interfaces.MTGDao;
import org.utils.patterns.observer.Observer;

public class CardsManagerService {
	
	private static Logger logger = MTGLogger.getLogger(CardsManagerService.class);

	public static MagicCard switchEditions(MagicCard mc, MagicEdition ed)
	{
		try {
			return getEnabledPlugin(MTGCardsProvider.class).searchCardByName(mc.getName(), ed, false).get(0);
		} catch (IOException e) {
			logger.error(mc +" is not found in " + ed);
			return mc;
		}
	}
	
	public static void removeCard(MagicCard mc , MagicCollection collection) throws SQLException
	{
		getEnabledPlugin(MTGDao.class).removeCard(mc, collection);
		if(MTGControler.getInstance().get("collections/stockAutoDelete").equals("true"))
		{ 
			getEnabledPlugin(MTGDao.class).listStocks(mc, collection,true).forEach(st->{
				try{
					getEnabledPlugin(MTGDao.class).deleteStock(st);	
				}
				catch(Exception e)
				{
					logger.error(e);
				}
			});
		}
		
	}
	
	public static void saveCard(MagicCard mc , MagicCollection collection,Observer o) throws SQLException
	{
		if(o!=null)
			getEnabledPlugin(MTGDao.class).addObserver(o);
		
		getEnabledPlugin(MTGDao.class).saveCard(mc, collection);
		
		if(MTGControler.getInstance().get("collections/stockAutoAdd").equals("true"))
		{ 
			MagicCardStock st = MTGControler.getInstance().getDefaultStock();
			st.setProduct(mc);
			st.setMagicCollection(collection);
			getEnabledPlugin(MTGDao.class).saveOrUpdateCardStock(st);
		}
		
		if(o!=null)
			getEnabledPlugin(MTGDao.class).removeObserver(o);
	}
	
	

}

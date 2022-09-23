package org.magic.services.workers;

import java.io.File;
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.logging.log4j.Logger;
import org.magic.api.beans.MTGNotification;
import org.magic.api.beans.MTGNotification.MESSAGE_TYPE;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicCardStock;
import org.magic.api.interfaces.MTGCardsExport;
import org.magic.gui.abstracts.AbstractBuzyIndicatorComponent;
import org.magic.services.MTGControler;
import org.magic.services.logging.MTGLogger;
import org.utils.patterns.observer.Observable;
import org.utils.patterns.observer.Observer;

public class StockExportWorker extends SwingWorker<Void, MagicCard> {

	protected MTGCardsExport exp;
	protected Logger logger = MTGLogger.getLogger(this.getClass());
	protected Observer o;
	protected List<MagicCard> cards = null;
	protected AbstractBuzyIndicatorComponent buzy;
	private File f;
	private List<MagicCardStock> export;
	private Exception err;



	public StockExportWorker(MTGCardsExport exp,List<MagicCardStock> export,AbstractBuzyIndicatorComponent buzy,File f) {
		init(exp,export,buzy,f);
	}

	public void init(MTGCardsExport exp,List<MagicCardStock> export,AbstractBuzyIndicatorComponent buzy,File f) {
		this.exp=exp;
		this.buzy=buzy;
		this.f=f;
		this.export=export;
		err=null;
		o=(Observable obs, Object c)->publish((MagicCard)c);
		exp.addObserver(o);
	}



	@Override
	protected Void doInBackground(){
		try {
			exp.exportStock(export, f);
		} catch (Exception e) {
			err=e;
			logger.error("error export with " + exp,e);
		}
		return null;
	}

	@Override
	protected void process(List<MagicCard> chunks) {
		buzy.progressSmooth(chunks.size());
	}

	@Override
	protected void done() {
		try {
			exp.removeObserver(o);
		} catch (Exception e) {
			logger.error(e);
		}
		buzy.end();

		if(err!=null)
		{
			MTGControler.getInstance().notify(err);
		}
		else
		{
			MTGControler.getInstance().notify(new MTGNotification(
					exp.getName() + " "+ MTGControler.getInstance().getLangService().get("FINISHED"),
					MTGControler.getInstance().getLangService().combine("EXPORT", "FINISHED"),
					MESSAGE_TYPE.INFO
					));
		}

	}

}

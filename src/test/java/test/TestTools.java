package test;

import static org.magic.services.tools.MTG.getEnabledPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.magic.api.beans.MagicCard;
import org.magic.api.exports.impl.JsonExport;
import org.magic.api.interfaces.MTGCardsProvider;
import org.magic.services.logging.MTGLogger;

public class TestTools {

	public static List<MagicCard> loadData() throws IOException, URISyntaxException
	{
		return new JsonExport().importDeckFromFile(new File(TestTools.class.getResource("/sample.json").toURI())).getMainAsList();
		
	}
	
	public static void initTest()
	{
//		MTGConstants.CONF_DIR = new File(System.getProperty("user.home") + "/.magicDeskCompanion-test/");
//		MTGConstants.DATA_DIR = new File(MTGConstants.CONF_DIR.getAbsolutePath(),"data");
		MTGLogger.changeLevel(Level.OFF);
		
		getEnabledPlugin(MTGCardsProvider.class).init();
	}
	
	
}

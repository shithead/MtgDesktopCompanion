package org.magic.api.generators.impl;

import static org.magic.tools.MTG.getEnabledPlugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.magic.api.beans.MagicCard;
import org.magic.api.beans.enums.CardsPatterns;
import org.magic.api.interfaces.MTGCardsIndexer;
import org.magic.api.interfaces.abstracts.AbstractMTGTextGenerator;
import org.magic.services.MTGConstants;
import org.magic.tools.FileTools;

import rita.RiMarkov;
import rita.RiTa;

public class MarkovGenerator extends AbstractMTGTextGenerator {

	private RiMarkov rs;
	private File cache;
	
	
	@Override
	public String generateText()
	{
		if(rs==null)
			init();
		
		return rs.generateSentence();
	}
	
	@Override
	public String[] suggestWords(String[] start)
	{
		if(rs==null)
			init();
		
		return rs.getCompletions(start);
	}

	public void init()
	{
		  rs = new RiMarkov(getInt("NGEN"),true,false);	
		  cache = getFile("CACHE_FILE");
		  
		  if(!cache.exists() || cache.length()==0)
		  {
			  logger.debug("Init MarkovGenerator");
			  StringBuilder build = new StringBuilder();
			  for(MagicCard mc : getEnabledPlugin(MTGCardsIndexer.class).listCards())
			  {
				  if((mc.getText()!=null || !mc.getText().isEmpty() || !mc.getText().equalsIgnoreCase("null"))) {
						  String r = mc.getText().replace(CardsPatterns.REMINDER.getPattern(), "")
								  				 .replace("\n", " ")
								  				 .replace(mc.getName(), getString("TAG_NAME"))
								  				 .trim();
						  build.append(r).append("\n");
				  }
			  }
			  
			try {
				saveCache(build.toString());
			} catch (IOException e) {
				logger.error("error saving file "+cache.getAbsolutePath(),e);
			}
			rs.loadText(build.toString());
		  }
		  else
		  {
			  try {
				logger.debug("loading cache from " + cache);
				rs.loadFrom(cache.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.error("error loading file "+cache.getAbsolutePath(),e);
			}
		  }
		  
		  
	}
	
	private void saveCache(String s) throws IOException
	{
		logger.debug("saving cache to " + cache);
		FileTools.saveFile(cache, s);		
	}

	@Override
	public String getName() {
		return "Markov";
	}
	
	
	@Override
	public void setProperty(String k, Object value) {
		super.setProperty(k, value);
		
		if(k.equals("NGEN")&& rs!=null)
			rs.N=getInt(k);
		
	}
	
	
	
	@Override
	public void initDefault() {
		setProperty("CACHE_FILE", new File(MTGConstants.DATA_DIR,"markov.gen").getAbsolutePath());
		setProperty("NGEN", "5");
		setProperty("TAG_NAME","CARD_NAME");
	}
	
	@Override
	public String getVersion() {
		return RiTa.VERSION;
	}
	
}

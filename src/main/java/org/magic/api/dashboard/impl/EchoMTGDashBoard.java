package org.magic.api.dashboard.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.magic.api.beans.CardDominance;
import org.magic.api.beans.CardShake;
import org.magic.api.beans.EditionsShakers;
import org.magic.api.beans.HistoryPrice;
import org.magic.api.beans.MTGFormat.FORMATS;
import org.magic.api.beans.MTGSealedProduct;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicEdition;
import org.magic.api.beans.enums.EnumCardVariation;
import org.magic.api.beans.enums.EnumMarketType;
import org.magic.api.interfaces.abstracts.AbstractDashBoard;
import org.magic.services.network.MTGHttpClient;
import org.magic.services.network.RequestBuilder;
import org.magic.services.network.RequestBuilder.METHOD;
import org.magic.services.network.URLTools;

public class EchoMTGDashBoard extends AbstractDashBoard {

	private static final String WEBSITE = "www.echomtg.com";
	private MTGHttpClient client;
	
	
	@Override
	public List<String> listAuthenticationAttributes() {
		return List.of("EMAIL","PASSWORD");
	}


	private String getCardId(MagicCard mc, boolean foil) {
		
		var extra="";
		
		if(mc.isBorderLess())
			extra=" (Borderless)";
		if(mc.isTimeshifted())
			extra=" (Retro Frame)";
		if(mc.isShowCase())
			extra=" (Showcase)";
		
		var arr = RequestBuilder.build().url("https://"+WEBSITE+"/user/")
				.method(METHOD.GET)
				.setClient(client)
				.url("https://"+WEBSITE+"/api/search/mass/")
				.addContent("search",mc.getName() + extra)
				.addContent("wcExpansion",mc.getCurrentSet().getSet())
				.addContent("limit","30")
				.addContent("textsearch","")
				.addContent("type","")
				.toJson().getAsJsonObject().get("data").getAsJsonArray();
		
		
		if(arr.isEmpty())
			return null;
		
		return arr.get(0).getAsJsonObject().get("emid").getAsString();
	}

	private void init() throws IOException {
		if(client==null)
		{
			client = URLTools.newClient();
			if(getAuthenticator().getLogin().isEmpty() && getAuthenticator().getPassword().isEmpty())
			{
				logger.error("Please fill account in config panel");
				return ;
			}
			
			
			var res = RequestBuilder.build().url("https://"+WEBSITE+"/user/")
											.method(METHOD.POST)
											.setClient(client)
											.addContent("email",getAuthenticator().get("EMAIL"))
											.addContent("password",getAuthenticator().get("PASSWORD")).toHtml();
			
				logger.info("logged as {}",res.select("title").text());
		}
		
	}

	@Override
	protected HistoryPrice<MagicCard> getOnlinePricesVariation(MagicCard mc, boolean foil) throws IOException {
		init();
		var history = new HistoryPrice<>(mc);
		
		history.setCurrency(getCurrency());
		history.setFoil(foil);

		
		var id = getCardId(mc,foil);
		
		if(id==null)
		{
			logger.warn("no id found for {}", mc);
			return history;
		}
		
		
		var arr = RequestBuilder.build().url("https://"+WEBSITE+"/cache/"+id+"."+(foil?"f":"r"+".json"))
													  .method(METHOD.GET)
													  .setClient(client)
													  .toJson()
													  .getAsJsonArray();
		for(var e : arr)
		{
			var d = e.getAsJsonArray();
			history.put(new Date(d.get(0).getAsLong()), d.get(1).getAsDouble());
		}
		return history;
	}

	
	@Override
	protected HistoryPrice<MagicEdition> getOnlinePricesVariation(MagicEdition ed) throws IOException {
		return null;
	}


	@Override
	protected EditionsShakers getOnlineShakesForEdition(MagicEdition ed) throws IOException {

		var variations = new EditionsShakers();
			variations.setDate(new Date());
			variations.setEdition(ed);
			variations.setProviderName(getName());
		
		
		init();

		var data = RequestBuilder.build().url("https://"+WEBSITE+"/api/data/set/")
				  .addContent("set_code",ed.getId())
				  .method(METHOD.GET)
				  .setClient(client)
				  .toJson()
				  .getAsJsonObject();
		
		
		data.get("set").getAsJsonObject().get("items").getAsJsonArray().forEach(je->{
			var obj = je.getAsJsonObject();
			
			if(!obj.get("main_type").getAsString().contains("Token"))
			{
				
				var cs = new CardShake();
				cs.setCurrency(getCurrency());
				cs.setDateUpdate(new Date());
				cs.setProviderName(getName());
				cs.setLink(obj.get("echo_set_url").getAsString());
				cs.setName(obj.get("name").getAsString());
				cs.setEd(ed.getId());
				
				if(cs.getName().contains("(Retro Frame)"))
				{
					cs.setCardVariation(EnumCardVariation.TIMESHIFTED);
					cs.setName(cs.getName().replace("(Retro Frame)", "").trim());
				}

			 
				if(cs.getName().contains("(Borderless)"))
				{
					cs.setCardVariation(EnumCardVariation.BORDERLESS);
					cs.setName(cs.getName().replace("(Borderless)", "").trim());
				}
				
				if(cs.getName().contains("(Extended Art)"))
				{
					cs.setCardVariation(EnumCardVariation.EXTENDEDART);
					cs.setName(cs.getName().replace("(Extended Art)", "").trim());
				}
				
				if(!obj.get("tcg_mid").isJsonNull())
					cs.setPrice(obj.get("tcg_mid").getAsDouble());
				
				if(!obj.get("price_change").isJsonNull())
					cs.setPercentDayChange(obj.get("price_change").getAsDouble()/100);
				
				cs.setPriceDayChange(cs.getPrice().doubleValue()*cs.getPercentDayChange());
		
				variations.addShake(cs);
			}
		});
		
		
		return variations;
	}


	@Override
	public String[] getDominanceFilters() {
		return new String[] { "magic-reserve-list", "lands", "creatures", "artifacts" };
	}


	@Override
	public List<CardDominance> getBestCards(FORMATS f, String filter) throws IOException {
		ArrayList<CardDominance> list = new ArrayList<>();

		return list;
	}

	@Override
	public Date getUpdatedDate() {
		return new Date();
	}

	@Override
	public String getName() {
		return "EchoMTG";
	}

	@Override
	protected List<CardShake> getOnlineShakerFor(FORMATS gameFormat) throws IOException {
		return  new ArrayList<>();
	}


	@Override
	public HistoryPrice<MTGSealedProduct> getOnlinePricesVariation(MTGSealedProduct packaging) throws IOException {
		return new HistoryPrice<>(packaging);
	}


	@Override
	public EnumMarketType getMarket() {
		return EnumMarketType.US_MARKET;
	}

}

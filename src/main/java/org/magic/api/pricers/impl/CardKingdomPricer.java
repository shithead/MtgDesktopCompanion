package org.magic.api.pricers.impl;

import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.filter;
import static com.jayway.jsonpath.JsonPath.parse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jsoup.select.Elements;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicPrice;
import org.magic.api.exports.impl.CardKingdomCardExport;
import org.magic.api.interfaces.abstracts.AbstractPricesProvider;
import org.magic.services.MTGConstants;
import org.magic.services.MTGControler;
import org.magic.services.network.URLTools;
import org.magic.services.tools.CardKingdomTools;
import org.magic.services.tools.Chrono;
import org.magic.services.tools.FileTools;
import org.magic.services.tools.UITools;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Filter;

public class CardKingdomPricer extends AbstractPricesProvider {

	private static final String API_URI="https://api.cardkingdom.com/api/pricelist";
	private static final String WEB_URI="https://www.cardkingdom.com";
	private File jsonFile;
	private DocumentContext cont;

	@Override
	public STATUT getStatut() {
		return STATUT.BETA;
	}

	private void init() throws IOException
	{
		var c = new Chrono();
		c.start();
		cont = parse(jsonFile);
		logger.debug("Init {} dataFile in {}s",jsonFile,c.stop());
	}


	private String getUrlFor(MagicCard mc,boolean foil) throws IOException
	{
		if(!jsonFile.exists()|| FileTools.daysBetween(jsonFile)>1) {
			logger.debug("{} is not present or out of date. Downloading new one",jsonFile);
			FileTools.saveFile(jsonFile, URLTools.extractAsJson(API_URI).toString());
		}

		if(cont==null)
			init();


        String name = CardKingdomTools.getCKFormattedName(mc);
        String ed = CardKingdomTools.getCKFormattedSet(mc);
		
		var filtres =where("name").is(name)
				.and("sku").contains(mc.getCurrentSet().getId().toUpperCase())
				.and("is_foil").is(String.valueOf(foil));

		if(mc.isShowCase())
			filtres=filtres.and("variation").is("Showcase");
		else if(mc.isBorderLess())
			filtres=filtres.and("variation").is("Borderless");
		else if (mc.isExtendedArt())
			filtres=filtres.and("variation").is("Extended Art");


		if(aliases.getSetNameFor(new CardKingdomCardExport() , mc.getCurrentSet()).contains("Mystery Booster"))
		{
			filtres = where("name").is(name)
					  .and("edition").is(aliases.getSetNameFor(new CardKingdomCardExport() , mc.getCurrentSet()))
					  .and("is_foil").is(String.valueOf(foil));
		}

		if(ed.contains("Duel Decks"))
		{
			ed = ed.replace("vs.", "Vs.");
		}

		if(mc.isToken())
		{
			name = name + " Token";
			//ed = EnumExtra.getSetNameFor(new CardKingdomCardExport() , mc.getCurrentSet());
			ed = ed.replace(" Tokens", "");
			ed = aliases.getSetNameFor(new CardKingdomCardExport() , ed);
			filtres = where("name").is(name)
					  .and("edition").is(ed)
					  .and("is_foil").is(String.valueOf(foil));
		}


		Filter cheapFictionFilter = filter(filtres);

		Chrono c = new Chrono();

		c.start();
		logger.debug("Reading file {} with {} ",jsonFile,cheapFictionFilter );
		List<Map<String, Object>> arr = cont.read("$.data[?]",cheapFictionFilter);
		var res = c.stop();
		logger.debug("Ending reading after {}sec",res);
		try {

			if(arr.size()>1)
			{
				logger.warn(" found multiples values for {} : {}", mc,arr);
			}
			return arr.get(0).get("url").toString();
		}
		catch(Exception e)
		{
			logger.error("No product found for {} foil={}",mc,foil) ;
		}
		return null;
	}


	public CardKingdomPricer() {
		jsonFile=new File(MTGConstants.DATA_DIR,"mtgkingdom.json");
	}

	@Override
	public List<MagicPrice> getLocalePrice(MagicCard card) throws IOException {

		var ret = getPrices(card,false);
		ret.addAll(getPrices(card, true));
		return ret;

	}



	public List<MagicPrice> getPrices(MagicCard card,boolean foil) throws IOException {

		List<MagicPrice> list = new ArrayList<>();
		var productUri =getUrlFor(card,foil);

		if(productUri==null)
			return list;


		String url = WEB_URI+ "/"+productUri;
		Elements prices = null;
		Elements qualities = null;

		logger.info("{} looking for prices {} foil={}",getName(),card,foil);
		try {
			var doc = URLTools.extractAsHtml(url);
			qualities = doc.select(".cardTypeList li");
			prices = doc.select(".stylePrice");

		} catch (Exception e) {
			logger.info("{} no item : {}",getName(),e.getMessage());
			return list;
		}

		List<MagicPrice> lstPrices = new ArrayList<>();
		for (var i = 0; i < qualities.size(); i++) {
			var mp = new MagicPrice();

			String price = prices.get(i).html();
			mp.setMagicCard(card);
			mp.setValue(UITools.parseDouble(price));
			mp.setCurrency("USD");
			mp.setCountry(Locale.US.getDisplayCountry(MTGControler.getInstance().getLocale()));
			mp.setSeller(getName());
			mp.setSite(getName());
			mp.setUrl(url+"?partner=Mtgdesktopcompanion&utm_source=Mtgdesktopcompanion&utm_medium=affiliate&utm_campaign=condition");
			mp.setSellerUrl(url+"?partner=Mtgdesktopcompanion&utm_source=Mtgdesktopcompanion&utm_medium=affiliate&utm_campaign=condition");
			mp.setQuality(qualities.get(i).html());
			mp.setLanguage("English");
			mp.setFoil(foil);
			if (!qualities.get(i).hasClass("disabled"))
				lstPrices.add(mp);
		}
		logger.info("{} found {} offers",getName(), lstPrices.size());
		return lstPrices;
	}

	@Override
	public String getName() {
		return "Card Kingdom";
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {

		if(obj ==null)
			return false;

		return hashCode()==obj.hashCode();
	}

	@Override
	public boolean isPartner() {
		return true;
	}

}

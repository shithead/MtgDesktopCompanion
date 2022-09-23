package org.magic.api.exports.impl;

import static org.magic.tools.MTG.getEnabledPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicCardStock;
import org.magic.api.beans.MagicDeck;
import org.magic.api.beans.enums.EnumCondition;
import org.magic.api.interfaces.MTGCardsProvider;
import org.magic.api.interfaces.abstracts.extra.AbstractFormattedFileCardExport;
import org.magic.services.MTGControler;
import org.magic.services.providers.PluginsAliasesProvider;
import org.magic.tools.FileTools;
import org.magic.tools.UITools;

public class UrzaGathererExport extends AbstractFormattedFileCardExport {

	private static final String COLUMNS="Name,Type,Color,Rarity,Author,Power,Toughness,Mana cost,Converted mana cost,Count,Foil count,Special foil count,Price,Foil price,Number,Set,ID,Multiverse ID,Comments,To trade,Condition,Grading,Languages,TCG ID,Cardmarket ID";

	@Override
	public STATUT getStatut() {
		return STATUT.DEV;
	}


	@Override
	public String getFileExtension() {
		return ".csv";
	}

	@Override
	public void exportDeck(MagicDeck deck, File dest) throws IOException {
		StringBuilder temp = new StringBuilder("\"sep=").append(getSeparator()).append("\"").append(System.lineSeparator());
		  			  temp.append(COLUMNS).append(",Deck count,Sideboard count,Maybeboard count").append(System.lineSeparator());

		writeDeckLine(temp,deck.getMain().entrySet(),1);
		writeDeckLine(temp,deck.getSideBoard().entrySet(),2);
		writeDeckLine(temp,deck.getMaybeBoard().entrySet(),3);

		FileTools.saveFile(dest, temp.toString());
	}

	@Override
	public MagicDeck importDeck(String content, String name) throws IOException {
		var d = new MagicDeck();
		d.setName(name);
		return d;
	}

	@Override
	public List<MagicCardStock> importStock(String content) throws IOException {
		List<MagicCardStock> list = new ArrayList<>();

		matches(content, true).forEach(m->{

		MagicCard mc=readCard(m);
		if(mc!=null)
		{

			int nbFoil = Integer.parseInt(m.group(11));
			int nbNormal = Integer.parseInt(m.group(10));
			int nbEtched = Integer.parseInt(m.group(12));

			var st = buildStockItem(mc,m,nbNormal,false);
			list.add(st);


			if(nbFoil>0)
				list.add(buildStockItem(mc,m,nbFoil,true));


			if(nbEtched>0)
			{
				var st3 = buildStockItem(mc,m,nbEtched,false);
				st3.setQte(nbEtched);
				list.add(st3);
			}
			notify(mc);
		}
		});

		return list;
	}

	private MagicCardStock buildStockItem(MagicCard mc , Matcher m,Integer qty,boolean foil)
	{

		var st = MTGControler.getInstance().getDefaultStock();
		st.setProduct(mc);
		st.setLanguage(m.group(23));
		st.setFoil(foil);
		st.setComment(m.group(19));
		if(foil)
			st.setPrice(UITools.parseDouble(m.group(14).trim()));
		else
			st.setPrice(UITools.parseDouble(m.group(13).trim()));
		st.setQte(qty);
		var strCondition = m.group(21);
		if(strCondition.indexOf("x")>-1)
			strCondition = strCondition.substring(strCondition.indexOf("x")+1);

		st.setCondition(PluginsAliasesProvider.inst().getReversedConditionFor(this, strCondition, EnumCondition.NEAR_MINT)  );


		return st;
	}


	private MagicCard readCard(Matcher m) {
		try {
			return getEnabledPlugin(MTGCardsProvider.class).searchCardByName(m.group(1),null,true).stream().filter(c->
				(!m.group(18).isEmpty()&&m.group(18).equals(c.getCurrentSet().getMultiverseid()))||(m.group(15).equals(c.getCurrentSet().getNumber()))
			).findFirst().orElse(null);
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}


	@Override
	public void exportStock(List<MagicCardStock> stock, File f) throws IOException {
		StringBuilder temp = new StringBuilder("\"sep=").append(getSeparator()).append("\"").append(System.lineSeparator());
					  temp.append(COLUMNS).append(System.lineSeparator());

					  for(var mcs : stock)
						{
							writeLine(temp,mcs);
							temp.append(System.lineSeparator());

						}

		FileTools.saveFile(f, temp.toString());


	}


	private void writeDeckLine(StringBuilder temp, Set<Entry<MagicCard, Integer>> set, int i )
	{

		set.forEach(entry->{
			var mcs= MTGControler.getInstance().getDefaultStock();
			mcs.setProduct(entry.getKey());
			mcs.setQte(entry.getValue());

			writeLine(temp, mcs);

			temp.append(getSeparator()).append((i==1)?entry.getValue():0)
			.append(getSeparator()).append((i==2)?entry.getValue():0)
			.append(getSeparator()).append((i==3)?entry.getValue():0)
			.append(System.lineSeparator());
		});


	}


	private void writeLine(StringBuilder temp,MagicCardStock mcs) {
		temp.append("\"").append(mcs.getProduct().getName()).append("\"").append(getSeparator());
		temp.append("\"").append(mcs.getProduct().getFullType()).append("\"").append(getSeparator());
		temp.append(parseColors(mcs.getProduct())).append(getSeparator());
		temp.append(mcs.getProduct().getRarity().toPrettyString()).append(getSeparator());
		temp.append("\"").append(mcs.getProduct().getArtist()).append("\"").append(getSeparator());
		temp.append(mcs.getProduct().getPower()).append(getSeparator());
		temp.append(mcs.getProduct().getToughness()).append(getSeparator());
		temp.append(mcs.getProduct().getCost()).append(getSeparator());
		temp.append(mcs.getProduct().getCmc()).append(getSeparator());

		temp.append(!mcs.isFoil()?mcs.getQte():0).append(getSeparator());
		temp.append(mcs.isFoil()?mcs.getQte():0).append(getSeparator());
		temp.append(mcs.isEtched()?mcs.getQte():0).append(getSeparator());

		temp.append("$").append(!mcs.isFoil()?UITools.formatDouble(mcs.getPrice()).replace(",", "."):0).append(getSeparator());
		temp.append("$").append(mcs.isFoil()?UITools.formatDouble(mcs.getPrice()).replace(",", "."):0).append(getSeparator());

		temp.append(mcs.getProduct().getCurrentSet().getNumber()).append(getSeparator());
		temp.append("\"").append(mcs.getProduct().getCurrentSet().getSet()).append("\"").append(getSeparator());
		temp.append("-1").append(getSeparator());
		temp.append(mcs.getProduct().getCurrentSet().getMultiverseid()).append(getSeparator());
		temp.append("\"").append(mcs.getComment()).append("\"").append(getSeparator());
		temp.append("0").append(getSeparator());
		temp.append("\"").append(mcs.getQte()).append("x").append(PluginsAliasesProvider.inst().getConditionFor(this, mcs.getCondition())).append("\"").append(getSeparator());
		temp.append("\"").append(mcs.getGrade()).append("\"").append(getSeparator());
		temp.append("\"").append(mcs.getLanguage()).append("\"").append(getSeparator());
		temp.append(mcs.getProduct().getTcgPlayerId()).append(getSeparator());
		temp.append(mcs.getProduct().getMkmId());
		notify(mcs.getProduct());
	}


	private String parseColors(MagicCard mc) {


		if(mc.isMultiColor())
			return "Multi-couleurs";

		if(mc.isColorless())
			return "Sans couleur";


		switch(mc.getColors().get(0))
		{
			case WHITE: return "Blanc";
			case BLUE: return "Bleu";
			case BLACK: return "Noir";
			case RED : return "Rouge";
			case GREEN: return "Vert";
			default : return "";
		}

	}




	@Override
	public String getName() {
	return "UrzaGatherer";
	}

	@Override
	protected boolean skipFirstLine() {
		return false;
	}

	@Override
	protected String[] skipLinesStartWith() {
		return new String[] {"\"sep=,","Name"};
	}

	@Override
	protected String getStringPattern() {
		return "\\\"(.*?)\\\",\\\"(.*?)\\\",(.*?),(.*?),\\\"(.*?)\\\",(\\d+),(\\d+),(.*?),(\\d+),(\\d+),(\\d+),(\\d+),(.*?),(.*?),(\\d+),\\\"(.*?)\\\",(\\d+),(\\d+),(.*?),(\\d+),\\\"(.*?)\\\",\\\"(.*?)\\\",\\\"(.*?)\\\",(\\d+),(\\d+)";
	}

	@Override
	protected String getSeparator() {
		return ",";
	}



}

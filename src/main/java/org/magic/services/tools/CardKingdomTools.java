package org.magic.services.tools;

import org.apache.logging.log4j.Logger;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.enums.EnumLayout;
import org.magic.api.beans.enums.EnumPromoType;
import org.magic.api.exports.impl.CardKingdomCardExport;
import org.magic.services.logging.MTGLogger;
import org.magic.services.providers.PluginsAliasesProvider;






/**
@author bewarellamas 
*/
public class CardKingdomTools {

	private CardKingdomTools()
	{
		
	}
	
	public static String getCKFormattedName(MagicCard card) {
		final Logger logger = MTGLogger.getLogger(CardKingdomTools.class);
		String name = card.getName();
			
		if(name.contains("//") && card.getLayout() != EnumLayout.SPLIT)
		{
			name = name.split(" //")[0];
		}
		
		
		if(card.isToken())
		{
			name = name + " Token";
		}
		
		//Check Special Characters
		name = name.replace("ú", "u");
		name = name.replace("â", "a");
		name = name.replace("á", "a");
		name = name.replace("ö", "o");
		name = name.replace("é", "e");
		name = name.replace("û", "u");
		
		
		//Check Promo Types
		if(card.isShowCase() && !card.getCurrentSet().getSet().contains("Strixhaven Mystical Archive"))
		{
			logger.debug("Showcase triggered: {}",name);
			if(card.getFrameVersion().contains("1997")) // maybe using isTimeShifted() ? --> AbstractCardsProvider.postTreatmentCard
			{
				name = name + " (Retro Frame)";
			}
			else {
				name = name + " (Showcase)";
			}
		}
		
		if(card.getPromotypes().contains(EnumPromoType.BUYABOX))
			name = name + " (Buy-a-Box Foil)";
		
		if(card.getPromotypes().contains(EnumPromoType.PRERELEASE))
			name = name + " (Prerelease Foil)";
		
		if(card.getPromotypes().contains(EnumPromoType.RELEASE))
			name = name + " (Launch Foil)";
		
		if(card.getPromotypes().contains(EnumPromoType.RELEASE))
			name = name + " (FNM Foil)";
		
		if(card.getPromotypes().contains(EnumPromoType.PLANESWALKERDECK))
			name = name + " (Planeswalker Deck)";
		
		if(card.getPromotypes().contains(EnumPromoType.STARTERDECK))
		{
			if(card.getCurrentSet().toString().contains("Welcome Deck 2016"))
				name = name + " (Welcome 2016)";
			
			if(card.getCurrentSet().toString().contains("Welcome Deck 2017"))
				name = name + " (Welcome 2017)";
			
		}
		
		
		
		
		
		//Specific set issues
		
		if(card.getCurrentSet().toString().contains("Duel Decks Anthology:"))
		{
			name = name + " (" + card.getCurrentSet().toString().split(": ")[1] + ")";
			name = name.replace("vs.", "vs");
		}
		
		
		//Specific cards
		if(card.getName().contains("Our Market Research"))
			name = "Our Market Research";
		
		if(card.getName().contains("Rumors of My Death"))
			name = "\"Rumors of My Death...\"";
		
		return name;
	}
	
	
	public static String getCKFormattedSet(MagicCard card) {
		
		String set = PluginsAliasesProvider.inst().getSetNameFor(new CardKingdomCardExport(), card.getCurrentSet());
		
		if(card.isToken())
		{
			set = set.replace(" Tokens", "");
			set = PluginsAliasesProvider.inst().getSetNameFor(new CardKingdomCardExport(), set);
		}
		
		if(card.isShowCase() && !set.contains("Strixhaven Mystical Archive"))
		{
			set = set + " Variants";
		}
		
		//not using PluginsAliasesProvider because of the multiple sets in this release
		if(set.contains("Duel Decks Anthology:"))
		{
			set = "Duel Decks: Anthology";
		}
		
		//promo sets
		
		if(card.getPromotypes().contains(EnumPromoType.FNM)||card.getPromotypes().contains(EnumPromoType.RELEASE)||
				card.getPromotypes().contains(EnumPromoType.PRERELEASE)||card.getPromotypes().contains(EnumPromoType.BUYABOX))
			set = "Promotional";

		if(card.getPromotypes().contains(EnumPromoType.STARTERDECK))
		{
			if(!set.contains("Eighth Edition"))
			{
				set = "Promotional";
			}
			
			
		}
			
				
		
		
		return set;
	}
	
	
	
	
	
	
	
}

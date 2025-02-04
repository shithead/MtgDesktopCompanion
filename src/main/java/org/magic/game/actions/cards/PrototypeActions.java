package org.magic.game.actions.cards;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import org.magic.game.actions.abbstract.AbstractCardAction;
import org.magic.game.gui.components.DisplayableCard;
import org.magic.game.gui.components.GamePanelGUI;
import org.magic.game.model.ZoneEnum;

public class PrototypeActions extends AbstractCardAction {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final String K = "Prototype";
	
	public PrototypeActions(DisplayableCard card) {
		super(card,K);
		putValue(SHORT_DESCRIPTION, "Cast creature as Prototype");
		putValue(MNEMONIC_KEY, KeyEvent.VK_P);
		parse(card.getMagicCard().getText());
	}

	private void parse(String text) {
		var cost="";
		try {
			String regex = "/*" + K + " \\{(.*?)\\ ";
			var p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
			var m = p.matcher(text);
			
			if (m.find())
				cost = m.group().replaceAll(K, "").trim();
			else
				cost = text.substring(text.indexOf(K + "\u2014") + K.length(), text.indexOf('('));
			
		} catch (Exception e) {
			logger.error(e);
			cost = "";
		}
		
		card.getMagicCard().setCost(cost);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		GamePanelGUI.getInstance().getHandPanel().remove(card);
		GamePanelGUI.getInstance().getPanelBattleField().add(card);
		
		GamePanelGUI.getInstance().getPanelBattleField().revalidate();
		GamePanelGUI.getInstance().getPanelBattleField().repaint();

	}

	@Override
	public ZoneEnum playableFrom() {
			return ZoneEnum.HAND;
	}

}

package hopshackle.dominion;

import java.util.List;

import hopshackle.simulation.*;

public class CardTypeList implements ActionEnum<Player> {
	
	public List<CardType> cards;

	public CardTypeList(List<CardType> purc) {
		cards = purc;
	}

	@Override
	public boolean isChooseable(Player a) {return true;}

	@Override
	public Enum<CardType> getEnum() {return null;}

	@Override
	public String getChromosomeDesc() {return "NONE";}

	@Override
	public Action<Player> getAction(Player a1, Agent a2) {return null;}

	@Override
	public DominionBuyAction getAction(Player a) {return new DominionBuyAction((Player)a, cards);}
	
	@Override
	public String toString() {
		StringBuffer retValue = new StringBuffer();
		for (CardType c : cards) {
			retValue.append(c.toString() + " ");
		}
		return retValue.toString();
	}
}


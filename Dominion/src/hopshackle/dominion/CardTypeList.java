package hopshackle.dominion;

import java.util.List;

import hopshackle.simulation.*;

public class CardTypeList implements ActionEnum {
	
	public List<CardType> cards;

	public CardTypeList(List<CardType> purc) {
		cards = purc;
	}

	@Override
	public boolean isChooseable(Agent a) {return true;}

	@Override
	public Enum getEnum() {return null;}

	@Override
	public String getChromosomeDesc() {return "NONE";}

	@Override
	public Action getAction(Agent a1, Agent a2) {return null;}

	@Override
	public Action getAction(Agent a) {return new DominionAction((Player)a, cards);}
	
	@Override
	public String toString() {
		StringBuffer retValue = new StringBuffer();
		for (CardType c : cards) {
			retValue.append(c.toString() + " ");
		}
		return retValue.toString();
	}
}


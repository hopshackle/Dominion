package hopshackle.dominion;

import java.util.*;

import hopshackle.simulation.*;

public class DominionAction extends Action {
	
	private List<CardType> cardType;

	public DominionAction(Agent a, CardType cardType) {
		super(a, false);
		this.cardType = new ArrayList<CardType>();
		this.cardType.add(cardType);
	}

	public DominionAction(Player player, List<CardType> purc) {
		super(player, false);
		this.cardType = purc;
	}

	public String toString() {
		StringBuffer retValue = new StringBuffer();
		for (CardType c : cardType) {
			retValue.append(c.toString() + " ");
		}
		return retValue.toString();
	}
}

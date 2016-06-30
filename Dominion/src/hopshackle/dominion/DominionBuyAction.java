package hopshackle.dominion;

import java.util.*;

import hopshackle.simulation.*;

public class DominionBuyAction extends Action<Player> {
	
	private List<CardType> cardType;

	public DominionBuyAction(Player a, CardType cardType) {
		super(cardType, a, false);
		this.cardType = new ArrayList<CardType>();
		this.cardType.add(cardType);
	}

	public DominionBuyAction(Player player, List<CardType> purc) {
		super(purc.get(0), player, false);
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

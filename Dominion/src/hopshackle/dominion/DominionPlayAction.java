package hopshackle.dominion;

import hopshackle.simulation.*;

public class DominionPlayAction extends Action<Player> {
	
	private CardType cardType;

	public DominionPlayAction(Player a, CardType cardType) {
		super(cardType, a, false);
		this.cardType = cardType;
	}

	public String toString() {
		return cardType.toString();
	}
	
	
}

package hopshackle.dominion;

import java.util.*;

import hopshackle.simulation.*;

public class DominionBuyAction extends Action<Player> {
	
	private List<CardType> cardType;
	private Player player;

	public DominionBuyAction(Player p, CardType cardType) {
		super(cardType, p, false);
		this.cardType = new ArrayList<CardType>();
		this.cardType.add(cardType);
		player = p;
	}

	public DominionBuyAction(Player player, List<CardType> purc) {
		super(purc.get(0), player, false);
		this.cardType = purc;
		this.player = player;
	}

	public String toString() {
		StringBuffer retValue = new StringBuffer();
		for (CardType c : cardType) {
			retValue.append(c.toString() + " ");
		}
		return retValue.toString();
	}
	
	@Override
	protected void doStuff() {
		player.log("Has " + player.getBudget() + " money and buys " + toString()); 
		for (CardType cardBought : cardType) {
			player.takeCardFromSupplyIntoDiscard(cardBought);
		}
	}
	@Override 
	protected void doNextDecision(Player p) {
		// Do nothing .. this is all handled in Game/Player
		// Note that we do not override doNextDecision(), so that learning event
		// is still dispatched
	}
}

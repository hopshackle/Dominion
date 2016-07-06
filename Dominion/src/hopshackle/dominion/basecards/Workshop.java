package hopshackle.dominion.basecards;

import hopshackle.dominion.Card;
import hopshackle.dominion.CardType;
import hopshackle.dominion.Player;
import hopshackle.dominion.PositionSummary;
import hopshackle.simulation.LookaheadDecider;

public class Workshop extends Card {

	public Workshop() {
		super(CardType.WORKSHOP);
	}

	public void takeAction(Player player) {
		super.takeAction(player);
		LookaheadDecider<Player, PositionSummary> purchaseDecider = player.getPurchaseDecider();
		player.setStateToPurchase();
		CardType cardPurchased = purchaseDecider.buyingDecision(player, 4, 1).get(0);
		player.setStateToAction();
		player.takeCardFromSupplyIntoDiscard(cardPurchased);
		player.log("Gains " + cardPurchased);
	}
}

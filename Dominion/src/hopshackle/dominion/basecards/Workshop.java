package hopshackle.dominion.basecards;

import hopshackle.dominion.*;

public class Workshop extends Card {

	public Workshop() {
		super(CardType.WORKSHOP);
	}

	public void takeAction(Player player) {
		super.takeAction(player);
		player.setState(Player.State.PURCHASING);
		DominionBuyingDecision nextBuy = new DominionBuyingDecision(player, 4, 1);
		CardType cardPurchased = nextBuy.getBestPurchase().get(0);
		player.setState(Player.State.PLAYING);
		player.takeCardFromSupplyIntoDiscard(cardPurchased);
		player.log("Gains " + cardPurchased);
	}
}

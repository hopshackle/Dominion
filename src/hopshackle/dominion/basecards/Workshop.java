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
		DominionAction action = nextBuy.getBestPurchase();
		player.log("Workshop : " + action);
		action.start();
		action.run();
		player.setState(Player.State.PLAYING);
	}
}

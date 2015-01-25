package hopshackle.dominion;

import java.util.List;

public class Feast extends Card {

	public Feast() {
		super(CardType.FEAST);
	}

	@Override
	public void takeAction(Player player) {
		player.trashCardFromRevealed(CardType.FEAST);
		player.setStateToPurchase();
		CardType gainedCard = (new DominionBuyingDecision(player, 5, 1)).getBestMandatoryPurchase().get(0);
		player.takeCardFromSupplyIntoDiscard(gainedCard);
		player.setStateToAction();
		player.log("Gains " + gainedCard);
	}
}

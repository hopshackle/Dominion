package hopshackle.dominion.basecards;

import hopshackle.dominion.*;

public class Feast extends Card {

	public Feast() {
		super(CardType.FEAST);
	}

	@Override
	public void takeAction(Player player) {
		player.trashCardFromRevealed(CardType.FEAST);
		player.setState(Player.State.PURCHASING);
		CardType gainedCard = (new DominionBuyingDecision(player, 5, 1)).getBestMandatoryPurchase().get(0);
		player.takeCardFromSupplyIntoDiscard(gainedCard);
		player.setState(Player.State.PLAYING);
		player.log("Gains " + gainedCard);
	}
}

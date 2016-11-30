package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.*;

public class Feast extends Card {

	public Feast() {
		super(CardType.FEAST);
	}

	@Override
	public void takeAction(Player player) {
		player.trashCard(CardType.FEAST, CardSink.REVEALED);
		player.setState(Player.State.PURCHASING);
		DominionAction action = (new DominionBuyingDecision(player, 5, 1)).getBestMandatoryPurchase();
		action.start();
		action.run();
		player.setState(Player.State.PLAYING);
	}
}

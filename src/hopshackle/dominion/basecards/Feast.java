package hopshackle.dominion.basecards;

import java.util.List;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.*;
import hopshackle.simulation.Action;
import hopshackle.simulation.ActionEnum;

public class Feast extends Card {

	public Feast() {
		super(CardType.FEAST);
	}

	@Override
	public List<ActionEnum<Player>> takeAction(Player player) {
		player.trashCard(CardType.FEAST, CardSink.REVEALED);
		player.setState(Player.State.PURCHASING);
		Action<Player> action = (new DominionBuyingDecision(player, 5, 1)).getBestMandatoryPurchase();
		action.start();
		action.run();
		player.setState(Player.State.PLAYING);
		return emptyList;
	}
}

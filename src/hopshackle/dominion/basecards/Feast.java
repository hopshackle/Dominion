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
		super.takeAction(player);
		player.moveCard(CardType.FEAST, CardSink.REVEALED, CardSink.TRASH);

		DominionBuyingDecision nextBuy = new DominionBuyingDecision(player, 5, 1);
		return nextBuy.getPossibleGainsAsActionEnum();
	}
}

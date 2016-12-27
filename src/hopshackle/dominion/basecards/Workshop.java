package hopshackle.dominion.basecards;

import java.util.List;

import hopshackle.dominion.*;
import hopshackle.simulation.ActionEnum;

public class Workshop extends Card {
	public Workshop() {
		super(CardType.WORKSHOP);
	}

	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		DominionBuyingDecision nextBuy = new DominionBuyingDecision(player, 4, 1);
		return nextBuy.getPossiblePurchasesAsActionEnum();
	}
}

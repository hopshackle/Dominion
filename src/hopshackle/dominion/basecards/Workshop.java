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
		player.setState(Player.State.PURCHASING);
		DominionBuyingDecision nextBuy = new DominionBuyingDecision(player, 4, 1);
		DominionAction action = nextBuy.getBestPurchase();
		player.log("Workshop : " + action);
		action.start();
		action.run();
		player.setState(Player.State.PLAYING);
		return emptyList;
	}

}

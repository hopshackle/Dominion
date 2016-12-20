package hopshackle.dominion.basecards;

import java.util.List;

import hopshackle.dominion.Card;
import hopshackle.dominion.CardType;
import hopshackle.dominion.Player;
import hopshackle.simulation.ActionEnum;

public class CouncilRoom extends Card {
	
	public CouncilRoom() {
		super(CardType.COUNCIL_ROOM);
	}
	
	@Override
	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		List<Player> otherPlayers = player.getGame().getAllPlayers();
		for (Player other : otherPlayers) {
			if (other == player) continue;
			other.log("Draws Card due to COUNCIL_ROOM");
			other.drawTopCardFromDeckIntoHand();
		}
		return emptyList;
	}

}

package hopshackle.dominion.basecards;

import hopshackle.dominion.Card;
import hopshackle.dominion.CardType;
import hopshackle.dominion.Player;

public class CouncilRoom extends Card {
	
	public CouncilRoom() {
		super(CardType.COUNCIL_ROOM);
	}
	
	@Override
	public void takeAction(Player player) {
		super.takeAction(player);
		Player[] otherPlayers = player.getGame().getPlayers();
		for (Player other : otherPlayers) {
			if (other == player) continue;
			other.log("Draws Card due to COUNCIL_ROOM");
			other.drawTopCardFromDeckIntoHand();
		}
	}

}

package hopshackle.dominion.basecards;

import hopshackle.dominion.Card;
import hopshackle.dominion.CardType;
import hopshackle.dominion.Player;

import java.util.List;

public class Cellar extends Card{

	public Cellar() {
		super(CardType.CELLAR);
	}

	// Initially just discard all victory cards
	// TODO: also discard any action cards if no actions left
	public void takeAction(Player player) {
		super.takeAction(player);
		List<CardType> hand = player.getCopyOfHand();
		int cardsToDraw = 0;
		for (CardType ct : hand) {
			if (ct.isVictory() || ct.equals(CardType.CELLAR)) {
				player.discard(ct);
				cardsToDraw++;
			}
		}
		for (int loop=0; loop<cardsToDraw; loop++) {
			player.drawTopCardFromDeckIntoHand();
		}
	}
}

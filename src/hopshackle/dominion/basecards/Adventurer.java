package hopshackle.dominion.basecards;

import hopshackle.dominion.Card;
import hopshackle.dominion.CardType;
import hopshackle.dominion.Player;

import java.util.*;

public class Adventurer extends Card {

	public Adventurer() {
		super(CardType.ADVENTURER);
	}

	@Override
	public void takeAction(Player player) {
		int treasureCardsDrawn = 0;
		boolean exhaustedDeckAndDiscard = false;
		List<Card> revealedCards = new ArrayList<Card>();
		do {
			Card drawnCard = player.drawTopCardFromDeckButNotIntoHand();
			if (drawnCard.isTreasure()) {
				treasureCardsDrawn++;
				player.insertCardDirectlyIntoHand(drawnCard);
				player.log("Takes " + drawnCard.toString() + " into hand");
			} else {
				revealedCards.add(drawnCard);
				player.insertCardDirectlyIntoHand(drawnCard);	// to be discarded in a second
			}
			exhaustedDeckAndDiscard = (player.getDeckSize() == 0 && player.getDiscardSize() == 0);
		} while (!exhaustedDeckAndDiscard && treasureCardsDrawn < 2);

		for (Card card : revealedCards)
			player.discard(card.getType());
	}
}

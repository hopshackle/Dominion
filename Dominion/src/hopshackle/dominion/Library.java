package hopshackle.dominion;

import java.util.*;

public class Library extends Card {

	public Library() {
		super(CardType.LIBRARY);
	}

	public void takeAction(Player player) {
		super.takeAction(player);
		int numberOfActionsOutstanding = player.getActionsLeft() - 1;	// as this otherwise includes the Library action itself
		List<Card> setAsideCards = new ArrayList<Card>();
		if (player.getHandSize() > 6) return;
		do {
			Card nextCard = player.drawTopCardFromDeckButNotIntoHand();
			if (nextCard.getType() == CardType.NONE) {
				player.log("Has no more cards in deck or discard. So stops.");
				break;
			}
			if (nextCard.isAction() && numberOfActionsOutstanding > 0) {
				player.insertCardDirectlyIntoHand(nextCard);
				numberOfActionsOutstanding--;
				numberOfActionsOutstanding += nextCard.getType().getAdditionalActions();
			} else if (nextCard.isAction()) {
				setAsideCards.add(nextCard);
			} else {
				player.insertCardDirectlyIntoHand(nextCard);
			}
		} while (player.getHandSize() < 7);
		for (Card c : setAsideCards)
			player.putCardOnDiscard(c);
	}

}

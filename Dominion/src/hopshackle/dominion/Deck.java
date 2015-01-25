package hopshackle.dominion;

import java.util.*;

public class Deck {

	private LinkedList<Card> cards;

	public Deck() {
		cards = new LinkedList<Card>();
	}
	protected Deck(LinkedList<Card> cards) {
		this.cards = cards;
	}

	public Card drawTopCard() {
		return cards.remove();
	}

	public boolean isEmpty() {
		if (cards.size() == 0) return true;
		return false;
	}

	public int getSize() {
		return cards.size();
	}

	public void addCard(Card card) {
		if (card != null && card.getType() != CardType.NONE)
			cards.addFirst(card);
	}

	public void shuffle() {
		if (isEmpty() || cards.size() == 1) return;
		LinkedList<Card> shuffledDeck = new LinkedList<Card>();
		do {
			int randomIndex = (int)(cards.size() * Math.random());
			Card randomCard = cards.remove(randomIndex);
			shuffledDeck.add(randomCard);
		} while (cards.size() > 0);
		cards = shuffledDeck;
	}

	public int getNumberOfType(CardType type) {
		int counter = 0;
		for (Card c : cards) {
			if (c.getType() == type) 
				counter++;
		}
		return counter;
	}

	public void addDeck(Deck hand) {
		cards.addAll(hand.cards);
	}

	public int getVictoryPoints(PositionSummary ps) {
		int total = 0;
		for (Card c : cards) {
			total += c.getVictory(ps);
		}
		return total;
	}

	public int getTreasureValue() {
		int total = 0;
		for (Card c : cards) {
			total += c.getTreasure();
		}
		return total;
	}

	public List<CardType> getAllCards() {
		List<CardType> retValue = new ArrayList<CardType>();
		for (Card c : cards) {
			retValue.add(c.getType());
		}
		return retValue;
	}

	public Card removeSpecificCard(CardType cardTypeToRemove) {
		Card cardToRemove = new Card(CardType.NONE);
		for (Card c : cards) {
			if (c.getType() == cardTypeToRemove) {
				cardToRemove = c;
				break;
			}
		}
		if (cardToRemove.getType() != CardType.NONE) 
			cards.remove(cardToRemove);
		return cardToRemove;
	}

	public int getAdditionalPurchasePower() {
		int retValue = 0;
		for (Card c : cards) 
			retValue += c.getAdditionalPurchasePower();
		return retValue;
	}

	public int getAdditionalBuys() {
		int retValue = 0;
		for (Card c : cards) 
			retValue += c.getAdditionalBuys();
		return retValue;
	}
	public void reset() {
		for (Card c: cards) {
			c.reset();
		}
	}
}

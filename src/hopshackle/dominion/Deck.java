package hopshackle.dominion;

import hopshackle.simulation.HopshackleUtilities;

import java.util.*;

public class Deck {

	private LinkedList<Card> cards;

	public Deck() {
		cards = new LinkedList<Card>();
	}
	private Deck(Deck toClone, boolean keepOrder, DominionGame newGame) {
		cards = new LinkedList<Card>();
		for (Card c : toClone.cards) {
			cards.add(c.clone(newGame));
		}
		if (!keepOrder)
			this.shuffle();
	}

	public Deck copy(DominionGame newGame) {
		return new Deck(this, true, newGame);
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
		if (type == CardType.NONE) return 1;
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
	
	public boolean removeSpecificCard(Card cardToRemove) {
		return cards.remove(cardToRemove);
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
	public Card getTopCard() {
		return cards.getFirst();
	}

	public List<List<CardType>> getPossibleDiscards(int min, int max) {
		Map<CardType, Integer> allCardTypesInDeck = new HashMap<CardType, Integer>();
		for (Card c : cards) {
			CardType ct = c.getType();
			if (allCardTypesInDeck.containsKey(ct)) {
				allCardTypesInDeck.put(ct, allCardTypesInDeck.get(ct) + 1);
			} else {
				allCardTypesInDeck.put(ct, 1);
			}
		}

		List<List<CardType>> retValue = new ArrayList<List<CardType>>();

		for (CardType ct : allCardTypesInDeck.keySet()) {
			List<List<CardType>> newDiscardStems = new ArrayList<List<CardType>>();
			for (int i = 1; i <= allCardTypesInDeck.get(ct) && i <= max; i++) {
				for (List<CardType> discardStem : retValue) {
					if (discardStem.size() + i <= max) {
						List<CardType> newDiscardStem = HopshackleUtilities.cloneList(discardStem);
						for (int j = 0; j < i; j++) {
							newDiscardStem.add(ct);
						}
						newDiscardStems.add(newDiscardStem);
					}
				}
				List<CardType> newDiscardStem = new ArrayList<CardType>();
				for (int j = 0; j < i; j++) {
					newDiscardStem.add(ct);
				}
				newDiscardStems.add(newDiscardStem);
			}
			retValue.addAll(newDiscardStems);
		}
		if (min > 0) {
			List<List<CardType>> optionsBelowMinimum = new ArrayList<List<CardType>>();
			for (List<CardType> option : retValue) {
				if (option.size() < min)
					optionsBelowMinimum.add(option);
			}
			retValue.removeAll(optionsBelowMinimum);
		}
		if (min == 0) {
			List<CardType> noAction = new ArrayList<CardType>();
			retValue.add(noAction);
		}
		return retValue;
	}
}

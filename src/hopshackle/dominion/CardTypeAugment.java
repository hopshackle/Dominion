package hopshackle.dominion;

import hopshackle.simulation.*;

public class CardTypeAugment implements ActionEnum<Player> {

	private static final long serialVersionUID = 1L;

	public enum CardSink {
		HAND, DISCARD, DECK, REVEALED, SUPPLY, TRASH;
	}
	public enum ChangeType {
		MOVE, PLAY;
	}
	
	public CardType card;
	public CardSink from, to;
	public ChangeType type;
	
	public static CardTypeAugment playCard(CardType card) {
		return new CardTypeAugment(card, CardSink.HAND, CardSink.REVEALED, ChangeType.PLAY);
	}
	public static CardTypeAugment takeCard(CardType card) {
		return new CardTypeAugment(card, CardSink.SUPPLY, CardSink.DISCARD, ChangeType.MOVE);
	}
	public static CardTypeAugment moveCard(CardType card, CardSink from, CardSink to) {
		return new CardTypeAugment(card, from, to, ChangeType.MOVE);
	}
	public static CardTypeAugment trashCard(CardType card, CardSink from) {
		return new CardTypeAugment(card, from, CardSink.TRASH, ChangeType.MOVE);
	}
	public static CardTypeAugment discardCard(CardType card) {
		return new CardTypeAugment(card, CardSink.HAND, CardSink.DISCARD, ChangeType.MOVE);
	}

	public CardTypeAugment(CardType card, CardSink from, CardSink to, ChangeType type) {
		this.card = card;
		this.from = from;
		this.to = to;
		this.type = type;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof CardTypeAugment) {
			CardTypeAugment otherCTA = (CardTypeAugment) other;
			if (otherCTA.card == this.card && otherCTA.from == this.from && otherCTA.to == this.to && otherCTA.type == this.type)
				return true;
		} 
		return false;
	}

	@Override
	public String toString() {
		if (type == ChangeType.PLAY) {
			return "Plays " + card.toString();
		} else {
			if (from == CardSink.HAND && to == CardSink.DISCARD)
				return "Discards " + card.toString();
			if (from == CardSink.SUPPLY && to == CardSink.DISCARD)
				return "Gains " + card.toString();
			return "Moves " + card.toString() + " from " + from.toString() + " to " + to.toString();
		}
	}
	
	@Override
	public boolean isChooseable(Player p) {
		if (p.isTakingActions()) {
			// distinguish between chooseability at purchase and use
			if (p.getNumberOfTypeInHand(this.card) > 0 && this.type == ChangeType.PLAY)
				return true;
			else
				return false;
		} else {
			if (this.type == ChangeType.MOVE && this.from == CardSink.SUPPLY && p.getGame().getNumberOfCardsRemaining(this.card) == 0)
				return false;
			if (this.type == ChangeType.MOVE && this.from != CardSink.SUPPLY && p.getNumberOfTypeInDeck(this.card) == 0)
				return false;
			return true;
		}
	}

	@Override
	public Action<Player> getAction(Player p) {
		return new DominionAction(p, this);
	}
	
	@Override
	public String getChromosomeDesc() {return "DOM1";}

	@Override
	public Enum<CardType> getEnum() {
		return this.card;
	}
}



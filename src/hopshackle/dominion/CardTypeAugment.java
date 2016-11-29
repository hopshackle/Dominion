package hopshackle.dominion;

import hopshackle.simulation.*;

public class CardTypeAugment implements ActionEnum<Player> {

	private static final long serialVersionUID = 1L;

	public enum CardSink {
		HAND, DISCARD, DECK, REVEALED;
	}
	public enum ChangeType {
		GAIN, LOSS, PLAY;
		@Override
		public String toString() {
			switch (this) {
			case GAIN:
				return "+";
			case LOSS:
				return "-";
			case PLAY:
				return "Play from ";
			}
			return "X";
		}
	}

	public CardTypeAugment(CardType card, CardSink dest, ChangeType gainOrLoss) {
		this.card = card;
		this.dest = dest;
		this.type = gainOrLoss;
	}
	public CardType card;
	public CardSink dest;
	public ChangeType type;
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof CardTypeAugment) {
			CardTypeAugment otherCTA = (CardTypeAugment) other;
			if (otherCTA.card == this.card && otherCTA.dest == this.dest && otherCTA.type == this.type)
				return true;
		} 
		return false;
	}

	@Override
	public String toString() {
		String retValue = card.toString();
		if (type != ChangeType.GAIN || dest != CardSink.DISCARD) {
			retValue = retValue + "(" + type.toString() + dest.toString() + ")";
		}
		return retValue;
	}
	
	@Override
	public boolean isChooseable(Player p) {
		if (this.card == CardType.NONE) return true;
		if (p.isTakingActions()) {
			// distinguish between chooseability at purchase and use
			if (p.getNumberOfTypeInHand(this.card) > 0 && this.type == ChangeType.PLAY)
				return true;
			else
				return false;
		} else {
			if (this.type == ChangeType.GAIN && p.getGame().getNumberOfCardsRemaining(this.card) == 0)
				return false;
			if (this.type == ChangeType.LOSS && p.getNumberOfTypeInDeck(this.card) == 0)
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



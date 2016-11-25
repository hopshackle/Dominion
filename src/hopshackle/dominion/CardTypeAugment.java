package hopshackle.dominion;

public class CardTypeAugment {

	public CardTypeAugment(CardType card, CardSink dest, ChangeType gainOrLoss) {
		this.card = card;
		this.dest = dest;
		this.type = gainOrLoss;
	}
	public CardType card;
	public CardSink dest;
	public ChangeType type;

	@Override
	public String toString() {
		String retValue = card.toString();
		if (type != ChangeType.GAIN || dest != CardSink.DISCARD) {
			retValue = retValue + "(" + type.toString() + dest.toString() + ")";
		}
		return retValue;
	}
	
	public enum CardSink {
		HAND, DISCARD, DECK, REVEALED;
	}
	public enum ChangeType {
		GAIN, LOSS;
		@Override
		public String toString() {
			if (this == GAIN)
				return "+";
			return "-";
		}
	}
}



package hopshackle.dominion;

import java.util.*;

import hopshackle.dominion.CardTypeAugment.*;
import hopshackle.simulation.*;

public class CardTypeList implements ActionEnum<Player> {

	private static final long serialVersionUID = 1L;
	public List<CardTypeAugment> cards;

	/* 
	 * This base constructor assumes that the list of cards are all to be purchased
	 * and put into the player's discard pile
	 */
	public CardTypeList(List<CardType> purc) {
		cards = new ArrayList<CardTypeAugment>(purc.size());
		for (CardType p : purc) {
			cards.add(new CardTypeAugment(p, CardSink.DISCARD, ChangeType.GAIN));
		}
	}

	/*
	 * flag is simply to provide a different signature to get around java erasure
	 * (otherwise there is no difference between this constructor and the one above)
	 */
	public CardTypeList(List<CardTypeAugment> augments, boolean flag) {
		cards = augments;
	}

	public List<CardType> getCards() {
		List<CardType> retValue = new ArrayList<CardType>(cards.size());
		for (CardTypeAugment cta : cards) {
			retValue.add(cta.card);
		}
		return retValue;
	}

	@Override
	public boolean isChooseable(Player a) {return true;}

	@Override
	public Enum<CardType> getEnum() {return null;}

	@Override
	public String getChromosomeDesc() {return "NONE";}

	@Override
	public boolean equals(Object other) {
		if (other instanceof CardTypeList) {
			CardTypeList otherCTL = (CardTypeList) other;
			List<CardTypeAugment> inBoth = HopshackleUtilities.cloneList(otherCTL.cards);
			for (CardTypeAugment ct : cards) {
				if (inBoth.contains(ct)) {
					inBoth.remove(ct);
				} else {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public DominionAction getAction(Player a) {
		return new DominionAction((Player)a, this);
	}

	@Override
	public String toString() {
		StringBuffer retValue = new StringBuffer();
		for (CardTypeAugment c : cards) {
			retValue.append(c.toString() + " ");
		}
		return retValue.toString();
	}
}


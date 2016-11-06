package hopshackle.dominion;

import java.util.List;

import hopshackle.simulation.*;

public class CardTypeList implements ActionEnum<Player> {

	private static final long serialVersionUID = 1L;
	public List<CardType> cards;

	public CardTypeList(List<CardType> purc) {
		cards = purc;
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
			List<CardType> inBoth = HopshackleUtilities.cloneList(otherCTL.cards);
			for (CardType ct : cards) {
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
	public DominionBuyAction getAction(Player a) {return new DominionBuyAction((Player)a, cards);}
	
	@Override
	public String toString() {
		StringBuffer retValue = new StringBuffer();
		for (CardType c : cards) {
			retValue.append(c.toString() + " ");
		}
		return retValue.toString();
	}
}


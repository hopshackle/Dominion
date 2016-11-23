package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
import hopshackle.simulation.*;

public class Thief extends AttackCard {

	public Thief() {
		super(CardType.THIEF);
	}

	@Override
	public void executeAttackOnPlayer(Player target, Player attacker) {
		target.log("Is target of THIEF");
		Card[] topTwoCards = discardTopTwoCards(target);
		Card cardToTrash = getHighestTreasureCard(topTwoCards);
		if (cardToTrash.getType() == CardType.NONE)
			return;
		target.trashCardFromDiscard(cardToTrash.getType());
		LookaheadDecider<Player> decider = attacker.getLookaheadDecider();
		PositionSummary ps = (PositionSummary) decider.getCurrentState(attacker);
		double baseValue = decider.value(ps);
		ps.addCard(cardToTrash.getType());
		double newValue = decider.value(ps);
		if (newValue > baseValue) {
			attacker.putCardOnDiscard(cardToTrash);
			attacker.log("Keeps " + cardToTrash);
			target.log(cardToTrash + " is taken by THIEF");
		} else {
			target.log("THIEF Trashes " + cardToTrash);
		}
	}

	private Card[] discardTopTwoCards(Player target) {
		Card[] retValue = new Card[2];
		retValue[0] = target.drawTopCardFromDeckButNotIntoHand();
		target.putCardOnDiscard(retValue[0]);
		retValue[1] = target.drawTopCardFromDeckButNotIntoHand();
		target.putCardOnDiscard(retValue[1]);
		return retValue;
	}
	
	private Card getHighestTreasureCard(Card[] topTwoCards) {
		Card retValue = CardFactory.instantiateCard(CardType.NONE);
		int highestValue = 0;
		for (Card c : topTwoCards) {
			if (!c.isTreasure()) continue;
			if (c.getTreasure() > highestValue) {
				highestValue = c.getTreasure();
				retValue = c;
			}
		}
		return retValue;
	}

}
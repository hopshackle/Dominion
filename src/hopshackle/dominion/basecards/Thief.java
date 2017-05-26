package hopshackle.dominion.basecards;

import java.util.*;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.simulation.*;

public class Thief extends AttackCard {

	public Thief() {
		super(CardType.THIEF);
	}

	@Override
	public List<ActionEnum<Player>>  executeAttackOnPlayer(Player target) {
		target.log("Is target of THIEF");
		Card[] topTwoCards = discardTopTwoCards(target);

		// we always trash the highest treasure card for the moment.
		// The decision is then whether to take it into hand personally
		Card cardToTrash = getHighestTreasureCard(topTwoCards);
		if (cardToTrash.getType() == CardType.NONE)
			return emptyList;
		target.moveCard(cardToTrash.getType(), CardSink.DISCARD, CardSink.TRASH);

		CardTypeAugment doNothing = CardTypeAugment.takeCard(CardType.NONE);
		CardTypeAugment gainCard = CardTypeAugment.moveCard(cardToTrash.getType(), CardSink.TRASH, CardSink.DISCARD);
		List<ActionEnum<Player>> allOptions = new ArrayList<ActionEnum<Player>>();
		allOptions.add(doNothing);
		allOptions.add(gainCard);

		removeVictimFromToBeAttackedList(target.getNumber()); // we just do this once
		return allOptions;
	}

	private Card[] discardTopTwoCards(Player target) {
		Card[] retValue = new Card[2];
		retValue[0] = target.drawTopCardFromDeckInto(CardSink.DISCARD);
		retValue[1] = target.drawTopCardFromDeckInto(CardSink.DISCARD);
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

	@Override
	public Player nextActor() {
		return game.getPlayer(attacker);
		// as when the attack occurs, the decisions are made by the attacker, not the defender, as assumed
		// in the default AttackCard implementation
	}
}

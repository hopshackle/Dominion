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
		Card[] topTwoCards = revealTopTwoCards(target);

		// we always trash the highest treasure card for the moment.
		// The decision is then whether to take it into hand personally
		Card cardToTrash = getHighestTreasureCard(topTwoCards);

		for (Card c : topTwoCards)
			target.moveCard(c.getType(), CardSink.REVEALED, CardSink.DISCARD);
			// do this separately, otherwise if we have 1 card in deck, then this is shuffled back in if we
			// discard it before drawing the second card
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

	private Card[] revealTopTwoCards(Player target) {
		Card[] retValue = new Card[2];
		retValue[0] = target.drawTopCardFromDeckInto(CardSink.REVEALED);
		retValue[1] = target.drawTopCardFromDeckInto(CardSink.REVEALED);
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
		if (game == null)
			return null;
		return game.getPlayer(attacker);
		// as when the attack occurs, the decisions are made by the attacker, not the defender, as assumed
		// in the default AttackCard implementation
	}
}

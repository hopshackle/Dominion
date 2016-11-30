package hopshackle.dominion.basecards;

import java.util.ArrayList;
import java.util.List;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.*;

public class Thief extends AttackCard {

	public Thief() {
		super(CardType.THIEF);
	}

	@Override
	public void executeAttackOnPlayer(Player target, Player attacker) {
		target.log("Is target of THIEF");
		// always trash the highest value treasure card drawn
		Card[] topTwoCards = discardTopTwoCards(target);
		Card cardToTrash = getHighestTreasureCard(topTwoCards);
		if (cardToTrash.getType() == CardType.NONE)
			return;
		target.trashCard(cardToTrash.getType(), CardSink.DISCARD);

		// then decide whether to keep the trashed card
		CardTypeAugment doNothing = new CardTypeAugment(CardType.NONE, CardSink.DISCARD, ChangeType.GAIN);
		CardTypeAugment gainCard = new CardTypeAugment(cardToTrash.getType(), CardSink.DISCARD, ChangeType.GAIN);
		List<ActionEnum<Player>> allOptions = new ArrayList<ActionEnum<Player>>();
		allOptions.add(doNothing);
		allOptions.add(gainCard);
		
		DominionAction decision = (DominionAction) attacker.getDecider().decide(attacker, allOptions);
		decision.start();
		decision.run();
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

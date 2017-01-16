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
		// always trash the highest value treasure card drawn
		Card[] topTwoCards = discardTopTwoCards(target);
		Card cardToTrash = getHighestTreasureCard(topTwoCards);
		if (cardToTrash.getType() == CardType.NONE)
			return emptyList;
		target.moveCard(cardToTrash.getType(), CardSink.DISCARD, CardSink.TRASH);

		// then decide whether to keep the trashed card
		CardTypeAugment doNothing = CardTypeAugment.takeCard(CardType.NONE);
		CardTypeAugment gainCard = CardTypeAugment.moveCard(cardToTrash.getType(), CardSink.TRASH, CardSink.DISCARD);
		List<ActionEnum<Player>> allOptions = new ArrayList<ActionEnum<Player>>();
		allOptions.add(doNothing);
		allOptions.add(gainCard);
		
		Player attacker = game.getPlayer(this.attacker);
		Action<Player> decision = (Action<Player>) attacker.getDecider().decide(attacker, allOptions);
		decision.start();
		decision.run();
		return emptyList;
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

}

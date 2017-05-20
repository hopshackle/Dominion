package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
import hopshackle.simulation.*;

import java.util.*;

public class Militia extends AttackCard {

	public Militia() {
		super(CardType.MILITIA);
	}

	@Override
	public List<ActionEnum<Player>> executeAttackOnPlayer(Player victim) {
		List<CardType> hand = victim.getCopyOfHand();
		CardType[] cardsInHand = hand.toArray(new CardType[1]);
		if (cardsInHand.length < 4) {
			victim.log("MILITIA has no effect given hand size of " + cardsInHand.length);
			return emptyList;
		}
		victim.log("Victim of MILITIA:");

		List<ActionEnum<Player>> retValue = new ArrayList<>();
		List<List<CardType>> discardOptions = victim.getPossibleDiscardsFromHand(1, 1);
		for (List<CardType> discard : discardOptions) {
			retValue.add(CardTypeAugment.discardCard(discard.get(0)));
		}
		return retValue;
	}

}

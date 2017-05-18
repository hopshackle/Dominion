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
	
		List<List<CardType>> discardOptions = victim.getPossibleDiscardsFromHand(cardsInHand.length - 3, cardsInHand.length - 3);
		return CardType.listOfDiscardsToActionEnumList(discardOptions);
	}

}

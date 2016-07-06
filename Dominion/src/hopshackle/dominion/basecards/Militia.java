package hopshackle.dominion.basecards;

import hopshackle.dominion.AttackCard;
import hopshackle.dominion.CardType;
import hopshackle.dominion.Player;
import hopshackle.dominion.PositionSummary;
import hopshackle.simulation.LookaheadDecider;

import java.util.*;

public class Militia extends AttackCard {

	public Militia() {
		super(CardType.MILITIA);
	}

	@Override
	public void executeAttackOnPlayer(Player victim, Player attacker) {

		List<CardType> hand = victim.getCopyOfHand();
		CardType[] cardsInHand = hand.toArray(new CardType[1]);
		if (cardsInHand.length < 4) {
			victim.log("MILITIA has no effect given hand size of " + cardsInHand.length);
			return;
		}
		victim.log("Victim of MILITIA:");
		CardType[] possibleCombination = new CardType[3];
		CardType[] bestCombination = new CardType[3];
		LookaheadDecider<Player, PositionSummary> discardDecider = victim.getHandDecider();
		double bestValue = -1.0;
		for (int loop=0; loop<cardsInHand.length; loop++) {
			possibleCombination[0] = cardsInHand[loop];
			for (int loop2=loop+1; loop2<cardsInHand.length; loop2++){
				possibleCombination[1] = cardsInHand[loop2];
				for (int loop3=loop2+1; loop3<cardsInHand.length; loop3++) {
					possibleCombination[2] = cardsInHand[loop3];
					PositionSummary positionWithReducedHand = victim.getPositionSummaryCopy();
					positionWithReducedHand.changeHand(possibleCombination);
					double value = discardDecider.value(positionWithReducedHand);
					if (value > bestValue) {
						bestValue = value;
						for (int n=0; n<3; n++)
							bestCombination[n] = possibleCombination[n];
					}
				}
			}
		}

		List<CardType> cardsToKeep = new ArrayList<CardType>();
		for (CardType cardToKeep : bestCombination) {
			cardsToKeep.add(cardToKeep);
		}
		for (CardType cardInHand : hand) {
			if (cardsToKeep.contains(cardInHand))
				cardsToKeep.remove(cardInHand);
			else {
				victim.discard(cardInHand);
			}
		}
	}

}

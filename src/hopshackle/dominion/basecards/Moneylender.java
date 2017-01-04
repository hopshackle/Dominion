package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.*;

import java.util.ArrayList;
import java.util.List;

public class Moneylender extends Card {

	private int treasureValue = 0;

	public Moneylender() {
		super(CardType.MONEYLENDER);
	}

	@Override
	public List<ActionEnum<Player>> takeAction(Player player) {

		boolean hasCopper = false;
		for (CardType ct : player.getCopyOfHand()) {
			if (ct == CardType.COPPER)
				hasCopper = true;
		}

		if (!hasCopper) 
			return emptyList;;

		// There are two actions to be evaluated:
		// - Do Nothing
		// - Trash Copper

		CardTypeAugment doNothing = CardTypeAugment.trashCard(CardType.NONE, CardSink.HAND);
		CardTypeAugment trashCopper = CardTypeAugment.trashCard(CardType.COPPER, CardSink.HAND);
		List<ActionEnum<Player>> allOptions = new ArrayList<ActionEnum<Player>>();
		allOptions.add(doNothing);
		allOptions.add(trashCopper);

		player.setState(Player.State.PURCHASING);
		Action<Player> chosenAction = (Action<Player>) player.getDecider().decide(player, allOptions);
		chosenAction.start();
		chosenAction.run();
		player.setState(Player.State.PLAYING);
		if (chosenAction.getType().equals(trashCopper)) {
			treasureValue += 3;		// to cope with Throne Rooms, which might trash a copper twice (or more)
		}
		return emptyList;
	}

	@Override
	public int getAdditionalPurchasePower() {
		return treasureValue;
	}

	@Override
	public void reset() {
		treasureValue = 0;
	}

}

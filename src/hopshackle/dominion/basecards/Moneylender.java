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
		super.takeAction(player);
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

		// We will introduce a new CardTypeAugment for MONEYLENDER, which will augment the
		// treasure value of the MONEYLENDER card if a COPPER is trashed

		CardTypeAugment doNothing = CardTypeAugment.trashCard(CardType.NONE, CardSink.HAND);
		CardTypeAugment trashCopper = new CardTypeAugment(CardType.COPPER, CardSink.HAND, CardSink.TRASH, ChangeType.MONEYLENDER);
		List<ActionEnum<Player>> allOptions = new ArrayList<ActionEnum<Player>>();
		allOptions.add(doNothing);
		allOptions.add(trashCopper);

		return allOptions;
	}

	public void incrementTreasureValue(int increase) {
		treasureValue += increase;
	}

	@Override
	public int getAdditionalPurchasePower() {
		return treasureValue;
	}

	@Override
	public void reset() {
		treasureValue = 0;
	}

	@Override
	public Moneylender clone(DominionGame newGame) {
		Moneylender retValue = (Moneylender) super.clone(newGame);
		retValue.treasureValue = treasureValue;
		return retValue;
	}

}

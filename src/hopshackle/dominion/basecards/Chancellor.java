package hopshackle.dominion.basecards;

import java.util.ArrayList;
import java.util.List;

import hopshackle.dominion.*;
import hopshackle.simulation.ActionEnum;

public class Chancellor extends Card {

	/*
	Relatively straightforward. We need a new ChangeType to stick the whole discard into the desk
	But we always have just two options - to do so, or not.
	 */
	public Chancellor() {
		super(CardType.CHANCELLOR);
	}

	@Override
	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		List<ActionEnum<Player>> retValue = new ArrayList<>();
		retValue.add(new CardTypeAugment(CardType.NONE, CardTypeAugment.CardSink.DISCARD, CardTypeAugment.CardSink.DECK, CardTypeAugment.ChangeType.CHANCELLOR));
		retValue.add(new CardTypeAugment(CardType.NONE, CardTypeAugment.CardSink.DISCARD, CardTypeAugment.CardSink.DECK, CardTypeAugment.ChangeType.NOCHANGE));
		return retValue;
	}
}

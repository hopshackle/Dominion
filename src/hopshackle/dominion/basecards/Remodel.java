package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.*;
import hopshackle.simulation.*;

import java.util.*;

public class Remodel extends Card {

	public Remodel() {
		super(CardType.REMODEL);
	}

	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);

		List<CardType> hand = new ArrayList<CardType>();
		for (CardType card : player.getCopyOfHand()) {
			if (!hand.contains(card))
				hand.add(card);
		}

		List<ActionEnum<Player>> allOptions = new ArrayList<ActionEnum<Player>>();
		for (CardType cardToTrash : hand) {
			List<CardType> targets = new ArrayList<CardType>();
			for (CardType t : player.getGame().availableCardsToPurchase()) {
				if (t != CardType.NONE && t.getCost() <= cardToTrash.getCost() + 2)
					targets.add(t);
			}
			for (CardType cardToAcquire : targets) {
				CardTypeAugment trashAction = CardTypeAugment.trashCard(cardToTrash, CardSink.HAND);
				CardTypeAugment gainAction = CardTypeAugment.takeCard(cardToAcquire);
				List<CardTypeAugment> compositeAction = new ArrayList<CardTypeAugment>();
				compositeAction.add(trashAction);
				compositeAction.add(gainAction);
				allOptions.add(new CardTypeList(compositeAction, true));
			}
		}
		allOptions.add(CardTypeAugment.takeCard(CardType.NONE));
		// choose best option, and execute it
		
		return allOptions;
	}
}

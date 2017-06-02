package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.*;
import hopshackle.simulation.*;

import java.util.*;

public class Remodel extends Card {

	private boolean secondPhase;

	public Remodel() {
		super(CardType.REMODEL);
	}

	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);

		List<ActionEnum<Player>> allOptions = new ArrayList<ActionEnum<Player>>();
		if (!secondPhase) {
			List<CardType> hand = new ArrayList<CardType>();
			for (CardType card : player.getCopyOfHand()) {
				if (!hand.contains(card))
					hand.add(card);

			}
			for (CardType cardToTrash : hand) {
				allOptions.add(new CardTypeAugment(cardToTrash, CardSink.HAND, CardSink.TRASH, ChangeType.REMODEL));
			}
		} else {
			int budget = player.getOneOffBudget() + 2;

			List<CardType> targets = new ArrayList<CardType>();
			for (CardType t : player.getGame().availableCardsToPurchase()) {
				if (t != CardType.NONE && t.getCost() <= budget)
					targets.add(t);
			}
			for (CardType cardToAcquire : targets) {
				CardTypeAugment gainAction = CardTypeAugment.takeCard(cardToAcquire);
				allOptions.add(gainAction);
			}
			player.oneOffBudget(0); // and reset
		}
		secondPhase = !secondPhase;
		return allOptions;
	}

	@Override
	public Remodel clone(DominionGame newGame) {
		Remodel retValue = (Remodel) super.clone(newGame);
		retValue.secondPhase = secondPhase;
		return retValue;
	}

	@Override
	public void reset() {
		secondPhase = false;
	}
}

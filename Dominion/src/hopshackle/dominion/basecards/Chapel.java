package hopshackle.dominion.basecards;

import hopshackle.dominion.Card;
import hopshackle.dominion.CardType;
import hopshackle.dominion.Player;
import hopshackle.dominion.PositionSummary;
import hopshackle.simulation.LookaheadDecider;

import java.util.List;

public class Chapel extends Card {

	public Chapel() {
		super(CardType.CHAPEL);
	}

	@Override
	public void takeAction(Player player) {
		for (int loop = 0; loop < 4; loop ++)
			trashCard(player);
	}

	private void trashCard(Player player) {
		List<CardType> hand = player.getCopyOfHand();
		LookaheadDecider<Player> dpd = player.getPositionDecider();
		PositionSummary ps = (PositionSummary) dpd.getCurrentState(player);
		double startingValue = dpd.value(ps);
		double bestGain = 0.0;
		CardType cardToTrash = CardType.NONE;
		for (CardType ct : hand) {
			ps.removeCard(ct);
			double newValue = dpd.value(ps);
			ps.addCard(ct);

			if (newValue - startingValue > bestGain) {
				bestGain = newValue - startingValue;
				cardToTrash = ct;
			}
		}

		if (cardToTrash != CardType.NONE) {
			player.log("Trashes " + cardToTrash);
			player.trashCardFromHand(cardToTrash);
		}
	}
}

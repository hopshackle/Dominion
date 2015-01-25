package hopshackle.dominion;

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
		PositionSummary ps = player.getPositionSummaryCopy();
		List<CardType> hand = player.getCopyOfHand();
		DominionPositionDecider dpd = player.getPurchaseDecider();
		double startingValue = dpd.valuePosition(ps);
		double bestGain = 0.0;
		CardType cardToTrash = CardType.NONE;
		for (CardType ct : hand) {
			ps.removeCard(ct);
			double newValue = dpd.valuePosition(ps);
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

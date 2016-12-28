package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.*;
import hopshackle.simulation.ActionEnum;

import java.util.*;

public class Chapel extends Card {

	public Chapel() {
		super(CardType.CHAPEL);
	}

	@Override
	public List<ActionEnum<Player>> takeAction(Player player) {
		for (int loop = 0; loop < 4; loop ++)
			trashCard(player);
		return emptyList;
	}

	private void trashCard(Player player) {
		List<CardType> hand = new ArrayList<CardType>();
		for (CardType c : player.getCopyOfHand()) {
			if (!hand.contains(c))
				hand.add(c);
		}
		List<ActionEnum<Player>> options = new ArrayList<ActionEnum<Player>>();
		for (CardType card : hand) {
			options.add(CardTypeAugment.trashCard(card, CardSink.HAND));
		}
		options.add(CardTypeAugment.trashCard(CardType.NONE, CardSink.HAND));

		player.setState(Player.State.PURCHASING);
		DominionAction choice = (DominionAction) player.getDecider().decide(player, options);
		choice.start();
		choice.run();
		player.setState(Player.State.PLAYING);
	}
}

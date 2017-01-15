package hopshackle.dominion.basecards;

import java.util.*;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.ActionEnum;

public class Bureaucrat extends AttackCard {

	public Bureaucrat() {
		super(CardType.BUREAUCRAT);
	}

	public List<ActionEnum<Player>> takeAction(Player player) {
		DominionGame game = player.getGame();
		if (game.drawCard(CardType.SILVER)) {
			player.insertCardDirectlyIntoHand(new Card(CardType.SILVER));
			player.putCardFromHandOnTopOfDeck(CardType.SILVER);
		}
		return super.takeAction(player);
	}

	@Override
	public List<ActionEnum<Player>>  executeAttackOnPlayer(Player target) {
		target.log("Is target of BUREAUCRAT");
		Set<CardType> victoryCardTypes = new HashSet<CardType>();
		for (CardType ct : target.getCopyOfHand()) {
			if (ct.isVictory() && ct != CardType.CURSE) {		// technically CURSE cards are not Victory cards
				victoryCardTypes.add(ct);
			}
		}

		switch (victoryCardTypes.size()) {
		case 0:
			return emptyList;
		case 1:
			target.putCardFromHandOnTopOfDeck(victoryCardTypes.iterator().next());
			return emptyList;
		default:
			List<ActionEnum<Player>> retValue = new ArrayList<ActionEnum<Player>>();
			for (CardType eligibleCard : victoryCardTypes) {
				ActionEnum<Player> option = new CardTypeAugment(eligibleCard, CardSink.HAND, CardSink.DECK, ChangeType.MOVE);
				retValue.add(option);
			}
			return retValue;
		}
	}
}

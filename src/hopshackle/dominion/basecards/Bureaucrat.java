package hopshackle.dominion.basecards;

import java.util.List;

import hopshackle.dominion.AttackCard;
import hopshackle.dominion.Card;
import hopshackle.dominion.CardType;
import hopshackle.dominion.DominionGame;
import hopshackle.dominion.Player;
import hopshackle.simulation.ActionEnum;

public class Bureaucrat extends AttackCard {

	public Bureaucrat() {
		super(CardType.BUREAUCRAT);
	}

	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		DominionGame game = player.getGame();
		if (game.drawCard(CardType.SILVER)) {
			player.insertCardDirectlyIntoHand(new Card(CardType.SILVER));
			player.putCardFromHandOnTopOfDeck(CardType.SILVER);
		}
		return emptyList;
	}

	@Override
	public List<ActionEnum<Player>>  executeAttackOnPlayer(Player target, Player attacker) {
		target.log("Is target of BUREAUCRAT");
		for (CardType ct : target.getCopyOfHand()) {
			if (ct.isVictory() && ct != CardType.CURSE) {		// technically CURSE cards are not Victory cards
				target.putCardFromHandOnTopOfDeck(ct);
				return emptyList;
			}
		}
		return emptyList;
	}


}

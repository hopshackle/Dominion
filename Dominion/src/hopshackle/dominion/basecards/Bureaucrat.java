package hopshackle.dominion.basecards;

import hopshackle.dominion.AttackCard;
import hopshackle.dominion.Card;
import hopshackle.dominion.CardType;
import hopshackle.dominion.Game;
import hopshackle.dominion.Player;

public class Bureaucrat extends AttackCard {

	public Bureaucrat() {
		super(CardType.BUREAUCRAT);
	}

	public void takeAction(Player player) {
		super.takeAction(player);
		Game game = player.getGame();
		if (game.drawCard(CardType.SILVER)) {
			player.insertCardDirectlyIntoHand(new Card(CardType.SILVER));
			player.putCardFromHandOnTopOfDeck(CardType.SILVER);
		}
	}

	@Override
	public void executeAttackOnPlayer(Player target, Player attacker) {
		target.log("Is target of BUREAUCRAT");
		for (CardType ct : target.getCopyOfHand()) {
			if (ct.isVictory() && ct != CardType.CURSE) {		// technically CURSE cards are not Victory cards
				target.putCardFromHandOnTopOfDeck(ct);
				return;
			}
		}
	}


}

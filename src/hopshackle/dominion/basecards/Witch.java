package hopshackle.dominion.basecards;

import hopshackle.dominion.AttackCard;
import hopshackle.dominion.CardType;
import hopshackle.dominion.DominionGame;
import hopshackle.dominion.Player;

public class Witch extends AttackCard {
	
	public Witch() {
		super(CardType.WITCH);
	}

	@Override
	public void executeAttackOnPlayer(Player target, Player attacker) {
		DominionGame game = target.getGame();
	
		if (game.getNumberOfCardsRemaining(CardType.CURSE) > 0) {
			target.log("Attacked by WITCH and draws CURSE");
			target.takeCardFromSupplyIntoDiscard(CardType.CURSE);
		}
	}

}

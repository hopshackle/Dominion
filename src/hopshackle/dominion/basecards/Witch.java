package hopshackle.dominion.basecards;

import java.util.List;

import hopshackle.dominion.AttackCard;
import hopshackle.dominion.CardType;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.DominionGame;
import hopshackle.dominion.Player;
import hopshackle.simulation.ActionEnum;

public class Witch extends AttackCard {
	
	public Witch() {
		super(CardType.WITCH);
	}

	@Override
	public List<ActionEnum<Player>> executeAttackOnPlayer(Player target, Player attacker) {
		DominionGame game = target.getGame();
	
		if (game.getNumberOfCardsRemaining(CardType.CURSE) > 0) {
			target.log("Attacked by WITCH and draws CURSE");
			target.takeCardFromSupply(CardType.CURSE, CardSink.DISCARD);
		}
		return emptyList;
	}

}

package hopshackle.dominion.basecards;

import hopshackle.dominion.AttackCard;
import hopshackle.dominion.Card;
import hopshackle.dominion.CardType;
import hopshackle.dominion.Player;

public class Moat extends Card {


	public Moat() {
		super(CardType.MOAT);
	}

	@Override
	public boolean executeReactionAgainst(AttackCard attack, Player attacker, Player victim) {
		victim.log("MOAT: Defends against " + attack.toString());
		return true;
	}
}

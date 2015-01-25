package hopshackle.dominion;

public class Witch extends AttackCard {
	
	public Witch() {
		super(CardType.WITCH);
	}

	@Override
	public void executeAttackOnPlayer(Player target, Player attacker) {
		Game game = target.getGame();
	
		if (game.getNumberOfCardsRemaining(CardType.CURSE) > 0) {
			target.log("Attacked by WITCH and draws CURSE");
			target.takeCardFromSupplyIntoDiscard(CardType.CURSE);
		}
	}

}

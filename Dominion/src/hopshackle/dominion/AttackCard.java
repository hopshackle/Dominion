package hopshackle.dominion;

public abstract class AttackCard extends Card {

	public AttackCard(CardType type) {
		super(type);
	}

	public abstract void executeAttackOnPlayer(Player target, Player attacker);
	
	@Override
	public void takeAction(Player player) {
		super.takeAction(player);
		executeAttack(player);
	}

	public void executeAttack(Player attacker) {
		Game game = attacker.getGame();
		int attackingPlayerNumber = game.getPlayerNumber(attacker);
		Player[] victims = new Player[3];
		int victimNumber = attackingPlayerNumber + 1;	// start with person to the left
		Player[] allPlayers = game.getPlayers();
		for (int i = 0 ; i < 3; i++) {
			if (victimNumber > 4)
				victimNumber = 1;
			victims[i] = allPlayers[victimNumber-1];
			victimNumber++;
		}
		for (Player victim : victims) {
			if (victim == attacker) continue;
			boolean defended = false;
			for (CardType ct : victim.getCopyOfHand()) {
				if (ct.isReactive()) {
					Card defenceCard = CardFactory.instantiateCard(ct);
					defended = defended | defenceCard.executeReactionAgainst(this, attacker, victim);
				}
			}
			if (!defended) 
				executeAttackOnPlayer(victim, attacker);
		}
	}

}

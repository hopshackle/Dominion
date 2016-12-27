package hopshackle.dominion;

import hopshackle.simulation.ActionEnum;

import java.util.List;

public abstract class AttackCard extends Card {

	public AttackCard(CardType type) {
		super(type);
	}

	public abstract List<ActionEnum<Player>>  executeAttackOnPlayer(Player target, Player attacker);
	
	@Override
	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		List<ActionEnum<Player>> retValue = executeAttack(player);
		return retValue;
	}

	public List<ActionEnum<Player>> executeAttack(Player attacker) {
		DominionGame game = attacker.getGame();
		int attackingPlayerNumber = game.getPlayerNumber(attacker);
		Player[] victims = new Player[3];
		int victimNumber = attackingPlayerNumber + 1;	// start with person to the left
		List<Player> allPlayers = game.getAllPlayers();
		for (int i = 0 ; i < 3; i++) {
			if (victimNumber > 4)
				victimNumber = 1;
			victims[i] = allPlayers.get(victimNumber-1);
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
		
		return emptyList;
	}

}

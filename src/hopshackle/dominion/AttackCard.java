package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.ArrayList;
import java.util.List;

public abstract class AttackCard extends Card {

	protected int[] notYetAttacked;
	protected Player defendingPlayer;
	protected Player attacker;
	protected DominionGame game;

	public AttackCard(CardType type) {
		super(type);
	}

	public abstract List<ActionEnum<Player>> executeAttackOnPlayer(Player target);

	@Override
	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		notYetAttacked = new int[3];
		this.attacker = player;
		game = attacker.getGame();
		int attackingPlayerNumber = game.getPlayerNumber(attacker);
		int victimNumber = attackingPlayerNumber + 1;	// start with person to the left
		for (int i = 0 ; i < 3; i++) {
			if (victimNumber > 4)
				victimNumber = 1;
			notYetAttacked[i] = victimNumber;
			victimNumber++;
		}
		List<ActionEnum<Player>> retValue = executeAttack();
		return retValue;
	}

	protected List<ActionEnum<Player>> executeAttack() {
		List<ActionEnum<Player>> defenceOptions = emptyList;
		for (int v = 0; v < notYetAttacked.length; v++) {
			boolean defended = false;
			Player victim = game.getPlayer(notYetAttacked[v]);
			for (CardType ct : victim.getCopyOfHand()) {
				if (ct.isReactive()) {
					Card defenceCard = CardFactory.instantiateCard(ct);
					defended = defended | defenceCard.executeReactionAgainst(this, attacker, victim);
				}
			}
			if (!defended) {
				defenceOptions = executeAttackOnPlayer(victim);
				if (!defenceOptions.isEmpty()) {
					defendingPlayer = victim;
					createFollowOnAction(v);
					return defenceOptions;
				}
			} 
		}
		defendingPlayer = null;
		notYetAttacked = null;
		attacker = null;
		game = null;
		return defenceOptions;
	}

	private void createFollowOnAction(int playerIndex) {
		if (playerIndex < notYetAttacked.length - 1){
			int[] updatedVictimList = new int[notYetAttacked.length - playerIndex - 1];
			for (int i = playerIndex+1; i < notYetAttacked.length; i++)
				updatedVictimList[i-playerIndex-1] = notYetAttacked[i];

			notYetAttacked = updatedVictimList;
		} else {
			notYetAttacked = null;
		}
	}

	@Override
	public DominionAction followUpAction() {
		if (notYetAttacked == null) {
			return null;
		} else {
			return new AttackCardFollowOnAction(this);
		}
	} 
	@Override
	public Player nextActor() {
		return defendingPlayer;
	}

	@Override
	public AttackCard clone(DominionGame newGame) {
		AttackCard retValue = (AttackCard) super.clone(newGame);
		if (defendingPlayer != null)
			retValue.defendingPlayer = newGame.getPlayer(game.getPlayerNumber(defendingPlayer));
		if (attacker != null)
			retValue.attacker = newGame.getPlayer(game.getPlayerNumber(attacker));
		if (notYetAttacked != null)
			retValue.notYetAttacked = new int[notYetAttacked.length];
		for (int i =0; i < notYetAttacked.length; i++)
			retValue.notYetAttacked[i] = notYetAttacked[i];
		if (game != null)
			retValue.game = newGame;
		return retValue;
	}
}

class AttackCardFollowOnAction extends DominionAction{

	private AttackCard attackSource;	// this will know what it is

	public AttackCardFollowOnAction(AttackCard card) {
		super(card.attacker, new CardTypeList(new ArrayList<CardType>()));
		attackSource = card;
	}

	@Override 
	public AttackCardFollowOnAction clone(Player newPlayer) {
		AttackCard newAttackCard = (AttackCard) newPlayer.getCardLastPlayed();
		return new AttackCardFollowOnAction(newAttackCard);
	}

	@Override
	public void doStuff() {
		// We just need to continue the attack.
		// The state information is retained within the AttackCard
		possibleOptions = attackSource.executeAttack();
		nextActor = attackSource.defendingPlayer;
		if (nextActor != null)	// still more attacking to do
			followUpAction = attackSource.followUpAction();
		else
			followUpAction = null;
	}

	@Override
	public ActionEnum<Player> getType() {
		return CardTypeAugment.playCard(attackSource.getType());
	}
}

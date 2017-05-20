package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.ArrayList;
import java.util.List;

public abstract class AttackCard extends Card {

    protected int[] notYetAttacked;
	protected int defendingPlayer;
	protected int attacker;
	protected DominionGame game;

	public AttackCard(CardType type) {
		super(type);
	}

	public abstract List<ActionEnum<Player>> executeAttackOnPlayer(Player target);

	@Override
	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		game = player.getGame();
		notYetAttacked = new int[3];
		attacker = game.getPlayerNumber(player);
		int victimNumber = attacker + 1;	// start with person to the left
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
		while (notYetAttacked != null){
			boolean defended = false;
			Player attacker = game.getPlayer(this.attacker);
			int victimPlayerNumber = notYetAttacked[0];
			Player victim = game.getPlayer(victimPlayerNumber);
			victim.refreshPositionSummary();
			for (CardType ct : victim.getCopyOfHand()) {
				if (ct.isReactive()) {
					Card defenceCard = CardFactory.instantiateCard(ct);
					defended = defended | defenceCard.executeReactionAgainst(this, attacker, victim);
				}
			}
			if (!defended) {
				defenceOptions = executeAttackOnPlayer(victim);
				if (!defenceOptions.isEmpty()) {
					defendingPlayer = victimPlayerNumber;
					return defenceOptions;
				}
			}
            removeVictimFromToBeAttackedList(victimPlayerNumber); // so we assume attack is completed, unless there are some options to be taken
		}
		defendingPlayer = 0;
		notYetAttacked = null;
		attacker = 0;
		game = null;
		return defenceOptions;
	}

	protected void removeVictimFromToBeAttackedList(int playerNumber) {
	    if (notYetAttacked.length > 1) {
			int[] updatedVictimList = new int[notYetAttacked.length - 1];
			int count = 0;
			for (int player : notYetAttacked) {
			    if (player == playerNumber) continue;
                updatedVictimList[count] = player;
                count++;
            }
			notYetAttacked = updatedVictimList;
		} else {
	        if (notYetAttacked[0] != playerNumber)
	            throw new AssertionError("Expecting to remove last player " + playerNumber + ", but found " + notYetAttacked[0]);
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
		if (game != null && defendingPlayer != 0)
			return game.getPlayer(defendingPlayer);
		return null;
	}

	@Override
	public AttackCard clone(DominionGame newGame) {
		AttackCard retValue = (AttackCard) super.clone(newGame);
		retValue.defendingPlayer = defendingPlayer;
		retValue.attacker = attacker;
		if (notYetAttacked != null) {
			retValue.notYetAttacked = new int[notYetAttacked.length];
			for (int i=0; i < notYetAttacked.length; i++)
				retValue.notYetAttacked[i] = notYetAttacked[i];
		}
		if (game != null)
			retValue.game = newGame;
		return retValue;
	}
	@Override
	public void reset() {
		notYetAttacked = null;
		defendingPlayer = 0;
		attacker = 0;
		game = null;
	}
}

class AttackCardFollowOnAction extends DominionAction{

	private AttackCard attackSource;	// this will know what it is

	public AttackCardFollowOnAction(AttackCard card) {
		super(card.game.getPlayer(card.attacker), new CardTypeList(new ArrayList<CardType>()));
		attackSource = card;
		hasNoAssociatedDecision = true;
	}
	private AttackCardFollowOnAction(AttackCardFollowOnAction master, Player newPlayer, AttackCard newCard) {
		super(master, newPlayer);
		attackSource = newCard;
		hasNoAssociatedDecision = true;
	}

	@Override
	public AttackCardFollowOnAction clone(Player newPlayer) {
		AttackCard newAttackCard = (AttackCard) newPlayer.getCardLastPlayed();
		return new AttackCardFollowOnAction(this, newPlayer, newAttackCard);
	}

	@Override
	public void doStuff() {
		// We just need to continue the attack.
		// The state information is retained within the AttackCard
		possibleOptions = attackSource.executeAttack();
		if (attackSource.defendingPlayer > 0) {
			nextActor = player.getGame().getPlayer(attackSource.defendingPlayer);
		}
		if (nextActor != null)	// still more attacking to do
			followUpAction = attackSource.followUpAction();
		else
			followUpAction = null;
	}

	@Override
	public String toString() {
		return "Follow-on " + attackSource;
	}
	@Override
	public ActionEnum<Player> getType() {
		return new CardTypeAugment(attackSource.getType(), CardTypeAugment.CardSink.DISCARD, CardTypeAugment.CardSink.DISCARD, CardTypeAugment.ChangeType.NOCHANGE);
	}
}


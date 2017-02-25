package hopshackle.dominion;

import java.util.*;

import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.dominion.basecards.ThroneRoom;
import hopshackle.simulation.*;

public class DominionAction extends Action<Player> {

	private static boolean hardCodedActionDecider = SimProperties.getProperty("DominionHardCodedActionDecider", "false").equals("true");
	private List<CardTypeAugment> cardType;
	protected Player player;
	private boolean isAction;
	public DominionAction(Player p, CardTypeAugment actionEnum) {
		super(actionEnum, p, 0l, false);
		player = p;
		this.cardType = new ArrayList<CardTypeAugment>();
		if (actionEnum != null) {
			this.cardType.add(actionEnum);
			if (actionEnum.type == ChangeType.PLAY || actionEnum.type == ChangeType.ENTHRONE) isAction = true;
		}
	}

	public DominionAction(Player player, CardTypeList aeList) {
		super(aeList, player, 0l, false);
		this.cardType = aeList.cards;
		this.player = player;
	}

	public DominionAction(DominionAction master, Player newPlayer) {
		super(master.actionType, newPlayer, 0l, false);
		player = newPlayer;
		DominionGame masterGame = master.actor.getGame();
		int nextActorNumber = masterGame.getPlayerNumber(master.nextActor);
		nextActor = newPlayer.getGame().getPlayer(nextActorNumber);
		cardType = master.cardType;
		isAction = master.isAction();
		followUpAction = (master.followUpAction == null) ? null : master.followUpAction.clone(newPlayer);
		possibleOptions = master.possibleOptions;
	}

	public String toString() {
		StringBuffer retValue = new StringBuffer();
		for (CardTypeAugment c : cardType) {
			retValue.append(c.toString() + " ");
		}
		return retValue.toString();
	}

	@Override
	protected void doStuff() {
		for (CardTypeAugment component : cardType) {
			switch (component.type) {
			case ENTHRONE:
				ThroneRoom parentCard = (ThroneRoom) player.getCardLastPlayed();
				parentCard.enthrone(component.card);
				// then continue with PLAY
			case PLAY:
				Card cardToPlay = player.playFromHandToRevealedCards(component.card);
				if (cardToPlay != null) {
					if (cardToPlay.getType() == CardType.NONE) {
						player.log("Chooses not to play an Action card.");
					} else {
						player.log("Plays " + cardToPlay.toString());
						possibleOptions = cardToPlay.takeAction(player);
						followUpAction = cardToPlay.followUpAction();
						nextActor = cardToPlay.nextActor();
						if (nextActor == null)
							nextActor = player;
					}
					player.decrementActionsLeft();
				} else {
					logger.severe("No Actual card found in hand for type " + cardType);
				}
				break;
			case MOVE:
				if (component.card == CardType.NONE)
					continue;
				player.log(component.toString()); 
				player.moveCard(component.card, component.from, component.to);
			}
		}
	}
	@Override 
	protected void doNextDecision(Player p) {
		// Do nothing .. this is all handled in Game/Player
		// Note that we do not override doNextDecision(), so that learning event
		// is still dispatched
	}
	@Override
	protected void eventDispatch(AgentEvent learningEvent) {
		if (hardCodedActionDecider && (cardType.isEmpty() || cardType.get(0).type == ChangeType.PLAY)) {
			return;	// override to turn off any ER stream
		}
		super.eventDispatch(learningEvent);
	}

	public boolean isAction() {
		return isAction;
	}
	@Override
	public DominionAction clone(Player newPlayer) {
		return new DominionAction(this, newPlayer);
	}
}

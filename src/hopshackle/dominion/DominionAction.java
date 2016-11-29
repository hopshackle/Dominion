package hopshackle.dominion;

import java.util.*;

import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.*;

public class DominionAction extends Action<Player> {
	
	private static boolean hardCodedActionDecider = SimProperties.getProperty("DominionHardCodedActionDecider", "false").equals("true");
	private List<CardTypeAugment> cardType;
	private Player player;
	private boolean isAction;

	public DominionAction(Player p, CardTypeAugment actionEnum) {
		super(actionEnum, p, false);
		this.cardType = new ArrayList<CardTypeAugment>();
		this.cardType.add(actionEnum);
		player = p;
		if (actionEnum.type == ChangeType.PLAY) isAction = true;
	}

	public DominionAction(Player player, CardTypeList aeList) {
		super(aeList, player, false);
		this.cardType = aeList.cards;
		this.player = player;
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
			case PLAY:
				Card cardToPlay = player.playFromHandToRevealedCards(component.card);
				if (cardToPlay != null) {
					if (cardToPlay.getType() == CardType.NONE) {
						player.log("Chooses not to play an Action card.");
					} else {
						player.log("Plays " + cardToPlay.toString());
						cardToPlay.takeAction(player);
						for (int i = 0; i < cardToPlay.getAdditionalActions(); i++)
							player.incrementActionsLeft();
					}
				} else {
					logger.severe("No Actual card found in hand for type " + cardType);
				}
				break;
			case GAIN:
				player.log("Gains " + component.card.toString()); 
				player.takeCardFromSupply(component.card, component.dest);
				break;
			case LOSS:
				player.log("Trashes " + component.card.toString()); 
				player.trashCard(component.card, component.dest);
				break;
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
		if (hardCodedActionDecider && cardType.get(0).type == ChangeType.PLAY) {
			return;	// override to turn off any ER stream
		}
		super.eventDispatch(learningEvent);
	}
	
	public static void refresh() {
		hardCodedActionDecider = SimProperties.getProperty("DominionHardCodedActionDecider", "false").equals("true");
	}

	public boolean isAction() {
		return isAction;
	}
}

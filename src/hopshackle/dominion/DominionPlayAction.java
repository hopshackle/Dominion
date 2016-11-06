package hopshackle.dominion;

import hopshackle.simulation.*;

public class DominionPlayAction extends Action<Player> {
	
	private static boolean hardCodedActionDecider = SimProperties.getProperty("DominionHardCodedActionDecider", "false").equals("true");
	private CardType cardType;
	private Player player;

	public DominionPlayAction(Player a, CardType cardType) {
		super(cardType, a, false);
		this.cardType = cardType;
		player = a;
	}

	public String toString() {
		return "Play " + cardType.toString();
	}
	
	@Override
	public void doStuff() {
		Card cardToPlay = player.playFromHandToRevealedCards(cardType);
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
	}

	@Override 
	protected void doNextDecision(Player p) {
		// Do nothing .. this is all handled in Game/Player
	}
	@Override
	protected void eventDispatch(AgentEvent learningEvent) {
		if (hardCodedActionDecider) {
			return;	// override to turn off any ER stream
		}
		super.eventDispatch(learningEvent);
	}
	
	public static void refresh() {
		hardCodedActionDecider = SimProperties.getProperty("DominionHardCodedActionDecider", "false").equals("true");
	}
	
}

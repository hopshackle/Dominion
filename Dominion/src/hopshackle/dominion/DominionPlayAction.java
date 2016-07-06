package hopshackle.dominion;

import hopshackle.simulation.*;

public class DominionPlayAction extends Action<Player> {
	
	private CardType cardType;
	private Player player;

	public DominionPlayAction(Player a, CardType cardType) {
		super(cardType, a, false);
		this.cardType = cardType;
		player = a;
	}

	public String toString() {
		return cardType.toString();
	}
	
	@Override
	public void doStuff() {
		Card cardToPlay = player.playFromHandToRevealedCards(cardType);
		if (cardToPlay != null && cardToPlay.getType() != CardType.NONE) {
			player.log("Plays " + cardToPlay.toString());
			cardToPlay.takeAction(player);
			for (int i = 0; i < cardToPlay.getAdditionalActions(); i++)
				player.incrementActionsLeft();
		}
	}
	@Override 
	protected void doNextDecision() {
		// Do nothing .. this is all handled in Game/Player
		// Note that no learning events are generated either (in contrast to DominionBuyDecision)
		// TODO: learn how to play cards as well as how to buy them
	}
	
}

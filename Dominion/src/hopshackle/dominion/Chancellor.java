package hopshackle.dominion;

public class Chancellor extends Card {

	public Chancellor() {
		super(CardType.CHANCELLOR);
	}

	@Override
	public void takeAction(Player player) {
		super.takeAction(player);
		// rough heuristic that early in game this is worthwhile
		if (CardValuationVariables.PROVINCES_BOUGHT.getValue(player, player) < 0.50)
			player.putDeckIntoDiscard();
	}
}

package hopshackle.dominion.basecards;

import hopshackle.dominion.Card;
import hopshackle.dominion.CardType;
import hopshackle.dominion.Game;
import hopshackle.dominion.Player;

import java.util.List;

public class Mine extends Card {
	
	public Mine() {
		super(CardType.MINE);
	}

	public void takeAction(Player player) {		
		super.takeAction(player);
		Game game = player.getGame();
		List<CardType> hand = player.getCopyOfHand();
		boolean hasSilver = false;
		boolean hasCopper = false;
		for (CardType ct : hand) {
			if (ct == CardType.COPPER)
				hasCopper = true;
			if (ct == CardType.SILVER)
				hasSilver = true;
		}
		if (hasSilver) {
			player.trashCardFromHand(CardType.SILVER);
			player.insertCardDirectlyIntoHand(new Card(CardType.GOLD));
			game.drawCard(CardType.GOLD);
			player.log("Trashes SILVER to gain GOLD");
			return;
		}
		if (hasCopper) {
			player.trashCardFromHand(CardType.COPPER);
			player.insertCardDirectlyIntoHand(new Card(CardType.SILVER));
			game.drawCard(CardType.SILVER);
			player.log("Trashes COPPER to gain SILVER");
			return;
		}
	}

}

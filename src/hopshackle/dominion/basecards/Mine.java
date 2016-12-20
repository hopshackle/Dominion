package hopshackle.dominion.basecards;

import hopshackle.dominion.Card;
import hopshackle.dominion.CardType;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.DominionGame;
import hopshackle.dominion.Player;
import hopshackle.simulation.ActionEnum;

import java.util.List;

public class Mine extends Card {
	
	public Mine() {
		super(CardType.MINE);
	}

	public List<ActionEnum<Player>> takeAction(Player player) {		
		super.takeAction(player);
		DominionGame game = player.getGame();
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
			player.trashCard(CardType.SILVER, CardSink.HAND);
			player.insertCardDirectlyIntoHand(new Card(CardType.GOLD));
			game.drawCard(CardType.GOLD);
			player.log("Trashes SILVER to gain GOLD");
			return emptyList;
		}
		if (hasCopper) {
			player.trashCard(CardType.COPPER, CardSink.HAND);
			player.insertCardDirectlyIntoHand(new Card(CardType.SILVER));
			game.drawCard(CardType.SILVER);
			player.log("Trashes COPPER to gain SILVER");
			return emptyList;
		}
		return emptyList;
	}

}

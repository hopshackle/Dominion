package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.simulation.*;

import java.util.*;

public class Mine extends Card {
	
	public Mine() {
		super(CardType.MINE);
	}

	public List<ActionEnum<Player>> takeAction(Player player) {		
		super.takeAction(player);
		List<ActionEnum<Player>> retValue = new ArrayList<ActionEnum<Player>>();
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
		if (hasSilver && game.getNumberOfCardsRemaining(CardType.GOLD) > 0) {
			List<CardTypeAugment> mineSilver = new ArrayList<CardTypeAugment>();
			mineSilver.add(CardTypeAugment.moveCard(CardType.GOLD, CardSink.SUPPLY, CardSink.HAND));
			mineSilver.add(CardTypeAugment.trashCard(CardType.SILVER, CardSink.HAND));
			retValue.add(new CardTypeList(mineSilver, false));
		}
		if (hasCopper && game.getNumberOfCardsRemaining(CardType.SILVER) > 0) {
			List<CardTypeAugment> mineCopper = new ArrayList<CardTypeAugment>();
			mineCopper.add(CardTypeAugment.moveCard(CardType.SILVER, CardSink.SUPPLY, CardSink.HAND));
			mineCopper.add(CardTypeAugment.trashCard(CardType.COPPER, CardSink.HAND));
			retValue.add(new CardTypeList(mineCopper, false));
		}
		retValue.add(CardTypeAugment.takeCard(CardType.NONE));
		return retValue;
	}

}

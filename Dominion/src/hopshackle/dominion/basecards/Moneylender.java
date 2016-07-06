package hopshackle.dominion.basecards;

import hopshackle.dominion.Card;
import hopshackle.dominion.CardType;
import hopshackle.dominion.DominionBuyingDecision;
import hopshackle.dominion.Player;
import hopshackle.dominion.PositionSummary;

import java.util.List;

public class Moneylender extends Card {
	
	private int treasureValue = 0;

	public Moneylender() {
		super(CardType.MONEYLENDER);
	}

	@Override
	public void takeAction(Player player) {
		
		// ideally what I need to do is:
		//	1) determine purchase if I trash a copper - and value resultant Deck
		//  2) determine purchase if I do not trash a copper; and hence decide whether its advantageous or not
		// in other words a What If modelling similar to Remodel
		
		int treasure = 0;
		boolean hasCopper = false;
		for (CardType ct : player.getCopyOfHand()) {
			treasure += ct.getTreasure();
			if (ct == CardType.COPPER)
				hasCopper = true;
		}
		
		if (!hasCopper) 
			return;
		
		player.setState(Player.State.PURCHASING);
		
		double valueOfNotTrashing = value(player, treasure, null);
		PositionSummary ps = player.getPositionSummaryCopy();
		ps.removeCard(CardType.COPPER);
		double valueOfTrashing = value(player, treasure+2, ps);	// only +2 as we have to remove the Copper that was just trashed
		
		if (valueOfTrashing > valueOfNotTrashing) {
			player.trashCardFromHand(CardType.COPPER);
			player.log("Trashes COPPER");
			treasureValue += 3;		// to cope with Throne Rooms, which might trash a copper twice (or more)
		}
		
		player.setState(Player.State.PLAYING);
	}
	
	@Override
	public int getAdditionalPurchasePower() {
		return treasureValue;
	}
	
	@Override
	public void reset() {
		treasureValue = 0;
	}
	
	private double value(Player player, int treasure, PositionSummary ps) {
		if (ps == null)
			ps = player.getPositionSummaryCopy();
		DominionBuyingDecision decision = new DominionBuyingDecision(player, treasure, player.getBuys());
		decision.setPositionSummaryOverride(ps);
		List<CardType> purchase = decision.getBestPurchase();
		for (CardType cardBought : purchase) {
			ps.addCard(cardBought);
		}
		return player.getPurchaseDecider().value(ps);
	}
	
}

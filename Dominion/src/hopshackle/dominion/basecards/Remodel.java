package hopshackle.dominion.basecards;

import hopshackle.dominion.Card;
import hopshackle.dominion.CardType;
import hopshackle.dominion.DominionBuyingDecision;
import hopshackle.dominion.Player;
import hopshackle.dominion.PositionSummary;
import hopshackle.simulation.LookaheadDecider;

import java.util.*;

public class Remodel extends Card {

	public Remodel() {
		super(CardType.REMODEL);
	}

	public void takeAction(Player player) {
		super.takeAction(player);
		List<CardType> hand = player.getCopyOfHand();
		CardType[] cardsInHand = hand.toArray(new CardType[1]);
		if (cardsInHand[0] == null) return;
		CardType cardToRemodel = CardType.NONE;
		CardType cardToPurchase = CardType.NONE;
		LookaheadDecider<Player, PositionSummary> purchaseDecider = player.getPurchaseDecider();
		int treasureInHand = player.remainingTreasureValueOfHand();		// doesn't take account of unrevealed purchase power...but still better
		int buys = player.getBuys();
		DominionBuyingDecision nextBuy = new DominionBuyingDecision(player, treasureInHand, buys);
		List<CardType> purchaseBeforeRemodel = nextBuy.getBestPurchase();
		PositionSummary basePS = player.getPositionSummaryCopy();
		for (CardType ct : purchaseBeforeRemodel)
			basePS.addCard(ct);
		double startValue = purchaseDecider.value(basePS);

		player.setState(Player.State.PURCHASING);
		double bestValue = 0.0;
		Set<CardType> optionsAlreadyExamined = new HashSet<CardType>();
		for (int loop=0; loop<cardsInHand.length; loop++) {
			CardType possibleCardToRemodel = cardsInHand[loop];
			if (optionsAlreadyExamined.contains(possibleCardToRemodel)) continue;
			optionsAlreadyExamined.add(possibleCardToRemodel);
			PositionSummary withoutCard = player.getPositionSummaryCopy();
			withoutCard.removeCard(possibleCardToRemodel);
			int budget = possibleCardToRemodel.getCost() + 2;
			DominionBuyingDecision dpd = new DominionBuyingDecision(player, budget, 1);
			dpd.setPositionSummaryOverride(withoutCard);
			CardType replacementCard = dpd.getBestMandatoryPurchase().get(0);
			withoutCard.drawCard(replacementCard);

			// but we also need to take into account the upcoming purchase, as we don't want to trash a Treasure card if it stops us buying what we want immediately afterwards
			// if it is not a treasure card, then this should result in the same purchase as included in basePS above
			DominionBuyingDecision nextBuyWithoutRemodeledCard = new DominionBuyingDecision(player, treasureInHand - possibleCardToRemodel.getTreasure(), buys);
			List<CardType> purchaseAfterRemodel = nextBuyWithoutRemodeledCard.getBestPurchase();
			for (CardType ct : purchaseAfterRemodel)
				withoutCard.addCard(ct);

			double value = purchaseDecider.value(withoutCard) - startValue;
			if (value > bestValue) {
				bestValue = value;
				cardToRemodel = possibleCardToRemodel;
				cardToPurchase = replacementCard;
			}
		}

		if (cardToRemodel != CardType.NONE && cardToPurchase != CardType.NONE) {
			player.trashCardFromHand(cardToRemodel);
			player.takeCardFromSupplyIntoDiscard(cardToPurchase);
			player.log("Trashes a " + cardToRemodel + " for a " + cardToPurchase);
		}

		player.setState(Player.State.PLAYING);
	}

}

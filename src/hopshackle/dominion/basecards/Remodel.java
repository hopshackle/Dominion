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
		
		/*
		 * Now I want to:
		 * 	- Compile a list of all possible actions, which is simply every distinct card in hand, and every
		 *  card it could be remodelled to. 
		 *  - This does not currently specify an ActionEnum. I can have an ActionEnum that is a set of acquired cards,
		 *  but not one that is a combination of acquiring and losing. This is therefore what I need first.
		 *  - That would give the ActionEnum list that I simply feed to the Decider. 
		 *  - OK, it' slightly more complicated in that the Trashed card comes out of hand! This needs to be factored in.
		 *  - So in the ActionEnum that defines a change to the cards possessed, each element also needs to specify whether the
		 *  loss/gain occurs from/to Hand/Discard/Deck. I think I can skip Revealed cards as an option, as that occurs when
		 *  a card is played rather then acquired. (Although there is bound to be a card out there that affects this...)
		 */
		
		LookaheadDecider<Player> purchaseDecider = player.getLookaheadDecider();
		List<CardType> hand = player.getCopyOfHand();
		CardType[] cardsInHand = hand.toArray(new CardType[1]);
		if (cardsInHand[0] == null) return;
		CardType cardToRemodel = CardType.NONE;
		CardType cardToPurchase = CardType.NONE;

		int treasureInHand = player.getBudget();		// doesn't take account of unrevealed purchase power...but still better
		int buys = player.getBuys();
		DominionBuyingDecision nextBuy = new DominionBuyingDecision(player, treasureInHand, buys);
		List<CardType> purchaseBeforeRemodel = nextBuy.getBestPurchase();
		PositionSummary basePS = (PositionSummary) purchaseDecider.getCurrentState(player);
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
			PositionSummary withoutCard = (PositionSummary) purchaseDecider.getCurrentState(player);
			withoutCard.trashCard(possibleCardToRemodel);
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

package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.*;

public class DominionBuyingDecision {

	private Player player;
	private int totalBudget;
	private int totalBuys;
	private Map<CardType, Integer> limitedCards;
	private PositionSummary overridePS = null;
	private DominionStateFactory stateFactory;

	public DominionBuyingDecision(Player player, int budget, int buys) {
		if (buys > 3) buys = 3;		// for performance reasons to avoid combinatorial explosion
		this.player = player;
		totalBudget = budget;
		totalBuys = buys;
		stateFactory = new DominionStateFactory(player.getPurchaseDecider().getVariables());
		DominionGame game = player.getGame();
		limitedCards = new HashMap<CardType, Integer>();
		for (CardType card : game.availableCardsToPurchase()) {
			if (game.getNumberOfCardsRemaining(card) < buys)
				limitedCards.put(card, game.getNumberOfCardsRemaining(card));
		}
	}

	public void setPositionSummaryOverride(PositionSummary ps) {
		if (ps != null)
			overridePS = ps.clone();
		else
			overridePS = null;
	}

	public List<CardType> getBestPurchase() {
		List<List<CardType>> possiblePurchases = getPossibleBuys(totalBuys, 20, totalBudget);
		List<CardType> noPurchases = new ArrayList<CardType>();
		noPurchases.add(CardType.NONE);
		possiblePurchases.add(noPurchases);
		return chooseBestOption(possiblePurchases);
	}

	public List<CardType> getBestMandatoryPurchase() {
		// in this case, CardType.NONE is not permitted. We must buy something.
		List<List<CardType>> possiblePurchases = getPossibleBuys(totalBuys, 20, totalBudget);
		return chooseBestOption(possiblePurchases);
	}

	private List<CardType> chooseBestOption(List<List<CardType>> allOptions) {
		double bestValue = Double.NEGATIVE_INFINITY;
		List<CardType> bestPurchase = null;
		PositionSummary ps = null;
		if (overridePS == null) 
			ps = (PositionSummary) stateFactory.getCurrentState(player);
		else
			ps = overridePS.clone();

		for (List<CardType> purchase : allOptions) {
			for (CardType card : purchase) 
				ps.drawCard(card);
			LookaheadDecider<Player> decider = (LookaheadDecider<Player>) player.getPurchaseDecider();
			double value = decider.value(ps);
			if (value > bestValue) {
				bestValue = value;
				bestPurchase = purchase;
			}
			for (CardType card : purchase) 
				ps.undrawCard(card);
		}
		return bestPurchase;
	}

	private boolean breaksCardLimit(List<CardType> purchase) {
		for (CardType limitedCard : limitedCards.keySet()) {
			int total = 0;
			for (CardType c : purchase)
				if (c == limitedCard)
					total++;
			if (total > limitedCards.get(limitedCard))
				return true;
		}
		return false;
	}

	private List<List<CardType>> getPossibleBuys(int remainingBuys, int maxValueCard, int budget) {
		List<List<CardType>> retValue = new ArrayList<List<CardType>>();
		if (remainingBuys == 0)
			return retValue;
		List<CardType> purchasableCards = getPurchasableCards(Math.min(maxValueCard, budget));
		for (CardType A : purchasableCards) {
			List<List<CardType>> B = getPossibleBuys(remainingBuys-1, A.getCost(), budget - A.getCost());
			for (List<CardType> subList : B) {
				subList.add(A);
				if (!breaksCardLimit(subList))
					retValue.add(subList);
			}
			List<CardType> justA = new ArrayList<CardType>();
			justA.add(A);
			retValue.add(justA);
		}
		return retValue;
	}

	private List<CardType> getPurchasableCards(int maxValue) {
		List<CardType> retValue = new ArrayList<CardType>();
		Set<CardType> cardsPurchasable = player.getGame().availableCardsToPurchase();
		for (CardType ct : cardsPurchasable) {
			if (ct.getCost() <= maxValue)
				retValue.add(ct);
		}
		retValue.remove(CardType.NONE);
		return retValue;
	}

	public List<ActionEnum<Player>> getPossiblePurchasesAsActionEnum() {
		List<List<CardType>> temp = getPossibleBuys(totalBuys, 20, totalBudget);
		List<ActionEnum<Player>> retValue = new ArrayList<ActionEnum<Player>>();
		for (final List<CardType> purc : temp) {
			retValue.add(new CardTypeList(purc));
		}
		List<CardType> noPurchases = new ArrayList<CardType>();
		noPurchases.add(CardType.NONE);
		retValue.add(new CardTypeList(noPurchases));
		return retValue;
	}
}

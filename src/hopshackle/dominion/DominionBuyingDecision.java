package hopshackle.dominion;

import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.*;

import java.util.*;

public class DominionBuyingDecision {

	private Player player;
	private int totalBudget;
	private int totalBuys;
	private Map<CardType, Integer> limitedCards;

	public DominionBuyingDecision(Player player, int budget, int buys) {
		if (buys > 3) buys = 3;		// for performance reasons to avoid combinatorial explosion
		this.player = player;
		totalBudget = budget;
		totalBuys = buys;
		DominionGame game = player.getGame();
		limitedCards = new HashMap<CardType, Integer>();
		for (CardType card : game.availableCardsToPurchase()) {
			if (game.getNumberOfCardsRemaining(card) < buys)
				limitedCards.put(card, game.getNumberOfCardsRemaining(card));
		}
	}

	public DominionAction getBestPurchase() {
		List<List<CardType>> possiblePurchases = getPossibleBuys(totalBuys, 20, totalBudget);
		List<CardType> noPurchases = new ArrayList<CardType>();
		noPurchases.add(CardType.NONE);
		possiblePurchases.add(noPurchases);
		return chooseBestOption(possiblePurchases);
	}

	public DominionAction getBestMandatoryPurchase() {
		// in this case, CardType.NONE is not permitted. We must buy something.
		List<List<CardType>> possiblePurchases = getPossibleBuys(totalBuys, 20, totalBudget);
		return chooseBestOption(possiblePurchases);
	}

	private DominionAction chooseBestOption(List<List<CardType>> optionList) {
		List<ActionEnum<Player>> allOptions = new ArrayList<ActionEnum<Player>>();
		for (List<CardType> option : optionList) {
			allOptions.add(convertCardListToActionEnum(option));
		}
		return (DominionAction) player.getDecider().decide(player, allOptions);
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
		// this excludes any cards with higher values than those purchased so far
		// but includes cards with the same value (even if already purchased)

		for (CardType A : purchasableCards) {
			List<List<CardType>> B = getPossibleBuys(remainingBuys-1, A.getCost(), budget - A.getCost());
			for (List<CardType> subList : B) {
				int initialIndex = purchasableCards.indexOf(A);
				// only consider cards on or after this index if identical cost
				boolean skip = false;
				for (CardType ct : subList) {
					if (ct.getCost() == A.getCost() && purchasableCards.indexOf(ct) < initialIndex) {
						skip = true;
						break;
					}
				}
				if (!skip) {
					subList.add(A);
					if (!breaksCardLimit(subList))
						retValue.add(subList);
				}
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
		List<List<CardType>> temp = getPossibleBuys(totalBuys, totalBudget, totalBudget);
		List<ActionEnum<Player>> retValue = new ArrayList<ActionEnum<Player>>();
		for (final List<CardType> purc : temp) {
			retValue.add(convertCardListToActionEnum(purc));
		}
		retValue.add(CardTypeAugment.takeCard(CardType.NONE));
		return retValue;
	}
	
	private ActionEnum<Player> convertCardListToActionEnum(List<CardType> cardList) {
		if (cardList.size() == 1) {
			return CardTypeAugment.takeCard(cardList.get(0));
		} else {
			return new CardTypeList(cardList);
		}
	}
}

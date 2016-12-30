package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
import hopshackle.simulation.*;

import java.util.*;

public class Cellar extends Card {

	private Player player;
	private int startingHandSize;

	public Cellar() {
		super(CardType.CELLAR);
	}

	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		/*
		 * We can discard any of the cards in hand. So we could represent this as 2^4 = 16 possibilities
		 * for a hand of 5 cards (1 being CELLAR). Or, we could treat this as 4 separate decisions, each with just 2 possibilities.
		 * 
		 * Then, we need to track the number that were discarded, and draw this number as the follow-up action.
		 */
		this.player = player;
		this.startingHandSize = player.getHandSize();
		List<List<CardType>> possibleDiscards = getPossibleDiscards();
		List<ActionEnum<Player>> possibleActions = convertToActionEnum(possibleDiscards);
		
		return possibleActions;
	}
	
	@Override
	public DominionAction followUpAction() {
		DominionAction retValue = new CellarFollowOnAction(player, startingHandSize);
		return retValue;
	}

	private List<List<CardType>> getPossibleDiscards() {
		Map<CardType, Integer> allCardTypesInHand = new HashMap<CardType, Integer>();
		for (CardType ct : player.getCopyOfHand()) {
			if (allCardTypesInHand.containsKey(ct)) {
				allCardTypesInHand.put(ct, allCardTypesInHand.get(ct) + 1);
			} else {
				allCardTypesInHand.put(ct, 1);
			}
		}

		List<List<CardType>> retValue = new ArrayList<List<CardType>>();

		for (CardType ct : allCardTypesInHand.keySet()) {
			List<List<CardType>> newDiscardStems = new ArrayList<List<CardType>>();
			for (int i = 1; i <= allCardTypesInHand.get(ct); i++) {
				for (List<CardType> discardStem : retValue) {
					List<CardType> newDiscardStem = HopshackleUtilities.cloneList(discardStem);
					for (int j = 0; j < i; j++) {
						newDiscardStem.add(ct);
					}
					newDiscardStems.add(newDiscardStem);
				}
				List<CardType> newDiscardStem = new ArrayList<CardType>();
				for (int j = 0; j < i; j++) {
					newDiscardStem.add(ct);
				}
				newDiscardStems.add(newDiscardStem);
			}
			retValue.addAll(newDiscardStems);
		}
		List<CardType> noAction = new ArrayList<CardType>();
		retValue.add(noAction);
		return retValue;
	}
	private List<ActionEnum<Player>> convertToActionEnum(List<List<CardType>> possibleDiscards) {
		List<ActionEnum<Player>> retValue = new ArrayList<ActionEnum<Player>>(possibleDiscards.size());
		for (List<CardType> discard : possibleDiscards) {
			retValue.add(convertCardListToActionEnum(discard));
		}
		return retValue;
	}
	private ActionEnum<Player> convertCardListToActionEnum(List<CardType> cardList) {
		List<CardTypeAugment> asCTA = new ArrayList<CardTypeAugment>(cardList.size());
		if (cardList.isEmpty()) {
			asCTA.add(CardTypeAugment.discardCard(CardType.NONE));
		}
		for (CardType ct : cardList) {
			asCTA.add(CardTypeAugment.discardCard(ct));
		}
		return new CardTypeList(asCTA, false);
	}
}


class CellarFollowOnAction extends DominionAction {

	private int startingHandSize = 0;
	
	public CellarFollowOnAction(Player player, int startingHand) {
		super(player, new CardTypeList(new ArrayList<CardType>()));
		startingHandSize = startingHand;
	}
	
	@Override 
	public CellarFollowOnAction clone(Player newPlayer) {
		return new CellarFollowOnAction(newPlayer, this.startingHandSize);
	}
	
	@Override
	public void doStuff() {
		int cardsToDraw = startingHandSize - player.getHandSize();
		for (int i = 0; i < cardsToDraw; i++) {
			player.drawTopCardFromDeckIntoHand();
		}
	}
	
	@Override
	public ActionEnum<Player> getType() {
		List<CardTypeAugment> drawnCards = new ArrayList<CardTypeAugment>();
		for (int i = 0; i < startingHandSize - player.getHandSize(); i++) {
			drawnCards.add(CardTypeAugment.drawCard());
		}
		return new CardTypeList(drawnCards, false);
	}
}

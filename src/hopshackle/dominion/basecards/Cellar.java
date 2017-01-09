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
		List<List<CardType>> possibleDiscards = player.getPossibleDiscardsFromHand(0, startingHandSize);
		List<ActionEnum<Player>> possibleActions = CardType.listToActionEnumList(possibleDiscards);
		
		return possibleActions;
	}
	
	@Override
	public DominionAction followUpAction() {
		DominionAction retValue = new CellarFollowOnAction(player, startingHandSize);
		return retValue;
	}
}

class CellarFollowOnAction extends DominionAction {

	private int startingHandSize = 0;
	
	public CellarFollowOnAction(Player player, int startingHand) {
		super(player, new CardTypeList(new ArrayList<CardType>()));
		startingHandSize = startingHand;
		hasNoAssociatedDecision = true;
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
	public String toString() {
		return "Follow-on CELLAR";
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

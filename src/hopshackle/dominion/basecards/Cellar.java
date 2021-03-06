package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.simulation.*;

import java.util.*;

public class Cellar extends Card {

	boolean inPlay;
	private Player player;

	public Cellar() {
		super(CardType.CELLAR);
	}

	public List<ActionEnum<Player>> takeAction(Player player) {
		if (!inPlay) {
		    player.oneOffBudget(0); // in case this still has a value from a previous REMODEL or something
		    super.takeAction(player);		// just once; else we add an action for each discarded card
			inPlay = true;
        }
		/*
			We discard one card at a time
			DominionAction will track how many have been discarded using CURRENT_FEATURE
		 */
		this.player = player;
		List<List<CardType>> possibleDiscards = player.getPossibleDiscardsFromHand(0, 1);
		List<ActionEnum<Player>> retValue = new ArrayList<ActionEnum<Player>>(possibleDiscards.size());

		for (List<CardType> discard : possibleDiscards) {
			retValue.add(new CardTypeAugment(discard.get(0), CardSink.HAND, CardSink.DISCARD, CardTypeAugment.ChangeType.CELLAR));
		}
		return retValue;
	}
	
	@Override
	public DominionAction followUpAction() {
		DominionAction retValue = new CellarFollowOnAction(player);
		return retValue;
	}
	@Override
	public void reset() {
		inPlay = false;
		player = null;
	}

	@Override
	public Cellar clone(DominionGame newGame) {
		Cellar retValue = (Cellar) super.clone(newGame);
		retValue.inPlay = inPlay;
		return retValue;
	}
}

class CellarFollowOnAction extends DominionAction {

	public CellarFollowOnAction(Player player) {
		super(player, new CardTypeList(new ArrayList<CardType>(), CardTypeAugment.ChangeType.BUY));
		hasNoAssociatedDecision = true;
	}

	public CellarFollowOnAction(CellarFollowOnAction master, Player newPlayer) {
		super(master, newPlayer);
	}

	@Override 
	public CellarFollowOnAction clone(Player newPlayer) {
		return new CellarFollowOnAction(this, newPlayer);
	}
	
	@Override
	public void doStuff() {
		int cardsToDraw = player.getOneOffBudget();
		for (int i = 0; i < cardsToDraw; i++) {
			player.drawTopCardFromDeckInto(CardSink.HAND);
		}
		player.oneOffBudget(0);		// and reset
	}
	
	@Override
	public String toString() {
		return "Follow-on CELLAR";
	}
	
	@Override
	public ActionEnum<Player> getType() {
		List<CardTypeAugment> drawnCards = new ArrayList<CardTypeAugment>();
		for (int i = 0; i < player.getOneOffBudget(); i++) {
			drawnCards.add(CardTypeAugment.drawCard());
		}
		return new CardTypeList(drawnCards, false);
	}
}

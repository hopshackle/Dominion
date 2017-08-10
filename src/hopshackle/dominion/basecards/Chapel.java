package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.*;
import hopshackle.simulation.Action;
import hopshackle.simulation.ActionEnum;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Chapel extends Card {

	private static AtomicLong idFountain = new AtomicLong(1);
	private boolean firstDecision = true;
	private int trashedSoFar;
	private int player;
	private DominionGame game;

	public Chapel() {
		super(CardType.CHAPEL);
	}

	@Override
	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		this.player = player.getActorRef();
		this.game = player.getGame();
		this.setRef("CHAPEL" + idFountain.getAndIncrement());
		// We can trash any one card from hand, or NONE
		// We can do this a total of four times
		// So the follow-on action can simply be a brand new action, referencing the same card
		// with a counter to make sure we only do this 4 times

		List<ActionEnum<Player>> retValue = trashOptions();
		firstDecision = false;
		return retValue;
	}

	protected List<ActionEnum<Player>> trashOptions() {
		if (trashedSoFar > 3 || lastTrashWasNONE())
			return emptyList;

		List<CardType> hand = new ArrayList<CardType>();
		for (CardType c : game.getPlayer(player).getCopyOfHand()) {
			if (!hand.contains(c))
				hand.add(c);
		}

		List<ActionEnum<Player>> retValue = new ArrayList<>();
		retValue.add(CardTypeAugment.trashCard(CardType.NONE, CardSink.HAND));

		for (CardType card : hand) {
			retValue.add(CardTypeAugment.trashCard(card, CardSink.HAND));
		}
		trashedSoFar++;

		return retValue;
	}

	private boolean lastTrashWasNONE() {
		// slightly awkward - but this avoids us having to put any Chapel-specific logic
		// in DominionAction, and keeps all the Chapel logic in the one class
		if (firstDecision) return false; // for the very first decision, we know that no cards have been trashed
										// This avoids us being misled if, say, the last card played was a Moneylender
										// which trashed a COPPER as the most recent action
		List<Action<?>> executedActions = game.getPlayer(player).getExecutedActions();
		if (executedActions.isEmpty()) return false;
		DominionAction lastAction = (DominionAction) executedActions.get(executedActions.size()-1);
		if (lastAction.getType() instanceof CardTypeList) return false;
		CardTypeAugment cta = (CardTypeAugment) lastAction.getType();
		return (cta.card == CardType.NONE && cta.to == CardSink.TRASH);
	}


	@Override
	public DominionAction followUpAction() {
		DominionAction retValue = new ChapelFollowOnAction(game.getPlayer(player),this);
		return retValue;
	}
	@Override
	public void reset() {
		trashedSoFar = 0;
		firstDecision = true;
		player = 0;
		game = null;
		this.setRef("");
	}

	@Override
	public Chapel clone(DominionGame newGame) {
		Chapel retValue = (Chapel) super.clone(newGame);
		retValue.player = player;
		retValue.game = newGame;
		retValue.trashedSoFar = trashedSoFar;
		retValue.firstDecision = firstDecision;
		return retValue;
	}

}

class ChapelFollowOnAction extends DominionAction {

	private Chapel chapel;

	public ChapelFollowOnAction(Player player, Chapel card) {
		super(player, new CardTypeList(new ArrayList<CardType>(), ChangeType.BUY));
		chapel = card;
	}

	public ChapelFollowOnAction (ChapelFollowOnAction master, Player newPlayer) {
		super(master, newPlayer);
		List<Card> newCards = newPlayer.getCardsWithRef(master.chapel.getRef());
		if (newCards.size() != 1)
			throw new AssertionError("Expect exactly one card with reference of " + chapel.getRef() + " and found " + newCards.size());
		chapel = (Chapel) newCards.get(0);
	}

	@Override
	public ChapelFollowOnAction clone(Player newPlayer) {
		return new ChapelFollowOnAction(this, newPlayer);
	}

	@Override
	public void doStuff() {
		possibleOptions = chapel.trashOptions();
		followUpAction = new ChapelFollowOnAction(player, chapel);
	}

	@Override
	public String toString() {
		return "Follow-on CHAPEL";
	}

	@Override
	public ActionEnum<Player> getType() {
		return new CardTypeAugment(CardType.CHAPEL, CardSink.HAND, CardSink.HAND, ChangeType.NOCHANGE);
	}
}


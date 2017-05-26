package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.*;
import hopshackle.simulation.Action;
import hopshackle.simulation.ActionEnum;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Chapel extends Card {

	private static AtomicLong idFountain = new AtomicLong(1);

	private int trashedSoFar;
	private Player player;

	public Chapel() {
		super(CardType.CHAPEL);
	}

	@Override
	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		this.player = player;
		this.setRef("CHAPEL" + idFountain.getAndIncrement());
		// We can trash any one card from hand, or NONE
		// We can do this a total of four times
		// So the follow-on action can simply be a brand new action, referencing the same card
		// with a counter to make sure we only do this 4 times

		return trashOptions();
	}

	protected List<ActionEnum<Player>> trashOptions() {
		if (trashedSoFar > 3 || lastTrashWasNONE())
			return emptyList;

		List<CardType> hand = new ArrayList<CardType>();
		for (CardType c : player.getCopyOfHand()) {
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
		List<Action<?>> executedActions = player.getExecutedActions();
		if (executedActions.isEmpty()) return false;
		DominionAction lastAction = (DominionAction) executedActions.get(executedActions.size()-1);
		CardTypeAugment cta = (CardTypeAugment) lastAction.getType();
		return cta.card == CardType.NONE;
	}


	@Override
	public DominionAction followUpAction() {
		DominionAction retValue = new ChapelFollowOnAction(player,this);
		return retValue;
	}
	@Override
	public void reset() {
		trashedSoFar = 0;
		player = null;
		this.setRef("");
	}

	@Override
	public Chapel clone(DominionGame newGame) {
		Chapel retValue = (Chapel) super.clone(newGame);
		if (player != null) retValue.player = newGame.getPlayer(player.getNumber());
		retValue.trashedSoFar = trashedSoFar;
		return retValue;
	}

}

class ChapelFollowOnAction extends DominionAction {

	private Chapel chapel;

	public ChapelFollowOnAction(Player player, Chapel card) {
		super(player, new CardTypeList(new ArrayList<CardType>()));
		chapel = card;
	}

	@Override
	public ChapelFollowOnAction clone(Player newPlayer) {
		List<Card> newCards = newPlayer.getCardsWithRef(chapel.getRef());
		if (newCards.size() != 1)
			throw new AssertionError("Expect exactly one card with reference of " + chapel.getRef() + " and found " + newCards.size());
		return new ChapelFollowOnAction(newPlayer, (Chapel) newCards.get(0));
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


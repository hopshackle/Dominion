package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.*;
import hopshackle.simulation.*;

import java.util.*;

public class Remodel extends Card {

	private int player;
	private DominionGame game;

	public Remodel() {
		super(CardType.REMODEL);
	}

	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		this.player = player.getNumber();
		game = player.getGame();
		List<CardType> hand = new ArrayList<CardType>();
		for (CardType card : player.getCopyOfHand()) {
			if (!hand.contains(card))
				hand.add(card);
		}

		/* We split this into two decisions:
			i) The card to Trash (here)
			ii) The card to Gain (the follow-on action)

			We need to provide a list of possibleActions (returned by takeActions())
			and a follow-on action to be executed next (from followUpAction()

			At no point are we informed of which decision was taken. So the follow-on action needs to
			work this out itself.

			We also need to provide clone() support for this and the follow-on action.
			Possibly using the reference facility - as THRONE_ROOM does.
			In this case, we get away without it, as we look at the last executed action, and the card trashed
		 */

		List<ActionEnum<Player>> allOptions = new ArrayList<ActionEnum<Player>>();
		for (CardType cardToTrash : hand) {
			allOptions.add(new CardTypeAugment(cardToTrash, CardSink.HAND, CardSink.TRASH, ChangeType.REMODEL));
		}
		return allOptions;
	}

	@Override
	public DominionAction followUpAction() {
		DominionAction retValue = new RemodelFollowOnAction(game.getPlayer(player));
		return retValue;
	}

	@Override
	public Remodel clone(DominionGame newGame) {
		Remodel retValue = (Remodel) super.clone(newGame);
		retValue.player = player;
		retValue.game = newGame;
		return retValue;
	}
}

class RemodelFollowOnAction extends DominionAction {

	public RemodelFollowOnAction(Player player) {
		super(player, new CardTypeList(new ArrayList<CardType>()));
	}

	public RemodelFollowOnAction(RemodelFollowOnAction master, Player newPlayer) {
		super(master, newPlayer);
	}

	@Override
	public RemodelFollowOnAction clone(Player newPlayer) {
		return new RemodelFollowOnAction(this, newPlayer);
	}

	@Override
	public void doStuff() {
		List<Action<Player>> previousActions = HopshackleUtilities.convertList(player.getExecutedActions());
		CardTypeAugment lastAction = (CardTypeAugment) previousActions.get(previousActions.size()-1).getType();
		if (lastAction.to != CardSink.TRASH || lastAction.from != CardSink.HAND)
			throw new AssertionError("Last action needs to be to Trash from Hand " + lastAction);
		int valueOfTrashedCard = lastAction.card.getCost();
		int budget = valueOfTrashedCard + 2;

		List<CardType> targets = new ArrayList<CardType>();
		for (CardType t : player.getGame().availableCardsToPurchase()) {
			if (t != CardType.NONE && t.getCost() <= budget)
				targets.add(t);
		}
		possibleOptions = new ArrayList<ActionEnum<Player>>();
		for (CardType cardToAcquire : targets) {
			CardTypeAugment gainAction = CardTypeAugment.takeCard(cardToAcquire);
			possibleOptions.add(gainAction);
		}
	}

	@Override
	public String toString() {
		return "Follow-on REMODEL";
	}

	@Override
	public ActionEnum<Player> getType() {
		return CardTypeAugment.drawCard();
		// well, it's as close as we can get before we make the decision as to which card
	}
}

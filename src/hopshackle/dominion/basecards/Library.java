package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.*;
import hopshackle.simulation.ActionEnum;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Library extends Card {

	private static AtomicLong idFountain = new AtomicLong(1);
	CardType currentActionCard;
	int playerNumber;
	DominionGame game;

	public Library() {
		super(CardType.LIBRARY);
	}

	/*
	We set a reference on the Library card when we take an action. This enables the Follow-On action
	to reliably find the card (or rather, for a clone of the Follow-On action to do so).
	With this card, the Follow-On action is responsible for coming back to the masterCard, and effectively calling
	its takeActions() method again, until we reach the end condition.
	Contrast this with the approach in CELLAR, where we introduce a new ChangeType.CELLAR, and then rely on
	DominionAction as the parent class to keep calling takeActions() until the end condition is reached. The Follow-On
	action is then used to draw up the number of cards discarded.
	REMODEL then works in a similar way to CELLAR, with a reliance on DominionAction to be aware of the card logic,
	via a new ChangeType.
	One difference with LIBRARY, is that we do not make use of CURRENT_FEATURE at all to track the status. Although
	I am not convinced this is important enough a distinction to warrant the very different design.
	 */
	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		setRef("LIB" + idFountain.getAndIncrement());
		playerNumber = player.getNumber();
		game = player.getGame();
		if (player.getHandSize() > 6) return emptyList;

		return drawToLimit();
	}
	protected List<ActionEnum<Player>> drawToLimit() {
		Player player = game.getPlayer(playerNumber);
		do {
			CardType nextCard = player.peekAtTopCardOfDeck();
			if (nextCard == CardType.NONE) {
				player.log("Has no more cards in deck or discard. So stops.");
				break;
			}
			if (nextCard.isAction()) {
				currentActionCard = nextCard;
				List<ActionEnum<Player>> retValue = new ArrayList<ActionEnum<Player>>();
				retValue.add(new CardTypeAugment(nextCard, CardSink.DECK, CardSink.HAND, ChangeType.MOVE));
				retValue.add(new CardTypeAugment(nextCard, CardSink.DECK, CardSink.DISCARD, ChangeType.MOVE));
				return retValue;
			} else {
				currentActionCard = null;
				player.drawTopCardFromDeckInto(CardSink.HAND);
			}
		} while (player.getHandSize() < 7);
		return emptyList;
	}

	@Override
	public DominionAction followUpAction() {
		DominionAction retValue = new LibraryFollowOnAction(this);
		return retValue;
	}
	@Override
	public Library clone(DominionGame newGame) {
		Library retValue = (Library) super.clone(newGame);
		retValue.game = newGame;
		retValue.playerNumber = playerNumber;
		retValue.currentActionCard = currentActionCard;
		return retValue;
	}
	@Override
	public void reset() {
		currentActionCard = null;
		playerNumber = 0;
		game = null;
		setRef("");
	}
}

class LibraryFollowOnAction extends DominionAction {

	private Library masterCard;

	public LibraryFollowOnAction(Library parent) {
		super(parent.game.getPlayer(parent.playerNumber), new CardTypeList(new ArrayList<CardType>()));
		masterCard = parent;
	}
	@Override 
	public LibraryFollowOnAction clone(Player newPlayer) {
		Library newMaster = (Library) newPlayer.getCardsWithRef(masterCard.getRef()).get(0);
		LibraryFollowOnAction retValue = new LibraryFollowOnAction(newMaster);
		retValue.possibleOptions = possibleOptions;
		if (retValue.followUpAction != null) 
			retValue.followUpAction = followUpAction.clone(newPlayer);
		return retValue;
	}

	@Override
	public void doStuff() {
		possibleOptions = masterCard.drawToLimit();
		if (possibleOptions.isEmpty()) {
			followUpAction = null;
		} else {
			followUpAction = new LibraryFollowOnAction(masterCard);
		}
	}

	@Override
	public String toString() {
		return "Follow-on LIBRARY";
	}

	@Override
	public ActionEnum<Player> getType() {
		return CardTypeAugment.drawCard();
	}
}

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
		return CardTypeAugment.playCard(CardType.LIBRARY);
	}
}

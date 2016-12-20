package hopshackle.dominion;

import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.*;

import java.util.*;

public class Player extends Agent {

	public enum State {
		PRE_PLAY, PLAYING, PRE_PURCHASE, PURCHASING;
	}

	private State playerState;
	private Deck hand;
	private Deck deck;
	private Deck discard;
	private Deck revealedCards;
	private DominionGame game;
	private PositionSummary summary;
	private int playerNumber;
	private int actionsLeft;
	private boolean onlyRewardVictory = SimProperties.getProperty("DominionOnlyRewardVictory", "false").equals("true");

	public Player(DominionGame game, int number) {
		super(game.getWorld());
		playerNumber = number;
		this.game = game;
		deck = new Deck();
		discard = new Deck();
		hand = new Deck();
		revealedCards = new Deck();
		actionsLeft = 1;
		setState(State.PRE_PLAY);
		dealFreshHand();
		log("Player #" + number + " in Game " + game.getUniqueId());
	}

	public Player(Player player, DominionGame newGame) {
		super(newGame.getWorld());
		playerState = player.playerState;
		playerNumber = player.playerNumber;
		actionsLeft = player.actionsLeft;
		game = newGame;
		// Responsibility for taking into account the information set resides in the caller
		// as that has the information from whose perspective the clone is taking place
		deck = player.deck.copy();
		discard = player.discard.copy();
		hand = player.hand.copy();
		revealedCards = player.revealedCards.copy();
		decider = player.decider;
		if (player.getNextAction() != null) {
			DominionAction da = (DominionAction) player.getNextAction();
			this.actionPlan.addAction(da.clone(this));
		}
	}

	private void dealFreshHand() {
		for (int n = 0; n < 7; n++) 
			deck.addCard(new Card(CardType.COPPER));	
		for (int n = 0; n < 3; n++)
			deck.addCard(new Card(CardType.ESTATE));

		deck.shuffle();
		tidyUp();
	}

	public int getHandSize() {
		return hand.getSize();
	}
	public int getDeckSize() {
		return deck.getSize();
	}
	public int getDiscardSize() {
		return discard.getSize();
	}

	public int getNumberOfTypeInHand(CardType type) {
		return hand.getNumberOfType(type);
	}

	public int getNumberOfTypePlayedSoFar(CardType type) {
		return revealedCards.getNumberOfType(type);
	}

	public int getNumberOfTypeInDeck(CardType type) {
		return deck.getNumberOfType(type);
	}

	public int getNumberOfTypeTotal(CardType type) {
		return deck.getNumberOfType(type) + discard.getNumberOfType(type) + hand.getNumberOfType(type) + revealedCards.getNumberOfType(type);
	}


	public double getScore() {
		double retValue = totalVictoryValue();
		int[] winningPlayers = game.getWinningPlayers();
		if (onlyRewardVictory){
			retValue = 0.0;
			for (int p : winningPlayers) {
				if (p == playerNumber) 
					retValue = 100.0 / (double)winningPlayers.length;
			}
		} else {
			for (int p : winningPlayers) {
				if (p == playerNumber) retValue += 50;
			}
		}

		return retValue;
	}

	public void buyCards() {
		if (playerState != State.PURCHASING && playerState != State.PRE_PURCHASE) 
			throw new AssertionError("Incorrect state for Purchasing " + playerState);
		setState(State.PURCHASING);
		refreshPositionSummary();
		String buys = " buys";
		if (getBuys() == 1) buys = " buy";
		log("Has budget of " + getBudget() + " with " + getBuys() + buys);
		DominionAction decision = (DominionAction) getDecider().decide(this);
		decision.start();
		decision.run();
		setState(State.PRE_PLAY);
	}

	public void takeActions() {
		if (playerState != State.PLAYING && playerState != State.PRE_PLAY) 
			throw new AssertionError("Incorrect state for taking actions " + playerState);
		if (playerState == State.PRE_PLAY)
			actionsLeft = 1;
		setState(State.PLAYING);
	
		while (actionsLeft > 0) {
			refreshPositionSummary();
			Action<Player> action = getDecider().decide(this);
			action.start();
			action.run();
			decrementActionsLeft();
		}
		setState(State.PRE_PURCHASE);
	}
	public void incrementActionsLeft() {
		actionsLeft++;
	}
	public void decrementActionsLeft() {
		actionsLeft--;
	}

	public void tidyUp() {
		revealedCards.reset();
		discard.addDeck(revealedCards);
		discard.addDeck(hand);
		revealedCards = new Deck();
		hand = new Deck();
		log("Ends turn and discards remainder of hand.");
		for (int n = hand.getSize(); n < 5; n++) {
			if (deck.isEmpty()) {
				if (discard.isEmpty()) break;
				shuffleDiscardToFormNewDeck();
			}
			drawTopCardFromDeckIntoHand();
		}
		refreshPositionSummary();
	}

	public void shuffleDeckAndHandTogether() {
		// for use when cloning for MCTS
		int handSize = hand.getSize();
		deck.addDeck(hand);
		deck.shuffle();
		hand = new Deck();
		for (int i = 0; i < handSize; i++)
			hand.addCard(deck.drawTopCard());
		refreshPositionSummary();
	}

	public void shuffleDeck() {
		deck.shuffle();
	}

	private void shuffleDiscardToFormNewDeck() {
		log("Shuffles discard to form new deck");
		deck = discard;
		deck.shuffle();
		summary.discardCard(-discard.getSize());
		discard = new Deck();
	}

	@Override
	public double getMaxScore() {
		return 100.0;
	}

	public DominionGame getGame() {
		return game;
	}

	public Card drawTopCardFromDeckIntoHand() {
		Card cardDrawn = drawTopCardFromDeckButNotIntoHand();
		if (cardDrawn.getType() != CardType.NONE) {
			hand.addCard(cardDrawn);
			summary.updateHandFromPlayer();
		}
		return cardDrawn;
	}

	public Card drawTopCardFromDeckButNotIntoHand() {
		if (deck.isEmpty())
			shuffleDiscardToFormNewDeck();
		if (!deck.isEmpty()) {
			Card cardDrawn = deck.drawTopCard();
			log("Draws a " + cardDrawn);
			return cardDrawn;
		}
		return new Card(CardType.NONE);
	}

	public void takeCardFromSupply(CardType card, CardSink dest) {
		Deck destination = discard;
		if (dest == CardSink.HAND) destination = hand;
		if (dest == CardSink.REVEALED) destination = revealedCards;
		if (dest == CardSink.DECK) destination = deck;
		if (card.equals(CardType.NONE)) return;
		if (game.drawCard(card)) {
			destination.addCard(CardFactory.instantiateCard(card));
			summary.drawCard(card, dest);
		} else {
			throw new AssertionError("Card Type " + card + " not available." );
		}
	}

	public void insertCardDirectlyIntoHand(Card c) {
		hand.addCard(c);
		refreshPositionSummary();
	}

	public int totalTreasureValue() {
		return hand.getTreasureValue() + deck.getTreasureValue() + discard.getTreasureValue() + revealedCards.getTreasureValue();
	}

	public int totalVictoryValue() {
		return hand.getVictoryPoints(summary) + deck.getVictoryPoints(summary) + discard.getVictoryPoints(summary) + revealedCards.getVictoryPoints(summary);
	}

	public String toString() {
		return "Player " + playerNumber + " (" + getUniqueID() + ")";
	}

	public int totalNumberOfCards() {
		return hand.getSize() + deck.getSize() + discard.getSize() + revealedCards.getSize();
	}

	public List<CardType> getAllCards() {
		List<CardType> retValue = new ArrayList<CardType>();
		retValue.addAll(hand.getAllCards());
		retValue.addAll(deck.getAllCards());
		retValue.addAll(discard.getAllCards());
		retValue.addAll(revealedCards.getAllCards());
		return retValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Decider<Player> getDecider() {
		return decider;
	}

	public PositionSummary getPositionSummaryCopy() {
		return summary.clone();
	}
	public List<CardType> getCopyOfHand() {
		return hand.getAllCards();
	}
	public List<CardType> getCopyOfDiscard() {
		return discard.getAllCards();
	}
	public List<CardType> getCopyOfDeck() {
		return deck.getAllCards();
	}
	public List<CardType> getCopyOfPlayedCards() {
		return revealedCards.getAllCards();
	}

	public boolean discard(CardType cardTypeToDiscard) {
		Card discarded = hand.removeSpecificCard(cardTypeToDiscard);
		if (discarded.getType() != CardType.NONE) {
			summary.updateHandFromPlayer();
			putCardOnDiscard(discarded);
			return true;
		}
		return false;
	}

	public void trashCard(CardType card, CardSink dest) {
		switch (dest) {
		case HAND:
			hand.removeSpecificCard(card);
			break;
		case REVEALED:
			revealedCards.removeSpecificCard(card);
			break;
		case DISCARD:
			discard.removeSpecificCard(card);
			break;
		case DECK:
			deck.removeSpecificCard(card);
			break;
		}
		summary.trashCard(card, dest);
	}
	
	/**
	 * Indicates whether we are taking Actions (true), or making a purchase (false).
	 * This is a bit of a hack to cater for Purchase decisions made during the
	 * Action Phase. 
	 * If it is not the player's turn, then the default is false.
	 */
	public boolean isTakingActions() {
		return playerState == State.PLAYING || playerState == State.PRE_PLAY;
	}

	public void setState(Player.State newState) {
		playerState = newState;
		refreshPositionSummary();
	}
	public Player.State getPlayerState() {
		return playerState;
	}

	public void putCardFromHandOnTopOfDeck(CardType cardType) {
		hand.removeSpecificCard(cardType);
		deck.addCard(CardFactory.instantiateCard(cardType));
		log("Puts " + cardType + " on top of deck");
	}

	public int getActionsLeft() {
		return actionsLeft;
	}
	public int getBuys() {
		return  1 + revealedCards.getAdditionalBuys();
	}

	public void putCardOnDiscard(Card discarded) {
		if (discarded.getType() != CardType.NONE) {
			discard.addCard(discarded);
			summary.discardCard(1);
			log("Discards " + discarded.getType());
		}
	}

	public Card playFromHandToRevealedCards(CardType cardType) {
		if (cardType != CardType.NONE) {
			Card retValue = hand.removeSpecificCard(cardType);
			revealedCards.addCard(retValue);
			summary.updateHandFromPlayer();
			return retValue;
		}
		return new Card(CardType.NONE);
	}

	public void putDeckIntoDiscard() {
		discard.addDeck(deck);
		summary.discardCard(deck.getSize());
		deck = new Deck();
	}

	public void putCardOnTopOfDeck(CardType cardType) {
		deck.addCard(CardFactory.instantiateCard(cardType));
	}

	public int getBudget() {
		return hand.getTreasureValue() + revealedCards.getAdditionalPurchasePower();
	}
	public int getAdditionalPurchasePower() {
		return revealedCards.getAdditionalPurchasePower();
	}

	public void takeTurn() {
		switch (playerState) {
		case PRE_PLAY:
		case PLAYING:
			takeActions();
		case PRE_PURCHASE:
		case PURCHASING:
			buyCards();
		}
		tidyUp();
	}

	public List<ActionEnum<Player>> getActionsInHand() {
		List<ActionEnum<Player>> retValue = new ArrayList<ActionEnum<Player>>();
		for (CardType card : hand.getAllCards()) {
			if (card.isAction())
				retValue.add(new CardTypeAugment(card, CardSink.HAND, ChangeType.PLAY));
		}
		return retValue;
	}

	public Player clone(DominionGame newGame) {
		return new Player(this, newGame);
	}
	public void refreshPositionSummary() {
		summary = new PositionSummary(this, null);
	}
}

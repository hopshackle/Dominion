package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.*;

public class Player extends Agent {

	public enum State {
		PLAYING, PURCHASING, WAITING;
	}
	
	private State playerState;
	private Deck hand;
	private Deck deck;
	private Deck discard;
	private Deck revealedCards;
	private Game game;
	private PositionSummary summary;
	private int playerNumber;
	private int actionsLeft;
	private LookaheadDecider<Player> actionDecider;
	private LookaheadDecider<Player> purchaseDecider;
	HardCodedDiscardDecider discardDecider;
	private boolean onlyRewardVictory = SimProperties.getProperty("DominionOnlyRewardVictory", "false").equals("true");
	
	public Player(Game game, int number) {
		super(game.getWorld());
		playerState = State.WAITING;
		playerNumber = number;
		this.game = game;
		deck = new Deck();
		discard = new Deck();
		hand = new Deck();
		revealedCards = new Deck();
		summary = new PositionSummary(this, null);
		dealFreshHand();
		log("Player #" + number + " in Game " + game.getUniqueId());
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

	public void takeActions() {
		if (actionDecider == null) return;
		setState(State.PLAYING);
		do {
			Action<Player> action = actionDecider.decide(this);
			if (!(action instanceof DominionPlayAction)) {
				throw new AssertionError("Incorrect Action type in Player.takeActions()");
			}
			action.start();
			action.run();
			decrementActionsLeft();
			summary = new PositionSummary(this, null);
		} while (actionsLeft > 0);
		actionsLeft = 0;
	}
	public void incrementActionsLeft() {
		actionsLeft++;
	}
	public void decrementActionsLeft() {
		actionsLeft--;
	}

	public void buyCards() {
		setState(State.PURCHASING);
		DominionBuyAction decision = (DominionBuyAction) getPositionDecider().decide(this);
		decision.start();
		decision.run();
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
		summary = new PositionSummary(this, null);
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

	public Game getGame() {
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

	public void takeCardFromSupplyIntoDiscard(CardType type) {
		if (type.equals(CardType.NONE)) return;
		if (game.drawCard(type)) {
			discard.addCard(CardFactory.instantiateCard(type));
			summary.addCard(type);
			summary.discardCard(1);
		} else {
			throw new AssertionError("Card Type " + type + " not available." );
		}
	}

	public void insertCardDirectlyIntoHand(Card c) {
		hand.addCard(c);
		summary = new PositionSummary(this, null);
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

	public LookaheadDecider<Player> getPositionDecider() {
		return purchaseDecider;
	}
	@Override
	public Decider<Player> getDecider() {
		return purchaseDecider;
	}
	public LookaheadDecider<Player> getActionDecider() {
		return actionDecider;
	}
	public void setPositionDecider(LookaheadDecider<Player> newDecider) {
		purchaseDecider = newDecider;
		discardDecider = new HardCodedDiscardDecider(new DominionStateFactory(newDecider.getVariables()), HopshackleUtilities.convertList(newDecider.getActions()));
		log("Using purchase strategy " + purchaseDecider.toString());
	}
	public void setActionDecider(LookaheadDecider<Player> newDecider) {
		actionDecider = newDecider;
		log("Using action strategy " + actionDecider.toString());
	}
	public PositionSummary getPositionSummaryCopy() {
		return summary.clone();
	}
	public List<CardType> getCopyOfHand() {
		return hand.getAllCards();
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

	public void trashCardFromHand(CardType cardTypeToTrash) {
		hand.removeSpecificCard(cardTypeToTrash);
		summary.removeCard(cardTypeToTrash);
		summary.updateHandFromPlayer();
	}
	public void trashCardFromRevealed(CardType cardTypeToTrash) {
		revealedCards.removeSpecificCard(cardTypeToTrash);
		summary.removeCard(cardTypeToTrash);
	}
	public void trashCardFromDiscard(CardType cardTypeToTrash) {
		discard.removeSpecificCard(cardTypeToTrash);
		summary.removeCard(cardTypeToTrash);
		summary.discardCard(-1);
	}

	/**
	 * Indicates whether we are taking Actions (true), or making a purchase (false).
	 * This is a bit of a hack to cater for Purchase decisions made during the
	 * Action Phase. 
	 * If it is not the player's turn, then the default is false.
	 * NOT to be confused with isInActionPhase(), which indicates which Phase we are in.
	 */
	public boolean isTakingActions() {
		return playerState == State.PLAYING;
	}

	public void setState(Player.State newState) {
		playerState = newState;
		switch (newState) {
		case PLAYING: 
			actionsLeft = 1;
		default: 
		}
		summary = new PositionSummary(this, null);
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

	public HardCodedDiscardDecider getHandDecider() {
		return discardDecider;
	}

	public void takeTurn() {
		takeActions();
		buyCards();
		tidyUp();
		setState(State.WAITING);
	}

	public List<ActionEnum<Player>> getActionsInHand() {
		List<ActionEnum<Player>> retValue = new ArrayList<ActionEnum<Player>>();
		for (CardType card : hand.getAllCards()) {
			if (card.isAction())
				retValue.add(card);
		}
		return retValue;
	}
}

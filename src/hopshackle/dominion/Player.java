package hopshackle.dominion;

import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.simulation.*;

import java.util.*;

public class Player extends Agent {

	public enum State {
		WAITING, PLAYING, PURCHASING;
	}

	private State playerState;
	private Deck hand;
	private Deck deck;
	private Deck discard;
	private Deck revealedCards;
	private DominionGame game;
	private PositionSummary summary;
	private int playerNumber;
	private int actionsLeft, buysLeft, spentBudget;

	public Player(DominionGame game, int number) {
		super(game.getWorld());
		playerNumber = number;
		this.game = game;
		deck = new Deck();
		discard = new Deck();
		hand = new Deck();
		revealedCards = new Deck();
		actionsLeft = 1;
		setState(State.WAITING);
		dealFreshHand();
		log("Player #" + number + " in Game " + game.getUniqueId());
	}

	public Player(Player player, DominionGame newGame) {
		super(newGame.getWorld());
		setBirth(0l);
		playerState = player.playerState;
		playerNumber = player.playerNumber;
		actionsLeft = player.actionsLeft;
		buysLeft = player.buysLeft;
		spentBudget = player.spentBudget;
		game = newGame;
		// Responsibility for taking into account the information set resides in the caller
		// as that has the information from whose perspective the clone is taking place
		deck = player.deck.copy(game);
		discard = player.discard.copy(game);
		hand = player.hand.copy(game);
		revealedCards = player.revealedCards.copy(game);
		decider = player.decider;
		if (player.getNextAction() != null) {
			DominionAction da = (DominionAction) player.getNextAction();
			Action<Player> clonedAction = null;
			clonedAction = da.clone(this);
			this.actionPlan.addAction(clonedAction);
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
	public List<List<CardType>> getPossibleDiscardsFromHand(int min, int max) {
		return hand.getPossibleDiscards(min, max);
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
		if (game.gameOver()) {
			boolean onlyRewardVictory = false;
			if (decider != null) {
				onlyRewardVictory = decider.getProperties().getProperty("DominionOnlyRewardVictory", "false").equals("true");
			}
			if (onlyRewardVictory){
				int winningPlayers = 0;
				for (int i = 0; i < 4; i++)
					if (game.getOrdinalPosition(i) == 1) winningPlayers++;
				retValue = 0.0;
				if (game.getOrdinalPosition(playerNumber) == 1)
					retValue = 100.0 / (double)winningPlayers;
			} else {
				double[] rewardVector = new double[4];
				String rewardString = decider.getProperties().getProperty("DominionGameOrdinalRewards", "50:0:0:0");
				String[] rewards = rewardString.split(":");
				for (int i = 0; i < 4; i++) {
					rewardVector[i] = Double.valueOf(rewards[i]);
				}
				retValue += rewardVector[game.getOrdinalPosition(playerNumber) - 1];
			}
		}
		return retValue;
	}

	/* 
	 * Only used for testing
	 */
	public void takeActions() {
		if (playerState != State.PLAYING)
			throw new AssertionError("Should be in PLAYING State before taking actions");

		do {
			game.oneAction(false, false);
		} while (playerState == State.PLAYING);
	}

	/* 
	 * Only used for testing
	 */
	public void buyCards() {
		buyCards(false);
	}
	public void buyCards(boolean updateStatusAfter) {
		if (playerState != State.PURCHASING)
			throw new AssertionError("Should be in PURCHASING State before buying cards");
		do {
			game.oneAction(!updateStatusAfter, false); 
		} while (buysLeft > 0 && playerState == State.PURCHASING);
	}

	public void incrementActionsLeft() {
		actionsLeft++;
		summary.setActions(actionsLeft);
	}
	public void decrementActionsLeft() {
		actionsLeft--;
		summary.setActions(actionsLeft);
	}

	public void incrementBuysLeft() {
		buysLeft++;
		summary.setBuys(buysLeft);
	}
	public void decrementBuysLeft() {
		buysLeft--;
		summary.setBuys(buysLeft);
	}

	public void tidyUp() {
		buysLeft = 1;
		actionsLeft = 1;
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
			drawTopCardFromDeckInto(CardSink.HAND);
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
		discard = new Deck();
		refreshPositionSummary();
	}

	@Override
	public double getMaxScore() {
		return 100.0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DominionGame getGame() {
		return game;
	}

	public Card drawTopCardFromDeckInto(CardSink to) {
		Card cardDrawn = drawTopCardFromDeck();
		if (cardDrawn.getType() != CardType.NONE) {
			insertCardDirectlyInto(cardDrawn, to);
			summary.updateHandFromPlayer();
		}
		return cardDrawn;
	}

	public Card drawTopCardFromDeck() {
		if (deck.isEmpty()) {
			shuffleDiscardToFormNewDeck();
			refreshPositionSummary();
		}
		if (!deck.isEmpty()) {
			Card cardDrawn = deck.drawTopCard();
			summary.removeCardFrom(cardDrawn.getType(), CardSink.DECK);
			log("Draws a " + cardDrawn);
			return cardDrawn;
		}
		return new Card(CardType.NONE);
	}

	public Card takeCardFromSupply(CardType card, CardSink dest) {
		Card retValue = CardFactory.instantiateCard(card);
		if (card.equals(CardType.NONE)) return retValue;
		Deck destination = null;
		if (dest == CardSink.HAND) destination = hand;
		if (dest == CardSink.REVEALED) destination = revealedCards;
		if (dest == CardSink.DECK) destination = deck;
		if (dest == CardSink.DISCARD) destination = discard;
		if (game.drawCard(card)) {
			summary.removeCardFrom(card, CardSink.SUPPLY);
			if (dest != null) {
				destination.addCard(retValue);
				summary.addCard(card, dest);
			}
		} else {
			throw new AssertionError("Card Type " + card + " not available." );
		}
		return retValue;
	}

	public Card moveCard(CardType cardType, CardSink from, CardSink to) {
		if (cardType != CardType.NONE) {
			Card retValue = removeCardFrom(cardType, from);
			if (retValue.getType() == CardType.NONE) 
				throw new AssertionError(cardType + " not found in " + from);
			insertCardDirectlyInto(retValue, to);
			return retValue;
		}
		return new Card(CardType.NONE);
	}

	public Card removeCardFrom(CardType ct, CardSink from) {
		Deck deckToUpdate = null;
		switch (from) {
		case DECK:
			deckToUpdate = deck;
			break;
		case DISCARD:
			deckToUpdate = discard;
			break;
		case HAND:
			deckToUpdate = hand;
			break;
		case REVEALED:
			deckToUpdate = revealedCards;
			break;
		case SUPPLY:
			return takeCardFromSupply(ct, null);
		case TRASH:
			return CardFactory.instantiateCard(ct);
		}
		summary.removeCardFrom(ct, from);
		return deckToUpdate.removeSpecificCard(ct);
	}

	public void insertCardDirectlyIntoHand(Card c) {
		insertCardDirectlyInto(c, CardSink.HAND);
	}
	public void insertCardDirectlyInto(Card c, CardSink to) {
		Deck deckToUse = null;
		switch (to) {
		case DECK:
			deckToUse = deck;
			break;
		case DISCARD:
			deckToUse = discard;
			break;
		case HAND:
			deckToUse = hand;
			break;
		case REVEALED:
			deckToUse = revealedCards;
			break;
		case TRASH:
			// no deck currently maintained
			break;
		default: 
			throw new AssertionError("Should not be here for sink " + to);
		}
		if (deckToUse != null) {
			summary.addCard(c.getType(), to);
			deckToUse.addCard(c);
		}
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
	public Card getCardLastPlayed() {
		return revealedCards.getTopCard();
	}

	public Deck getCardsInPlay() {
		return revealedCards;
	}

	/**
	 * Indicates whether we are taking Actions (true), or making a purchase (false).
	 * This is a bit of a hack to cater for Purchase decisions made during the
	 * Action Phase. 
	 * If it is not the player's turn, then the default is false.
	 */
	public boolean isTakingActions() {
		return playerState == State.PLAYING;
	}

	public void setState(Player.State newState) {
		if (playerState != State.PURCHASING && newState == State.PURCHASING) {
			buysLeft = 1 + revealedCards.getAdditionalBuys();
			spentBudget = 0;
		}
		if (playerState != State.PLAYING && newState == State.PLAYING)
			actionsLeft = 1;
		playerState = newState;
		refreshPositionSummary();
	}
	public Player.State getPlayerState() {
		return playerState;
	}

	public int getActionsLeft() {
		return actionsLeft;
	}
	public int getBuys() {
		return buysLeft;
	}

	public Card playFromHandToRevealedCards(CardType cardType) {
		if (cardType != CardType.NONE) {
			summary = summary.apply(CardTypeAugment.playCard(cardType));
			Card retValue = hand.removeSpecificCard(cardType);
			revealedCards.addCard(retValue);
			return retValue;
		}
		return new Card(CardType.NONE);
	}

	public void putDeckIntoDiscard() {
		discard.addDeck(deck);
		deck = new Deck();
		refreshPositionSummary();
	}

	public void putCardOnTopOfDeck(CardType cardType) {
		deck.addCard(CardFactory.instantiateCard(cardType));
	}

	public int getBudget() {
		return hand.getTreasureValue() + revealedCards.getAdditionalPurchasePower() - spentBudget;
	}
	public void spend(int spent) {
		spentBudget += spent;
		summary.spend(spent);
	}

	public List<ActionEnum<Player>> getActionsInHand() {
		List<ActionEnum<Player>> retValue = new ArrayList<ActionEnum<Player>>();
		Set<CardType> cardsSeen = new HashSet<CardType>();
		for (CardType card : hand.getAllCards()) {
			if (card.isAction() && !cardsSeen.contains(card)) {
				retValue.add(CardTypeAugment.playCard(card));
				cardsSeen.add(card);
			}
		}
		return retValue;
	}

	public Player clone(DominionGame newGame) {
		return new Player(this, newGame);
	}

	public void refreshPositionSummary() {
		summary = new PositionSummary(this, null);
	}
	
	public void setGame(DominionGame dominionGame) {
		game = dominionGame;
	}

	public int getNumber() {
		return playerNumber;
	}

	public void removeCardsWithRef(String ref) {
		deck.removeCardWithRef(ref);
		hand.removeCardWithRef(ref);
		discard.removeCardWithRef(ref);
		revealedCards.removeCardWithRef(ref);
	}
	public List<Card> getCardsWithRef(String requiredRef) {
		List<Card> retValue = new ArrayList<Card>();
		retValue.addAll(deck.getCardsWithRef(requiredRef));
		retValue.addAll(hand.getCardsWithRef(requiredRef));
		retValue.addAll(discard.getCardsWithRef(requiredRef));
		retValue.addAll(revealedCards.getCardsWithRef(requiredRef));
		return retValue;
	}

	public CardType peekAtTopCardOfDeck() {
		if (deck.isEmpty())
			shuffleDiscardToFormNewDeck();
		return deck.getTopCard().getType();
	}

	public void logCurrentState() {
		log("Using Decider " + decider);
		StringBuffer deckContents = new StringBuffer("Deck: ");
		List<CardType> allCards = getAllCards();
		for (CardType ct : CardType.values()) {
			if (ct != CardType.NONE && allCards.contains(ct)) {
				int count = 0;
				for (CardType ct1 : allCards)
					if (ct1 == ct) count++;
				deckContents.append(count + " " + ct + ", ");
			}
		}
		deckContents.substring(0, deckContents.length()-2);
		log(deckContents.substring(0, deckContents.length()-2));
		StringBuffer handContents = new StringBuffer("Hand: ");
		for (CardType c : hand.getAllCards()) {
			handContents.append(c + ", ");
		}
		log(handContents.substring(0, handContents.length()-2));
		StringBuffer playedCards = new StringBuffer("Revealed: ");
		for (CardType c : revealedCards.getAllCards()) {
			playedCards.append(c + ", ");
		}
		playedCards.substring(0, playedCards.length()-3);
		log(playedCards.substring(0, playedCards.length()-2));
	}

}

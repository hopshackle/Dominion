package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.*;

import com.sun.media.jfxmedia.events.PlayerStateEvent.PlayerState;

public class PositionSummary implements LookaheadState<Player> {

	private double victoryPoints, treasureValue;
	private int turnNumber, buys, actions, money;
	private double victoryMargin, wealthDensity, victoryDensity, percentVictory, percentAction;
	private double totalCards, victoryCards, actionCards, cardsInDiscard;
	private HashMap<CardType, Integer> cardsRemovedFromTable = new HashMap<CardType, Integer>();
	private HashMap<CardType, Integer> cardsInDeck;
	private HashMap<CardType, Integer> cardsOnTable = new HashMap<CardType, Integer>();
	private List<CardType> hand, cardsPlayed;
	private Player player;
	private List<CardValuationVariables> variables;
	private Player.State positionState;

	public PositionSummary(Player basePlayer, List<CardValuationVariables> variableList) {
		variables = variableList;
		player = basePlayer;
		initiateVariables(basePlayer.getGame());
	}

	private PositionSummary(PositionSummary base) {
		player = base.player;
		positionState = base.positionState;
		cardsInDeck = new HashMap<CardType, Integer>();
		for (CardType ct : base.cardsInDeck.keySet()) {
			cardsInDeck.put(ct, base.cardsInDeck.get(ct));
		}
		cardsOnTable = new HashMap<CardType, Integer>();
		for (CardType ct : base.cardsOnTable.keySet()) {
			cardsOnTable.put(ct, base.cardsOnTable.get(ct));
		}
		cardsRemovedFromTable = new HashMap<CardType, Integer>();
		for (CardType ct : base.cardsRemovedFromTable.keySet()) {
			cardsRemovedFromTable.put(ct, base.cardsRemovedFromTable.get(ct));
		}
		victoryCards = base.victoryCards;
		victoryMargin = base.victoryMargin;
		victoryPoints = base.victoryPoints;
		treasureValue = base.treasureValue;
		totalCards = base.totalCards;
		victoryCards = base.victoryCards;
		actionCards = base.actionCards;
		cardsInDiscard = base.cardsInDiscard;
		variables = base.variables;
		turnNumber = base.turnNumber;
		hand = new ArrayList<CardType>();
		hand.addAll(base.hand);
		actions = base.actions;
		buys = base.buys;
		money = base.money;
		cardsPlayed = new ArrayList<CardType>();
		cardsPlayed.addAll(base.cardsPlayed);
		cardsPlayed = player.getCopyOfPlayedCards();
		updateDerivedVariables();
	}

	public void drawCard(CardType drawnCard) {
		if (drawnCard != null && drawnCard != CardType.NONE) {
			changeNumberOnTable(drawnCard, -1);
			addCard(drawnCard);
		}
	}

	@Override
	public PositionSummary apply(ActionEnum<Player> ae) {
		PositionSummary retValue = this.clone();
		if (ae == null) return retValue;
		switch (positionState) {
		case PLAYING:
			retValue.playCardFromHand((CardType) ae);
			break;
		case PURCHASING:
			if (ae instanceof CardType){
				retValue.drawCard((CardType) ae);
			} else if (ae instanceof CardTypeList) {
				CardTypeList ctl = (CardTypeList) ae;
				for (CardType c : ctl.cards) 
					retValue.drawCard(c);
			}
			break;
		default:
			throw new AssertionError("Invalid PlayerState in PositionSummary: " + positionState);
		}

		return retValue;
	}

	public void undrawCard(CardType drawnCard) {
		if (drawnCard != null && drawnCard != CardType.NONE) {
			changeNumberOnTable(drawnCard, 1);
			removeCard(drawnCard);
		}
	}

	public void undrawCard(ActionEnum<Player> ae) {
		if (ae == null) return;
		if (ae instanceof CardType){
			undrawCard((CardType) ae);
			return;
		}
		if (ae instanceof CardTypeList) {
			CardTypeList ctl = (CardTypeList) ae;
			for (CardType c : ctl.cards) 
				undrawCard(c);
			return;
		}
		return;
	}

	public void addCard(CardType newCard) {
		if (newCard != CardType.NONE) {
			totalCards++;
			if (newCard.isVictory()) victoryCards++;
			if (newCard.isAction()) {
				actionCards++;
			}
			Integer currentAmount = cardsInDeck.get(newCard);
			if (currentAmount == null)
				currentAmount = 0;
			currentAmount++;
			cardsInDeck.put(newCard, currentAmount);
			treasureValue += newCard.getTreasure();
			recalculateVictoryPoints();
			updateDerivedVariables();
		}
	}

	public void removeCard(CardType oldCard) {
		if (oldCard != CardType.NONE && oldCard != null) {
			totalCards--;
			if (oldCard.isVictory()) victoryCards--;
			if (oldCard.isAction()) {
				actionCards--;
			}
			Integer currentAmount = cardsInDeck.get(oldCard);
			if (currentAmount == null)
				return;
			currentAmount--;
			cardsInDeck.put(oldCard, currentAmount);
			treasureValue -= oldCard.getTreasure();
			recalculateVictoryPoints();
			updateDerivedVariables();
		}
	}

	private void recalculateVictoryPoints() {
		double oldVictoryMargin = victoryMargin;
		double oldVictoryPoints = victoryPoints;
		victoryPoints = 0.0;
		for (CardType ct : cardsInDeck.keySet()) {
			int number = cardsInDeck.get(ct);
			victoryPoints += number * ct.getVictory(this);
		}
		victoryMargin = oldVictoryMargin + victoryPoints - oldVictoryPoints;
	}

	public void discardCard(double number) {
		cardsInDiscard += number;
	}

	private void changeNumberOnTable(CardType drawnCard, int number) {
		if (cardsRemovedFromTable.containsKey(drawnCard)) {
			int newTotal = cardsRemovedFromTable.get(drawnCard) - number;
			cardsRemovedFromTable.put(drawnCard, newTotal);
		} else {
			cardsRemovedFromTable.put(drawnCard, -number);
		}
	}

	private void initiateVariables(Game game) {
		Player[] players = game.getPlayers();
		updateHandFromPlayer();
		positionState = player.getPlayerState();
		victoryPoints = 0;
		totalCards = 0;
		treasureValue = 0.0;
		actions = player.getActionsLeft();
		buys = player.getBuys();
		money = player.getAdditionalPurchasePower();
		cardsInDeck = new HashMap<CardType, Integer>();
		for (CardType card : player.getAllCards()) {
			addCard(card);
		}
		for (CardType ct : game.startingCardTypes()) {
			cardsOnTable.put(ct, game.getNumberOfCardsRemaining(ct));
		}
		cardsInDiscard = player.getDiscardSize();
		if (players[3] != null) {
			double highestScore = -100;
			double secondScore = -100;
			for (int n = 0; n < 4; n++) {
				double nextScore = players[n].getScore();
				if (nextScore > highestScore) {
					secondScore = highestScore;
					highestScore = nextScore;
				} else if (nextScore > secondScore) {
					secondScore = nextScore;
				}
			}
			if (victoryPoints > secondScore) 
				victoryMargin = victoryPoints - secondScore;
			else
				victoryMargin = victoryPoints - highestScore;
		}
		turnNumber = game.turnNumber() - 1;
		updateDerivedVariables();
	}


	private void updateDerivedVariables() {
		wealthDensity = treasureValue / totalCards;
		victoryDensity = victoryPoints / totalCards;
		percentVictory = victoryCards / totalCards;
		percentAction = actionCards / totalCards;
	}

	public double getVictoryMargin() {
		return victoryMargin;
	}
	public double getWealthDensity() {
		return wealthDensity;
	}
	public double getVictoryDensity() {
		return victoryDensity;
	}
	public double getPercentVictory() {
		return percentVictory;
	}
	public double getPercentAction() {
		return percentAction;
	}
	public Player getPlayer() {
		return player;
	}
	public int getActions() {
		return actions;
	}
	public int getBuys() {
		return buys;
	}
	public int getAdditionalPurchasePower() {
		return money;
	}

	public double totalNumberOfCards() {
		return totalCards;
	}
	public PositionSummary clone() {
		return new PositionSummary(this);
	}
	
	public List<CardType> getHand() {
		return HopshackleUtilities.cloneList(hand);
	}
	public int getHandSize() {
		return hand.size();
	}

	public void changeHand(CardType[] newHand) {
		hand = new ArrayList<CardType>();
		for (CardType ct : newHand)
			hand.add(ct);
		updateDerivedVariables();
	}

	public int getNumberInHand(CardType cardType) {
		int retValue = 0;
		for (CardType ct : hand) {
			if (ct == cardType)
				retValue++;
		}
		return retValue;
	}
	public int getNumberPlayed(CardType cardType) {
		int retValue = 0;
		for (CardType ct : cardsPlayed) {
			if (ct == cardType)
				retValue++;
		}
		return retValue;
	}

	public void updateHandFromPlayer() {
		hand = player.getCopyOfHand();
		cardsPlayed = player.getCopyOfPlayedCards();
	}
	
	private void playCardFromHand(CardType type) {
		if (getNumberInHand(type) > 0) {
			hand.remove(type);
			actions--;
			actions += type.getAdditionalActions();
			buys += type.getAdditionalBuys();
			money += type.getAdditionalPurchasePower();
			for (int i = 0; i < type.getDraw(); i++)
				hand.add(CardType.UNKNOWN); 
			// TODO: This is the possible point to consider full MOnte Carlo rollouts..i.e. pick at random form the cards known to be in the deck
			// For the moment, we just use a dummy card to keep track of hand size (as a variable).
			cardsPlayed.add(type);
		} else {
			throw new AssertionError("Cannot play " + type + " as none in hand.");
		}
	}

	public double getNumberOfCardsRemaining(CardType type) {
		double retValue = cardsOnTable.get(type);
		if (cardsRemovedFromTable.containsKey(type)) {
			retValue = retValue - cardsRemovedFromTable.get(type);
		}
		return retValue;
	}

	public double[] getPercentageDepleted() {
			int lowest = 10;
			int nextLowest = 10;
			int thirdLowest = 10;
			for (CardType ct : cardsOnTable.keySet()) {
				if (ct == CardType.NONE) continue;
				int cardsLeft = cardsOnTable.get(ct);
				if (cardsRemovedFromTable.containsKey(ct)) {
					cardsLeft = cardsLeft - cardsRemovedFromTable.get(ct);
				}
				if (cardsLeft > 10) cardsLeft = 10;
				if (cardsLeft < lowest) {
					thirdLowest = nextLowest;
					nextLowest = lowest;
					lowest = cardsLeft;
				} else if (cardsLeft < nextLowest) {
					thirdLowest = nextLowest;
					nextLowest = cardsLeft;
				} else if (cardsLeft < thirdLowest) {
					thirdLowest = cardsLeft;
				}
			}
			double[] retValue = new double[3];
			retValue[0] = 1.0 - ((double) lowest / 10.0);
			retValue[1] = 1.0 - ((double) nextLowest / 10.0);
			retValue[2] = 1.0 - ((double) thirdLowest / 10.0);
			return retValue;
	}

	public int getNumberOfCardsTotal(CardType card) {
		Integer retValue = cardsInDeck.get(card);
		if (retValue == null) return 0;
		return retValue;
	}

	public double getPercent(CardType cardType) {
		int number = getNumberOfCardsTotal(cardType);
		if (number == 0) return 0.0;
		//		if (!cardType.isAction()) {
		return (double)number / totalCards;
		//		} else {
		//			return (double)number / actionCards / 5.0;
		//		}
	}

	public double getPercentageInDiscard() {
		return cardsInDiscard / totalCards;
	}

	public double getTurns() {
		return turnNumber;
	}

	public void setVariables(List<CardValuationVariables> var) {
		variables = var;
	}
	@Override
	public double[] getAsArray() {
		if (variables == null) {
			throw new AssertionError("No Variables in PositionSummary.getAsArray()");
		}
		double[] values = new double[variables.size()];
		for (int i = 0; i < variables.size(); i ++) {
			CardValuationVariables gv = variables.get(i);
			values[i] = gv.getValue(this);
		}
		return values;
	}

}

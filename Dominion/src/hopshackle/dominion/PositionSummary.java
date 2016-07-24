package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.*;

public class PositionSummary implements LookaheadState<Player> {

	private double victoryPoints, treasureValue;
	private double victoryMargin, wealthDensity, victoryDensity, percentVictory, percentAction;
	private double totalCards, victoryCards, actionCards, cardsInDiscard;
	private HashMap<CardType, Integer> cardsRemovedFromTable = new HashMap<CardType, Integer>();
	private HashMap<CardType, Integer> cardsInDeck;
	private CardType[] hand;
	private Player player;
	private Game game;
	private List<CardValuationVariables> variables;
	
	public PositionSummary(Player basePlayer) {
		this(basePlayer, null);
	}

	public PositionSummary(Player basePlayer, List<CardValuationVariables> variableList) {
		variables = variableList;
		player = basePlayer;
		game = player.getGame();
		initiateVariables();
	}

	private PositionSummary(PositionSummary base) {
		player = base.player;
		game = base.game;
		hand = base.hand.clone();
		cardsInDeck = new HashMap<CardType, Integer>();
		for (CardType ct : base.cardsInDeck.keySet()) {
			cardsInDeck.put(ct, base.cardsInDeck.get(ct));
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
		if (ae instanceof CardType){
			retValue.drawCard((CardType) ae);
		} else if (ae instanceof CardTypeList) {
			CardTypeList ctl = (CardTypeList) ae;
			for (CardType c : ctl.cards) 
				retValue.drawCard(c);
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

	private void initiateVariables() {
		Player[] players = game.getPlayers();
		updateHand();
		victoryPoints = 0;
		totalCards = 0;
		treasureValue = 0.0;
		cardsInDeck = new HashMap<CardType, Integer>();
		for (CardType card : player.getAllCards()) {
			addCard(card);
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
	public Game getGame() {
		return game;
	}

	public double totalNumberOfCards() {
		return totalCards;
	}
	public PositionSummary clone() {
		return new PositionSummary(this);
	}

	public void changeHand(CardType[] newHand) {
		hand = newHand;
		updateDerivedVariables();
	}

	public CardType[] getHand() {
		return hand.clone();
	}

	public int getNumberInHand(CardType cardType) {
		int retValue = 0;
		for (CardType ct : hand) {
			if (ct == cardType)
				retValue++;
		}
		return retValue;
	}

	public void updateHand() {
		hand = player.getCopyOfHand().toArray(new CardType[1]);
		if (hand[0] == null)
			hand = new CardType[0];
	}

	public double getNumberOfCardsRemaining(CardType type) {
		double retValue = game.getNumberOfCardsRemaining(type);
		if (cardsRemovedFromTable.containsKey(type)) {
			retValue = retValue - cardsRemovedFromTable.get(type);
		}
		return retValue;
	}

	public double[] getPercentageDepleted() {
		return game.getPercentageDepleted(cardsRemovedFromTable);
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
		return ((double) (game.turnNumber() - 1));
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

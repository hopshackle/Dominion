package hopshackle.dominion;

import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.simulation.*;

import java.util.*;

public class PositionSummary implements State<Player> {

    private double victoryPoints, treasureValue;
    private int turnNumber, buys, actions, spent, baseBudget, currentFeature;
    private double victoryMargin, wealthDensity, victoryDensity, percentVictory, percentAction;
    private double totalCards, victoryCards, actionCards, cardsInDiscard;
    private HashMap<CardType, Integer> cardsRemovedFromTable = new HashMap<CardType, Integer>();
    private HashMap<CardType, Integer> cardsInDeck;
    private HashMap<CardType, Integer> cardsOnTable = new HashMap<CardType, Integer>();
    private List<CardType> hand;
    private LinkedList<CardType> cardsPlayed;
    private Player player;
    private List<CardValuationVariables> variables;
    private Player.State positionState;
    private double[] playerScores = new double[4];
    private String asString = "";
    private boolean hasChanged;

    public PositionSummary(Player basePlayer, List<CardValuationVariables> variableList) {
        variables = variableList;
        player = basePlayer;
        initiateVariables(basePlayer.getGame());
        hasChanged = false;
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
        spent = base.spent;
        currentFeature = base.currentFeature;
        baseBudget = base.baseBudget;
        cardsPlayed = new LinkedList<>();
        cardsPlayed.addAll(base.cardsPlayed);
        for (int i = 0; i < 4; i++)
            playerScores[i] = base.playerScores[i];
        updateDerivedVariables();
        hasChanged = false;
    }

    @Override
    public PositionSummary apply(ActionEnum<Player> ae) {
        PositionSummary retValue = this.clone();
        if (ae == null) return retValue;
        List<CardTypeAugment> components = new ArrayList<CardTypeAugment>();
        if (ae instanceof CardTypeList) {
            components = ((CardTypeList) ae).cards;
        } else {
            components.add((CardTypeAugment) ae);
        }
        for (CardTypeAugment componentAction : components) {
            switch (componentAction.type) {
                case PLAY:
                    if (positionState != Player.State.PLAYING)
                        throw new AssertionError("Should not PLAY card unless PLAYING");
                    retValue.playCardFromHand(componentAction.card);
                    break;
                case BUY:
                    retValue.spend(componentAction.card.getCost());
                    retValue.buys--;
                    // then continue
                case REMODEL: // we trash, but record the value of the trashed card
                    if (componentAction.type == CardTypeAugment.ChangeType.REMODEL)
                        retValue.oneOffBudget(componentAction.card.getCost());
                    // then continue
                case CELLAR:
                    if (componentAction.type == CardTypeAugment.ChangeType.CELLAR) {
                        if (componentAction.card == CardType.NONE) {
                            retValue.oneOffBudget(0);
                            for (int i = 0; i < this.getOneOffBudget(); i++){
                                retValue.addCard(CardType.UNKNOWN, CardSink.HAND);
                            }
                        } else {
                            retValue.oneOffBudget(this.getOneOffBudget() + 1);
                        }
                    }
                case MONEYLENDER:
                    if (componentAction.type == CardTypeAugment.ChangeType.MONEYLENDER) {
                        if (componentAction.card == CardType.COPPER && componentAction.to == CardSink.TRASH) {
                            retValue.baseBudget = baseBudget + 3;
                        }
                    }
                case MOVE:
                    retValue.removeCardFrom(componentAction.card, componentAction.from);
                    retValue.addCard(componentAction.card, componentAction.to);
                    break;
                case NOCHANGE:
                    break;
                case ENTHRONE:
                    break;
                case CHANCELLOR:
                    retValue.cardsInDiscard = 0.0;
                    break;
                case SPY:
                    // we have no features to track opponent hands
                    break;
                default:
                    throw new AssertionError("Unsupported action " + componentAction);
            }
        }
        return retValue;
    }

    public void drawCard(CardType drawnCard) {
        drawCard(drawnCard, CardSink.DISCARD);
        hasChanged = true;
    }

    public void drawCard(CardType drawnCard, CardSink to) {
        removeCardFrom(drawnCard, CardSink.SUPPLY);
        addCard(drawnCard, to);
        hasChanged = true;
    }

    public void addCard(CardType newCard, CardSink to) {
        if (newCard != CardType.NONE) {
            totalCards++;
            if (newCard.isVictory()) victoryCards++;
            if (newCard.isAction()) {
                actionCards++;
            }
            if (to == CardSink.DISCARD) {
                cardsInDiscard++;
            } else if (to == CardSink.HAND) {
                hand.add(newCard);
                baseBudget += newCard.getTreasure();
            } else if (to == CardSink.TRASH) {
                totalCards--;
                if (newCard.isVictory()) victoryCards--;
                if (newCard.isAction()) {
                    actionCards--;
                }
                Integer currentAmount = cardsInDeck.get(newCard);
                if (currentAmount == null)
                    return;
                currentAmount--;
                cardsInDeck.put(newCard, currentAmount);
                treasureValue -= newCard.getTreasure();
            } else if (to == CardSink.REVEALED) {
                revealCard(newCard);
            } else if (to == CardSink.SUPPLY) {
                changeNumberOnTable(newCard, 1);
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

    public void removeCardFrom(CardType card, CardSink from) {
        switch (from) {
            case DISCARD:
                cardsInDiscard--;
                break;
            case HAND:
                hand.remove(card);
                baseBudget -= card.getTreasure();
                break;
            case SUPPLY:
                changeNumberOnTable(card, -1);
                return; // this does not affect number of cards held
            case DECK:
                break;
            case REVEALED:
                cardsPlayed.remove(card);
                baseBudget -= card.getAdditionalPurchasePower();
                actions -= card.getAdditionalActions();
                buys -= card.getAdditionalBuys();
                break;
            case TRASH:
                return;    // this does not affect cards in hand/deck
        }
        totalCards--;
        if (card.isVictory()) victoryCards--;
        if (card.isAction()) {
            actionCards--;
        }
        Integer currentAmount = cardsInDeck.get(card);
        if (currentAmount == null)
            currentAmount = 0;
        currentAmount--;
        cardsInDeck.put(card, currentAmount);
        treasureValue -= card.getTreasure();
        recalculateVictoryPoints();
        updateDerivedVariables();
    }

    public void trashCard(CardType oldCard, CardSink from) {
        if (oldCard != CardType.NONE && oldCard != null) {
            removeCardFrom(oldCard, from);
            addCard(oldCard, CardSink.TRASH);
            hasChanged = true;
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

    private void changeNumberOnTable(CardType drawnCard, int number) {
        if (cardsRemovedFromTable.containsKey(drawnCard)) {
            int newTotal = cardsRemovedFromTable.get(drawnCard) - number;
            cardsRemovedFromTable.put(drawnCard, newTotal);
        } else {
            cardsRemovedFromTable.put(drawnCard, -number);
        }
        hasChanged = true;
    }

    private void initiateVariables(DominionGame game) {
        Player[] players = game.getAllPlayers().toArray(new Player[1]);
        updateHandFromPlayer();
        positionState = player.getPlayerState();
        victoryPoints = 0.0;
        totalCards = 0;
        treasureValue = 0.0;
        currentFeature = player.getOneOffBudget();
        actions = player.getActionsLeft();
        buys = player.getBuys();
        cardsInDeck = new HashMap<CardType, Integer>();
        for (CardType card : player.getAllCards()) {
            addCard(card, CardSink.DECK);
        }
        for (CardType ct : game.startingCardTypes()) {
            cardsOnTable.put(ct, game.getNumberOfCardsRemaining(ct));
        }
        cardsInDiscard = player.getDiscardSize();
        recalculateVictoryPoints();
        if (players[3] != null) {
            double highestScore = -100;
            double secondScore = -100;
            playerScores = new double[4];
            for (int n = 0; n < 4; n++) {
                double nextScore = players[n].getScore();
                playerScores[n] = nextScore;
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
        hasChanged = true;
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

    public int getBudget() {
        return baseBudget - spent;
    }

    public void spend(int money) {
        spent += money;
    }

    public double totalNumberOfCards() {
        return totalCards;
    }

    @Override
    public PositionSummary clone() {
        return new PositionSummary(this);
    }

    public PositionSummary clone(Player newPlayer) {
        PositionSummary retValue = new PositionSummary(this);
        retValue.player = newPlayer;
        return retValue;
    }

    public List<CardType> getHand() {
        return HopshackleUtilities.cloneList(hand);
    }

    public int getHandSize() {
        return hand.size();
    }

    public double getHandMoneyValue() {
        double retValue = 0.0;
        for (CardType ct : hand) {
            retValue += ct.getTreasure();
        }
        return retValue;
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

    public int getNumberPlayed() {
        return cardsPlayed.size();
    }

    public void updateHandFromPlayer() {
        hand = player.getCopyOfHand();
        cardsPlayed = new LinkedList<>();
        for (CardType c : player.getCopyOfPlayedCards()) {
            cardsPlayed.add(c);
        }
        baseBudget = 0;
        for (CardType ct : hand) {
            baseBudget += ct.getTreasure();
        }
        for (CardType ct : cardsPlayed) {
            baseBudget += ct.getAdditionalPurchasePower();
        }
        spent = baseBudget - player.getBudget();
        hasChanged = true;
    }

    private void revealCard(CardType newCard) {
        cardsPlayed.addFirst(newCard);
        baseBudget += newCard.getAdditionalPurchasePower();
        buys += newCard.getAdditionalBuys();
        actions += newCard.getAdditionalActions();
    }

    private void playCardFromHand(CardType type) {
        if (type == CardType.NONE)  {
            actions = 0;
            return;
        }
        hasChanged = true;
        if (getNumberInHand(type) > 0) {
            hand.remove(type);
            actions--;
            revealCard(type);
            for (int i = 0; i < type.getDraw(); i++)
                hand.add(CardType.UNKNOWN);
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
        return (double) number / totalCards;
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
        hasChanged = true;
    }

    public Player.State getPlayerState() {
        return positionState;
    }

    @Override
    public double[] getAsArray() {
        if (variables == null) {
            throw new AssertionError("No Variables in PositionSummary.getAsArray()");
        }
        double[] values = new double[variables.size()];
        for (int i = 0; i < variables.size(); i++) {
            CardValuationVariables gv = variables.get(i);
            values[i] = gv.getValue(this);
        }
        return values;
    }

    @Override
    public String getAsString() {
        if (!hasChanged && (asString.length() > 0)) {
            return asString;
        }
        double[] values = getAsArray();
        StringBuffer retValue = new StringBuffer();
        for (double d : values) {
            boolean negative = false;
            if (d < 0.00) {
                negative = true;
                d = -d;
            }
            int asInt = (int) ((d + 0.005) * 100.0);
            String padded = String.valueOf(asInt);
            while (padded.length() < 3) padded = "0" + padded;
            if (negative) padded = "-" + padded;
            retValue.append(padded + "|");
        }
        retValue.deleteCharAt(retValue.length() - 1);
        asString = retValue.toString();
        return asString;
    }

    public void setActions(int actionsLeft) {
        actions = actionsLeft;
    }

    public void setBuys(int buysLeft) {
        buys = buysLeft;
    }

    @Override
    public int getActorRef() {
        return player.getActorRef();
    }

    @Override
    public double[] getScore() {
        double[] retValue = new double[4];
        for (int i = 0; i < 4; i++) {
            if (i == player.getActorRef() - 1) {
                retValue[i] = victoryPoints;
            } else {
                retValue[i] = playerScores[i];
            }
        }
        // TODO: May not be ideal. We assume other player's scores never change
        // as a result of one of our actions. This might not be too bad in the base game, but will need update
        // for the WITCH (and others).
        return retValue;
    }

    public CardType getCardInPlay() {
        if (positionState == Player.State.PLAYING && cardsPlayed != null && cardsPlayed.size() > 0)
            return cardsPlayed.getFirst();
        return CardType.NONE;
    }

    public int getOneOffBudget() {
        return currentFeature;
    }

    public void oneOffBudget(int cost) {
        currentFeature = cost;
    }
}

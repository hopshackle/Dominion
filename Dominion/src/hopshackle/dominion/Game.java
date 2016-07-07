package hopshackle.dominion;
import hopshackle.simulation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Game implements Persistent {

	private Player[] players;
	private HashMap<CardType, Integer> cardsOnTable;
	private int currentPlayer;
	private static AtomicInteger idFountain = new AtomicInteger(1);
	private int id = idFountain.getAndIncrement();
	private int turn;
	private RunGame	seqOfGames;
	private int losingPlayerNumber;
	private int[] winningPlayerNumbers = new int[0];
	private double highestScore, lowestScore;
	private double score[] = new double[4];
	private static DatabaseWriter<Game> gameWriter = new DatabaseWriter<Game>(new GameDAO());
	private DeciderGenerator deciderGenerator;
	private final int MAX_TURNS = 200;

	public Game(RunGame gameHolder) {
		seqOfGames = gameHolder;
		deciderGenerator = gameHolder.getDeciderDenerator();
		players = new Player[4];
		boolean debugGame = false;
		setUpCardsOnTable(deciderGenerator.getGameSetup());
		if (Math.random() < 0.005) debugGame = true;
		for (int n = 0; n < players.length; n++) {
			players[n] = new Player(this, n+1);
			players[n].setDebugLocal(debugGame);
			if (deciderGenerator != null) {
				players[n].setPositionDecider(deciderGenerator.getPurchaseDecider());
				players[n].setActionDecider(deciderGenerator.getActionDecider());
//				players[n].setHandDecider(deciderGenerator.getDiscardDecider());
				players[n].setGameEndComputer(deciderGenerator.getGameEndComputer());
			}
		}
		currentPlayer = -1;
	}

	public void playGame() {
		do {
			nextPlayersTurn();
		} while (!gameOver());

		highestScore = -100;
		lowestScore = 100;
		for (int loop = 0; loop < 4; loop++) {
			score[loop] = players[loop].totalVictoryValue();
			players[loop].log("Final score is " + score[loop]);
			if (score[loop] >= highestScore) {
				highestScore = score[loop];
			}
			if (score[loop] < lowestScore) {
				lowestScore = score[loop];
				losingPlayerNumber = loop+1;
			}
		}

		determineWinners();

		for (int n = 0; n < 4; n++) 
			players[n].die("Game Over");

		gameWriter.write(this, seqOfGames.toString());
	}

	public void nextPlayersTurn() {
		currentPlayer++;
		if (currentPlayer == 4)
			currentPlayer = 0;

		if (currentPlayer == 0) turn++;
		players[currentPlayer].takeTurn();
	}

	public boolean gameOver() {
		boolean retValue = false;
		if (cardsOnTable.get(CardType.PROVINCE) == 0)
			return true;
		int counter = 0;
		for (CardType ct : cardsOnTable.keySet()) {
			if (cardsOnTable.get(ct) == 0)
				counter++;
		}
		if (counter > 2)
			retValue = true;
		if (turn > MAX_TURNS)
			retValue = true;
		return retValue;
	}


	private void determineWinners() {
		List<Integer> tempWinners = new ArrayList<Integer>();
		for (int loop = 0; loop < 4; loop++) {
			if (score[loop] == highestScore) 
				tempWinners.add(loop+1);
		}
		winningPlayerNumbers = new int[tempWinners.size()];
		for (int i = 0; i < tempWinners.size(); i++) {
			winningPlayerNumbers[i] = tempWinners.get(i);
			players[tempWinners.get(i)-1].log("Wins Game.");
		}
		if (turn > MAX_TURNS) {
			winningPlayerNumbers = new int[0];
			losingPlayerNumber = -1;
		}
	}

	public Player[] getPlayers() {
		return players;
	}

	public Player getCurrentPlayer() {
		if (currentPlayer == -1)
			return players[0];		// i.e. fudge for when game has not yet started
		return players[currentPlayer];
	}

	private void setUpCardsOnTable(GameSetup gs) {
		cardsOnTable = new HashMap<CardType, Integer>();
		addCardType(CardType.NONE, 1);
		addCardType(CardType.PROVINCE, 12);
		addCardType(CardType.DUCHY, 12);
		addCardType(CardType.ESTATE, 12);
		addCardType(CardType.GOLD, 60);
		addCardType(CardType.SILVER, 80);
		addCardType(CardType.COPPER, 32);
		addCardType(CardType.CURSE, 30);

		for (CardType ct : gs.getCardTypes())  {
			if (!cardsOnTable.containsKey(ct)) {
				int numberToUse = 10;
				if (ct.isVictory())
					numberToUse = 12;
				addCardType(ct, numberToUse);
			}
		}

	}

	public void addCardType(CardType newCard, int number) {
		if (cardsOnTable.containsKey(newCard)) {
			cardsOnTable.put(newCard, cardsOnTable.get(newCard) + number);
		} else {
			cardsOnTable.put(newCard, number);
		}
	}

	public void removeCardType(CardType cardToRemove) {
		cardsOnTable.remove(cardToRemove);
	}

	public Set<CardType> availableCardsToPurchase() {
		Set<CardType> retValue = new HashSet<CardType>();
		for (CardType ct : cardsOnTable.keySet()) {
			if (cardsOnTable.get(ct) > 0)
				retValue.add(ct);
		}
		return retValue;
	}

	public boolean drawCard(CardType type) {
		if (cardsOnTable.containsKey(type)) {
			Integer numberLeft = cardsOnTable.get(type);
			if (numberLeft > 0) {
				numberLeft--;
				cardsOnTable.put(type, numberLeft);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}


	public int getNumberOfCardsRemaining(CardType type) {
		if (!cardsOnTable.containsKey(type)) {
			return 0;
		} else {
			return cardsOnTable.get(type);
		}
	}

	public int getUniqueId() {return id;}


	public int turnNumber() {
		return turn;
	}

	public int[] getWinningPlayers() {return winningPlayerNumbers;}
	public int getLosingPlayer() {return losingPlayerNumber;}

	public double getRelativeScore(Player p) {
		if (gameOver() && winningPlayerNumbers.length > 0) {
			int playerNumber = getPlayerNumber(p);
			if (playerNumber > 0) {
				if (highestScore - lowestScore < 0.1) return 50.0;
				return 100.0 * (score[playerNumber-1] - lowestScore) / (highestScore - lowestScore);
			}
		}
		return 0.0;
	}

	public int getPlayerNumber(Player p) {
		for (int loop = 0; loop < 4; loop++) {
			if (p == players[loop]) {
				return loop+1;
			}
		}
		return 0;
	}

	@Override
	public World getWorld() {
		return seqOfGames;
	}


	public double[] getPercentageDepleted(HashMap<CardType, Integer> cardsRemovedUnderScenario) {
		int lowest = 10;
		int nextLowest = 10;
		int thirdLowest = 10;
		for (CardType ct : cardsOnTable.keySet()) {
			if (ct == CardType.NONE) continue;
			int cardsLeft = cardsOnTable.get(ct);
			if (cardsRemovedUnderScenario.containsKey(ct)) {
				cardsLeft = cardsLeft - cardsRemovedUnderScenario.get(ct);
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
}

package hopshackle.dominion;
import hopshackle.simulation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DominionGame extends Game<Player, CardTypeAugment> implements Persistent {

	protected Player[] players;
	private HashMap<CardType, Integer> cardsOnTable;
	private Set<CardType> allStartingCardTypes = new HashSet<CardType>();
	private int currentPlayer;
	private static AtomicInteger idFountain = new AtomicInteger(1);
	private int id = idFountain.getAndIncrement();
	private int turn;
	private int losingPlayerNumber;
	private int[] winningPlayerNumbers = new int[0];
	private double highestScore, lowestScore;
	private double score[] = new double[4];
	private static DatabaseWriter<DominionGame> gameWriter = new DatabaseWriter<DominionGame>(new GameDAO());
	protected DeciderGenerator deciderGenerator;
	private final int MAX_TURNS = 200;
	private double debugGameProportion = SimProperties.getPropertyAsDouble("DominionGameDebugProportion", "0.00");
	private boolean clonedGame = false;
	private String tableSuffix;
	private FastCalendar calendar;
	private World world = new World();

	public static DominionGame againstDecider(DeciderGenerator deciderGen, String name, Decider<Player> deciderToUse) {
		DominionGame retValue = new DominionGame(deciderGen, name, false);
		int randomPlayer = Dice.roll(1, 4) - 1;
		retValue.players[randomPlayer].setDecider(deciderToUse);
		return retValue;
	}

	public DominionGame(DeciderGenerator deciderGen, String name, boolean paceSetters) {
		deciderGenerator = deciderGen;
		this.tableSuffix = name;
		turn = 1;
		players = new Player[4];
		boolean debugGame = false;
		calendar = new FastCalendar(0l);
		setUpCardsOnTable(deciderGenerator.getGameSetup());
		if (Math.random() < debugGameProportion) debugGame = true;
		for (int n = 0; n < players.length; n++) {
			players[n] = new Player(this, n+1);
			players[n].setDebugLocal(debugGame);
			if (deciderGenerator != null) {
				Decider<Player> pd = deciderGenerator.getPurchaseDecider(paceSetters);
				DominionDeciderContainer ddc = new DominionDeciderContainer(
						pd, 
						deciderGenerator.getActionDecider());
				ddc.setName(pd.toString());
				players[n].setDecider(ddc);
			}
			players[n].setGame(this);
		}
		for (int n = 0; n < players.length; n++)
			players[n].refreshPositionSummary();
		currentPlayer = 0;
		players[0].setState(Player.State.PLAYING);
	}

	protected DominionGame(DominionGame master) {
		deciderGenerator = master.deciderGenerator;
		players = new Player[4];
		long currentTime = master.calendar.getTime();
		calendar = new FastCalendar(currentTime);
		cardsOnTable = new HashMap<CardType, Integer>();
		for (CardType ct : master.cardsOnTable.keySet()) {
			cardsOnTable.put(ct, master.getNumberOfCardsRemaining(ct));
		}
		currentPlayer = master.currentPlayer;
		allStartingCardTypes = master.allStartingCardTypes;
		turn = master.turn;
		clonedGame = true;

		for (int i = 0; i < 4; i++)
			players[i] = master.players[i].clone(this);
		for (int i = 0; i < master.actionStack.size(); i++) {
			Player oldPlayer = master.actionStack.get(i).getActor();
			int playerNumber = master.getPlayerNumber(oldPlayer) - 1;
			actionStack.push(master.actionStack.get(i).clone(players[playerNumber]));
		}
		for (int n = 0; n < players.length; n++)
			players[n].refreshPositionSummary();
	}

	@Override
	public Player getCurrentPlayer() {
		return players[currentPlayer];
	}

	@Override
	public List<ActionEnum<Player>> getPossibleCurrentActions() {
		return players[currentPlayer].getDecider().getChooseableOptions(players[currentPlayer]);
	}

	@Override
	public void updateGameStatus() {
		// We should only be here when we have finished a players last action
		// If the current player is taking actions, we move to purchasing
		// if the current player is purchasing, we move to the next player
		Player p = players[currentPlayer];

		switch(p.getPlayerState()) {
		case WAITING:	
			throw new AssertionError("Current Player State should never be WAITING");
		case PLAYING:
			if (p.getActionsLeft() < 0) 
				throw new AssertionError("Actions Left should never be negative");
			if (p.getActionsLeft() == 0) {
				p.setState(Player.State.PURCHASING);
				String buys = " buys";
				if (p.getBuys() == 1) buys = " buy";
				p.log("Has budget of " + p.getBudget() + " with " + p.getBuys() + buys);
			} else {
				// do nothing...we continue in the current state until all actions used
			}
			break;
		case PURCHASING:
			p.tidyUp();
			p.setState(Player.State.WAITING);
			if (currentPlayer == 3) turn++;
			calendar.setTime((long) ((turn-1) * 4 + currentPlayer));
			currentPlayer++;
			if (currentPlayer == 4)
				currentPlayer = 0;

			players[currentPlayer].setState(Player.State.PLAYING);
		}
	}

	public boolean gameOver() {
		boolean retValue = false;
		if (cardsOnTable.get(CardType.PROVINCE) == 0)
			retValue = true;
		int counter = 0;
		for (CardType ct : cardsOnTable.keySet()) {
			if (cardsOnTable.get(ct) == 0)
				counter++;
		}
		if (counter > 2)
			retValue = true;
		if (turn > MAX_TURNS)
			retValue = true;
		if (retValue == true) {
			endOfGameHouseKeeping();
		}
		return retValue;
	}

	private void endOfGameHouseKeeping() {
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

		if (!clonedGame) {
			gameWriter.write(this, tableSuffix);
		}
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

	@Override
	public List<Player> getAllPlayers() {
		List<Player> retValue = new ArrayList<Player>();
		for (Player p : players)
			retValue.add(p);
		return retValue;
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
			allStartingCardTypes.add(newCard);
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

	public Set<CardType> startingCardTypes() {
		Set<CardType> retValue = new HashSet<CardType>();
		for (CardType ct : allStartingCardTypes) {
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
	public Player getPlayer(int n) {
		return players[n-1];
	}

	@Override
	public DominionGame clone(Player perspectivePlayer) {
		int perspective = perspectivePlayer.getGame().getPlayerNumber(perspectivePlayer);
		DominionGame newGame = new DominionGame(this);
		for (int i = 1; i <= 4; i++) {
			Player p = newGame.getPlayer(i);
			if (i != perspective) {
				// We know what is in the discard pile, but not the distribution between Hand and Deck
				// Technically we might know a bit more (e.g. from Bureaucrats, Spies, Moats revealed defensively). TODO: 
				// Also some stuff could be inferred from discards (Militia).
				// Could add some form of opponent information to Player at a later point
				p.shuffleDeckAndHandTogether();
			} else {
				// while the perspective player does not know the contents of the Deck (in most, albeit not all cases)
				// TODO: for track of known features of Deck that could be tracked
				p.shuffleDeck();
			}
		}
		return newGame;
	}

	@Override
	public int getCurrentPlayerNumber() {
		return currentPlayer + 1;
	}

	@Override
	public World getWorld() {
		return world;
	}

	public void setDatabaseAccessUtility(DatabaseAccessUtility databaseUtility) {
		world.setDatabaseAccessUtility(databaseUtility);
	}

	public void nextPlayersTurn() {
		int cp = getCurrentPlayerNumber();
		do {
			this.oneAction(false);
		} while (getCurrentPlayerNumber() == cp);
	}
}

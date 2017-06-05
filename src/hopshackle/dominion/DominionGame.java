package hopshackle.dominion;
import hopshackle.simulation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DominionGame extends Game<Player, CardTypeAugment> implements Persistent {

	private static AtomicInteger idFountain = new AtomicInteger(1);
	private static int[] turnsToRecord;
	private static DatabaseWriter<DominionGame>[] gameWriters;

	static {
		String[] toRecord = SimProperties.getProperty("DominionTurnsToRecord", "4:16").split(":");
		turnsToRecord = new int[toRecord.length +1];
		gameWriters = new DatabaseWriter[toRecord.length +1];
		for (int i = 0; i < toRecord.length; i++) {
			turnsToRecord[i] = Integer.valueOf(toRecord[i]);
			gameWriters[i] = new DatabaseWriter<DominionGame>(new GameDAO());
		}
		gameWriters[toRecord.length] = new DatabaseWriter<DominionGame>(new GameDAO());
	}

	protected Player[] players;
	private HashMap<CardType, Integer> cardsOnTable;
	private Set<CardType> allStartingCardTypes = new HashSet<CardType>();
	private int currentPlayer;
	private int id = idFountain.getAndIncrement();
	private int turn;
	private double score[] = new double[4];
	protected DeciderGenerator deciderGenerator;
	private final int MAX_TURNS = 200;
	private double debugGameProportion = SimProperties.getPropertyAsDouble("DominionGameDebugProportion", "0.00");
	private double debugRolloutGameProportion = SimProperties.getPropertyAsDouble("DominionRolloutGameDebugProportion", "0.00");
	private boolean clonedGame = false;
	private String tableSuffix;
	private FastCalendar calendar;
	private World world = new World();
	private int[] ordinalPositions = new int[4];
	private long startTime, endTime;

	public static DominionGame againstDecider(DeciderGenerator deciderGen, String name, Decider<Player> deciderToUse) {
		DominionGame retValue = new DominionGame(deciderGen, name, false);
		int randomPlayer = Dice.roll(1, 4) - 1;
		retValue.players[randomPlayer].setDecider(deciderToUse);
		return retValue;
	}

	public DominionGame(DeciderGenerator deciderGen, String name, boolean paceSetters) {
		Agent.clearAndResetCacheBuffer(0);
		deciderGenerator = deciderGen;
		this.tableSuffix = name;
		turn = 1;
		startTime = System.currentTimeMillis();
		players = new Player[4];
		boolean debugGame = false;
		calendar = new FastCalendar(1l);
		world.setCalendar(calendar);
		world.registerWorldLogic(this, "AGENT");
		setUpCardsOnTable(deciderGenerator.getGameSetup());
		if (Math.random() < debugGameProportion) debugGame = true;
		for (int n = 0; n < players.length; n++) {
			players[n] = new Player(this, n+1);
			players[n].setDebugLocal(debugGame);
			if (deciderGenerator != null) {
				Decider<Player> d = deciderGenerator.getDecider(paceSetters);
				players[n].setDecider(d);
	//			System.out.println("Setting decider for " + players[n].toString() + " to " + d.toString());
				players[n].log("Using Decider " + d.toString());
			}
			players[n].setGame(this);
		}
		// We then need to refresh the PS of the players, because victory margin is dependent on
		// the other players, so we need all of them initialised
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
		world.setCalendar(calendar);
		cardsOnTable = new HashMap<CardType, Integer>();
		for (CardType ct : master.cardsOnTable.keySet()) {
			cardsOnTable.put(ct, master.getNumberOfCardsRemaining(ct));
		}
		currentPlayer = master.currentPlayer;
		allStartingCardTypes = master.allStartingCardTypes;
		turn = master.turn;
		clonedGame = true;
		boolean debugGame = false;
		if (Math.random() < debugRolloutGameProportion) debugGame = true;
		
		for (int i = 0; i < 4; i++) {
			players[i] = master.players[i].clone(this);
			players[i].setDebugLocal(debugGame);
		}
		for (int i = 0; i < master.actionStack.size(); i++) {
			Player oldPlayer = master.actionStack.get(i).getActor();
			int playerNumber = master.getPlayerNumber(oldPlayer);
			actionStack.push(master.actionStack.get(i).clone(players[playerNumber-1]));
		}
//		for (int n = 0; n < players.length; n++) {
//			players[n].refreshPositionSummary();
//		}
	}

	@Override
	public Player getCurrentPlayer() {
		return players[currentPlayer];
	}

	public List<ActionEnum<Player>> dominionPurchaseOptions(Player player) {
		int maxCardsPerBuy = player.getDecider().getProperties().getPropertyAsInteger("DominionMaxCardsPerBuy", "1");
		int cardsForThisBuy = Math.min(maxCardsPerBuy, player.getBuys());
		List<ActionEnum<Player>> retValue = null;
		DominionBuyingDecision dpd = new DominionBuyingDecision(player, player.getBudget(), cardsForThisBuy);
		retValue = dpd.getPossiblePurchasesAsActionEnum();
		return retValue;
	}
	public List<ActionEnum<Player>> dominionPlayOptions(Player player) {
		List<ActionEnum<Player>> retValue = null;
		retValue = player.getActionsInHand();
		retValue.add(CardTypeAugment.playCard(CardType.NONE));
		return retValue;
	}

	@Override
	public List<ActionEnum<Player>> getPossibleActions(Player p) {
		List<ActionEnum<Player>> retValue = new ArrayList<ActionEnum<Player>>();
		switch (p.getPlayerState()) {
		case PURCHASING:
			retValue = dominionPurchaseOptions(p);
			break;
		case PLAYING:
			retValue = dominionPlayOptions(p);
			break;
		case WAITING:
			throw new AssertionError("No valid options if current player is WAITING");
		}
		return retValue;
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
			if (p.getBuys() < 0) 
				throw new AssertionError("Buys Left should never be negative");
			if (p.getBuys() == 0) { // have finished
				p.tidyUp();
				p.setState(Player.State.WAITING);
				if (currentPlayer == 3) {
					for (int i = 0; i < turnsToRecord.length; i++) {
						if (!clonedGame && turnsToRecord[i] == turn) {
							endTime = System.currentTimeMillis();
							gameWriters[i].write(this, tableSuffix + "_" + turnsToRecord[i]);
						}
					}
					turn++;
				}
				currentPlayer++;
				if (currentPlayer == 4)
					currentPlayer = 0;
				calendar.setTime((long) ((turn-1) * 4 + currentPlayer + 1));
				players[currentPlayer].setState(Player.State.PLAYING);
			} else {
				// do nothing...we continue in the current state until all buys used
			}
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
		return retValue;
	}

	@Override
	protected void endOfGameHouseKeeping() {
		if (!gameOver()) 
			throw new AssertionError("Cannot do housekeeping unless game is over");
		for (int i = 0; i < 4; i++)
			score[i] = getPlayer(i+1).totalVictoryValue();

		for (int i = 0; i < 4; i++)
			ordinalPositions[i] = getOrdinalPosition(i+1);

		for (int n = 0; n < 4; n++) {
			players[n].log(String.format("Final Score is %d, with utility of %.0f.", players[n].totalVictoryValue(),  players[n].getScore()));
			if (getOrdinalPosition(n+1) == 1) 
				players[n].log("Wins Game!");
			else 
				players[n].log("Ends game at position " + getOrdinalPosition(n+1));
		}

		if (!clonedGame) {
			endTime = System.currentTimeMillis();
			gameWriters[turnsToRecord.length-1].write(this, tableSuffix);
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

	public int getOrdinalPosition(int p) {
		// we count how many scores are less than or equal to the players score
		int retValue = 5;
		for (double s : score) {
			if (s <= score[p-1]) retValue--;
		}
		return retValue;
	}
	public int getNumberOfPlayersInOrdinalPosition(int i) {
		int retValue = 0;
		for (int j = 0; j < 4; j++) {
			if (ordinalPositions[j] == i) retValue++;
		}
		return retValue;
	}
	public int getPlayerInOrdinalPosition(int i) {
		for (int j = 3; j >=0; j--)
			if (ordinalPositions[j] == i) return j+1;
		return 0;
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
			if (p.getDebugLocal()) {
				p.logCurrentState();
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
			this.oneAction();
		} while (getCurrentPlayerNumber() == cp);
	}
	public double gameLength() {
		return (double) ((endTime - startTime) / 1000);
	}

	public String getRef() {return String.valueOf(id);}
}

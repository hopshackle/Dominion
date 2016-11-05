package hopshackle.dominion;

import hopshackle.simulation.DAO;

public class GameDAO implements DAO<DominionGame> {

	@Override
	public String getTableCreationSQL(String tableSuffix) {

		return  "CREATE TABLE IF NOT EXISTS DomAllGames_" + tableSuffix +
		" ( id 				INT			PRIMARY KEY,"		+
		" turns				INT			NOT NULL,"		+
		" winningScore		INT			NOT NULL,"		+
		" winningPlayer		INT			NOT NULL,"		+
		" lowestScore		INT			NOT NULL,"		+
		" provinces		 	INT			NOT NULL,"		+
		" duchies	 		INT			NOT NULL,"		+
		" estates			INT			NOT NULL,"		+
		" curses			INT			NOT NULL,"		+
		" adventurers		INT			NOT NULL,"		+
		" bureaucrats		INT			NOT NULL,"		+
		" cellars			INT			NOT NULL,"		+
		" chancellors		INT			NOT NULL,"		+
		" chapels			INT			NOT NULL,"		+
		" council_rooms		INT			NOT NULL,"		+
		" feasts			INT			NOT NULL,"		+
		" festivals			INT			NOT NULL,"		+
		" gardens			INT			NOT NULL,"		+
		" laboratories		INT			NOT NULL,"		+
		" libraries			INT			NOT NULL,"		+
		" markets			INT			NOT NULL,"		+
		" militia			INT			NOT NULL,"		+
		" mines				INT			NOT NULL,"		+
		" moats				INT			NOT NULL,"		+
		" moneylenders		INT			NOT NULL,"		+
		" remodels			INT			NOT NULL,"		+
		" smithies			INT			NOT NULL,"		+
		" spies				INT			NOT NULL,"		+
		" thieves			INT			NOT NULL,"		+
		" throne_rooms		INT			NOT NULL,"		+
		" villages			INT			NOT NULL,"		+
		" witches			INT			NOT NULL,"		+
		" woodcutters		INT			NOT NULL,"		+
		" workshops			INT			NOT NULL,"		+
		" avgDeckSize		FLOAT		NOT NULL,"		+
		" avgTreasure		FLOAT		NOT NULL,"		+
		" avgVictory		FLOAT		NOT NULL,"		+
		" winningPStrategy   VARCHAR(30) NOT NULL,"		+
		" winningAStrategy   VARCHAR(30) NOT NULL"		+
		");";
	}

	@Override
	public String getTableDeletionSQL(String tableSuffix) {
		return "DROP TABLE IF EXISTS DomAllGames_" + tableSuffix + ";";
	}

	@Override
	public String getTableUpdateSQL(String tableSuffix) {
		return "INSERT INTO DomAllGames_" + tableSuffix + 
		" (id,  turns, winningScore, winningPlayer, lowestScore, provinces, duchies, estates, curses, " +
			"adventurers, bureaucrats, cellars, chapels, chancellors, council_rooms, feasts, festivals, gardens, laboratories, " +
			"libraries, markets, militia, mines, moats, moneylenders, remodels, " +
			"smithies, spies, thieves, throne_rooms, villages, witches, woodcutters, workshops, " + 
			"avgDeckSize, avgTreasure, avgVictory, winningPStrategy, winningAStrategy) VALUES";
	}

	@Override
	public String getValues(DominionGame game) {

		Player[] players = game.getAllPlayers().toArray(new Player[1]);
		double winningScore = -100, lowestScore, avgScore = 0, avgTreasure = 0, avgDeckSize = 0.0;
		int winningPlayerNumber = -1;
		lowestScore = 100.0;
		for (int loop = 0; loop < 4; loop++) {
			double score = players[loop].totalVictoryValue();
			avgScore += score;
			avgTreasure += players[loop].totalTreasureValue();
			avgDeckSize += players[loop].totalNumberOfCards();
			if (score > winningScore) {
				winningScore = score;
			}
			if (score < lowestScore)
				lowestScore = score;
		}
		avgScore /= 4.0;
		avgTreasure /= 4.0;
		avgDeckSize /= 4.0;

		int numberOfWinningPlayers = game.getWinningPlayers().length;
		boolean gameHasWinner = (numberOfWinningPlayers > 0);
		if (gameHasWinner) {
			winningPlayerNumber = game.getWinningPlayers()[numberOfWinningPlayers-1];
			// on the basis that we want to pick the winning player with least first player advantage
		}

		return String.format(" (%d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %d, %.2f, %.2f, %.2f, '%s', '%s')",
				game.getUniqueId(),
				game.turnNumber(),
				(int) winningScore,
				winningPlayerNumber,
				(int) lowestScore,
				game.getNumberOfCardsRemaining(CardType.PROVINCE),
				game.getNumberOfCardsRemaining(CardType.DUCHY),	
				game.getNumberOfCardsRemaining(CardType.ESTATE),
				game.getNumberOfCardsRemaining(CardType.CURSE),
				game.getNumberOfCardsRemaining(CardType.ADVENTURER),
				game.getNumberOfCardsRemaining(CardType.BUREAUCRAT),
				game.getNumberOfCardsRemaining(CardType.CELLAR),
				game.getNumberOfCardsRemaining(CardType.CHAPEL),
				game.getNumberOfCardsRemaining(CardType.CHANCELLOR),
				game.getNumberOfCardsRemaining(CardType.COUNCIL_ROOM),
				game.getNumberOfCardsRemaining(CardType.FEAST),
				game.getNumberOfCardsRemaining(CardType.FESTIVAL),
				game.getNumberOfCardsRemaining(CardType.GARDENS),
				game.getNumberOfCardsRemaining(CardType.LABORATORY),
				game.getNumberOfCardsRemaining(CardType.LIBRARY),
				game.getNumberOfCardsRemaining(CardType.MARKET),
				game.getNumberOfCardsRemaining(CardType.MILITIA),
				game.getNumberOfCardsRemaining(CardType.MINE),
				game.getNumberOfCardsRemaining(CardType.MOAT),
				game.getNumberOfCardsRemaining(CardType.MONEYLENDER),
				game.getNumberOfCardsRemaining(CardType.REMODEL),
				game.getNumberOfCardsRemaining(CardType.SMITHY),
				game.getNumberOfCardsRemaining(CardType.SPY),
				game.getNumberOfCardsRemaining(CardType.THIEF),
				game.getNumberOfCardsRemaining(CardType.THRONE_ROOM),
				game.getNumberOfCardsRemaining(CardType.VILLAGE),
				game.getNumberOfCardsRemaining(CardType.WITCH),
				game.getNumberOfCardsRemaining(CardType.WOODCUTTER),
				game.getNumberOfCardsRemaining(CardType.WORKSHOP),
				avgTreasure,
				avgDeckSize,
				avgScore,
				(gameHasWinner)?players[winningPlayerNumber-1].getPurchaseDecider().toString():"NONE",
				(gameHasWinner)?players[winningPlayerNumber-1].getActionDecider().toString():"NONE"
		);

	}

}

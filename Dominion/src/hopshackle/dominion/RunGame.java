package hopshackle.dominion;

import hopshackle.simulation.*;

public class RunGame extends World {

	private static boolean learningOn = SimProperties.getProperty("DominionLearningOn", "true").equals("true");
	private static boolean extraLastK = SimProperties.getProperty("DominionLastThousandForScoring", "true").equals("true");
	private DeciderGenerator dg;
	private int finalScoring = extraLastK ? 1000 : 0;
	private long count, maximum;

	public static void main(String[] args) {

		String nameOfRun = HopshackleUtilities.getArgument(args, 0, "FGNoBM_R_");
		int firstSuffix = HopshackleUtilities.getArgument(args, 1, 1);
		int secondSuffix = HopshackleUtilities.getArgument(args, 2, 100);
		int numberOfGames = HopshackleUtilities.getArgument(args, 3, 5000);
		int numberOfDeciders = HopshackleUtilities.getArgument(args, 4, 1);
		int gamesPerCycle = HopshackleUtilities.getArgument(args, 5, 101000);
		int numberToAddPerCycle = HopshackleUtilities.getArgument(args, 6, 0);
		int numberToRemovePerCycle = HopshackleUtilities.getArgument(args, 7, 0);

		int sequencesToRun = secondSuffix - firstSuffix + 1;
		int iteration = 0;
		double rdmc = 0.00;
		try {
			do {
				String name = nameOfRun;
				if (sequencesToRun > 1) {
					rdmc += 0.005;
					//		numberOfGames += 250;
					name = name + (firstSuffix + iteration);
					//		SimProperties.setProperty("Alpha", String.valueOf(rdmc));
				}
				System.out.println("Starting Game " + (iteration+firstSuffix));
				RunGame setOfGames = new RunGame(name, numberOfGames, numberOfDeciders, gamesPerCycle, numberToRemovePerCycle, numberToAddPerCycle);
				setOfGames.runAll();
				iteration++;
			} while (iteration < sequencesToRun);
		} catch (Error e) {
			System.out.println(e.toString());
			e.printStackTrace(System.out);
		}
	}

	public RunGame(String descriptor, int games, DeciderGenerator providedDG) {
		super(null, descriptor, games);
		maximum = games;
		this.setCalendar(new FastCalendar(0));
		DatabaseAccessUtility databaseUtility = new DatabaseAccessUtility();
		setDatabaseAccessUtility(databaseUtility);
		Thread t = new Thread(databaseUtility);
		t.start();
	}
	
	public RunGame(String descriptor, int games, int deciders, int cycleSize, int removalsPerCycle, int additionsPerCycle) {
		this(descriptor, games, newDG(deciders, cycleSize, removalsPerCycle, additionsPerCycle));
	}
	private static DeciderGenerator newDG(int deciders, int cycleSize, int removalsPerCycle, int additionsPerCycle) {
		GameSetup gamesetup = new GameSetup();
		DeciderGenerator newDG = new DeciderGenerator(gamesetup, deciders, cycleSize, removalsPerCycle, additionsPerCycle);
		if (deciders > 1)
			newDG.useDecidersEvenly(true);
		return newDG;
	}
	
	public void runAll() {
		do {
			if (learningOn)
				runNextGameWithLearning();
			else
				runNextGameWithoutLearning();
		} while (!finishedLearningRun());
		if (extraLastK) {
			System.out.println("Finished Learning Run - starting last 1000 games");
			do {
				runNextGameWithoutLearning();
			} while (!finishedRun());
		}
		dg.recordBestBrains(toString());
	}

	public void runNextGameWithLearning() {
		Game game = new Game(this);
		ExperienceRecordCollector<Player> erc = new ExperienceRecordCollector<Player>();
		OnInstructionTeacher<Player> teacher = new OnInstructionTeacher<Player>();
		for (Player p : game.getPlayers()) {
			erc.registerAgent(p);
		}
		teacher.registerToERStream(erc);
		runGame(game);
		teacher.teach();
	}

	public void runNextGameWithoutLearning() {
		Game game = new Game(this);
		runGame(game);
	}

	private void runGame(Game game) {
		count++;
		setCurrentTime(count);
		maintenance();

		game.playGame();

		Player[] players = game.getPlayers();
		for (int p : game.getWinningPlayers()) {
			players[p-1].log("Wins Game!");
			if (dg != null)
				dg.reportVictory(players[p-1]);
		}

		if (maximum + finalScoring > count)
			return;

		worldDeath();
	}

	public boolean finishedRun() {
		return (maximum + finalScoring <= count);
	}

	public boolean finishedLearningRun() {
		return (maximum <= count);
	}

	public void worldDeath() {
		super.worldDeath();
		updateDatabase("EXIT");
	}

	public DeciderGenerator getDeciderDenerator() {
		return dg;
	}
}

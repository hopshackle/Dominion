package hopshackle.dominion;

import java.util.*;

import hopshackle.simulation.*;

public class RunGame extends World {

	private static boolean learningOn = SimProperties.getProperty("DominionLearningOn", "true").equals("true");
	private static boolean extraLastK = SimProperties.getProperty("DominionLastThousandForScoring", "true").equals("true");
	private static String teachingStrategy = SimProperties.getProperty("DominionTeachingStrategy", "AllPlayers");
	private static boolean useBigMoneyInLastK = SimProperties.getProperty("DominionBigMoneyBenchmarkWithNoLearning", "false").equals("true");
	private static int pastSetsToIncludeInTraining = SimProperties.getPropertyAsInteger("DominionPastSetsToIncludeInTraining", "0");
	private static int gamesPerSet = SimProperties.getPropertyAsInteger("DominionGamesPerSet", "1");
	private boolean addPaceSetters = SimProperties.getProperty("DominionAddPacesetters", "false").equals("true");
	private boolean trainAllDeciders = SimProperties.getProperty("DominionTrainAll", "false").equals("true");
	private String baseDir = SimProperties.getProperty("BaseDirectory", "C:");
	private DeciderGenerator dg;
	private int finalScoring = extraLastK ? 1000 : 0;
	private long count, maximum;
	private DominionLookaheadFunction lookahead = new DominionLookaheadFunction();
	private ExperienceRecordFactory<Player> factory = new LookaheadERFactory<Player>(lookahead);
	private Map<String, ExperienceRecordCollector<Player>> ercMap = new HashMap<String, ExperienceRecordCollector<Player>>();
	private Map<String, OnInstructionTeacher<Player>> teacherMap = new HashMap<String, OnInstructionTeacher<Player>>();

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
		try {
			do {
				String name = nameOfRun;
				if (sequencesToRun > 1) {
					name = name + (firstSuffix + iteration);
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
		dg = providedDG;
		maximum = games;
		for (LookaheadDecider<Player> d : dg.getAllPurchaseDeciders()) {
			ercMap.put(d.toString(), new ExperienceRecordCollector<Player>(factory));
			OnInstructionTeacher<Player> teacher = new OnInstructionTeacher<Player>(pastSetsToIncludeInTraining);
			teacher.registerToERStream(ercMap.get(d.toString()));
			teacherMap.put(d.toString(), teacher);
			if (trainAllDeciders) {
				for (Decider<Player> d2 : dg.getAllPurchaseDeciders())
					teacher.registerDecider(d2);
			} else {
				teacher.registerDecider(d);
			}
		}
		
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
			if (learningOn) {
				runNextSet(gamesPerSet);
			} else
				runNextGameWithoutLearning();
		} while (!finishedLearningRun());
		if (extraLastK) {
			System.out.println("Finished Learning Run - starting last 1000 games");
			do {
				runNextGameWithoutLearning();
			} while (!finishedRun());
		}
		dg.recordBestBrains(toString(), baseDir + "\\recordedBrains");
	}

	private void runNextSet(int numberOfGames) {
		for (int i = 0; i < numberOfGames; i++) {
			Game game = new Game(this, addPaceSetters);
			for (Player p : game.getPlayers()) {
				LookaheadDecider<Player> pd = p.getPositionDecider();
				switch(teachingStrategy) {
				case "AllPlayers" :
					for (Player p2 : game.getPlayers())  {
						ercMap.get(pd.toString()).registerAgent(p2);
					}
					break;
				case "SelfOnly" :
					ercMap.get(pd.toString()).registerAgent(p);
					break;
				default:
					throw new AssertionError("Unknown teaching strategy: " + teachingStrategy);
				}
			}
			runGame(game);
		}
		for (OnInstructionTeacher<Player> teacher : teacherMap.values()) {
			teacher.teach();
		}
	}

	private void runNextGameWithoutLearning() {
		Game game = null;
		if (useBigMoneyInLastK) {
			game = Game.againstDecider(this, dg.bigMoney);
		} else {
			game = new Game(this, false);
		}
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

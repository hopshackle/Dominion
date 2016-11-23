package hopshackle.dominion;

import java.util.*;

import hopshackle.simulation.*;

public class RunGame {

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
	private ExperienceRecordFactory<Player> factory = new StandardERFactory<Player>();
	private Map<String, ExperienceRecordCollector<Player>> ercMap = new HashMap<String, ExperienceRecordCollector<Player>>();
	private Map<String, OnInstructionTeacher<Player>> teacherMap = new HashMap<String, OnInstructionTeacher<Player>>();
	private DatabaseAccessUtility databaseUtility;
	private String name;

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
		dg = providedDG;
		maximum = games;
		name = descriptor;
		EventFilter purchaseEventFilter = new EventFilter() {
			@Override
			public boolean ignore(AgentEvent event) {
				Action<?> action = event.getAction();
				if (action == null || action instanceof DominionBuyAction)
					return false;
				return true;
			}
		};
		EventFilter actionEventFilter = new EventFilter() {
			@Override
			public boolean ignore(AgentEvent event) {
				Action<?> action = event.getAction();
				if (action == null || action instanceof DominionPlayAction)
					return false;
				return true;
			}
		};
		for (Decider<Player> d : dg.getAllPurchaseDeciders()) {
			ercMap.put(d.toString(), new ExperienceRecordCollector<Player>(factory, purchaseEventFilter));
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
		for (Decider<Player> d : dg.getAllActionDeciders()) {
			ercMap.put(d.toString(), new ExperienceRecordCollector<Player>(factory, actionEventFilter));
			OnInstructionTeacher<Player> teacher = new OnInstructionTeacher<Player>(pastSetsToIncludeInTraining);
			teacher.registerToERStream(ercMap.get(d.toString()));
			teacherMap.put(d.toString(), teacher);
			if (trainAllDeciders) {
				for (Decider<Player> d2 : dg.getAllActionDeciders())
					teacher.registerDecider(d2);
			} else {
				teacher.registerDecider(d);
			}
		}
		
		databaseUtility = new DatabaseAccessUtility();
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
		dg.recordBestPurchaseBrains(toString(), baseDir + "\\recordedBrains");
		databaseUtility.addUpdate("EXIT");
	}

	private void runNextSet(int numberOfGames) {
		for (int i = 0; i < numberOfGames; i++) {
			DominionGame game = new DominionGame(this.getDeciderDenerator(), this.name, addPaceSetters);
			game.setDatabaseAccessUtility(databaseUtility);
			for (Player p : game.getAllPlayers()) {
				Decider<Player> decider = p.getDecider();
				switch(teachingStrategy) {
				case "AllPlayers" :
					for (Player p2 : game.getAllPlayers())  {
						ercMap.get(decider.toString()).registerAgent(p2);
					}
					break;
				case "SelfOnly" :
					ercMap.get(decider.toString()).registerAgent(p);
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
		DominionGame game = null;
		if (useBigMoneyInLastK) {
			game = DominionGame.againstDecider(getDeciderDenerator(), name, dg.bigMoney);
		} else {
			game = new DominionGame(getDeciderDenerator(), name, false);
		}
		game.setDatabaseAccessUtility(databaseUtility);
		runGame(game);
	}

	private void runGame(DominionGame game) {
		count++;
		game.playGame();

		Player[] players = game.getAllPlayers().toArray(new Player[1]);
		for (int p : game.getWinningPlayers()) {
			players[p-1].log("Wins Game!");
			if (dg != null)
				dg.reportVictory(players[p-1]);
		}

		if (maximum + finalScoring > count)
			return;
	}

	public boolean finishedRun() {
		return (maximum + finalScoring <= count);
	}

	public boolean finishedLearningRun() {
		return (maximum <= count);
	}

	public DeciderGenerator getDeciderDenerator() {
		return dg;
	}

	public DatabaseAccessUtility getDatabaseUtility() {
		return databaseUtility;
	}
	
	@Override
	public String toString() {
		return name;
	}
}

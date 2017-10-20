package hopshackle.dominion;

import java.util.*;

import hopshackle.simulation.*;

public class RunGame {

    private boolean useBigMoneyInLastK, bigMoneyCombinations;
    private static int gamesPerSet;
    private static boolean addPaceSetters;
    private static boolean trainAllDeciders;
    private static boolean hardCodedActionDecider;
    private DeciderGenerator dg;
    private int finalScoring;
    private long count, maximum;
    private ExperienceRecordFactory<Player> factory;
    private Map<String, ExperienceRecordCollector<Player>> ercMainMap = new HashMap<String, ExperienceRecordCollector<Player>>();
    private Map<String, ExperienceRecordCollector<Player>> ercPurcOnlyMap = new HashMap<String, ExperienceRecordCollector<Player>>();

    private Map<String, OnInstructionTeacher<Player>> teacherMap = new HashMap<String, OnInstructionTeacher<Player>>();
    private DatabaseAccessUtility databaseUtility;
    private String name;

    public static void main(String[] args) {

        String nameOfRun = HopshackleUtilities.getArgument(args, 0, "FGNoBM_R_");
        int firstSuffix = HopshackleUtilities.getArgument(args, 1, 1);
        int secondSuffix = HopshackleUtilities.getArgument(args, 2, 100);
        int numberOfGames = HopshackleUtilities.getArgument(args, 3, 5000);
        int numberOfScoringGames = HopshackleUtilities.getArgument(args, 4, 1000);
        String propertiesFile = HopshackleUtilities.getArgument(args, 5, "");

        if (propertiesFile != "")
            SimProperties.setFileLocation(propertiesFile);

        ParameterSearch ps = new ParameterSearch(nameOfRun);

        do { // outer loop for parameter search
            ps.setParameterSearchValues();

            gamesPerSet = SimProperties.getPropertyAsInteger("DominionGamesPerSet", "1");
            addPaceSetters = SimProperties.getProperty("DominionAddPacesetters", "false").equals("true");
            trainAllDeciders = SimProperties.getProperty("DominionTrainAll", "false").equals("true");
            hardCodedActionDecider = SimProperties.getProperty("DominionHardCodedActionDecider", "false").equals("true");

            int sequencesToRun = secondSuffix - firstSuffix + 1;
            int iteration = 0;
            List<String> tableNames = new ArrayList<>();
            try {
                do {
                    String name = nameOfRun;
                    if (sequencesToRun > 1) {
                        name = name + (firstSuffix + iteration);
                    }
                    tableNames.add("DomAllGames_" + name);
                    System.out.println("Starting Set " + (iteration + firstSuffix) + ", PS: " + ps.getIteration());
                    GameSetup gamesetup = new GameSetup();
                    DeciderGenerator newDG = new DeciderGenerator(name, gamesetup);
                    RunGame setOfGames = new RunGame(name, numberOfGames, numberOfScoringGames, newDG);
                    setOfGames.runAll();
                    iteration++;
                } while (iteration < sequencesToRun);
            } catch (Error e) {
                System.out.println(e.toString());
                e.printStackTrace(System.out);
            }

            ps.calculateAndWriteScore(tableNames);
            DominionGame.resetDatabase(); // this will ensure that the tables are dropped on the next run, rather than being added to
        } while (!ps.complete());
        System.out.println("Exiting....");
        System.exit(0);
    }

    public RunGame(String descriptor, int games, int scoringGames, DeciderGenerator providedDG) {
        dg = providedDG;
        maximum = games;
        finalScoring = scoringGames;
        name = descriptor;
        EventFilter followOnFilter = new EventFilter() {
            @Override
            public boolean ignore(AgentEvent event) {
                return (event.getAction() == null) ? false : event.getAction().hasNoAssociatedDecision();
            }
        };
        EventFilter purchaseEventFilter = new EventFilter() {
            @Override
            public boolean ignore(AgentEvent event) {
                DominionAction action = (DominionAction) event.getAction();
                if (action == null || !action.isAction())
                    return followOnFilter.ignore(event);
                return true;
            }
        };

        for (Decider<Player> d : dg.getAllDeciders()) {
            DeciderProperties localProp = d.getProperties();
            int sets = localProp.getPropertyAsInteger("DominionPastSetsToIncludeInTraining", "0");
            factory = new StandardERFactory<Player>(localProp);
            ercMainMap.put(d.toString(), new ExperienceRecordCollector<Player>(factory, followOnFilter));
            ercPurcOnlyMap.put(d.toString(), new ExperienceRecordCollector<Player>(factory, purchaseEventFilter));
            boolean excludeSingleChoiceRecordFromTeaching = localProp.getProperty("ExcludeSingleChoiceExperienceRecords", "false").equals("true");
            OnInstructionTeacher<Player> teacher = new OnInstructionTeacher<Player>(sets, excludeSingleChoiceRecordFromTeaching);
            // each teacher can only have one ER stream registered
            teacher.registerToERStream(ercMainMap.get(d.toString()));
            if (hardCodedActionDecider) // only use purchase actions for training
                teacher.registerToERStream(ercPurcOnlyMap.get(d.toString()));
            teacherMap.put(d.toString(), teacher);
            if (trainAllDeciders) {
                for (Decider<Player> d2 : dg.getAllDeciders())
                    teacher.registerDecider(d2);
            } else {
                teacher.registerDecider(d);
            }
        }

        databaseUtility = new DatabaseAccessUtility();
        Thread t = new Thread(databaseUtility);
        t.start();
    }

    public void runAll() {
        runAll(null);
    }

    public void runAll(DeciderProperties override) {
        if (override == null)
            override = SimProperties.getDeciderProperties("GLOBAL");
        double startTemp = override.getPropertyAsDouble("StartTemperature", "1.0");
        double endTemp = override.getPropertyAsDouble("EndTemperature", "0.0");
        useBigMoneyInLastK = override.getProperty("DominionBigMoneyBenchmarkWithNoLearning", "false").equals("true");
        bigMoneyCombinations = override.getProperty("DominionBigMoneyBenchmarkCombination", "true").equals("true");
        Temperature temperature = new Temperature(startTemp, endTemp);

        while (!finishedLearningRun()) {
            runNextSet(gamesPerSet);
            for (OnInstructionTeacher<Player> teacher : teacherMap.values()) {
                teacher.teach();
            }
            dg.breed();
            temperature.setTime(count / (double) (maximum - finalScoring));
            SimProperties.setProperty("Temperature", String.format("%.3f", temperature.getTemperature()));
        }
 //       System.out.println("Finished Learning Run - starting scoring games");
        SimProperties.setProperty("Temperature", "0.0");
        count = maximum;    // to cater for gamesPerSet that are not exact divisors
        if (finalScoring > 0) dg.prepareForScoringGames();
        while (!finishedRun()) {
            runNextGameWithoutLearning();
        }
        Decider<Player> winner = dg.getSingleBestPurchaseBrain();
        if (winner instanceof NeuralDecider) {
            ((NeuralDecider<Player>) winner).saveBrain(name, "C://Simulation//brains");
        }
        databaseUtility.flushWriters();
        databaseUtility.addUpdate("EXIT");

        synchronized (this) {
            boolean first = true;
            while (databaseUtility.isAlive()) {
                if (!first) System.out.println("Waiting for previous DBU to complete writing data");
                try {
                    this.wait(5000);
                    first = false;
                } catch (Exception e) {

                }
            }
        }
    }

    private void runNextSet(int numberOfGames) {
        for (int i = 0; i < numberOfGames; i++) {
            DominionGame game = new DominionGame(this.getDeciderDenerator(), this.name, addPaceSetters);
            game.setDatabaseAccessUtility(databaseUtility);
            for (Player p : game.getAllPlayers()) {
                DeciderProperties playerProp = p.getDecider().getProperties();
                String teachingStrategy = playerProp.getProperty("DominionTeachingStrategy", "AllPlayers");
                switch (teachingStrategy) {
                    case "AllPlayers":
                        for (Player p2 : game.getAllPlayers()) {
                            if (!hardCodedActionDecider) {
                                ExperienceRecordCollector<Player> erc = ercMainMap.get(p.getDecider().toString());
                                if (erc != null) erc.registerAgent(p2);
                            } else {
                                ExperienceRecordCollector<Player> erc = ercPurcOnlyMap.get(p.getDecider().toString());
                                if (erc != null) erc.registerAgent(p2);
                            }
                        }
                        break;
                    case "SelfOnly":
                        if (!hardCodedActionDecider) {
                            ExperienceRecordCollector<Player> erc = ercMainMap.get(p.getDecider().toString());
                            if (erc != null) erc.registerAgent(p);
                        } else {
                            ExperienceRecordCollector<Player> erc = ercPurcOnlyMap.get(p.getDecider().toString());
                            if (erc != null) erc.registerAgent(p);
                        }
                        break;
                    case "None":
                    case "EA":
                        // No learning
                        break;
                    default:
                        throw new AssertionError("Unknown teaching strategy: " + teachingStrategy);
                }
            }
            runGame(game);
        }
    }

    private void runNextGameWithoutLearning() {
        DominionGame game = null;
        if (useBigMoneyInLastK) {
            if (bigMoneyCombinations) {
                game = DominionGame.againstDecider(getDeciderDenerator(), name, dg.bigMoney, (int) (count % 3) + 1);
            } else {
                game = DominionGame.againstDecider(getDeciderDenerator(), name, dg.bigMoney, 1);
            }
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
        for (int p = 0; p < 4; p++) {
            if (game.getOrdinalPosition(p + 1) == 1) {
                if (dg != null) {
                    dg.reportVictory(players[p]);
                    for (int o = 0; o < 4; o++) {
                        // iterate through opponents over which victory is to be reported
                        if (game.getOrdinalPosition(o+1) != 1) {
                            dg.reportVictory(players[p], players[o]);
                        }
                    }
                }
            }
        }
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

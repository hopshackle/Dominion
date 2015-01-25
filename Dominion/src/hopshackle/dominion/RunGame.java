package hopshackle.dominion;

import hopshackle.simulation.*;

public class RunGame {


	private static boolean learningOn = SimProperties.getProperty("DominionLearningOn", "true").equals("true");
	private static boolean extraLastK = SimProperties.getProperty("DominionLastThousandForScoring", "true").equals("true");

	/**
	 * @param args
	 */

	public RunGame(String descriptor, int games, int deciders, int cycleSize, int removalsPerCycle, int additionsPerCycle) {
		GameSetup gamesetup = new GameSetup();
		DeciderGenerator dg = new DeciderGenerator(gamesetup, deciders, cycleSize, removalsPerCycle, additionsPerCycle);
		if (deciders > 1)
			dg.useDecidersEvenly(true);
		SequenceOfGames setOfGames = new SequenceOfGames(descriptor, games, dg);
		do {
			if (learningOn)
				setOfGames.runNextGameWithLearning();
			else
				setOfGames.runNextGameWithoutLearning();
		} while (!setOfGames.finishedLearningRun());
		if (extraLastK) {
			System.out.println("Finished Learning Run - starting last 1000 games");
			do {
				setOfGames.runNextGameWithoutLearning();
			} while (!setOfGames.finishedRun());
		}
		dg.recordBestBrains(descriptor);
	}


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
				if (numberOfDeciders == 1) {
					SimProperties.setProperty("DominionSingleBrain", "true");
				} else {
					SimProperties.setProperty("DominionSingleBrain", "false");
				}
				System.out.println("Starting Game " + (iteration+firstSuffix));
				new RunGame(name, numberOfGames, numberOfDeciders, gamesPerCycle, numberToRemovePerCycle, numberToAddPerCycle);
				iteration++;
			} while (iteration < sequencesToRun);
		} catch (Error e) {
			System.out.println(e.toString());
			e.printStackTrace(System.out);
		}
	}

}

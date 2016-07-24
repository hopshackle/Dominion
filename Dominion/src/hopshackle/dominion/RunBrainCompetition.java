package hopshackle.dominion;

import java.text.*;
import java.util.*;

import hopshackle.simulation.*;

public class RunBrainCompetition {

	private String baseDir = SimProperties.getProperty("BaseDirectory", "C:");
	private DeciderGenerator dg;

	/**
	 * @param args
	 */

	public RunBrainCompetition(String descriptor, int deciders, int cycleSize, int removalsPerCycle, int additionsPerCycle) {
		GameSetup gamesetup = new GameSetup();
		dg = new DeciderGenerator(gamesetup, deciders, Integer.MAX_VALUE, removalsPerCycle, additionsPerCycle);
		dg.useDecidersEvenly(true);
		dg.deterministicReplacement(true);
		SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss");
		int metaCycles = 1;
		int trainingGames = 1500;
		double totalCycles = 30.0;
		do {
			System.out.println("Time: " + df.format(new Date()));
			
			CompetitionRound nextRound = new CompetitionRound(descriptor, dg, cycleSize, metaCycles);

			trainingGames += 500;
			nextRound.run(trainingGames);
			
			SimProperties.setProperty("StartTemperature", String.valueOf(1.0 * (totalCycles - metaCycles) / totalCycles));

			// record best brain
			LookaheadDecider<Player> bestBrain = dg.getSingleBestBrain();
			if (bestBrain instanceof DominionNeuralDecider) {
				DominionNeuralDecider bb = (DominionNeuralDecider) bestBrain;
				bb.saveToFile(descriptor + "_" + String.valueOf(metaCycles), baseDir);
			}
			
			// mutate brains and reset for next cycle
			nextRound.recordResults();

			String output = String.format("Meta-cycle: %d, Deciders: %d", metaCycles, dg.getAllPurchaseDeciders().size());
			System.out.println(output);
			
			metaCycles++;
			
		} while (metaCycles <= totalCycles);
	}


	public static void main(String[] args) {

		String nameOfRun = HopshackleUtilities.getArgument(args, 0, "SZBC6");
		int numberOfDeciders = HopshackleUtilities.getArgument(args, 4, 100);
		int gamesPerCycle = HopshackleUtilities.getArgument(args, 5, 30);
		int numberToAddPerCycle = HopshackleUtilities.getArgument(args, 6, 15);
		int numberToRemovePerCycle = HopshackleUtilities.getArgument(args, 7, 30);

		new RunBrainCompetition(nameOfRun, numberOfDeciders, gamesPerCycle, numberToRemovePerCycle, numberToAddPerCycle);

	}
}

package hopshackle.dominion;

import java.util.*;

import hopshackle.simulation.*;

public class CompetitionRound {

	private DeciderGenerator dg;
	private String descriptor;
	private int cycleSize, metaCycle;
	private RunGame setOfGames;
	private static DatabaseWriter<VariableRoundResults> agentWriter = new DatabaseWriter<VariableRoundResults>(new CompetitionDAO());
	private int noCycles = 1;
	
	public CompetitionRound(String competitionName, DeciderGenerator dg, int cycleSize, int metaCycle) {
		this.dg = dg;
		descriptor = competitionName;
		this.cycleSize = cycleSize;
		this.metaCycle = metaCycle;
	}

	public void run(int trainingGames) {
		setOfGames = new RunGame(descriptor, trainingGames, dg);
		for (int i = 0; i < trainingGames; i++)
			setOfGames.runNextGameWithLearning();

		double sdHigh = 0.0;
		double sdLow = 0.0;
		double sdBetween = 0.0;

		dg.resetVariables();
		SimProperties.setProperty("StartTemperature", "0.00");	// set exploration to zero during the comparison phase
		do {
			setOfGames.worldDeath();
			// Now we work out the best brains
			int deciders = dg.getAllPurchaseDeciders().size();
			setOfGames = new RunGame(descriptor, (cycleSize * deciders / 4) + cycleSize, dg);

			for (int i = 0; i < cycleSize * deciders / 4; i++)
				setOfGames.runNextGameWithoutLearning();

			int hiScore = dg.getScore(0.75);
			int loScore = dg.getScore(0.25);

			double p = (double) dg.getTotalWinners() / (double) (noCycles * cycleSize * deciders);
			//			System.out.println(String.format("%d winners in %d games gives p = %.3f", dg.getTotalWinners(), noCycles * cycleSize * deciders / 4, p));

			double variance = p * cycleSize * noCycles * (1.0 - p);	// npq
			double sd = Math.sqrt(variance);
			double mean = p * cycleSize * noCycles;	// np

			sdHigh = (hiScore - mean) / sd;
			sdLow = (mean - loScore) / sd;
			sdBetween = (hiScore - loScore) / sd;
			System.out.println(String.format("Cycle %d: 75th percentile %.2f sigma above mean; 25th percentile %.2f sigma below mean; Difference %.2f sigma (p=%.3f)", noCycles, sdHigh, sdLow, sdBetween, p));

			noCycles++;

		} while ((sdBetween < 10.0) && noCycles < 100);

	}

	public void recordResults() {
		outputNeuronUsage();
		setOfGames.worldDeath();
		dg.initiateTurnOver();
	}
	

	private void outputNeuronUsage() {
		List<LookaheadDecider<Player>> allBrains = dg.getAllPurchaseDeciders();
		List<LookaheadDecider<Player>> bestBrains = dg.getTopPercentageOfBrains(0.5);
		Map<GeneticVariable<Player>, Integer> gvUsage = new HashMap<GeneticVariable<Player>, Integer>();
		for (LookaheadDecider<Player> brain : allBrains) {
			for (GeneticVariable<Player> gv : brain.getVariables()) {
				if (gvUsage.containsKey(gv)) {
					gvUsage.put(gv, gvUsage.get(gv) + 1);
				} else {
					gvUsage.put(gv, 1);
				}
			}
		}
		Map<GeneticVariable<Player>, Integer> bestGvUsage = new HashMap<GeneticVariable<Player>, Integer>();
		for (LookaheadDecider<Player> brain : bestBrains) {
			for (GeneticVariable<Player> gv : brain.getVariables()) {
				if (bestGvUsage.containsKey(gv)) {
					bestGvUsage.put(gv, bestGvUsage.get(gv) + 1);
				} else {
					bestGvUsage.put(gv, 1);
				}
			}
		}
		for (int i = allBrains.size(); i >= 1; i--) {
			for (GeneticVariable<Player> gv : gvUsage.keySet()) {
				if (gvUsage.get(gv) == i) {
					double totalPercentage = 100.0 * (double)i/(double)allBrains.size();
					double bestPercentage = 0.00;
					if (bestGvUsage.containsKey(gv))
						bestPercentage = 100.0 * (double)bestGvUsage.get(gv)/(double)bestBrains.size();
					String output = String.format("%25s : Total %.0f%%  /  Best Half %.0f%% ", gv.toString(), totalPercentage, bestPercentage);
					System.out.println(output);
					VariableRoundResults vrr = new VariableRoundResults(setOfGames, metaCycle, gv.toString(), totalPercentage, bestPercentage);
					agentWriter.write(vrr, descriptor);
				}
			}
		}
	}

}

class VariableRoundResults implements Persistent {
	
	String variableName;
	int round;
	double percent, top50;
	private World world;
	
	VariableRoundResults(World world, int round, String variableName, double percent, double top50) {
		this.world = world;
		this.round = round;
		this.percent = percent;
		this.top50 = top50;
		this.variableName = variableName;
	}

	@Override
	public World getWorld() {
		return world;
	}
	
}

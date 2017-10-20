package hopshackle.dominion;

import hopshackle.simulation.*;
import java.util.*;
import java.util.logging.Logger;
import java.io.*;

public class DeciderGenerator {

	private List<Decider<Player>> purchaseDeciders, unusedPurchaseDeciders;
	protected BigMoneyDecider bigMoneyPurchase;
	protected DominionDeciderContainer bigMoney, chrisPethers;
	protected HardCodedActionDecider hardCodedActionDecider;
	protected ChrisPethersDecider chrisPethersPurchase;
	private GameSetup gamesetup;
	private EntityLog entityLogger;
	private List<Integer> purchaseVictories;
	private double bigMoneyPacesetter = SimProperties.getPropertyAsDouble("DominionBigMoneyPacesetter", "0.00");
	private double chrisPethersPacesetter = SimProperties.getPropertyAsDouble("DominionChrisPethersPacesetter", "0.00");
	private boolean evenUseOfDeciders = SimProperties.getProperty("DominionEvenUseOfDeciders", "true").equals("true");
	private String EA;
	private int ESParentSize, ESProgenySize;
	private boolean nicheScoring;
	private int progenyCounter = 0, cycle = 0;
	private double mutationIntensity = 1.0;
	private Map<String, String> breedingRecord = new HashMap<>();
	private Map<String, Integer> generationRecord = new HashMap<>();
	private int[][] victorMatrix;
	private List<Decider<Player>> hallOfFame = new ArrayList<>();
	private DeciderProperties localProp;
	private String name;

	private DeciderGenerator(String name, GameSetup gameDetails, DeciderProperties localProp, List<Decider<Player>> decidersToUse) {
		this.name = name;
		if (localProp == null) localProp = SimProperties.getDeciderProperties("GLOBAL");
		this.localProp = localProp;
		EA = localProp.getProperty("EvolutionaryAlgorithm", "None");
		ESParentSize = localProp.getPropertyAsInteger("ESParentSize", "1");
		ESProgenySize = localProp.getPropertyAsInteger("ESProgenySize", "1");
		nicheScoring = localProp.getProperty("ESNicheScoring", "false").equals("true");
		gamesetup = gameDetails;
		purchaseDeciders = HopshackleUtilities.cloneList(decidersToUse);
		purchaseVictories = new ArrayList<>();
		for (int i = 0; i < purchaseDeciders.size(); i++)
			purchaseVictories.add(0);

		// now we create the pace-setters
		Decider<Player> hack = decidersToUse.get(0);
		if (hack instanceof DominionDeciderContainer) {
			hardCodedActionDecider = new HardCodedActionDecider(((DominionDeciderContainer)hack).getActionVariables());
		} else {
			hardCodedActionDecider = new HardCodedActionDecider(hack.getVariables());
		}

		victorMatrix = new int[purchaseDeciders.size()+1][purchaseDeciders.size()+1];

		bigMoneyPurchase = new BigMoneyDecider(hack.getVariables());
		bigMoney = new DominionDeciderContainer(bigMoneyPurchase, hardCodedActionDecider);
		bigMoney.setName("BigMoney");
		bigMoney.injectProperties(SimProperties.getDeciderProperties("GLOBAL"));
		chrisPethersPurchase = new ChrisPethersDecider(hack.getVariables());
		chrisPethers = new DominionDeciderContainer(chrisPethersPurchase, hardCodedActionDecider);
		chrisPethers.setName("ChrisPethers");
		chrisPethers.injectProperties(SimProperties.getDeciderProperties("GLOBAL"));

		unusedPurchaseDeciders = HopshackleUtilities.cloneList(purchaseDeciders);
	}

	public DeciderGenerator(String name, GameSetup gameDetails, DeciderProperties override) {
		this(name, gameDetails, override, extractDecidersToUse(gameDetails, override));
	}

	static private List<Decider<Player>> extractDecidersToUse(GameSetup gameDetails, DeciderProperties prop) {

		List<Decider<Player>> decidersToUse = new ArrayList<Decider<Player>>();

		// first we create the configured deciders
		Set<String> deciderTypes = SimProperties.allDeciderNames();
		for (String deciderName : deciderTypes) {
			if (prop == null) prop = SimProperties.getDeciderProperties(deciderName);
			int decidersByType = prop.getPropertyAsInteger("DominionDecidersOfEachType", "1");
			for (int i = 0; i < decidersByType; i++) {
				String nameToUse = deciderName;
				if (decidersByType > 1) nameToUse = deciderName + "_" + (i+1);
				Decider<Player> newDecider = DominionDeciderContainer.factory(nameToUse, gameDetails, prop);
				decidersToUse.add(newDecider);
			}
		}
		boolean useSavedDeciders = prop.getProperty("DominionUseSavedDeciders", "false").equals("true");
		boolean onlyUseSavedDeciders = prop.getProperty("DominionOnlyUseSavedDeciders", "false").equals("true");
		String savedDirectory = SimProperties.getProperty("DominionSavedDeciderDirectory", "C://Simulations//brains");

		if (onlyUseSavedDeciders) {
			decidersToUse.clear();
		}

		if (useSavedDeciders) {
			HopshackleFilter findBrains = new HopshackleFilter("DOM", "brain");
			File directory = new File(savedDirectory);
			if (!directory.exists() || !directory.isDirectory())
				throw new AssertionError(savedDirectory + " is not a valid directory");
			String[] brainLocations = directory.list(findBrains);
			for (String loc : brainLocations) {
				Decider<Player> newDecider = DominionNeuralDecider.createFromFile(new File(savedDirectory, loc));
				decidersToUse.add(newDecider);
			}
		}

		return decidersToUse;
	}
	
	public DeciderGenerator(String name, GameSetup gameDetails) {
		this(name, gameDetails, null);
	}

	public Decider<Player> getDecider(boolean paceSetters) {
		double randomNumber = Math.random();
		if (paceSetters && randomNumber < bigMoneyPacesetter) 
			return bigMoney;
		if (paceSetters && randomNumber < bigMoneyPacesetter + chrisPethersPacesetter)
			return chrisPethers;
		Decider<Player> choice = purchaseDeciders.get((int)(Math.random()*purchaseDeciders.size()));
		if (evenUseOfDeciders) {
			int index = (int)(Math.random()*unusedPurchaseDeciders.size());
			choice = unusedPurchaseDeciders.get(index);
			unusedPurchaseDeciders.remove(index);
			if (unusedPurchaseDeciders.size() == 0)
				unusedPurchaseDeciders = HopshackleUtilities.cloneList(purchaseDeciders);	// reset
		}
		return choice;
	}

	public void reportVictory(Player winner) {
		if (winner != null) {
			Decider<Player> purchaseWinner = winner.getDecider();
			for (int loop = 0; loop < purchaseDeciders.size(); loop++) {
				if (purchaseWinner.equals(purchaseDeciders.get(loop))) 
					purchaseVictories.set(loop, purchaseVictories.get(loop) + 1);
			}
		}
	}

	public void reportVictory(Player victor, Player loser) {
		int victorIndex = purchaseDeciders.indexOf(victor.getDecider());
		int loserIndex = purchaseDeciders.indexOf(loser.getDecider());
		if (victor.getDecider() == bigMoney) victorIndex = purchaseDeciders.size();
		if (loser.getDecider() == bigMoney) loserIndex = purchaseDeciders.size();
		if (victorIndex == -1 || loserIndex == -1)
			throw new AssertionError("Unknown Decider " + victor.toString() + " : " + loser.toString());
		victorMatrix[victorIndex][loserIndex]++;
		victorMatrix[loserIndex][victorIndex]--;
	}

	public Decider<Player> getSingleBestPurchaseBrain() {
		List<Integer> copy = HopshackleUtilities.cloneList(purchaseVictories);
		Collections.sort(copy);
		int bestScore = copy.get(purchaseVictories.size() - 1);
		int index = purchaseVictories.indexOf(bestScore);
		return purchaseDeciders.get(index);
	}

	public List<Decider<Player>> sortDecidersInDescendingVictoryOrder(double[] scores) {
		List<Decider<Player>> retValue = HopshackleUtilities.cloneList(purchaseDeciders);
		Collections.sort(retValue, new Comparator<Decider<Player>>() {
			@Override
			public int compare(Decider<Player> o1, Decider<Player> o2) {
				int index1 = purchaseDeciders.indexOf(o1);
				int index2 = purchaseDeciders.indexOf(o2);
				if (scores[index2] > scores[index1])
					return +1;
				if (scores[index1] > scores[index2])
					return -1;
				return 0;
			}
		});
		return retValue;
	}

	public GameSetup getGameSetup() {
		return gamesetup;
	}

	public List<Decider<Player>> getAllDeciders() {
		return HopshackleUtilities.cloneList(purchaseDeciders);
	}

	public void breed() {
		// Produce the next generation of Deciders
		cycle++;
		switch (EA) {
			case "ES":
				log("Starting Breeding cycle " + cycle);
				double[] scores = nicheScoring ? calculateNicheScores() : calculateSimpleVictories();
				List<Decider<Player>> sortedInOrder = sortDecidersInDescendingVictoryOrder(scores);
				double totalScoreOfThisGenParents = 0.0;
				double totalScoreOfLastGenParents = 0.0;
				double totalScore = 0.0;
				int numberOfParentsFromPreviousGeneration = 0;
				updateHallOfFame(sortedInOrder.get(0));
				for (int i = 0; i < sortedInOrder.size(); i++) {
					int index = purchaseDeciders.indexOf(sortedInOrder.get(i));
					double score = scores[index];
					totalScore += score;
					if (index < ESParentSize) {
						totalScoreOfLastGenParents += score;
					}
					if (i < ESParentSize) {
						double meanW = 0.0;
						double sigmaW = 0.0;
						if (sortedInOrder.get(i) instanceof DominionDeciderContainer) {
							DominionDeciderContainer ddc = (DominionDeciderContainer)sortedInOrder.get(i);
							if (ddc.purchase instanceof NeuralDecider) {
								NeuralDecider nld = (NeuralDecider) ddc.purchase;
								double[] w = nld.getWeights();
								meanW = w[0];
								sigmaW = Math.sqrt(w[1]);
							}
						}
						log(String.format("\t%.2f\tscore for %s %s, (Gen: %d, Weights: %.2f / %.2f)", score,
								sortedInOrder.get(i).toString(), ancestryString(sortedInOrder.get(i).toString()),
								generationRecord.getOrDefault(sortedInOrder.get(i).toString(), 0), meanW, sigmaW));
						totalScoreOfThisGenParents += score;
						if (index < ESParentSize) {
							numberOfParentsFromPreviousGeneration++;
						}
					}
				}
				double meanThisParentScore = totalScoreOfThisGenParents / ESParentSize;
				double meanLastParentScore = totalScoreOfLastGenParents / ESParentSize;
				double meanScore = totalScore / (ESParentSize + ESProgenySize);
				double meanLastChildScore = (totalScore - totalScoreOfLastGenParents) / ESProgenySize;
				double percentSurvivingParents = 100.0 * numberOfParentsFromPreviousGeneration / (double) ESParentSize;
				log(String.format("%.0f%% of previous parents survived with score of %.1f versus score of %.1f for their children, average score of %.1f and %.1f for new parents.",
					percentSurvivingParents, meanLastParentScore, meanLastChildScore, meanScore, meanThisParentScore));
				log(String.format("Ratio of parent score to child score is %.2f", meanLastParentScore/meanLastChildScore));
				if (meanLastParentScore/meanLastChildScore < 1.1 && meanLastParentScore/meanLastChildScore > 0.9) mutationIntensity *= 1.1; // parents are too average; increase mutation rate
				if (meanLastParentScore/meanLastChildScore > 1.5 || percentSurvivingParents > 70.0) mutationIntensity /= 1.1; // parents are too good; mutation is destroying performance
				List<Decider<Player>> children = new ArrayList<Decider<Player>>(ESProgenySize);
				for (int count = 0; count < ESProgenySize; count++) {
					int parentIndex = count % ESParentSize;
					Decider<Player> child = sortedInOrder.get(parentIndex).mutate(mutationIntensity);
					if (child == sortedInOrder.get(parentIndex))
						throw new AssertionError("Mutate operator is not returning a new Decider");
					progenyCounter++;
					child.setName("Child_" + progenyCounter);
					breedingRecord.put(child.toString(), sortedInOrder.get(parentIndex).toString());
					generationRecord.put(child.toString(), generationRecord.getOrDefault(sortedInOrder.get(parentIndex).toString(), 0)+1);
					children.add(child);
				}
				purchaseDeciders = new ArrayList<>(ESParentSize + ESProgenySize);
				purchaseDeciders.addAll(sortedInOrder.subList(0, ESParentSize));
				purchaseDeciders.addAll(children);
				unusedPurchaseDeciders = HopshackleUtilities.cloneList(purchaseDeciders);
				log(String.format("Next Gen size is %d. Mutation Intensity %.2f", purchaseDeciders.size(), mutationIntensity));
				purchaseVictories = new ArrayList<>(purchaseDeciders.size());
				for (int i = 0; i < purchaseDeciders.size(); i++) {
					purchaseVictories.add(i, 0);
				}
				victorMatrix = new int[purchaseDeciders.size()+1][purchaseDeciders.size()+1];
				entityLogger.flush();
				break;
			case "None":
				break;
			default:
				throw new AssertionError("Unknown value for EvolutionaryAlgorithm " + EA);
		}
	}

	public void prepareForScoringGames() {
		// if using EA, then we cull back to the parents that survived the previous round
		// these will be the first ESParentSize entries in purchaseDeciders
		switch(EA) {
			case "ES":
				List<Decider<Player>> allDeciders = HopshackleUtilities.cloneList(purchaseDeciders);
				purchaseDeciders = new ArrayList<>();
				purchaseVictories = new ArrayList<>();
				purchaseDeciders.addAll(allDeciders.subList(0, ESParentSize));
				for (int i = 0; i < ESParentSize; i++) {
					purchaseVictories.add(0);
				}
				unusedPurchaseDeciders = HopshackleUtilities.cloneList(purchaseDeciders);
				victorMatrix = new int[purchaseDeciders.size()+1][purchaseDeciders.size()+1];
			default:
				break;
		}
	}

	public void log(String s) {
		if (entityLogger == null) {
			entityLogger = new EntityLog("DG_" + name, null);
		}
		entityLogger.log(s);
	}

	private String ancestryString(String name) {
		StringBuffer retValue = new StringBuffer();
		int gen = generationRecord.getOrDefault(name, 0);
		String currentAncestor = name;
		for (int g = 0; g < Math.min(4, gen); g++) {
			currentAncestor = breedingRecord.getOrDefault(currentAncestor, "");
			if (currentAncestor.equals(""))
				break;
			retValue.append(" -> " + currentAncestor);
		}
		return retValue.toString();
	}

	private double[] calculateNicheScores() {
		// returns a vector of size equal to the number of Deciders
		double[] retValue = new double[purchaseDeciders.size()];
		double[] beatenBy = new double[purchaseDeciders.size()];
		for (int i = 0; i < retValue.length; i++) {
			for (int j = 0; j < retValue.length; j++) {
				if (victorMatrix[j][i] > 0) beatenBy[i]++;  // j beat i
			}
			if (beatenBy[i] < 0.001) {
				beatenBy[i] = 1.0;
			} else {
				beatenBy[i] = 1.0 / beatenBy[i];
			}
		}
		// beatenBy now contains the credit to be assigned to every decider that beat decider i
		// we now reloop, and assign this credit as stated
		for (int i = 0; i < retValue.length; i++) {
			for (int j = 0; j < retValue.length; j++) {
				if (victorMatrix[i][j] > 0) retValue[i] += beatenBy[j]; // i beat j
			}
		}
		return retValue;
	}

	private double[] calculateSimpleVictories() {
		double[] retValue = new double[purchaseDeciders.size()];
		for (int i = 0; i < retValue.length; i++) {
			retValue[i] = purchaseVictories.get(i);
		}
		return retValue;
	}

	private void updateHallOfFame(Decider<Player> candidate) {
		// we want to compare this to all current hall, and only add it if it is better than *all* of them
		if (hallOfFame.isEmpty()) {
			hallOfFame.add(candidate);
			return;
		}
		if (hallOfFame.contains(candidate))
			return;
		// we now really want to RunGame(), with a subsidiary DG
		if (localProp == null) localProp = SimProperties.getDeciderProperties("GLOBAL");
		DeciderProperties subProp = localProp.clone();
		// do I want to change any of these?
		subProp.setProperty("DominionBigMoneyBenchmarkWithNoLearning", "false");
		subProp.setProperty("EvolutionaryAlgorithm", "None");
		List<Decider<Player>> forContest = new ArrayList<>();
		forContest.addAll(hallOfFame);
		forContest.add(candidate);
		DeciderGenerator childDG = new DeciderGenerator(name, gamesetup, subProp, forContest);

		// we want no learning games, and 125 scoring games per decider included, so each will play 500
		RunGame contest = new RunGame("HallOfFame", 0,forContest.size() * 125, childDG);
		contest.runAll(subProp);

		// we can now report on the results from childDG
		double[] nicheScores = childDG.calculateNicheScores();
		double[] simpleVictories = childDG.calculateSimpleVictories();
		double maxNiche = 0.0;
		double maxVict = 0.0;
		for (int i = 0; i < forContest.size(); i++) {
			double v = simpleVictories[i];
			double n = nicheScores[i];
			log(String.format("%s\tVictories: %.0f\tNiche Score: %.2f", forContest.get(i).toString(), v, n));
			if (v > maxVict) maxVict = v;
			if (n > maxNiche) maxNiche = n;
		}
		if (simpleVictories[forContest.size()-1] < maxVict && nicheScores[forContest.size()-1] < maxNiche) {
			log("Candidate not included in Hall of Fame");
		} else {
			hallOfFame.add(candidate);
			log("Candidate included in Hall of Fame");
		}
	}
}

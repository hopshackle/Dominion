package hopshackle.dominion;

import hopshackle.simulation.*;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class DeciderGenerator {

	private List<Decider<Player>> purchaseDeciders, unusedPurchaseDeciders, actionDeciders, unusedActionDeciders;
	protected BigMoneyDecider bigMoney;
	protected DominionDeciderContainer completeHeuristic;
	protected HardCodedActionDecider hardCodedActionDecider;
	protected ChrisPethersDecider chrisPethers;
	private GameSetup gamesetup;
	private List<Integer> purchaseVictories, lastPVictories;
	private Map<String, Double> purchaseScores;
	private int currentLoop;
	private int removalThreshold;
	private int decideNameCount = 0;
	private int toAdd, toRemove;
	private double sampleSize = SimProperties.getPropertyAsDouble("SampleSize", "5.0");
	private String baseDir = SimProperties.getProperty("BaseDirectory", "C:");
	protected static Logger logger = Logger.getLogger("hopshackle.simulation");
	private double bigMoneyPacesetter = SimProperties.getPropertyAsDouble("DominionBigMoneyPacesetter", "0.00");
	private double chrisPethersPacesetter = SimProperties.getPropertyAsDouble("DominionChrisPethersPacesetter", "0.00");
	protected int startingInputs = Integer.valueOf(SimProperties.getProperty("DominionStartingInputs", "99"));
	protected double selectionThreshold = Double.valueOf(SimProperties.getProperty("DominionSelectionThreshold", "1"));
	private boolean useHardCodedAction = SimProperties.getProperty("DominionHardCodedActionDecider", "false").equals("true");
	private double percentageMCTSToUse = SimProperties.getPropertyAsDouble("DominionMCTSDeciderProportion", "0.00");
	private boolean evenUseOfDeciders = false;
	private boolean replaceDecidersDeterministically = false;
	private int totalWinners, baseDeciders;
	protected MCTSMasterDecider<Player> mctsDecider;
	private List<ActionEnum<Player>> actionsToUse;
	private List<CardType> cardTypes;

	public DeciderGenerator(GameSetup gameDetails, int numberToMaintain, int roundsPerRemoval, int decidersToRemovePerRound, int decidersToAddPerRound) {
		baseDeciders = numberToMaintain;
		gamesetup = gameDetails;
		removalThreshold = roundsPerRemoval;
		currentLoop = 0;
		toAdd = decidersToAddPerRound;
		toRemove = decidersToRemovePerRound;
		purchaseDeciders = new ArrayList<Decider<Player>>();
		actionDeciders = new ArrayList<Decider<Player>>();
		purchaseVictories = new ArrayList<Integer>();
		purchaseScores = new HashMap<String, Double>();

		List<CardValuationVariables> variablesToUseForPurchase = gamesetup.getDeckVariables();
		List<CardValuationVariables> variablesToUseForActions = gamesetup.getHandVariables();

		cardTypes = gamesetup.getCardTypes();
		actionsToUse = CardType.generateListOfPossibleActionEnumsFromCardTypes(cardTypes);
		hardCodedActionDecider = new HardCodedActionDecider(cardTypes, variablesToUseForActions);
		hardCodedActionDecider.setName("DEFAULT");
		bigMoney = new BigMoneyDecider(actionsToUse, HopshackleUtilities.convertList(variablesToUseForPurchase));
		completeHeuristic = new DominionDeciderContainer(bigMoney, hardCodedActionDecider);
		chrisPethers = new ChrisPethersDecider(actionsToUse, HopshackleUtilities.convertList(variablesToUseForPurchase));
		mctsDecider = new MCTSMasterDominion(actionsToUse, variablesToUseForPurchase, completeHeuristic, completeHeuristic);
		mctsDecider.setName("MCTS");
		int numberMCTS = (int) (numberToMaintain * percentageMCTSToUse);
		for (int i = 0; i < numberMCTS; i++) {
			purchaseDeciders.add(mctsDecider);
			if (!useHardCodedAction) actionDeciders.add(mctsDecider);
		}

		FilenameFilter nameFilter = new HopshackleFilter("", "brain");
		loadDecidersFromFile(purchaseDeciders, new File(baseDir + "\\DecidersAtStart"), nameFilter, 
				new DominionNeuralDecider(actionsToUse, variablesToUseForPurchase), 0, "P");
		// TODO: This load from file only applies to Purchase Deciders currently

		for (int n = 0; n < baseDeciders; n++) {
			LookaheadDecider<Player> pd = null;
			if (purchaseDeciders.size() <= n){
				if (startingInputs < 99) {
					List<CardValuationVariables> varsToUse = new ArrayList<CardValuationVariables>();
					for (int i = 0; i < startingInputs; i++) {
						boolean choiceMade = false;
						do {
							int roll = Dice.roll(1, variablesToUseForPurchase.size()) - 1;
							CardValuationVariables choice = variablesToUseForPurchase.get(roll);
							if (!varsToUse.contains(choice)) {
								choiceMade = true;
								varsToUse.add(choice);
							}
						} while (!choiceMade);
					}
					if (!varsToUse.contains(CardValuationVariables.VICTORY_MARGIN)) {
						varsToUse.add(CardValuationVariables.VICTORY_MARGIN);
						varsToUse.remove(0);
					}
					pd = new DominionNeuralDecider(actionsToUse, varsToUse);
				} else {
					pd = new DominionNeuralDecider(actionsToUse, variablesToUseForPurchase);
				}
				pd.setName("P"+String.format("%03d", decideNameCount));
				purchaseDeciders.add(pd);
			}

			// TODO: We have duplicate code here for variable selection (albeit not a highly used option)
			// TODO: Some of the Purchase variables will also be useful for action selection (re: End Game changes)
			if (useHardCodedAction) {
				actionDeciders.add(hardCodedActionDecider);
			} else {
				if (actionDeciders.size() <= n){
					LookaheadDecider<Player> ad = null;
					if (startingInputs < 99) {
						List<CardValuationVariables> varsToUse = new ArrayList<CardValuationVariables>();
						for (int i = 0; i < startingInputs; i++) {
							boolean choiceMade = false;
							do {
								int roll = Dice.roll(1, variablesToUseForActions.size()) - 1;
								CardValuationVariables choice = variablesToUseForActions.get(roll);
								if (!varsToUse.contains(choice)) {
									choiceMade = true;
									varsToUse.add(choice);
								}
							} while (!choiceMade);
						}
						ad = new DominionNeuralDecider(actionsToUse, varsToUse);
					} else {
						ad = new DominionNeuralDecider(actionsToUse, variablesToUseForActions);
					}
					ad.setName("A"+String.format("%03d", decideNameCount));
					actionDeciders.add(ad);
				}
			}

			decideNameCount++;
			purchaseVictories.add(0);
		}
		unusedPurchaseDeciders = HopshackleUtilities.cloneList(purchaseDeciders);
		unusedActionDeciders = HopshackleUtilities.cloneList(actionDeciders);
	}

	private void loadDecidersFromFile(List<Decider<Player>> deciders, File directory, 
			FilenameFilter filter, Decider<Player> sampleDecider, int startCounter, String prefix) {
		if (directory == null || !directory.isDirectory()) {
			logger.severe("Error in loading brains, specified Directory isn't: " + directory);
		}
		File files[] = directory.listFiles(filter);

		if (sampleDecider instanceof DominionNeuralDecider) {
			for (File f : files) {
				NeuralDecider<Player> nd = NeuralDecider.createNeuralDecider(new DominionStateFactory(new ArrayList<GeneticVariable<Player>>()), f, 100.0);
				DominionNeuralDecider newDecider = new DominionNeuralDecider(actionsToUse, nd.getVariables());
				newDecider.setInternalNeuralNetwork(nd);
				deciders.add(newDecider);
				String newName = prefix + String.format("%03d", startCounter) + " : " + nd.toString();
				newDecider.setName(newName);
				startCounter++;
			}
		}
		if (startCounter > 0)
			logger.info("Loaded " + startCounter + " DominionNeuralDeciders from " + directory.toString());
	}

	public Decider<Player> getPurchaseDecider(boolean paceSetters) {
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
	public Decider<Player> getActionDecider() {
		Decider<Player> choice = actionDeciders.get((int)(Math.random()*actionDeciders.size()));
		if (evenUseOfDeciders) {
			int index = (int)(Math.random()*unusedActionDeciders.size());
			choice = unusedActionDeciders.get(index);
			unusedActionDeciders.remove(index);
			if (unusedActionDeciders.size() == 0)
				unusedActionDeciders = HopshackleUtilities.cloneList(actionDeciders);	// reset
		}
		return choice;
	}

	public void reportVictory(Player winner) {
		totalWinners++;
		if (winner != null) {
			Decider<Player> purchaseWinner = winner.getDecider();
			for (int loop = 0; loop < purchaseDeciders.size(); loop++) {
				if (purchaseWinner.equals(purchaseDeciders.get(loop))) 
					purchaseVictories.set(loop, purchaseVictories.get(loop) + 1);
			}
		}
		currentLoop++;
		if (removalThreshold > 0 && currentLoop >= removalThreshold) {
			initiateTurnOver();
		}
	}
	private void removeDeciders(List<? extends Decider<Player>> deciders, List<Integer> victories, int numberToRemove) {
		List<Integer> copy = HopshackleUtilities.cloneList(victories);
		Collections.sort(copy);
		for (int mainLoop = 0; mainLoop < numberToRemove; mainLoop++) {
			if (deciders.size() < 2) continue;
			int lowestVictories = Integer.MAX_VALUE;
			int deciderToRemove = -1;
			if (replaceDecidersDeterministically) {
				lowestVictories = copy.get(mainLoop);
				deciderToRemove = victories.indexOf(lowestVictories);
			} else {
				for (int loop = 0; loop < deciders.size(); loop++) {
					if (victories.get(loop) < lowestVictories) {
						lowestVictories = victories.get(loop);
						deciderToRemove = loop;
					}
				}
			}
			String deciderName = deciders.get(deciderToRemove).toString();
			double incrementalScore = 1.0 - (double) mainLoop / (double) numberToRemove;
			if (purchaseScores.containsKey(deciderName)) {
				double currentScore = purchaseScores.get(deciderName);
				purchaseScores.put(deciderName, currentScore + incrementalScore);
			} else {
				purchaseScores.put(deciderName, incrementalScore);
			}
			if (purchaseScores.get(deciderName) >= selectionThreshold) {
				deciders.remove(deciderToRemove);
				victories.remove(deciderToRemove);
				System.out.println(String.format("Removed %s with score %.2f (%s victories)", deciderName, purchaseScores.get(deciderName), victories.get(deciderToRemove)));
			} else {
				victories.set(deciderToRemove, -1);	// to not pick the same one next time
				System.out.println(String.format("Retains  %s with score %.2f (%s victories)", deciderName, purchaseScores.get(deciderName), victories.get(deciderToRemove)));
			}
		}
	}

	public void resetVariables() {
		lastPVictories = purchaseVictories;
		purchaseVictories = new ArrayList<Integer>();
		for (int n = 0; n < purchaseDeciders.size(); n++) 
			purchaseVictories.add(0);

		currentLoop = 0;
		totalWinners = 0;
	}

	private <T extends Decider<Player>> List<T> addNewCrossedDeciders(List<T> deciders, List<Integer> victories, String namePrefix, int number) {
		int numberAdded = 0;
		List<Integer> masterCopy = HopshackleUtilities.cloneList(victories);
		List<Integer> otherCopy = HopshackleUtilities.cloneList(victories);
		List<T> retValue = HopshackleUtilities.cloneList(deciders);
		do {
			List<Integer> localCopy = HopshackleUtilities.cloneList(masterCopy);
			Collections.sort(localCopy);
			int bestScore = localCopy.get(masterCopy.size() - 1);
			int indexToClone = masterCopy.indexOf(bestScore);
			masterCopy.set(indexToClone, 0);	// so that it is not picked again

			T deciderToCopy = deciders.get(indexToClone);
			List<T> crossDecider = getSampleTwoBestDeciders(deciders, otherCopy);
			Decider<Player> otherDecider = crossDecider.get(0);
			if (otherDecider == deciderToCopy)
				otherDecider = crossDecider.get(1);
			@SuppressWarnings("unchecked")
			T newDecider = (T) deciderToCopy.crossWith(otherDecider);
			retValue.add(newDecider);
			victories.add(0);
			String newName = deciderToCopy.toString().substring(1, 4);
			newName = namePrefix + String.format("%03d", decideNameCount + numberAdded) + " : " + newName;
			if (newName.length() > 20)
				newName = newName.substring(0, 20);
			newDecider.setName(newName);

			numberAdded++;
		} while (numberAdded < number);

		return retValue;
	}

	private <T extends Decider<Player>> List<T> addNewCrossedDecider(List<T> deciders, List<Integer> victories, String namePrefix) {
		List<T> retValue = HopshackleUtilities.cloneList(deciders);
		List<T> bestDeciders = getSampleTwoBestDeciders(deciders, victories);
		if (bestDeciders.get(1) == null) bestDeciders.set(1, bestDeciders.get(0));
		@SuppressWarnings("unchecked")
		T newDecider = (T) bestDeciders.get(0).crossWith(bestDeciders.get(1));
		retValue.add(newDecider);
		victories.add(0);
		//	String newName = dpDecider1.toString().substring(1, 4) + "-" + dpDecider2.toString().substring(1, 4);
		String newName = bestDeciders.get(0).toString().substring(1, 4);
		newName = namePrefix + String.format("%03d", decideNameCount) + " : " + newName;
		if (newName.length() > 20)
			newName = newName.substring(0, 20);
		newDecider.setName(newName);
		return retValue;
	}

	public Decider<Player> getSingleBestPurchaseBrain() {
		List<Integer> copy = HopshackleUtilities.cloneList(purchaseVictories);
		Collections.sort(copy);
		int bestScore = copy.get(purchaseVictories.size() - 1);
		int index = purchaseVictories.indexOf(bestScore);
		return purchaseDeciders.get(index);
	}

	public List<Decider<Player>> getTopPercentageOfPurchaseBrains(double percentile) {
		int criticalScore = this.getScore(percentile);
		List<Decider<Player>> retValue = new ArrayList<Decider<Player>>();
		for (int n = 0; n < purchaseDeciders.size(); n++) {
			if (purchaseVictories.get(n) >= criticalScore)
				retValue.add(purchaseDeciders.get(n));
		}
		return retValue;
	}

	public void recordBestPurchaseBrains(String descriptor, String directory) {
		List<Integer> victories = HopshackleUtilities.cloneList(lastPVictories);
		if (lastPVictories == null)
			victories = HopshackleUtilities.cloneList(purchaseVictories);
		for (int n = 0; n < 5; n++) {
			Decider<Player> sample = getSampleTwoBestDeciders(purchaseDeciders, victories).get(0);
			if(sample != null) {
				if (sample instanceof DominionNeuralDecider) {
					DominionNeuralDecider dec = (DominionNeuralDecider) sample;
					dec.saveToFile(descriptor, directory);
				}
				victories.remove(purchaseDeciders.indexOf(sample));
				purchaseDeciders.remove(sample);
			} else {
				return;
			}
		}
	}

	private <D extends Decider<Player>> List<D> getSampleTwoBestDeciders(List<D> deciders, List<Integer> victories) {
		if (deciders.size() != victories.size()) {
			logger.severe("Victories and Deciders don't match in DeciderGenerator");
			return null;
		}
		List<D> retValue = new ArrayList<D>();
		D dpDecider1 = null, dpDecider2 = null;
		if (deciders.size() != 0) { 
			int highestScore = -1, secondHighest = -1;
			for (int n = 0; n < (int)sampleSize; n++) {
				int randomChoice = (int)(Math.random() * deciders.size());
				if (victories.get(randomChoice) > highestScore) {
					dpDecider2 = dpDecider1;
					dpDecider1 = deciders.get(randomChoice);
					secondHighest = highestScore;
					highestScore = victories.get(randomChoice);
				} else if (victories.get(randomChoice) > secondHighest) {
					if (deciders.get(randomChoice).equals(dpDecider1)) continue;
					dpDecider2 = deciders.get(randomChoice);
					secondHighest = victories.get(randomChoice);
				}
			}
		}
		retValue.add(dpDecider1);
		retValue.add(dpDecider2);
		return retValue;
	}

	public GameSetup getGameSetup() {
		return gamesetup;
	}

	public List<Decider<Player>> getAllPurchaseDeciders() {
		return HopshackleUtilities.cloneList(purchaseDeciders);
	}
	public List<Decider<Player>> getAllActionDeciders() {
		return HopshackleUtilities.cloneList(actionDeciders);
	}

	public void useDecidersEvenly(boolean var) {
		evenUseOfDeciders = var;
	}

	public int getScore(double percentile) {
		List<Integer> copy = HopshackleUtilities.cloneList(purchaseVictories);
		Collections.sort(copy);
		return copy.get((int) ((percentile * copy.size()) + 0.5));
	}


	/*
	 *  What I really want to do here is:
	 *  - pick the n best Deciders (as now)
	 *  - create a descendant for each of them (as now)
	 *  - create another copy of them that is set to be non-learning (new)
	 *  - then run through the bottom xx%, and remove any that are either:
	 *  	- non-learners, or
	 *  	- on their second or greater learning run (i.e. always give a decider a second chance)
	 */

	public void initiateTurnOver() {
		int actualToRemove = toRemove;
		if (purchaseDeciders.size() > baseDeciders) {
			actualToRemove = (int) (purchaseDeciders.size() * ((double) toRemove / (double) baseDeciders));
		}
		removeDeciders(purchaseDeciders, purchaseVictories, actualToRemove);
		if (replaceDecidersDeterministically) {
			purchaseDeciders = addNewCrossedDeciders(purchaseDeciders, purchaseVictories, "P", toAdd);
			decideNameCount += toAdd;
		} else {
			for (int n = 0; n < toAdd; n++) {
				purchaseDeciders = addNewCrossedDecider(purchaseDeciders, purchaseVictories, "P");
				decideNameCount++;
			}
		}
		unusedPurchaseDeciders = HopshackleUtilities.cloneList(purchaseDeciders);
		resetVariables();
	}

	public void deterministicReplacement(boolean b) {
		replaceDecidersDeterministically = b;
	}

	public int getTotalWinners() {
		return totalWinners;
	}
}

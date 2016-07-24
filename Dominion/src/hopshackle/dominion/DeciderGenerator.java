package hopshackle.dominion;

import hopshackle.simulation.*;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class DeciderGenerator {

	private List<LookaheadDecider<Player>> purchaseDeciders, unusedPurchaseDeciders, actionDeciders;
	protected BigMoneyDecider bigMoney;
	protected ChrisPethersDecider chrisPethers;
	private GameSetup gamesetup;
	private List<Integer> purchaseVictories, actionVictories, lastPVictories;
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
	private boolean evenUseOfDeciders = false;
	private boolean replaceDecidersDeterministically = false;
	private boolean addPaceSetters = SimProperties.getProperty("DominionAddPacesetters", "false").equals("true");
	private int totalWinners, baseDeciders;
	private DominionLookaheadFunction lookahead = new DominionLookaheadFunction();
	private List<CardType> actionsToUse;

	public DeciderGenerator(GameSetup gameDetails, int numberToMaintain, int roundsPerRemoval, int decidersToRemovePerRound, int decidersToAddPerRound) {
		baseDeciders = numberToMaintain;
		gamesetup = gameDetails;
		removalThreshold = roundsPerRemoval;
		currentLoop = 0;
		toAdd = decidersToAddPerRound;
		toRemove = decidersToRemovePerRound;
		purchaseDeciders = new ArrayList<LookaheadDecider<Player>>();
		actionDeciders = new ArrayList<LookaheadDecider<Player>>();
		purchaseVictories = new ArrayList<Integer>();
		purchaseScores = new HashMap<String, Double>();
		actionVictories = new ArrayList<Integer>();

		List<CardValuationVariables> variablesToUseForPurchase = gamesetup.getDeckVariables();
		List<CardValuationVariables> variablesToUseForActions = gamesetup.getHandVariables();

		actionsToUse = gamesetup.getCardTypes();

		bigMoney = new BigMoneyDecider(HopshackleUtilities.convertList(actionsToUse), HopshackleUtilities.convertList(variablesToUseForPurchase));
		chrisPethers = new ChrisPethersDecider(HopshackleUtilities.convertList(actionsToUse), HopshackleUtilities.convertList(variablesToUseForPurchase));

		FilenameFilter nameFilter = new HopshackleFilter("", "brain");
		loadDecidersFromFile(purchaseDeciders, new File(baseDir + "\\DecidersAtStart"), nameFilter, 
				new DominionNeuralDecider(lookahead, actionsToUse, variablesToUseForPurchase), 0, "P");

		for (int n = 0; n<numberToMaintain; n++) {
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
					pd = new DominionNeuralDecider(lookahead, actionsToUse, varsToUse);
				} else {
					pd = new DominionNeuralDecider(lookahead, actionsToUse, variablesToUseForPurchase);
				}
				pd.setName("P"+String.format("%03d", decideNameCount));
				purchaseDeciders.add(pd);
			}

			HardCodedActionDecider ad = new HardCodedActionDecider(actionsToUse, variablesToUseForActions);
			ad.setName("DEFAULT");
			actionDeciders.add(ad);

			decideNameCount++;
			purchaseVictories.add(0);
			String tempName = "DEFAULT";
			if (pd != null)
				tempName = pd.toString();
			purchaseScores.put(tempName, 2.0);	// we start with a score of 2, as the initial ones are thrown out with greater alacrity
			actionVictories.add(0);
		}
		unusedPurchaseDeciders = HopshackleUtilities.cloneList(purchaseDeciders);
	}

	private void loadDecidersFromFile(List<LookaheadDecider<Player>> deciders, File directory, 
			FilenameFilter filter, LookaheadDecider<Player> sampleDecider, int startCounter, String prefix) {
		if (directory == null || !directory.isDirectory()) {
			logger.severe("Error in loading brains, specified Directory isn't: " + directory);
		}
		File files[] = directory.listFiles(filter);

		if (sampleDecider instanceof DominionNeuralDecider) {
			for (File f : files) {
				NeuralDecider<Player> nd = NeuralDecider.createNeuralDecider(new DominionStateFactory(new ArrayList<GeneticVariable<Player>>()), f);
				DominionNeuralDecider newDecider = new DominionNeuralDecider(lookahead, actionsToUse, nd.getVariables());
				newDecider.setInternalNeuralNetwork(nd);
				deciders.add(newDecider);
				String newName = prefix + String.format("%03d", startCounter) + " : " + newDecider.toString();
				newDecider.setName(newName);
				startCounter++;
			}
		}
		if (startCounter > 0)
			logger.info("Loaded " + startCounter + " DominionNeuralDeciders from " + directory.toString());
	}

	public LookaheadDecider<Player> getPurchaseDecider() {
		double randomNumber = Math.random();
		if (randomNumber < bigMoneyPacesetter) 
			return bigMoney;
		if (randomNumber < bigMoneyPacesetter + chrisPethersPacesetter)
			return chrisPethers;
		LookaheadDecider<Player> choice = purchaseDeciders.get((int)(Math.random()*purchaseDeciders.size()));
		if (evenUseOfDeciders) {
			int index = (int)(Math.random()*unusedPurchaseDeciders.size());
			choice = unusedPurchaseDeciders.get(index);
			unusedPurchaseDeciders.remove(index);
			if (unusedPurchaseDeciders.size() == 0)
				unusedPurchaseDeciders = HopshackleUtilities.cloneList(purchaseDeciders);	// reset
		}
		return choice;
	}
	public LookaheadDecider<Player> getActionDecider() {
		return actionDeciders.get((int)(Math.random()*actionDeciders.size()));
	}

	public void reportVictory(Player winner) {
		totalWinners++;
		if (winner != null) {
			LookaheadDecider<Player> purchaseWinner = winner.getPositionDecider();
			for (int loop = 0; loop < purchaseDeciders.size(); loop++) {
				if (purchaseWinner.equals(purchaseDeciders.get(loop))) 
					purchaseVictories.set(loop, purchaseVictories.get(loop) + 1);
			}
			Decider<Player> actionWinner = winner.getActionDecider();
			for (int loop = 0; loop < actionDeciders.size(); loop++) {
				if (actionWinner.equals(actionDeciders.get(loop))) 
					actionVictories.set(loop, actionVictories.get(loop) + 1);
			}
		}
		currentLoop++;
		if (currentLoop >= removalThreshold) {
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
		actionVictories = new ArrayList<Integer>();
		for (int n = 0; n < purchaseDeciders.size(); n++) 
			purchaseVictories.add(0);
		for (int n = 0; n < actionDeciders.size(); n++) 
			actionVictories.add(0);

		currentLoop = 0;
		totalWinners = 0;
	}

	private <T extends LookaheadDecider<Player>> List<T> addNewCrossedDeciders(List<T> deciders, List<Integer> victories, String namePrefix, int number) {
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

			if (addPaceSetters) {
				boolean createPacesetter = true;
				if (deciderToCopy instanceof DominionNeuralDecider) {
					DominionNeuralDecider dnd = (DominionNeuralDecider) deciderToCopy;
					createPacesetter = dnd.getLearning();
				}
				if (createPacesetter) {
					@SuppressWarnings("unchecked")
					T copyOfDecider = (T) deciderToCopy.crossWith(null);
					retValue.add(copyOfDecider);
					victories.add(0);
					newName = deciderToCopy.toString().substring(1, 4);
					newName = "C" + String.format("%03d", decideNameCount + numberAdded) + " : " + newName;
					if (newName.length() > 20)
						newName = newName.substring(0, 20);
					copyOfDecider.setName(newName);
					if (copyOfDecider instanceof DominionNeuralDecider) {
						DominionNeuralDecider dnd = (DominionNeuralDecider) copyOfDecider;
						dnd.setLearning(false);
						purchaseScores.put(dnd.toString(), 2.0);
					}
				}
			}
			numberAdded++;
		} while (numberAdded < number);

		return retValue;
	}

	private <T extends LookaheadDecider<Player>> List<T> addNewCrossedDecider(List<T> deciders, List<Integer> victories, String namePrefix) {
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

	public LookaheadDecider<Player> getSingleBestBrain() {
		List<Integer> copy = HopshackleUtilities.cloneList(purchaseVictories);
		Collections.sort(copy);
		int bestScore = copy.get(purchaseVictories.size() - 1);
		int index = purchaseVictories.indexOf(bestScore);
		return purchaseDeciders.get(index);
	}
	
	public List<LookaheadDecider<Player>> getTopPercentageOfBrains(double percentile) {
		int criticalScore = this.getScore(percentile);
		List<LookaheadDecider<Player>> retValue = new ArrayList<LookaheadDecider<Player>>();
		for (int n = 0; n < purchaseDeciders.size(); n++) {
			if (purchaseVictories.get(n) >= criticalScore)
				retValue.add(purchaseDeciders.get(n));
		}
		return retValue;
	}

	public void recordBestBrains(String descriptor, String directory) {
		List<Integer> victories = HopshackleUtilities.cloneList(lastPVictories);
		if (lastPVictories == null)
			victories = HopshackleUtilities.cloneList(purchaseVictories);
		for (int n = 0; n < 5; n++) {
			LookaheadDecider<Player> sample = getSampleTwoBestDeciders(purchaseDeciders, victories).get(0);
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

	public List<LookaheadDecider<Player>> getAllPurchaseDeciders() {
		return HopshackleUtilities.cloneList(purchaseDeciders);
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
		//			removeDecider(discardDeciders, discardVictories);
		//			removeDecider(actionDeciders, actionVictories);
		if (replaceDecidersDeterministically) {
			purchaseDeciders = addNewCrossedDeciders(purchaseDeciders, purchaseVictories, "P", toAdd);
			//			discardDeciders = addNewCrossedDeciders(discardDeciders, discardVictories, "D", false, toAdd);
			//			actionDeciders = addNewCrossedDeciders(actionDeciders, actionVictories, "A", false, toAdd);
			decideNameCount += toAdd;
		} else {
			for (int n = 0; n < toAdd; n++) {
				purchaseDeciders = addNewCrossedDecider(purchaseDeciders, purchaseVictories, "P");
				//				discardDeciders = addNewCrossedDecider(discardDeciders, discardVictories, "D", false);
				//				actionDeciders = addNewCrossedDecider(actionDeciders, actionVictories, "A", false);
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

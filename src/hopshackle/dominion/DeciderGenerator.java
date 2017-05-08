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
	private List<Integer> purchaseVictories;
	protected static Logger logger = Logger.getLogger("hopshackle.simulation");
	private double bigMoneyPacesetter = SimProperties.getPropertyAsDouble("DominionBigMoneyPacesetter", "0.00");
	private double chrisPethersPacesetter = SimProperties.getPropertyAsDouble("DominionChrisPethersPacesetter", "0.00");
	private boolean evenUseOfDeciders = SimProperties.getProperty("DominionEvenUseOfDeciders", "true").equals("true");
	private boolean useSavedDeciders = SimProperties.getProperty("DominionUseSavedDeciders", "false").equals("true");
	private boolean onlyUseSavedDeciders = SimProperties.getProperty("DominionOnlyUseSavedDeciders", "false").equals("true");
	private String savedDirectory = SimProperties.getProperty("DominionSavedDeciderDirectory", "C://Simulations//brains");
	private int decidersByType = SimProperties.getPropertyAsInteger("DominionDecidersOfEachType", "1");

	public DeciderGenerator(GameSetup gameDetails, DeciderProperties override) {
		gamesetup = gameDetails;
		purchaseDeciders = new ArrayList<Decider<Player>>();
		purchaseVictories = new ArrayList<Integer>();

		// first we create the configured deciders
		Set<String> deciderTypes = SimProperties.allDeciderNames();
		for (String deciderName : deciderTypes) {
			DeciderProperties localProp = override;
			if (localProp == null) localProp = SimProperties.getDeciderProperties(deciderName);
			Decider<Player> newDecider = DominionDeciderContainer.factory(deciderName, gamesetup, localProp);
	//		System.out.println("Created decider "+ newDecider.toString());
			for (int i = 0; i < decidersByType; i++) {
				purchaseDeciders.add(newDecider);
				purchaseVictories.add(0);
			}
		}

		// now we create the pace-setters
		Decider<Player> hack = purchaseDeciders.get(0);
		if (hack instanceof DominionDeciderContainer) {
			hardCodedActionDecider = new HardCodedActionDecider(((DominionDeciderContainer)hack).getActionVariables());
		} else {
			hardCodedActionDecider = new HardCodedActionDecider(hack.getVariables());
		}

		if (onlyUseSavedDeciders) {
			purchaseDeciders.clear();
			purchaseVictories.clear();
		}

		if (useSavedDeciders) {
			HopshackleFilter findBrains = new HopshackleFilter("DOM", "brain");
			File directory = new File(savedDirectory);
			if (!directory.exists() || !directory.isDirectory())
				throw new AssertionError(savedDirectory + " is not a valid directory");
			String[] brainLocations = directory.list(findBrains);
			for (String loc : brainLocations) {
				Decider<Player> newDecider = DominionNeuralDecider.createFromFile(new File(savedDirectory, loc));
				purchaseDeciders.add(newDecider);
				purchaseVictories.add(0);
			}
		}

		bigMoneyPurchase = new BigMoneyDecider(hack.getVariables());
		bigMoney = new DominionDeciderContainer(bigMoneyPurchase, hardCodedActionDecider);
		bigMoney.setName("BigMoney");
		chrisPethersPurchase = new ChrisPethersDecider(hack.getVariables());
		chrisPethers = new DominionDeciderContainer(chrisPethersPurchase, hardCodedActionDecider);
		chrisPethers.setName("ChrisPethers");

		unusedPurchaseDeciders = HopshackleUtilities.cloneList(purchaseDeciders);
	}
	
	public DeciderGenerator(GameSetup gameDetails) {
		this(gameDetails, null);
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

	public GameSetup getGameSetup() {
		return gamesetup;
	}

	public List<Decider<Player>> getAllDeciders() {
		return HopshackleUtilities.cloneList(purchaseDeciders);
	}

	public int getScore(double percentile) {
		List<Integer> copy = HopshackleUtilities.cloneList(purchaseVictories);
		Collections.sort(copy);
		return copy.get((int) ((percentile * copy.size()) + 0.5));
	}
}

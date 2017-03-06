package hopshackle.dominion;

import java.util.*;

import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.*;

public class DominionDeciderContainer implements Decider<Player> {

	protected Decider<Player> purchase, action;
	protected String name;

	public DominionDeciderContainer(Decider<Player> purchase, Decider<Player> action) {
		this.purchase = purchase;
		this.action = action;
		if (action == null || purchase == null)
			throw new AssertionError("Both Purchase and Action deciders must be specified");
	}

	public static DominionDeciderContainer factory(String name, GameSetup gamesetup, DeciderProperties properties) {

		boolean useHandVariables = properties.getProperty("DominionUseHandVariables", "false").equals("true");
		boolean hardCodedActions = properties.getProperty("DominionHardCodedActionDecider", "false").equals("true");
		String deciderType = properties.getProperty("DeciderType", "NN");
		String coreVariables = properties.getProperty("DominionCoreVariables", "");

		List<CardValuationVariables> variablesToUseForPurchase = gamesetup.getDeckVariables();
		List<CardValuationVariables> variablesToUseForActions = gamesetup.getHandVariables();
		String[] varsToInclude = coreVariables.split(",");
		for (String v : varsToInclude) {
			variablesToUseForPurchase.add(CardValuationVariables.valueOf(v));
			variablesToUseForActions.add(CardValuationVariables.valueOf(v));
		}

		Set<CardValuationVariables> allVariables = new HashSet<CardValuationVariables>();
		allVariables.addAll(variablesToUseForPurchase);
		allVariables.addAll(variablesToUseForActions);
		if (useHandVariables) {
			variablesToUseForPurchase = new ArrayList<CardValuationVariables>();
			variablesToUseForPurchase.addAll(allVariables);
		}

		Decider<Player> purchase = null;
		Decider<Player> action = null;

		Decider<Player> hardCodedActionDecider = new HardCodedActionDecider(variablesToUseForActions);

		Decider<Player> bigMoneyPurchase = new BigMoneyDecider(HopshackleUtilities.convertList(variablesToUseForPurchase));
		DominionDeciderContainer bigMoney = new DominionDeciderContainer(bigMoneyPurchase, hardCodedActionDecider);
		bigMoney.setName("BigMoney");
		bigMoney.injectProperties(properties);

		if (deciderType.equals("NN")) {
			List<CardType> cardTypes = gamesetup.getCardTypes();
			List<ActionEnum<Player>> actionsToUse = CardType.generateListOfPossibleActionEnumsFromCardTypes(cardTypes);
			purchase = new DominionNeuralDecider(variablesToUseForPurchase, actionsToUse);
			action = hardCodedActionDecider;
		} else if (deciderType.equals("MCTS")) {
			purchase = new MCTSMasterDecider<Player>(
					new DominionStateFactory(HopshackleUtilities.convertList(variablesToUseForPurchase)), 
					bigMoney, bigMoney);
			if (hardCodedActions) {
				action = hardCodedActionDecider;
			} else {
				action = purchase;
			}
		} else {
			throw new AssertionError("Unknown DeciderType " + deciderType);
		}

		DominionDeciderContainer retValue = new DominionDeciderContainer(purchase, action);
		retValue.setName(name);
		retValue.injectProperties(properties);
		return retValue;
	}

	private Decider<Player> getDecider(Player player) {
		switch (player.getPlayerState()) {
		case PURCHASING:
			return purchase;
		case PLAYING:
		case WAITING:
			return action;
		}
		return purchase;
	}

	private Decider<Player> getDecider(Player decidingAgent, List<ActionEnum<Player>> possibleActions) {
		if (useActionDeciderFor(possibleActions))
			return action;
		return purchase;
	}

	private boolean useActionDeciderFor(List<ActionEnum<Player>> options) {
		for (ActionEnum<Player> o : options) {
			if (o instanceof CardTypeList) {
				return false;
			} else if (o instanceof CardTypeAugment) {
				CardTypeAugment cta = (CardTypeAugment) o;
				if (cta.type == ChangeType.PLAY) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public double valueOption(ActionEnum<Player> option, Player decidingAgent) {
		return getDecider(decidingAgent).valueOption(option, decidingAgent);
	}

	@Override
	public void learnFrom(ExperienceRecord<Player> exp, double maxResult) {
		Action<Player> a = exp.getActionTaken();
		if (a instanceof DominionAction) {
			if (((DominionAction)a).isAction()) {
				action.learnFrom(exp, maxResult);
			} else {
				purchase.learnFrom(exp, maxResult);
			}
			return;
		}
		throw new AssertionError("Unknown action type " + a);
	}

	@Override
	public void learnFromBatch(List<ExperienceRecord<Player>> exp, double maxResult) {
		if (exp.isEmpty())
			return;
		Action<Player> a = exp.get(0).getActionTaken();
		if (a instanceof DominionAction) {
			if (((DominionAction)a).isAction()) {
				action.learnFromBatch(exp, maxResult);
			} else {
				purchase.learnFromBatch(exp, maxResult);
			}
			return;
		}
		throw new AssertionError("Unknown action type " + a);
	}

	@Override
	public void learnFromBatch(ExperienceRecord<Player>[] exp, double maxResult) {
		if (exp.length == 0)
			return;
		Action<Player> a = exp[0].getActionTaken();
		if (a instanceof DominionAction) {
			if (((DominionAction)a).isAction()) {
				action.learnFromBatch(exp, maxResult);
			} else {
				purchase.learnFromBatch(exp, maxResult);
			}
			return;
		}
		throw new AssertionError("Unknown action type " + a);
	}

	@Override
	public State<Player> getCurrentState(Player agent) {
		return getDecider(agent).getCurrentState(agent);
	}

	@Override
	public Decider<Player> crossWith(Decider<Player> decider) {
		return this;
	}

	@Override
	public <V extends GeneticVariable<Player>> List<V> getVariables() {
		return purchase.getVariables();
	}
	public <V extends GeneticVariable<Player>> List<V> getActionVariables() {
		return action.getVariables();
	}

	@Override
	public ActionEnum<Player> decideWithoutLearning(Player decidingAgent, List<ActionEnum<Player>> options) {
		return getDecider(decidingAgent).decideWithoutLearning(decidingAgent, options);
	}

	@Override
	public ActionEnum<Player> getOptimalDecision(Player decidingAgent, List<ActionEnum<Player>> options) {
		return getDecider(decidingAgent).getOptimalDecision(decidingAgent, options);
	}

	@Override
	public ActionEnum<Player> makeDecision(Player decidingAgent, List<ActionEnum<Player>> possibleActions) {
		return getDecider(decidingAgent, possibleActions).makeDecision(decidingAgent, possibleActions);
	}


	@Override
	public Action<Player> decide(Player decidingAgent, List<ActionEnum<Player>> possibleActions) {
		return getDecider(decidingAgent, possibleActions).decide(decidingAgent, possibleActions);
	}


	@Override
	public void injectProperties(DeciderProperties decProp) {
		if (purchase != null) purchase.injectProperties(decProp);
		if (action != null) action.injectProperties(decProp);
	}

	@Override
	public DeciderProperties getProperties() {
		return purchase.getProperties();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return name;
	}

}

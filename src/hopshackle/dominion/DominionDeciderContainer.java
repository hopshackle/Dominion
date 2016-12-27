package hopshackle.dominion;

import java.util.List;

import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.*;

public class DominionDeciderContainer implements Decider<Player> {

	protected Decider<Player> purchase, action;
	private String name;

	public DominionDeciderContainer(Decider<Player> purchase, Decider<Player> action) {
		this.purchase = purchase;
		this.action = action;
		if (action == null || purchase == null)
			throw new AssertionError("Both Purchase and Action deciders must be specified");
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
	public Action<Player> decide(Player decidingAgent) {
		return getDecider(decidingAgent).decide(decidingAgent);
	}

	@Override
	public List<ActionEnum<Player>> getChooseableOptions(Player a) {
		return getDecider(a).getChooseableOptions(a);
	}

	@Override
	public Decider<Player> crossWith(Decider<Player> decider) {
		return this;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		if (purchase == action) {
			purchase.setName(name);
		} else {
			purchase.setName(name + "_P");
			action.setName(name + "_A");
		}
	}

	@Override
	public <V extends GeneticVariable<Player>> List<V> getVariables() {
		return purchase.getVariables();
	}

	@Override
	public <V extends ActionEnum<Player>> List<V> getActions() {
		return purchase.getActions();
	}

	@Override
	public ActionEnum<Player> decideWithoutLearning(Player decidingAgent) {
		return getDecider(decidingAgent).decideWithoutLearning(decidingAgent);
	}

	@Override
	public ActionEnum<Player> getOptimalDecision(Player decidingAgent) {
		return getDecider(decidingAgent).getOptimalDecision(decidingAgent);
	}

	@Override
	public ActionEnum<Player> makeDecision(Player decidingAgent) {
		return getDecider(decidingAgent).makeDecision(decidingAgent);
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
	public String toString() {
		return name;
	}

}

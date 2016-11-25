package hopshackle.dominion;

import java.util.List;

import hopshackle.simulation.*;

public class DominionDeciderContainer implements Decider<Player> {

	protected Decider<Player> purchase, action;
	
	public DominionDeciderContainer(Decider<Player> purchase, Decider<Player> action) {
		this.purchase = purchase;
		this.action = action;
		if (action == null || purchase == null)
			throw new AssertionError("Both Purchase and Action deciders must be specified");
	}
	
	public Decider<Player> getDecider(Player player) {
		switch (player.getPlayerState()) {
		case PURCHASING:
			return purchase;
		case PLAYING:
			return action;
		case WAITING:
			return purchase;
		}
		return purchase;
	}

	@Override
	public double valueOption(ActionEnum<Player> option, Player decidingAgent) {
		return getDecider(decidingAgent).valueOption(option, decidingAgent);
	}

	@Override
	public void learnFrom(ExperienceRecord<Player> exp, double maxResult) {
		ActionEnum<Player> a = exp.getActionTaken().getType();
		if (a instanceof DominionBuyAction) {
			purchase.learnFrom(exp, maxResult);
			return;
		}
		if (a instanceof DominionPlayAction) {
			action.learnFrom(exp, maxResult);
			return;
		}
		throw new AssertionError("Unknown action type " + a);
	}

	@Override
	public void learnFromBatch(List<ExperienceRecord<Player>> exp, double maxResult) {
		if (exp.isEmpty())
			return;
		ActionEnum<Player> a = exp.get(0).getActionTaken().getType();
		if (a instanceof DominionBuyAction) {
			purchase.learnFromBatch(exp, maxResult);
			return;
		}
		if (a instanceof DominionPlayAction) {
			action.learnFromBatch(exp, maxResult);
			return;
		}
		throw new AssertionError("Unknown action type " + a);
	}

	@Override
	public void learnFromBatch(ExperienceRecord<Player>[] exp, double maxResult) {
		if (exp.length == 0)
			return;
		ActionEnum<Player> a = exp[0].getActionTaken().getType();
		if (a instanceof DominionBuyAction) {
			purchase.learnFromBatch(exp, maxResult);
			return;
		}
		if (a instanceof DominionPlayAction) {
			action.learnFromBatch(exp, maxResult);
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

}

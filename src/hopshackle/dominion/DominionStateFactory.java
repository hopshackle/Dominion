package hopshackle.dominion;

import java.util.List;

import hopshackle.simulation.*;

public class DominionStateFactory extends LinearStateFactory<Player> {

	public DominionStateFactory(List<GeneticVariable<Player>> var) {
		super(var);
	}

	@Override
	public State<Player> getCurrentState(Player agent) {
		PositionSummary retValue = agent.getPositionSummaryCopy();
		retValue.setVariables(getVariables());
		return retValue;
	}
}

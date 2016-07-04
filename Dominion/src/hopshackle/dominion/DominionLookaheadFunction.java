package hopshackle.dominion;

import hopshackle.simulation.*;

public class DominionLookaheadFunction implements LookaheadFunction<Player, PositionSummary> {

	@Override
	public PositionSummary apply(PositionSummary currentState, ActionEnum<Player> option) {
		PositionSummary ps = (PositionSummary) currentState.clone();
		ps.apply(option);
		return ps;
	}

	@Override
	public PositionSummary getCurrentState(Player agent) {
		return agent.getPositionSummaryCopy();
	}

}

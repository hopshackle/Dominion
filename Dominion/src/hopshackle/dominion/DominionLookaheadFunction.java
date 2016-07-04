package hopshackle.dominion;

import hopshackle.simulation.*;

public class DominionLookaheadFunction implements LookaheadFunction<Player> {

	@Override
	public LookaheadState<Player> apply(LookaheadState<Player> currentState, ActionEnum<Player> option) {
		PositionSummary ps = (PositionSummary) currentState.clone();
		ps.apply(option);
		return ps;
	}

	@Override
	public LookaheadState<Player> getCurrentState(Player agent) {
		return agent.getPositionSummaryCopy();
	}

}

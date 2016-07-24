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
	public PositionSummary getCurrentState(Player agent) {
		throw new AssertionError("Should not call getCurrentState from DominionLookaheadFunction. Use StateFactory method instead.");
//		return agent.getPositionSummaryCopy();
	}

}

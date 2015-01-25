package hopshackle.dominion;

import java.util.List;

import hopshackle.simulation.*;

public class DominionExperienceRecord extends ExperienceRecord {
	
	private PositionSummary startPS, endPS;
	private static DominionLookaheadFunction lookahead = new DominionLookaheadFunction();
	private static GameSetup gs = new GameSetup();
	
	public DominionExperienceRecord(PositionSummary ps, ActionEnum action, List<ActionEnum> possibleActions) {
		super(gs.getDeckVariables(), lookahead.convertPositionSummaryToAttributes(ps, gs.getDeckVariables()), action, possibleActions);
		startPS = ps.clone();
	}
	
	@Override
	public void updateWithResults(double reward, double[] newState, List<ActionEnum> actions, boolean endOfRun) {
		throw new AssertionError("Invalid Method for DominionExperienceRecord - use Positionsummary instead");
	}
	
	public void updateWithResults(double reward, PositionSummary newPS, List<ActionEnum> actions, boolean endOfRun) {
		double[] newState = lookahead.convertPositionSummaryToAttributes(newPS, variables);
		super.updateWithResults(reward, newState, actions, endOfRun);
		endPS = newPS.clone();
	}
	
	public PositionSummary getStartPS() {
		return startPS.clone();
	}
	
	public PositionSummary getEndPS() {
		return endPS.clone();
	}
	
	@Override
	public double[] getStartState() {
		return lookahead.convertPositionSummaryToAttributes(startPS, variables);
	}
	@Override
	public double[] getEndState() {
		return lookahead.convertPositionSummaryToAttributes(endPS, variables);
	}
	
}

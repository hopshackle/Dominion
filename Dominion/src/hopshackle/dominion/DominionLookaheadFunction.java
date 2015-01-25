package hopshackle.dominion;

import java.util.*;
import hopshackle.simulation.*;

public class DominionLookaheadFunction {

	public double[] oneStepLookahead(Agent agent, ActionEnum option, List<GeneticVariable> variableSet) {
		if (!(agent instanceof Player))
			throw new AssertionError("Agent input to DominionLookaheadFunction must be a Player");
		
		Player p = (Player) agent;
		PositionSummary ps = p.getPositionSummaryCopy();
		return oneStepLookahead(ps, option, variableSet);
	}
	
	public double[] oneStepLookahead(PositionSummary ps, ActionEnum option, List<GeneticVariable> variableSet) {
		ps.drawCard(option); 
		return convertPositionSummaryToAttributes(ps, variableSet);
	}

	public double[] convertPositionSummaryToAttributes(PositionSummary ps, List<GeneticVariable> variableSet) {
		double[] inputs = new double[variableSet.size()];
		for (int i = 0; i < variableSet.size(); i ++) {
			GeneticVariable gv = variableSet.get(i);
			inputs[i] = gv.getValue(ps, null);
		}

		return inputs;
	}

}

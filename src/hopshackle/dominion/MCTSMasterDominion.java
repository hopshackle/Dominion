package hopshackle.dominion;

import java.util.List;

import hopshackle.simulation.*;

public class MCTSMasterDominion extends MCTSMasterDecider<Player> {

	public MCTSMasterDominion(List<CardValuationVariables> variables, 
			Decider<Player> rolloutDecider, Decider<Player> opponentModel) {
		super(
				new DominionStateFactory(HopshackleUtilities.convertList(variables)), 
				rolloutDecider, opponentModel
			);
	}

	@Override
	protected MCTSChildDecider<Player> createChildDecider(MonteCarloTree<Player> tree, int refPlayer) {
		return new MCTSChildDominion(stateFactory, tree, rolloutDecider, decProp);
	}
}

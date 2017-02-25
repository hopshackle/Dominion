package hopshackle.dominion;

import hopshackle.simulation.*;

public class MCTSChildDominion extends MCTSChildDecider<Player> {

	public MCTSChildDominion(StateFactory<Player> stateFactory, MonteCarloTree<Player> tree, Decider<Player> opponentModel, DeciderProperties prop) {
		super(
				stateFactory, 
				tree, opponentModel, prop
				);
	}

}

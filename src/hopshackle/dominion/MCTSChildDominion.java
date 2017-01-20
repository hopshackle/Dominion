package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.*;

public class MCTSChildDominion extends MCTSChildDecider<Player> {

	public MCTSChildDominion(StateFactory<Player> stateFactory, List<ActionEnum<Player>> actions, MonteCarloTree<Player> tree, Decider<Player> opponentModel) {
		super(
				stateFactory, 
				actions,
				tree, opponentModel
				);
	}

	@Override
	public List<ActionEnum<Player>> getChooseableOptions(Player player) {
		switch (player.getPlayerState()) {
		case PURCHASING:
			return player.getGame().dominionPurchaseOptions(player);
		case PLAYING:
			return player.getGame().dominionPlayOptions(player);
		default:
			throw new AssertionError("Invalid Player State in getChooseableOptions : " + player.getPlayerState());
		}
	}



}

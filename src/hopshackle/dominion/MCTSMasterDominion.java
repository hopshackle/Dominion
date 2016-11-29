package hopshackle.dominion;

import java.util.List;

import hopshackle.simulation.*;

public class MCTSMasterDominion extends MCTSMasterDecider<Player> {

	public MCTSMasterDominion(List<ActionEnum<Player>> actions, List<CardValuationVariables> variables, Decider<Player> rolloutDecider, Decider<Player> opponentModel) {
		super(
				new DominionStateFactory(HopshackleUtilities.convertList(variables)), 
				actions,
				rolloutDecider, opponentModel
			);
	}
	
	@Override
	public List<ActionEnum<Player>> getChooseableOptions(Player player) {
		switch (player.getPlayerState()) {
		case PURCHASING:
			return DominionNeuralDecider.dominionPurchaseOptions(player);
		case PLAYING:
			return DominionNeuralDecider.dominionPlayOptions(player);
		default:
			throw new AssertionError("Invalid Player State in getChooseableOptions : " + player.getPlayerState());
		}
	}

	@Override
	protected MCTSChildDecider<Player> createChildDecider() {
		return new MCTSChildDominion(stateFactory, actionSet, tree, rolloutDecider);
	}
}

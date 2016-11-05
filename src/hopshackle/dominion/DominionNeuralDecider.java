package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.*;

public class DominionNeuralDecider extends NeuralLookaheadDecider<Player> {

	private boolean ableToLearn = true;

	public DominionNeuralDecider(List<CardType> actions, List<CardValuationVariables> variables) {
		super(
				new DominionStateFactory(HopshackleUtilities.convertList(variables)), 
				HopshackleUtilities.convertList(actions), 100.0
			);
	}

	@Override
	public List<ActionEnum<Player>> getChooseableOptions(Player player) {
		List<ActionEnum<Player>> retValue = null;
		switch (player.getPlayerState()) {
		case PURCHASING:
			DominionBuyingDecision dpd = new DominionBuyingDecision(player, player.getBudget(), player.getBuys());
			retValue = dpd.getPossiblePurchasesAsActionEnum();
			return retValue;
		case PLAYING:
			retValue = player.getActionsInHand();
			retValue.add(CardType.NONE);
			return retValue;
		default:
			throw new AssertionError("Invalid Player State in getChooseableOptions : " + player.getPlayerState());
		}

	}

	@Override
	public void learnFromBatch(ExperienceRecord<Player>[] expArray, double maxResult) {
		if (ableToLearn) {
			super.learnFromBatch(expArray, maxResult);
		} else {
			// ignore
		}
	}
	
	@Override
	public void learnFrom(ExperienceRecord<Player> exp, double maxResult) {
		if (ableToLearn) {
			super.learnFrom(exp, maxResult);
		} else {
			// ignore
		}
	}

	public void setLearning(boolean b) {
		ableToLearn = b;
	}
	public boolean getLearning() {
		return ableToLearn;
	}
}
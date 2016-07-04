package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.*;

public class HardCodedDiscardDecider extends LookaheadDecider<Player, PositionSummary> {
	
	public HardCodedDiscardDecider(List<CardType> actions, List<CardValuationVariables> variables) {
		super(null, CardType.toActionEnum(actions), CardValuationVariables.toGenVar(variables));
	}

	/*
	 * For this discard decider we just give a score for each card type as follows:
	 * 	Treasure cards = treasure value
	 *  Action cards = 1.5 points
	 *  Hence ensuring that victory cards are discarded first, then copper, then action cards
	 */
	@Override
	public double value(PositionSummary ps) {
		double retValue = 0.0;
		for (CardType ct : ps.getHand()) {
			if (ct.isTreasure()) retValue += ct.getTreasure();
			if (ct.isAction()) retValue += 1.5;
		}
		return retValue;
	}

	@Override
	public double valueOption(ActionEnum<Player> option, Player decidingAgent, Agent contextAgent) {
		return 0;
	}

	@Override
	public void learnFrom(ExperienceRecord<Player> exp, double maxResult) {	}
}

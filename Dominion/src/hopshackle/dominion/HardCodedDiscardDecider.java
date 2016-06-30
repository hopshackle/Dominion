package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.*;

public class HardCodedDiscardDecider extends BaseDecider<Player> implements DominionPositionDecider {
	
	public HardCodedDiscardDecider(List<? extends ActionEnum<Player>> actions, List<GeneticVariable> variables) {
		super(actions, variables);
	}

	/*
	 * For this discard decider we just give a score for each card type as follows:
	 * 	Treasure cards = treasure value
	 *  Action cards = 1.5 points
	 *  Hence ensuring that victory cards are discarded first, then copper, then action cards
	 */
	public double valuePosition(PositionSummary ps) {
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
	public List<CardType> buyingDecision(Player player, int budget, int buys) {
		// we should never be making a purchase decision (see name)
		return new ArrayList<CardType>();
	}
}

package hopshackle.dominion;

import java.util.List;

import hopshackle.simulation.*;

public interface DominionPositionDecider extends Decider {
	
	public double valuePosition(PositionSummary ps);

	public List<CardType> buyingDecision(Player player, int budget, int buys);
	
//	public List<CardType> decide(Player decidingAgent, List<List<CardType>> possiblePurchases);
	
}

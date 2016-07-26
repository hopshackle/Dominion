package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.*;

public class BigMoneyDecider extends LookaheadDecider<Player> {
	
	public BigMoneyDecider(List<ActionEnum<Player>> actionsToUse, List<GeneticVariable<Player>> variables) {
		super(new DominionStateFactory(variables), new DominionLookaheadFunction(), actionsToUse);
	}
	
	@Override
	public double valueOption(ActionEnum<Player> option, Player decidingAgent) {
		Player p = (Player) decidingAgent;
		PositionSummary ps = (PositionSummary) stateFactory.getCurrentState(p);
		ps = ps.apply(option); 
		
		double retValue = value(ps);

		if (localDebug)
			decidingAgent.log("Option " + option.toString() + " has base Value of " + retValue);
		
		return retValue;
	}
	
	@Override
	public double value(LookaheadState<Player> state) {
		PositionSummary ps = (PositionSummary) state;
		double retValue = 0.0;
		
		double provincesLeft = ps.getNumberOfCardsRemaining(CardType.PROVINCE);
		double wealthDensity = ps.getWealthDensity();
		double victoryDensity = ps.getVictoryDensity();
		double endGame = 1.0 - ps.getPercentageDepleted()[2];
		double provinces = ps.getNumberOfCardsTotal(CardType.PROVINCE);
		double cards = ps.totalNumberOfCards();
		
		retValue = wealthDensity + provinces * 6.0;
		
		if (provincesLeft <= 6.0 || endGame < 0.35)
			retValue += victoryDensity * 2.0;
		
		if (provincesLeft <= 3.0 || endGame < 0.20)
			retValue += cards * victoryDensity * 5.0;
		
		return retValue;
	}
	
	public List<ActionEnum<Player>> getChooseableOptions(Player decidingAgent, Agent contextAgent) {
		Player player = (Player) decidingAgent;
		DominionBuyingDecision dpd = new DominionBuyingDecision(player, player.getBudget(), player.getBuys());
		List<ActionEnum<Player>> retValue = dpd.getPossiblePurchasesAsActionEnum();
		return retValue;
	}
	
	@Override
	public String toString() {
		return "BigMoney";
	}

	@Override
	public void learnFrom(ExperienceRecord<Player> exp, double maxResult) {	}
	@Override
	public void learnFromBatch(List<ExperienceRecord<Player>> exp, double maxResult) {	}
}

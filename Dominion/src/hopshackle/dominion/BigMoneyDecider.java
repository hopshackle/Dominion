package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.*;

public class BigMoneyDecider extends BaseDecider implements DominionPositionDecider {

	private static ArrayList<GeneticVariable>variablesToUse = new ArrayList<GeneticVariable>(EnumSet.allOf(CardValuationVariables.class));
	private static ArrayList<ActionEnum> actionsToUse= new ArrayList<ActionEnum>(EnumSet.allOf(CardType.class));
	
	public BigMoneyDecider() {
		super(actionsToUse, variablesToUse);
	}
	
	@Override
	public double valueOption(ActionEnum option, Agent decidingAgent, Agent contextAgent) {
		Player p = (Player) decidingAgent;
		PositionSummary ps = p.getPositionSummaryCopy();
		ps.drawCard(option); 
		
		double retValue = valuePosition(ps);

		if (localDebug)
			decidingAgent.log("Option " + option.toString() + " has base Value of " + retValue);
		
		return retValue;
	}
	
	@Override
	public double valuePosition(PositionSummary ps) {
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
	
	public List<ActionEnum> getChooseableOptions(Agent decidingAgent, Agent contextAgent) {
		Player player = (Player) decidingAgent;
		DominionBuyingDecision dpd = new DominionBuyingDecision(player, player.getBudget(), player.getBuys());
		List<ActionEnum> retValue = dpd.getPossiblePurchasesAsActionEnum();
		return retValue;
	}
	
	@Override
	protected ExperienceRecord getExperienceRecord(Agent decidingAgent, Agent contextAgent, ActionEnum option) {
		Player player = (Player) decidingAgent;
		DominionExperienceRecord output = new DominionExperienceRecord(player.getPositionSummaryCopy(), option, getChooseableOptions(decidingAgent, contextAgent));
		return output;
	}
	
	@Override
	public String toString() {
		return "BigMoney";
	}

	@Override
	public List<CardType> buyingDecision(Player player, int budget, int buys) {
		List<CardType> retValue = (new DominionBuyingDecision(player, budget, buys)).getBestPurchase();
		for (CardType purchase : retValue)
			learnFromDecision(player, player, purchase);	
		return retValue;
	}
}

package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.*;

public class BigMoneyDecider extends LookaheadDecider<Player, PositionSummary> {

	private static ArrayList<GeneticVariable<Player>>variablesToUse = new ArrayList<GeneticVariable<Player>>(EnumSet.allOf(CardValuationVariables.class));
	private static ArrayList<ActionEnum<Player>> actionsToUse= new ArrayList<ActionEnum<Player>>(EnumSet.allOf(CardType.class));
	
	public BigMoneyDecider() {
		super(null, actionsToUse, variablesToUse);
	}
	
	@Override
	public double valueOption(ActionEnum<Player> option, Player decidingAgent, Agent contextAgent) {
		Player p = (Player) decidingAgent;
		PositionSummary ps = p.getPositionSummaryCopy();
		ps.apply(option); 
		
		double retValue = value(ps);

		if (localDebug)
			decidingAgent.log("Option " + option.toString() + " has base Value of " + retValue);
		
		return retValue;
	}
	
	@Override
	public double value(PositionSummary ps) {
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

package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.*;

public class BigMoneyDecider extends LookaheadDecider<Player> {
	
	public BigMoneyDecider(List<GeneticVariable<Player>> variables) {
		super(new DominionStateFactory(variables));
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
	public double value(State<Player> state) {
		PositionSummary ps = (PositionSummary) state;
		double retValue = 0.0;
		
		double provincesLeft = ps.getNumberOfCardsRemaining(CardType.PROVINCE);
		double wealthDensity = ps.getWealthDensity();
		double victoryDensity = ps.getVictoryDensity();
		double endGame = 1.0 - ps.getPercentageDepleted()[2];
		double penEndGame = 1.0 - ps.getPercentageDepleted()[1];
		double provinces = ps.getNumberOfCardsTotal(CardType.PROVINCE);
		double cards = ps.totalNumberOfCards();
		
		retValue = wealthDensity + provinces * 6.0;
		
		if (provincesLeft <= 6.0 || penEndGame < 0.50)
			retValue += victoryDensity * 2.0;
		
		if (provincesLeft <= 3.0 || endGame < 0.50)
			retValue += cards * victoryDensity * 5.0;

		double handValue = 0.0;
		for (CardType ct : ps.getHand()) {
			// for discards, we 
			handValue += ct.getTreasure() * ct.getTreasure() * 2;
			handValue -= ct.getVictory(ps) * 3;
			handValue += ct.getCost();		// assuming that expensive action cards are better
		}
		retValue += handValue / 10.0;
		
		return retValue * 100.0;
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

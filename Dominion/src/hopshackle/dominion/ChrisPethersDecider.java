package hopshackle.dominion;

import java.util.List;

import hopshackle.simulation.ActionEnum;
import hopshackle.simulation.GeneticVariable;
import hopshackle.simulation.LookaheadState;

public class ChrisPethersDecider extends BigMoneyDecider{

	public ChrisPethersDecider(List<ActionEnum<Player>> actionsToUse, List<GeneticVariable<Player>> variables) {
		super(actionsToUse, variables);
	}

	@Override
	public double value(LookaheadState<Player> state) {
		PositionSummary ps = (PositionSummary) state;
		double retValue = 0.0;

		double mines = ps.getNumberOfCardsTotal(CardType.MINE);
		double markets = ps.getNumberOfCardsTotal(CardType.MARKET);
		double remodel = ps.getNumberOfCardsTotal(CardType.REMODEL);
		double smithies = ps.getNumberOfCardsTotal(CardType.SMITHY);
		double militia = ps.getNumberOfCardsTotal(CardType.MILITIA);
		double workshop = ps.getNumberOfCardsTotal(CardType.WORKSHOP);
		double villages = ps.getNumberOfCardsTotal(CardType.VILLAGE);
		double cellars = ps.getNumberOfCardsTotal(CardType.CELLAR);

		double provincesLeft = ps.getNumberOfCardsRemaining(CardType.PROVINCE);
		double provinces = ps.getNumberOfCardsTotal(CardType.PROVINCE);
		double duchies = ps.getNumberOfCardsTotal(CardType.DUCHY);
		double estates = ps.getNumberOfCardsTotal(CardType.ESTATE);
		double silver = ps.getNumberOfCardsTotal(CardType.SILVER);
		double gold = ps.getNumberOfCardsTotal(CardType.GOLD);
		double curse = ps.getNumberOfCardsTotal(CardType.CURSE);
		double cards = ps.totalNumberOfCards();
		double depletion = 1.0 - ps.getPercentageDepleted()[2];

		if (cards < 12) 
			// first two turns
			retValue = mines * 5 + remodel * 4 + workshop * 3 + cellars * 2;
		else  {
			retValue = Math.min(mines, 1) * 10 + Math.min(markets, 2) * 7.25 + Math.min(remodel, 1) * 8 + Math.min(smithies, 1) * 7 + Math.min(militia, 1) * 6
			+ Math.min(Math.max(smithies-1, 0), 4) * 3 + Math.min(workshop, 1) * 4 + Math.min(villages, smithies+2) * 3.5 + Math.min(cellars, 1) * 2;
			retValue += 1.5 * villages;
			retValue += 1.75 * markets;
		}

		retValue += provinces * 24;

		if (provincesLeft < 4 || depletion < 0.25) {
			retValue += duchies * 12;
			if (provincesLeft < 2 || depletion < 0.15)
				retValue += estates * 4;
		}
		
		retValue -= 10 * curse;
		retValue -= cards;
		retValue += 1 * silver;
		retValue += 2 * gold;

		return retValue / 100.0;
	}
	
	@Override
	public String toString() {
		return "ChrisPethers";
	}

}

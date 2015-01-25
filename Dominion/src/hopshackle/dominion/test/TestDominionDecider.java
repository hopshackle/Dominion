package hopshackle.dominion.test;

import hopshackle.dominion.*;
import hopshackle.simulation.*;

import java.util.*;

public class TestDominionDecider extends BaseDecider implements DominionPositionDecider {

	private HashMap<CardType, Double> values;
	private static ArrayList<ActionEnum> actionsToUse;
	private static ArrayList<GeneticVariable> variablesToUse;

	static {
		variablesToUse = new ArrayList<GeneticVariable>(EnumSet.allOf(CardValuationVariables.class));
		actionsToUse = new ArrayList<ActionEnum>(EnumSet.allOf(CardType.class));
	}

	public TestDominionDecider(HashMap<CardType, Double> values) {
		super(actionsToUse, variablesToUse);
		this.values = values;
		if (this.values == null)
			this.values = new HashMap<CardType, Double>();
	}

	@Override
	public double valueOption(ActionEnum input, Agent decidingAgent, Agent contextAgent) {
		double retValue = 0.0;
		List<CardType> cards = new ArrayList<CardType>();
		if (input instanceof CardTypeList) {
			CardTypeList ctl = (CardTypeList) input;
			cards = ctl.cards;
		} else {
			cards.add((CardType)input);
		}
		for (CardType option : cards) {
			retValue -= 0.05;
			if (values.containsKey(option))
				retValue += values.get(option);
			if (option == CardType.NONE)
				retValue = 0.05;
		}
		return retValue;
	}

	public static TestDominionDecider getExample(CardType preferredCard) {
		HashMap<CardType, Double> map = new HashMap<CardType, Double>();
		map.put(preferredCard, 1.0);
		return new TestDominionDecider(map);
	}

	@Override
	public double valuePosition(PositionSummary ps) {
		double retValue = 0.0;
		//		for (CardType ct : ps.getHand()) {
		//			if (ct.isTreasure())
		//				retValue = retValue + (double)ct.getTreasure() * 0.05;
		//		}
		//		retValue = retValue + ps.totalNumberOfCards() * ps.getWealthDensity() * 0.05;

		for (CardType card : values.keySet()) {
			retValue += ps.getNumberOfCardsTotal(card) * values.get(card);
		}
		retValue -= ps.totalNumberOfCards() * 0.05;
		return retValue;
	}

	@Override
	public List<ActionEnum> getChooseableOptions(Agent decidingAgent, Agent contextAgent) {
		List<ActionEnum> retValue = null;
		Player player = (Player) decidingAgent;
		if (player.isTakingActions()) {
			return super.getChooseableOptions(decidingAgent, contextAgent);
		} else {
			DominionBuyingDecision dpd = new DominionBuyingDecision(player, player.getBudget(), player.getBuys());
			retValue = dpd.getPossiblePurchasesAsActionEnum();
		}
		return retValue;
	}

	@Override
	public List<CardType> buyingDecision(Player player, int budget, int buys) {
		List<CardType> retValue = (new DominionBuyingDecision(player, budget, buys)).getBestPurchase();
		for (CardType purchase : retValue)
			learnFromDecision(player, player, purchase);
		return retValue;
	}
}

class TestThiefDominionDecider extends TestDominionDecider {

	public TestThiefDominionDecider(HashMap<CardType, Double> values) {
		super(values);
	}

	@Override
	public double valuePosition(PositionSummary ps) {
		double retValue = 0.0;
		for (CardType ct : ps.getHand()) {
			if (ct.isTreasure())
				retValue = retValue + (double)ct.getTreasure() * 0.05;
		}
		retValue = retValue - ps.totalNumberOfCards() * 0.06;

		return retValue;
	}
}

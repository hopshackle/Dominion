package hopshackle.dominion.test;

import hopshackle.dominion.*;
import hopshackle.simulation.*;

import java.util.*;

public class TestDominionDecider extends LookaheadDecider<Player> {

	private HashMap<CardType, Double> values;
	private static ArrayList<CardType> actionsToUse;
	private static ArrayList<CardValuationVariables> variablesToUse;
	private static LookaheadFunction<Player> lookahead = new DominionLookaheadFunction();

	static {
		variablesToUse = new ArrayList<CardValuationVariables>(EnumSet.allOf(CardValuationVariables.class));
		actionsToUse = new ArrayList<CardType>(EnumSet.allOf(CardType.class));
	}

	public TestDominionDecider(HashMap<CardType, Double> values) {
		super(new DominionStateFactory(HopshackleUtilities.convertList(variablesToUse)), lookahead,  HopshackleUtilities.convertList(actionsToUse));
		this.values = values;
		if (this.values == null)
			this.values = new HashMap<CardType, Double>();
	}

	@Override
	public double valueOption(ActionEnum<Player> input, Player decidingAgent) {
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
	public double value(LookaheadState<Player> state) {
		PositionSummary ps = (PositionSummary) state;
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
	public List<ActionEnum<Player>> getChooseableOptions(Player decidingAgent) {
		List<ActionEnum<Player>> retValue = null;
		Player player = (Player) decidingAgent;
		if (player.isTakingActions()) {
			return super.getChooseableOptions(decidingAgent);
		} else {
			DominionBuyingDecision dpd = new DominionBuyingDecision(player, player.getBudget(), player.getBuys());
			retValue = dpd.getPossiblePurchasesAsActionEnum();
		}
		return retValue;
	}

	@Override
	public void learnFrom(ExperienceRecord<Player> exp, double maxResult) {	}
}

class TestThiefDominionDecider extends TestDominionDecider {

	public TestThiefDominionDecider(HashMap<CardType, Double> values) {
		super(values);
	}

	@Override
	public double value(LookaheadState<Player> state) {
		PositionSummary ps = (PositionSummary) state;
		double retValue = 0.0;
		for (CardType ct : ps.getHand()) {
			if (ct.isTreasure())
				retValue = retValue + (double)ct.getTreasure() * 0.05;
		}
		retValue = retValue - ps.totalNumberOfCards() * 0.06;

		return retValue;
	}
}

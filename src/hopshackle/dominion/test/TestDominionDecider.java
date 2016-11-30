package hopshackle.dominion.test;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.*;

import java.util.*;

public class TestDominionDecider extends LookaheadDecider<Player> {

	private HashMap<CardType, Double> values;
	private static List<ActionEnum<Player>> actionsToUse;
	private static List<CardValuationVariables> variablesToUse;

	static {
		variablesToUse = new ArrayList<CardValuationVariables>(EnumSet.allOf(CardValuationVariables.class));
		List<CardType> allCards = new ArrayList<CardType>(EnumSet.allOf(CardType.class));
		actionsToUse = CardType.toActionEnum(allCards);
	}

	public TestDominionDecider(HashMap<CardType, Double> values) {
		super(new DominionStateFactory(HopshackleUtilities.convertList(variablesToUse)),  HopshackleUtilities.convertList(actionsToUse));
		this.values = values;
		if (this.values == null)
			this.values = new HashMap<CardType, Double>();
	}

	@Override
	public double valueOption(ActionEnum<Player> input, Player decidingAgent) {
		double retValue = 0.0;
		List<CardTypeAugment> cards = new ArrayList<CardTypeAugment>();
		if (input instanceof CardTypeList) {
			CardTypeList ctl = (CardTypeList) input;
			cards = ctl.cards;
		} else {
			cards.add((CardTypeAugment) input);
		}
		for (CardTypeAugment option : cards) {
			double sign = 1.0;
			if (option.type == ChangeType.LOSS)
				sign = -1.0;
			retValue -= 0.05 * sign;
			if (values.containsKey(option.card))
				retValue += values.get(option.card) * sign;
			if (option.card == CardType.NONE)
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
	public double value(State<Player> state) {
		PositionSummary ps = (PositionSummary) state;
		double retValue = 0.0;

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
	public double value(State<Player> state) {
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

package hopshackle.dominion.test;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.*;

import java.util.*;

public class TestDominionDecider extends BaseAgentDecider<Player> {

	private HashMap<CardType, Double> values;
	private HashMap<CardType, Double> handValues;
	private static List<CardValuationVariables> variablesToUse;

	static {
		variablesToUse = new ArrayList<CardValuationVariables>(EnumSet.allOf(CardValuationVariables.class));
	}

	public TestDominionDecider(HashMap<CardType, Double> values, HashMap<CardType, Double> handValues) {
		super(new DominionStateFactory(HopshackleUtilities.convertList(variablesToUse)));
		this.values = values;
		this.handValues = handValues;
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
			if (option.type == ChangeType.PLAY) {
				if (values.containsKey(option.card)) {
					retValue += values.get(option.card);
				}
			} else {
				if (option.from == CardSink.SUPPLY || option.from == CardSink.TRASH) {
					retValue += 0.05;
					if (option.type == ChangeType.BUY) {
					int buysLeft = decidingAgent.getBuys() - 1;
					int budgetLeft = decidingAgent.getBudget() - option.card.getCost();
					if (buysLeft > 0) retValue += Math.pow(budgetLeft, 1.5) * 0.1;
					}
					if (values.containsKey(option.card)) {
						retValue += values.get(option.card);
					}
				}
				if (option.to == CardSink.TRASH) {
					retValue -= 0.05;
					if (values.containsKey(option.card)) {
						retValue -= values.get(option.card);
					}
				}

				if (option.to == CardSink.HAND && handValues.containsKey(option.card)) {
					retValue += handValues.get(option.card);
				}
				if (option.from == CardSink.HAND && handValues.containsKey(option.card)) {
					retValue -= handValues.get(option.card);
				}
			}
			if (option.card == CardType.NONE)
				retValue = 0.05;

		}
		return retValue;
	}

	public static TestDominionDecider getExample(CardType preferredCard) {
		HashMap<CardType, Double> map = new HashMap<CardType, Double>();
		map.put(preferredCard, 2.0);
		return new TestDominionDecider(map, new HashMap<CardType, Double>());
	}

	@Override
	public void learnFrom(ExperienceRecord<Player> exp, double maxResult) {	}
}

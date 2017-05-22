package hopshackle.dominion.test;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.*;

import java.util.*;

public class TestDominionDecider extends BaseAgentDecider<Player> {

    private HashMap<CardType, Double> values;
    private HashMap<CardType, Double> handValues;
    private HashMap<CardValuationVariables, Double> varValues;
    private static List<CardValuationVariables> variablesToUse;

    static {
        variablesToUse = new ArrayList<CardValuationVariables>(EnumSet.allOf(CardValuationVariables.class));
    }

    public TestDominionDecider(HashMap<CardType, Double> values, HashMap<CardType, Double> handValues) {
        this(values, handValues, new HashMap<CardValuationVariables, Double>());
    }

    public TestDominionDecider(HashMap<CardType, Double> values, HashMap<CardType, Double> handValues, HashMap<CardValuationVariables, Double> variableValues) {
        super(new DominionStateFactory(HopshackleUtilities.convertList(variablesToUse)));
        this.values = values;
        this.handValues = handValues;
        varValues = variableValues;
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
            } else if (option.type == ChangeType.SPY) {
                double sign = option.to.equals(CardSink.DISCARD) ? 1.0 : -1.0;
				if (option.card.isVictory()) retValue += sign * -2.0;
				if (option.card.isAction()) retValue += sign * 2.0;
				retValue += sign * (option.card.getTreasure() * 0.20 - 0.3);
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

            if (!varValues.isEmpty()) {
                PositionSummary ps = decidingAgent.getPositionSummaryCopy().apply(input);
                for (CardValuationVariables cvv : varValues.keySet()) {
                    retValue += cvv.getValue(ps) * varValues.get(cvv);
                }
            }
        }
        return retValue;
    }

    public static TestDominionDecider getExample(CardType preferredCard) {
        HashMap<CardType, Double> map = new HashMap<CardType, Double>();
        map.put(preferredCard, 2.0);
        return new TestDominionDecider(map, new HashMap<CardType, Double>());
    }

    @Override
    public void learnFrom(ExperienceRecord<Player> exp, double maxResult) {
    }
}

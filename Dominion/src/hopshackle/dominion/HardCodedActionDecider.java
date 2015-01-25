package hopshackle.dominion;

import hopshackle.simulation.*;

import java.util.*;

public class HardCodedActionDecider extends BaseDecider {

	public HardCodedActionDecider(List<? extends ActionEnum> actions, List<GeneticVariable> variables) {
		super(actions, variables);
	}

	@Override
	public double valueOption(ActionEnum option, Agent decidingAgent, Agent contextAgent) {
		CardType cardType = (CardType) option;
		Player p = (Player) decidingAgent;
		List<CardType> hand = null;
		int actionsLeft = p.getActionsLeft();
		int buys = p.getBuys();
		int handTreasure = p.remainingTreasureValueOfHand();
		switch (cardType) {
		case CELLAR:
			hand = p.getCopyOfHand();
			double cellarValue = 0.05;
			for (CardType ct : hand) 
				if (ct.isVictory()) cellarValue += 0.1;
			if (actionsLeft == 1 && cellarValue > 0.05)
				cellarValue = 0.90;
			return cellarValue;
		case MARKET:
			return 0.95;
		case MINE:
			hand = p.getCopyOfHand();
			if (hand.contains(CardType.SILVER)) return 0.35;
			if (hand.contains(CardType.COPPER)) return 0.20;
			return 0.05;
		case MILITIA:
			return 0.55;
		case VILLAGE:
			return 1.0;
		case WORKSHOP:
			return Math.max(0.90 - handTreasure * 0.1 - actionsLeft * 0.20, 0.10);
		case WOODCUTTER:
			return 0.50;
		case MOAT:
			return 0.20 + actionsLeft * 0.10;
		case REMODEL:
			return 0.65 - actionsLeft * 0.20;
		case SMITHY:
			return 0.45 + actionsLeft * 0.15;
		case NONE:
			return 0.0;
		case BUREAUCRAT:
			return 0.62;
		case LIBRARY:
			return 0.10 + (6 - p.getHandSize()) * 0.15 + actionsLeft * 0.10;
		case THRONE_ROOM:
			return 1.05;
		case FESTIVAL:
			return 1.02;
		case CHANCELLOR:
			return 0.20;
		case COUNCIL_ROOM:
			return 0.80;
		case SPY:
			return 1.10;
		case THIEF:
			return 0.60;
		case ADVENTURER:
			return Math.max(0.80 - handTreasure * 0.1 - actionsLeft * 0.20 + buys * 0.20, 0.15);
		case CHAPEL:
			return 0.10 + p.getNumberOfTypeInHand(CardType.CURSE) * 0.25 + p.getNumberOfTypeInHand(CardType.COPPER) * 0.10 + p.getNumberOfTypeInHand(CardType.ESTATE) * 0.05 - actionsLeft * 0.10;
		case FEAST:
			return Math.max(0.95 - handTreasure * 0.1 - actionsLeft * 0.20, 0.10);
		case LABORATORY:
			return 1.0;
		case MONEYLENDER:
			return Math.min(p.getNumberOfTypeInHand(CardType.COPPER), 1) * 0.75 + buys * 0.10 - handTreasure * 0.05;
		case WITCH:
			int cursesRemaining = p.getGame().getNumberOfCardsRemaining(CardType.CURSE);
			return 0.20 + actionsLeft * 0.10 + Math.min(cursesRemaining, 3) * 0.05;
		default:
			break;
		}
		return 0.0;
	}

	protected ActionEnum makeDecision(Agent decidingAgent, Agent contextAgent, double explorationChance) {
		return super.makeDecision(decidingAgent, contextAgent, 0.0);
		// i.e. never explore with a Hardcoded Decider
	}
}

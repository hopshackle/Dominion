package hopshackle.dominion;

import java.util.ArrayList;
import java.util.List;

import hopshackle.simulation.*;

public enum CardType implements ActionEnum<Player> {

	NONE		(0, 0, 0, 0, 0, 0, 0, 0),
	UNKNOWN		(0, 0, 0, 0, 0, 0, 0, 0),
	COPPER 		(0, 1, 0, 0, 0, 0, 0, 0),
	SILVER 		(3, 2, 0, 0, 0, 0, 0, 0),
	GOLD 		(6, 3, 0, 0, 0, 0, 0, 0),
	ESTATE		(2, 0, 1, 0, 0, 0, 0, 0),
	DUCHY		(5, 0, 3, 0, 0, 0, 0, 0),
	PROVINCE	(8, 0, 6, 0, 0, 0, 0, 0),
	CURSE		(0, 0, -1, 0, 0, 0, 0, 0),
	VILLAGE		(3, 0, 0, 0, 0, 2, 1, 0),
	CELLAR		(2, 0, 0, 0, 0, 1, 0, 0),
	MARKET		(5, 0, 0, 1, 1, 1, 1, 0),
	MILITIA		(4, 0, 0, 0, 2, 0, 0, 0),
	MINE		(5, 0, 0, 0, 0, 0, 0, 0),
	MOAT		(2, 0, 0, 0, 0, 0, 2, 1),
	REMODEL		(4, 0, 0, 0, 0, 0, 0, 0),
	SMITHY		(4, 0, 0, 0, 0, 0, 3, 0),
	WOODCUTTER	(3, 0, 0, 1, 2, 0, 0, 0),
	WORKSHOP	(3, 0, 0, 0, 0, 0, 0, 0), 
	BUREAUCRAT	(4, 0, 0, 0, 0, 0, 0, 0),
	FESTIVAL	(5, 0, 0, 1, 2, 2, 0, 0),
	LIBRARY		(5, 0, 0, 0, 0, 0, 0, 0),
	THRONE_ROOM (4, 0, 0, 0, 0, 0, 0, 0),
	CHANCELLOR	(3, 0, 0, 0, 2, 0, 0, 0),
	COUNCIL_ROOM (5, 0, 0, 1, 0, 0, 4, 0),
	SPY			(4, 0, 0, 0, 0, 1, 1, 0),
	THIEF		(4, 0, 0, 0, 0, 0, 0, 0),
	ADVENTURER	(6, 0, 0, 0, 0, 0, 0, 0),
	CHAPEL		(2, 0, 0, 0, 0, 0, 0, 0),
	FEAST		(4, 0, 0, 0, 0, 0, 0, 0),
	LABORATORY  (5, 0, 0, 0, 0, 1, 2, 0),
	MONEYLENDER (4, 0, 0, 0, 0, 0, 0, 0),
	GARDENS		(4, 0, 0, 0, 0, 0, 0, 0),
	WITCH		(5, 0, 0, 0, 0, 0, 2, 0);

	private int cost;
	private int treasureValue;
	private int victoryPoints;
	private int additionalBuys;
	private int additionalPurchasePower;
	private int actions;
	private int draw;
	private boolean reaction;

	CardType(int cost, int treasure, int victory, int buys, int purchasePower, int actions, int cardsToDraw, int reactionCard) {
		this.cost = cost;
		treasureValue = treasure;
		victoryPoints = victory;
		additionalBuys = buys;
		additionalPurchasePower = purchasePower;
		this.actions = actions;
		draw = cardsToDraw;
		reaction = (reactionCard == 1);
	}

	public int getCost() {
		return cost;
	}
	public int getTreasure() {
		return treasureValue;
	}
	public int getVictory(PositionSummary ps) {
		switch (this) {
		case GARDENS:
			return (int) (ps.totalNumberOfCards() / 10.0);
		default:
			return victoryPoints;
		}
	}
	public int getAdditionalBuys() {
		return additionalBuys;
	}
	public int getAdditionalPurchasePower() {
		return additionalPurchasePower;
	}
	public int getAdditionalActions() {
		return actions;
	}
	public int getDraw() {
		return draw;
	}
	public boolean isReactive() {
		return reaction;
	}
	public boolean isTreasure() {
		return (treasureValue > 0);
	}
	public boolean isVictory() {
		switch (this) {
		case GARDENS:
			return true;
		default:
		}
		return (victoryPoints != 0);
	}
	public boolean isAction() {
		return (!isTreasure() && !isVictory());
	}

	@Override
	public boolean isChooseable(Player p) {
		if (p.isTakingActions()) {
			// hack to distinguish between chooseability at purchase and use
			if (this == CardType.NONE) return true;
			if (p.getNumberOfTypeInHand(this) > 0 && isAction())
				return true;
			else
				return false;
		} else {
			if (!p.getGame().availableCardsToPurchase().contains(this))
				return false;
			return true;
		}
	}

	@Override
	public Action<Player> getAction(Player p) {
		switch (p.getPlayerState()) {
		case PLAYING:
			return new DominionPlayAction(p, this);
		case PURCHASING:
		case WAITING:
		}
		return new DominionBuyAction(p, this);
	}
	
	@Override
	public String getChromosomeDesc() {return "DOM1";}

	@Override
	public Enum<CardType> getEnum() {
		return this;
	}

	public static List<ActionEnum<Player>> toActionEnum(List<CardType> actionsToUse) {
		List<ActionEnum<Player>> retValue = new ArrayList<ActionEnum<Player>>();
		for (CardType ct : actionsToUse) {
			retValue.add((ActionEnum<Player>) ct);
		}
		return retValue;
	}
}

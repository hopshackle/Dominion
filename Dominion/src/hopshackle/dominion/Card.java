package hopshackle.dominion;

public class Card {

	private CardType type;
	
	public Card(CardType type) {
		this.type = type;
	}
	
	public CardType getType() {
		return type;
	}
	
	public int getCost() {
		return type.getCost();
	}
	public int getTreasure() {
		return type.getTreasure();
	}
	public int getVictory(PositionSummary ps) {
		return type.getVictory(ps);
	}
	public boolean isTreasure() {
		return type.isTreasure();
	}
	public boolean isVictory() {
		return type.isVictory();
	}
	public String toString() {
		return type.toString();
	}

	public boolean isAction() {
		return type.isAction();
	}

	public int getAdditionalBuys() {
		return type.getAdditionalBuys();
	}
	
	public int getAdditionalActions() {
		return type.getAdditionalActions();
	}
	
	public int getAdditionalPurchasePower() {
		return type.getAdditionalPurchasePower();
	}
	
	public void takeAction (Player player) {
		for (int n=0; n < type.getDraw(); n++) {
			player.drawTopCardFromDeckIntoHand();
		}
		return;
	}

	public boolean executeReactionAgainst(AttackCard attackCard, Player attacker, Player victim) {
		return false;
	}
	
	public void reset() {
	}
}

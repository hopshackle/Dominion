package hopshackle.dominion;

import hopshackle.simulation.*;
import java.util.*;

public class Card {

	private CardType type;
	protected static List<ActionEnum<Player>> emptyList = new ArrayList<ActionEnum<Player>>();
	
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
	
	public List<ActionEnum<Player>> takeAction (Player player) {
		for (int n=0; n < type.getDraw(); n++) {
			player.drawTopCardFromDeckIntoHand();
		}
		for (int i = 0; i < type.getAdditionalActions(); i++)
			player.incrementActionsLeft();
		return emptyList;
	}

	public boolean executeReactionAgainst(AttackCard attackCard, Player attacker, Player victim) {
		return false;
	}
	
	public void reset() {
	}
	
	public DominionAction followUpAction() {
		return null;
	}

	public Player nextActor() {
		return null;
	}
	
	public Card clone(DominionGame newGame) {
		return new Card(type);
	}
}

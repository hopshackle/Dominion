package hopshackle.dominion;

public class Workshop extends Card {

	public Workshop() {
		super(CardType.WORKSHOP);
	}

	public void takeAction(Player player) {
		super.takeAction(player);
		DominionPositionDecider purchaseDecider = player.getPurchaseDecider();
		player.setStateToPurchase();
		CardType cardPurchased = purchaseDecider.buyingDecision(player, 4, 1).get(0);
		player.setStateToAction();
		player.takeCardFromSupplyIntoDiscard(cardPurchased);
		player.log("Gains " + cardPurchased);
	}
}

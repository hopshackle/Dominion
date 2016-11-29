package hopshackle.dominion.basecards;

import hopshackle.dominion.*;
public class ThroneRoom extends Card {

	private CardType enthronedCard;

	public ThroneRoom() {
		super(CardType.THRONE_ROOM);
	}

	public void takeAction(Player player) {
		super.takeAction(player);
		enthronedCard = ((CardTypeAugment) player.getDecider().makeDecision(player)).card;
		player.log("Uses Throne Room to play " + enthronedCard);
		Card card = player.playFromHandToRevealedCards(enthronedCard);
		card.takeAction(player);
		card.takeAction(player);
	}

	@Override
	public int getAdditionalBuys() {
		if (enthronedCard != null) {
			return enthronedCard.getAdditionalBuys();
		}
		return CardType.THRONE_ROOM.getAdditionalBuys();
	}

	@Override
	public int getAdditionalPurchasePower() {
		if (enthronedCard != null) {
			return enthronedCard.getAdditionalPurchasePower();
		}
		return CardType.THRONE_ROOM.getAdditionalPurchasePower();
	}

	@Override
	public int getAdditionalActions() {
		if (enthronedCard != null) {
			return enthronedCard.getAdditionalActions() * 2;
		}
		return CardType.THRONE_ROOM.getAdditionalActions();
	}

	@Override
	public void reset() {
		enthronedCard = null;
	}

}

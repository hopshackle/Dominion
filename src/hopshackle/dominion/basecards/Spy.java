package hopshackle.dominion.basecards;

import java.util.List;

import hopshackle.dominion.AttackCard;
import hopshackle.dominion.Card;
import hopshackle.dominion.CardType;
import hopshackle.dominion.Player;
import hopshackle.simulation.ActionEnum;

public class Spy extends AttackCard {

	public Spy() {
		super(CardType.SPY);
	}


	@Override
	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		spyOnPlayer(player, false);
		return emptyList;
	}


	@Override
	public void executeAttackOnPlayer(Player target, Player attacker) {
		spyOnPlayer(target, true);
	}

	private void spyOnPlayer(Player player, boolean opponent) {
		if (opponent) player.log("Is target of SPY");
		Card topCard = player.drawTopCardFromDeckIntoHand();
		boolean discard = opponent;
		if (topCard.isVictory()) 
			discard = !discard;
		if (topCard.getType() == CardType.COPPER)
			discard = !discard;

		if (topCard.isAction() && !opponent) {
			int actions = player.getActionsLeft();
			int cardsToDraw = 0;
			for (CardType cardInHand : player.getCopyOfHand()) {
				actions += cardInHand.getAdditionalActions();
				cardsToDraw += cardInHand.getDraw();
			}
			if (actions < 2 || cardsToDraw > 0)
				discard = true;		// i.e. if its an action card but we're going to pull it into hand with no way of using it, then best to discard
		}

		if (discard) {
			player.discard(topCard.getType());
		} else {
			player.putCardFromHandOnTopOfDeck(topCard.getType());
		}
	}

}

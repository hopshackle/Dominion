package hopshackle.dominion.basecards;

import java.util.*;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.ActionEnum;
public class ThroneRoom extends Card {

	CardType enthronedCard;
	Card replicaCard;
	Player player;
	
	public ThroneRoom() {
		super(CardType.THRONE_ROOM);
	}

	@Override
	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		this.player= player;
		List<ActionEnum<Player>> retValue = new ArrayList<ActionEnum<Player>>();
		Set<CardType> cardsSeen = new HashSet<CardType>();
		for (CardType ct : player.getCopyOfHand()) {
			if (ct.isAction() && !cardsSeen.contains(ct)){
				cardsSeen.add(ct);
				retValue.add(new CardTypeAugment(ct, CardSink.HAND, CardSink.REVEALED, ChangeType.ENTHRONE));
			}
		}
		retValue.add(new CardTypeAugment(CardType.NONE, CardSink.HAND, CardSink.REVEALED, ChangeType.PLAY));
		
		return retValue;
	}
	
	public void enthrone(CardType ct) {
		enthronedCard = ct;
	}
	
	@Override
	public DominionAction followUpAction() {
		DominionAction retValue = new ThroneRoomFollowOnAction(this);
		return retValue;
	}

	@Override
	public void reset() {
		if (replicaCard != null) {
			player.removeCardFrom(replicaCard, CardSink.DECK);
			player.removeCardFrom(replicaCard, CardSink.DISCARD);
			player.removeCardFrom(replicaCard, CardSink.REVEALED);
			player.removeCardFrom(replicaCard, CardSink.HAND);
		}
		replicaCard = null;
		enthronedCard = null;
	}
}

class ThroneRoomFollowOnAction extends DominionAction {
	
	private ThroneRoom masterCard;
	
	public ThroneRoomFollowOnAction(ThroneRoom card) {
		super(card.player, new CardTypeList(new ArrayList<CardType>()));
		masterCard = card;
	}
	@Override
	public void doStuff() {
		// put masterCard back into hand from revealed
		CardType enthronedCard = masterCard.enthronedCard;
		// TODO: This could break with certain cards in future expansions
		if (enthronedCard == null || enthronedCard == CardType.NONE){
			// No card was actually played		
			return;
		}
		// Add two actions, one for each of the two cards we will be playing
		player.incrementActionsLeft();
		player.incrementActionsLeft();
//		player.removeCardFrom(enthronedCard, CardSink.REVEALED);
		masterCard.replicaCard = CardFactory.instantiateCard(enthronedCard);
		player.insertCardDirectlyIntoHand(masterCard.replicaCard);
		// set possibleOptions as just playing said card
		List<ActionEnum<Player>> singleOption = new ArrayList<ActionEnum<Player>>();
		singleOption.add(CardTypeAugment.playCard(enthronedCard));
		possibleOptions = singleOption;
		nextActor = masterCard.player;
		// finish
	}
	
	@Override
	public ActionEnum<Player> getType() {
		return CardTypeAugment.playCard(masterCard.getType());
	}
	
	@Override
	public String toString() {
		return "Follow-on ThroneRoom of " + masterCard.enthronedCard.toString();
	}
}
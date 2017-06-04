package hopshackle.dominion.basecards;

import java.util.*;
import java.util.concurrent.atomic.*;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.simulation.*;
public class ThroneRoom extends Card {

	private static AtomicLong idFountain = new AtomicLong(1);
	DominionGame game;
	CardType enthronedCard = CardType.NONE;
	int player;

	public ThroneRoom() {
		super(CardType.THRONE_ROOM);
	}

	@Override
	public List<ActionEnum<Player>> takeAction(Player player) {
		super.takeAction(player);
		this.setRef("TR" + idFountain.getAndIncrement());
		this.player = player.getNumber();
		this.game = player.getGame();
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
		Player player = game.getPlayer(this.player);
		// the problem is that we don't know where the replica card might be...
		if (enthronedCard != null && enthronedCard != CardType.NONE) {
			player.removeCardsWithRef(this.getRef()+ "-REP");
		}
		enthronedCard = CardType.NONE;
		game = null;
		this.setRef("");
		this.player = 0;
	}

	@Override
	public ThroneRoom clone(DominionGame newGame) {
		ThroneRoom retValue = (ThroneRoom) super.clone(newGame);
		retValue.enthronedCard = enthronedCard;
		retValue.player = player;
		retValue.game = newGame;
		return retValue;
	}
}

class ThroneRoomFollowOnAction extends DominionAction {

	private ThroneRoom masterCard;

	public ThroneRoomFollowOnAction(ThroneRoom card) {
		super(card.game.getPlayer(card.player), new CardTypeList(new ArrayList<CardType>()));
		masterCard = card;
		hasNoAssociatedDecision = true;
	}

	public ThroneRoomFollowOnAction(ThroneRoomFollowOnAction master, Player newPlayer) {
		super(master, newPlayer);
		masterCard = (ThroneRoom) newPlayer.getCardsWithRef(master.masterCard.getRef()).get(0);
	}

	@Override
	public void doStuff() {
		// put masterCard back into hand from revealed
		CardType enthronedCard = masterCard.enthronedCard;
		// Add two actions, one for each of the two cards we will be playing

		if (enthronedCard == null || enthronedCard == CardType.NONE){
			// No card was actually played
			player.incrementActionsLeft();
			return;
		}

		player.incrementActionsLeft();
		player.incrementActionsLeft();
		Card replicaCard = CardFactory.instantiateCard(enthronedCard);
		replicaCard.setRef(masterCard.getRef() + "-REP");
		player.insertCardDirectlyIntoHand(replicaCard);
		// set possibleOptions as just playing said card
		List<ActionEnum<Player>> singleOption = new ArrayList<ActionEnum<Player>>();
		singleOption.add(CardTypeAugment.playCard(enthronedCard));
		possibleOptions = singleOption;
		nextActor = masterCard.game.getPlayer(masterCard.player);
		// finish
	}

	@Override
	public ThroneRoomFollowOnAction clone(Player newPlayer) {
		return new ThroneRoomFollowOnAction(this, newPlayer);
	}
	
	@Override
	public ActionEnum<Player> getType() {
		return CardTypeAugment.playCard(CardType.NONE);
	}

	@Override
	public String toString() {
		return "Follow-on ThroneRoom of " + masterCard.enthronedCard.toString();
	}
}
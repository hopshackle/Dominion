package hopshackle.dominion;

import java.util.*;

import hopshackle.dominion.CardTypeAugment.ChangeType;
import hopshackle.dominion.basecards.Cellar;
import hopshackle.dominion.basecards.Remodel;
import hopshackle.dominion.basecards.ThroneRoom;
import hopshackle.simulation.*;

public class DominionAction extends Action<Player> {

    private List<CardTypeAugment> cardType;
    protected Player player;
    private boolean isAction;

    public DominionAction(Player p, CardTypeAugment actionEnum) {
        super(actionEnum, p, 0l, false);
        player = p;
        this.cardType = new ArrayList<CardTypeAugment>();
        if (actionEnum != null) {
            this.cardType.add(actionEnum);
            if (actionEnum.type == ChangeType.PLAY || actionEnum.type == ChangeType.ENTHRONE) isAction = true;
        }
    }

    public DominionAction(Player player, CardTypeList aeList) {
        super(aeList, player, 0l, false);
        this.cardType = aeList.cards;
        this.player = player;
    }

    public DominionAction(DominionAction master, Player newPlayer) {
        super(master.actionType, newPlayer, 0l, false);
        player = newPlayer;
        DominionGame masterGame = master.actor.getGame();
        int nextActorNumber = masterGame.getPlayerNumber(master.nextActor);
        nextActor = newPlayer.getGame().getPlayer(nextActorNumber);
        cardType = master.cardType;
        isAction = master.isAction();
        followUpAction = (master.followUpAction == null) ? null : master.followUpAction.clone(newPlayer);
        possibleOptions = master.possibleOptions;
    }

    public String toString() {
        StringBuffer retValue = new StringBuffer();
        for (CardTypeAugment c : cardType) {
            retValue.append(c.toString() + " ");
        }
        return retValue.toString();
    }

    @Override
    protected void doStuff() {
        for (CardTypeAugment component : cardType) {
            switch (component.type) {
                case ENTHRONE:
                    ThroneRoom parentCard = (ThroneRoom) player.getCardLastPlayed();
                    parentCard.enthrone(component.card);
                    // then continue with PLAY
                case PLAY:
                    Card cardToPlay = player.playFromHandToRevealedCards(component.card);
                    if (cardToPlay != null) {
                        if (cardToPlay.getType() == CardType.NONE) {
                            player.log("Chooses not to play an Action card.");
                            do {
                                player.decrementActionsLeft();
                            } while (player.getActionsLeft() > 0);
                            break;
                        } else {
                            player.log("Plays " + cardToPlay.toString());
                            possibleOptions = cardToPlay.takeAction(player);
                            followUpAction = cardToPlay.followUpAction();
                            nextActor = cardToPlay.nextActor();
                            if (nextActor == null)
                                nextActor = player;
                        }
                        player.decrementActionsLeft();
                    } else {
                        logger.severe("No Actual card found in hand for type " + cardType);
                    }
                    break;
                case BUY:
                    if (component.card == CardType.NONE) {
                        player.log("Chooses not to buy a card.");
                        do {
                            player.decrementBuysLeft();
                        } while (player.getBuys() > 0);
                        break;
                    }
                    player.spend(component.card.getCost());
                    player.decrementBuysLeft();
                    // then continue to move the card
                case REMODEL:
                    if (component.type == ChangeType.REMODEL) {
                        player.oneOffBudget(component.card.getCost());
                        cardToPlay = player.getCardLastPlayed();
                        if (!(cardToPlay instanceof Remodel))
                            throw new AssertionError("Last card played was not Remodel");
                        possibleOptions = cardToPlay.takeAction(player);
                    }
                    // then continue to move the card
                case CELLAR:
                    // then continue to move the card
                case MOVE:
                    if (component.card == CardType.NONE)
                        continue;
                    player.log(component.toString());
                    player.moveCard(component.card, component.from, component.to);
                    if (component.type == ChangeType.CELLAR) {
                        // we can only determine what is left to move, having moved the card
                        if (component.card != CardType.NONE) {
                            cardToPlay = player.getCardLastPlayed();
                            if (!(cardToPlay instanceof Cellar))
                                throw new AssertionError("Last card played was not Cellar");
                            player.oneOffBudget(player.getOneOffBudget() + 1);
                            possibleOptions = cardToPlay.takeAction(player);
                        }
                    }
            }
        }
    }

    @Override
    protected void doNextDecision(Player p) {
        // Do nothing .. this is all handled in Game/Player
        // Note that we do not override doNextDecision(), so that learning event
        // is still dispatched
    }

    @Override
    protected void eventDispatch(AgentEvent learningEvent) {
        super.eventDispatch(learningEvent);
    }

    public boolean isAction() {
        return isAction;
    }

    @Override
    public DominionAction clone(Player newPlayer) {
        return new DominionAction(this, newPlayer);
    }
}

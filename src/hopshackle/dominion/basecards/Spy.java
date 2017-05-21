package hopshackle.dominion.basecards;

import java.util.ArrayList;
import java.util.List;

import hopshackle.dominion.*;
import hopshackle.dominion.CardTypeAugment.CardSink;
import hopshackle.simulation.ActionEnum;

public class Spy extends AttackCard {

    public Spy() {
        super(CardType.SPY);
    }

    @Override
    public List<ActionEnum<Player>> takeAction(Player player) {
        List<ActionEnum<Player>> retValue = super.takeAction(player);
        if (retValue.isEmpty())
            retValue = spyOnPlayer(player, false); // do ourselves last
        return retValue;
    }

    @Override
    public List<ActionEnum<Player>> executeAttackOnPlayer(Player target) {
        removeVictimFromToBeAttackedList(target.getNumber());
        return spyOnPlayer(target, true);
    }

    private List<ActionEnum<Player>> spyOnPlayer(Player player, boolean opponent) {
        if (opponent) player.log("Is target of SPY");
        Card topCard = player.drawTopCardFromDeckInto(CardSink.HAND);

        List<ActionEnum<Player>> retValue = new ArrayList<>();
        if (opponent) {
            retValue.add(new CardTypeAugment(CardType.NONE, CardSink.HAND, CardSink.DISCARD, CardTypeAugment.ChangeType.SPY));
            retValue.add(new CardTypeAugment(topCard.getType(), CardSink.HAND, CardSink.DISCARD, CardTypeAugment.ChangeType.SPY));
        } else {
            retValue.add(CardTypeAugment.discardCard(CardType.NONE));
            retValue.add(CardTypeAugment.discardCard(topCard.getType()));
        }
        return retValue;
    }

    @Override
    public Player nextActor() {
        return game.getPlayer(attacker);
        // as when the attack occurs, the decisions are made by the attacker, not the defender, as assumed
        // in the default AttackCard implementation
    }

}
